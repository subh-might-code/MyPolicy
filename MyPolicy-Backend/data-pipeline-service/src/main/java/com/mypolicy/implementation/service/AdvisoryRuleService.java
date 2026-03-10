package com.mypolicy.implementation.service;

import com.mypolicy.implementation.model.AdvisoryNote;
import com.mypolicy.implementation.model.AdvisoryResponse;
import com.mypolicy.implementation.model.UnifiedPortfolioRecord;
import com.mypolicy.implementation.model.UnifiedPortfolioResponse;
import com.mypolicy.implementation.repository.UnifiedPortfolioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdvisoryRuleService {

    private final UnifiedPortfolioRepository unifiedPortfolioRepository;

    @Value("${mypolicy.advisory.life-sum-assured-multiplier:10}")
    private int lifeSumAssuredMultiplier;

    @Value("${mypolicy.advisory.health-coverage-min:300000}")
    private int healthCoverageMin;

    @Value("${mypolicy.advisory.auto-idv-min:100000}")
    private int autoIdvMin;

    @Value("${mypolicy.advisory.days-nearing-expiry:90}")
    private int daysNearingExpiry;

    private static final List<String> REQUIRED_CATEGORIES =
            List.of("life_insurance", "health_insurance", "auto_insurance");

    public AdvisoryRuleService(UnifiedPortfolioRepository unifiedPortfolioRepository) {
        this.unifiedPortfolioRepository = unifiedPortfolioRepository;
    }

    public AdvisoryResponse generateAdvisory(Integer customerId) {
        List<UnifiedPortfolioRecord> policies = unifiedPortfolioRepository.findByCustomerId(customerId);
        Set<String> categories = policies.stream()
                .map(UnifiedPortfolioRecord::getSourceCollection)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<AdvisoryNote> notes = new ArrayList<>();

        for (String cat : REQUIRED_CATEGORIES) {
            if (!categories.contains(cat)) {
                String label = formatCategory(cat);
                AdvisoryNote n = new AdvisoryNote();
                n.setType(AdvisoryNote.Type.PROTECTION_GAP);
                n.setSeverity(AdvisoryNote.Severity.high);
                n.setMessage("Product Gap: You have no " + label + ". " + getProductGapReason(cat));
                notes.add(n);
            }
        }

        LocalDate today = LocalDate.now();

        for (UnifiedPortfolioRecord p : policies) {
            String source = p.getSourceCollection();
            int premium = p.getPremium() != null ? p.getPremium().intValue() : 0;
            int sumAssured = p.getSumAssured() != null ? p.getSumAssured() : 0;
            Integer policyEnd = p.getPolicyEnd();

            if ("life_insurance".equals(source) && premium > 0 && sumAssured < premium * lifeSumAssuredMultiplier) {
                AdvisoryNote n = new AdvisoryNote();
                n.setType(AdvisoryNote.Type.SUM_ASSURED_ADEQUACY);
                n.setSeverity(AdvisoryNote.Severity.medium);
                n.setMessage(String.format("Protection Gap: Life coverage (₹%,d) is low vs premium (₹%,d). Why it matters: Your family's financial security depends on adequate sum assured—aim for at least %dx your annual premium to replace income and cover dependents.", sumAssured, premium, lifeSumAssuredMultiplier));
                n.setPolicyId(p.getPolicyId());
                notes.add(n);
            }
            if ("health_insurance".equals(source) && sumAssured > 0 && sumAssured < healthCoverageMin) {
                AdvisoryNote n = new AdvisoryNote();
                n.setType(AdvisoryNote.Type.SUM_ASSURED_ADEQUACY);
                n.setSeverity(AdvisoryNote.Severity.medium);
                n.setMessage(String.format("Protection Gap: Health coverage (₹%,d) is below ₹%,d. Why it matters: Medical inflation runs high; inadequate coverage can deplete savings during hospitalisation. Consider topping up.", sumAssured, healthCoverageMin));
                n.setPolicyId(p.getPolicyId());
                notes.add(n);
            }
            if ("auto_insurance".equals(source) && sumAssured > 0 && sumAssured < autoIdvMin) {
                AdvisoryNote n = new AdvisoryNote();
                n.setType(AdvisoryNote.Type.SUM_ASSURED_ADEQUACY);
                n.setSeverity(AdvisoryNote.Severity.low);
                n.setMessage(String.format("Protection Gap: Auto IDV (₹%,d) may be low. Why it matters: IDV determines claim payout; under-insurance means you bear the shortfall in case of total loss.", sumAssured));
                n.setPolicyId(p.getPolicyId());
                notes.add(n);
            }

            if (policyEnd != null) {
                LocalDate endDate = parseYyyymmdd(policyEnd);
                if (endDate != null) {
                    long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, endDate);
                    if (daysRemaining < 0) {
                        AdvisoryNote n = new AdvisoryNote();
                        n.setType(AdvisoryNote.Type.TEMPORAL_GAP);
                        n.setSeverity(AdvisoryNote.Severity.high);
                        n.setMessage(String.format("Lapsed: Policy %s (%s) expired on %s. Why it matters: You have no coverage until renewal; any claim will be rejected. Renew immediately to restore protection.", p.getPolicyId(), formatCategory(source), endDate));
                        n.setPolicyId(p.getPolicyId());
                        notes.add(n);
                    } else if (daysRemaining <= daysNearingExpiry) {
                        AdvisoryNote n = new AdvisoryNote();
                        n.setType(AdvisoryNote.Type.TEMPORAL_GAP);
                        n.setSeverity(AdvisoryNote.Severity.medium);
                        n.setMessage(String.format("Expiring soon: Policy %s (%s) expires in %d days (%s). Why it matters: Renew before lapse to avoid waiting periods and coverage gaps that impact your financial safety.", p.getPolicyId(), formatCategory(source), daysRemaining, endDate));
                        n.setPolicyId(p.getPolicyId());
                        notes.add(n);
                    }
                }
            }
        }

        List<UnifiedPortfolioResponse.PolicySummary> unifiedView = policies.stream()
                .map(r -> {
                    UnifiedPortfolioResponse.PolicySummary ps = new UnifiedPortfolioResponse.PolicySummary();
                    ps.setPolicyId(r.getPolicyId());
                    ps.setInsurer(r.getInsurer());
                    ps.setSourceCollection(r.getSourceCollection());
                    ps.setPremium(r.getPremium());
                    ps.setSumAssured(r.getSumAssured());
                    ps.setStartDate(r.getStartDate());
                    ps.setPolicyEnd(r.getPolicyEnd());
                    ps.setMatchMethod(r.getMatchMethod());
                    return ps;
                })
                .collect(Collectors.toList());

        AdvisoryResponse.AdvisorySummary sum = new AdvisoryResponse.AdvisorySummary();
        sum.setTotalPolicies(policies.size());
        sum.setCategoriesPresent(new ArrayList<>(categories));
        sum.setGapsIdentified(notes.size());

        AdvisoryResponse resp = new AdvisoryResponse();
        resp.setCustomerId(customerId);
        resp.setAdvisory(notes);
        resp.setSummary(sum);
        resp.setUnifiedView(unifiedView);
        return resp;
    }

    private static LocalDate parseYyyymmdd(Integer val) {
        if (val == null) return null;
        String s = String.valueOf(val);
        if (s.length() != 8) return null;
        try {
            return LocalDate.of(Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(4, 6)), Integer.parseInt(s.substring(6, 8)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String formatCategory(String source) {
        if (source == null) return "Unknown";
        return Arrays.stream(source.split("_"))
                .map(w -> w.isEmpty() ? w : w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private static String getProductGapReason(String category) {
        return switch (category) {
            case "life_insurance" -> "Why it matters: Life cover protects your family's future if you're no longer there; without it, dependents may face financial hardship.";
            case "health_insurance" -> "Why it matters: Medical emergencies can wipe out savings; health cover cushions you against rising hospitalisation costs.";
            case "auto_insurance" -> "Why it matters: Third-party liability is mandatory; comprehensive cover protects your vehicle and you from legal/financial fallout in accidents.";
            default -> "";
        };
    }
}
