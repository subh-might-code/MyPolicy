import 'package:flutter/material.dart';
import '../services/api_client.dart';
import '../widgets/donut_chart.dart';
import '../widgets/info_card.dart';
import '../widgets/custom_appbar.dart';
import '../models/policy_model.dart';
import 'dashboard_screen.dart';

class AnalyticsDashboard extends StatefulWidget {
  final String customerName;
  final String customerId;

  const AnalyticsDashboard({
    super.key,
    required this.customerName,
    required this.customerId,
  });

  @override
  State<AnalyticsDashboard> createState() => _AnalyticsDashboardState();
}

class _AnalyticsDashboardState extends State<AnalyticsDashboard> {
  bool _isLoading = true;
  String? _errorMessage;
  List<BackendPolicy> _policies = [];

  @override
  void initState() {
    super.initState();
    _loadPortfolio();
  }

  Future<void> _loadPortfolio() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final result =
          await ApiClient.instance.getPortfolio(widget.customerId);
      if (!mounted) return;
      setState(() {
        _policies = result.policies;
        _isLoading = false;
      });
    } on ApiException catch (e) {
      if (!mounted) return;
      setState(() {
        _isLoading = false;
        _errorMessage = e.message;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _isLoading = false;
        _errorMessage = 'Unable to load portfolio analytics.';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (_errorMessage != null) {
      return Scaffold(
        appBar: CustomAppBar(
          customerName: widget.customerName,
          customerId: widget.customerId,
          onLogoTap: () {
            Navigator.of(context).pushAndRemoveUntil(
              MaterialPageRoute(
                builder: (context) => DashboardScreen(
                  customerId: widget.customerId,
                  customerName: widget.customerName,
                ),
              ),
              (route) => false,
            );
          },
        ),
        body: Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Text(
              _errorMessage!,
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.red),
            ),
          ),
        ),
      );
    }

    final policies = _policies;

    // Calculate Summary Values from real backend policies
    final totalPolicies = policies.length;
    final totalProtection = policies.fold<double>(
      0.0,
      (sum, p) => sum + (p.sumAssured),
    );

    // Without explicit expiry info, treat everything as not expiring soon
    final expiringSoon = 0;

    double getCategoryPercent(PolicyCategory category) {
      if (policies.isEmpty) return 0;
      final count = policies.where((p) {
        final lowerType = p.policyType.toLowerCase();
        switch (category) {
          case PolicyCategory.life:
            return lowerType.contains('life');
          case PolicyCategory.health:
            return lowerType.contains('health');
          case PolicyCategory.motor:
            return lowerType.contains('auto') || lowerType.contains('motor');
          case PolicyCategory.expired:
          case PolicyCategory.others:
          case PolicyCategory.all:
            return !lowerType.contains('life') &&
                !lowerType.contains('health') &&
                !lowerType.contains('auto') &&
                !lowerType.contains('motor');
        }
      }).length;
      return (count / totalPolicies) * 100;
    }

    final lifePercent = getCategoryPercent(PolicyCategory.life);
    final healthPercent = getCategoryPercent(PolicyCategory.health);
    final vehiclePercent = getCategoryPercent(PolicyCategory.motor);

    return Scaffold(
      backgroundColor: const Color(0xFFE9EDF3),

      /// SAME APPBAR AS WELCOME PAGE
      appBar: CustomAppBar(
        customerName: widget.customerName,
        customerId: widget.customerId,
        onLogoTap: () {
          Navigator.of(context).pushAndRemoveUntil(
            MaterialPageRoute(
              builder: (context) => DashboardScreen(
                customerId: widget.customerId,
                customerName: widget.customerName,
              ),
            ),
            (route) => false,
          );
        },
      ),

      /// ================= BODY =================
      body: LayoutBuilder(
        builder: (context, constraints) {

          final width = constraints.maxWidth;
          final isMobile = width < 600;

          double donutSpacing;

          if (width > 1300) {
            donutSpacing = 80;
          } else if (width > 900) {
            donutSpacing = 60;
          } else {
            donutSpacing = 30;
          }

          return SingleChildScrollView(
            padding: EdgeInsets.symmetric(
              horizontal: isMobile ? 16 : 40,
              vertical: isMobile ? 16 : 30,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [

                /// HEADER
                const Text(
                  "Dashboard",
                  style: TextStyle(
                    fontSize: 26,
                    fontWeight: FontWeight.bold,
                  ),
                ),

                const SizedBox(height: 30),

                /// ================= DONUT SECTION =================
                Container(
                  width: double.infinity,
                  padding: EdgeInsets.symmetric(
                    vertical: 30,
                    horizontal: isMobile ? 10 : 30,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius:
                        BorderRadius.circular(isMobile ? 12 : 20),
                    boxShadow: isMobile
                        ? []
                        : const [
                            BoxShadow(
                              color: Colors.black12,
                              blurRadius: 15,
                              offset: Offset(0, 8),
                            )
                          ],
                  ),
                  child: Wrap(
                    alignment: WrapAlignment.center,
                    spacing: donutSpacing,
                    runSpacing: 40,
                    children: [
                      DonutChart(
                        title: "Life Insurance",
                        percent: lifePercent.toInt(),
                        label: lifePercent > 50 ? "Secure" : "Low",
                      ),
                      DonutChart(
                        title: "Health Insurance",
                        percent: healthPercent.toInt(),
                        label: healthPercent > 50 ? "Covered" : "Fair",
                      ),
                      DonutChart(
                        title: "Motor Insurance",
                        percent: vehiclePercent.toInt(),
                        label: vehiclePercent > 20 ? "Protected" : "Verify",
                      ),
                    ],
                  ),
                ),

                const SizedBox(height: 40),

                /// ================= INFO CARDS (OVERFLOW PROOF) =================
                LayoutBuilder(
                  builder: (context, constraints) {

                    final width = constraints.maxWidth;
                    double cardWidth;

                    if (width > 1300) {
                      cardWidth = (width / 4) - 24;
                    }
                    else if (width > 900) {
                      cardWidth = (width / 2) - 20;
                    }
                    else {
                      cardWidth = width;
                    }

                    return Wrap(
                      spacing: 24,
                      runSpacing: 24,
                      children: [

                        SizedBox(
                          width: cardWidth,
                          child: InfoCard(
                            icon: Icons.description_outlined,
                            color: const Color(0xFF2E49B8),
                            title: "Policies Linked",
                            value: "$totalPolicies",
                            subtitle: "$expiringSoon expiring soon",
                          ),
                        ),

                        SizedBox(
                          width: cardWidth,
                          child: InfoCard(
                            icon: Icons.shield_outlined,
                            color: const Color(0xFF2E49B8),
                            title: "Total Protection",
                            value: "₹ ${_formatAmount(totalProtection)}",
                            subtitle: "sum of all insurance",
                          ),
                        ),

                        SizedBox(
                          width: cardWidth,
                          child: const InfoCard(
                            icon: Icons.warning_amber_outlined,
                            color: Color(0xFF2E49B8),
                            title: "Coverage Gap",
                            value: "₹ 50.0 L",
                            subtitle:
                                "to reach recommended levels",
                          ),
                        ),

                        SizedBox(
                          width: cardWidth,
                          child: InfoCard(
                            icon: Icons.bar_chart,
                            color: const Color(0xFF2E49B8),
                            title: "Risk Status",
                            value: expiringSoon > 0 ? "HIGH" : "LOW",
                            subtitle: "see insights",
                          ),
                        ),
                      ],
                    );
                  },
                ),

              ],
            ),
          );
        },
      ),
    );
  }

  /// Amount Formatter (consistent with Home page)
  String _formatAmount(double amount) {
    if (amount >= 10000000) {
      return '${(amount / 10000000).toStringAsFixed(1)} Cr';
    } else if (amount >= 100000) {
      return '${(amount / 100000).toStringAsFixed(1)} L';
    } else if (amount >= 1000) {
      return '${(amount / 1000).toStringAsFixed(0)}K';
    }
    return amount.toStringAsFixed(0);
  }
}
