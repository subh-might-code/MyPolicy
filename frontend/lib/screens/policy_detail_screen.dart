import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/policy_model.dart';
import '../theme/app_theme.dart';
import '../widgets/custom_appbar.dart';

class PolicyDetailScreen extends StatelessWidget {
  final Policy policy;
  final String customerId;
  final String customerName;

  const PolicyDetailScreen({
    super.key,
    required this.policy,
    required this.customerId,
    required this.customerName,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppTheme.backgroundGrey,
      appBar: CustomAppBar(
        customerName: customerName,
        customerId: customerId,
        onLogoTap: () => Navigator.of(context).pop(),
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(AppTheme.spacing24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Top Summary Card
              _buildSummaryHeader(context),
              const SizedBox(height: AppTheme.spacing24),
              
              // Sections Grid for Web/Tablet, Column for Mobile
              LayoutBuilder(
                builder: (context, constraints) {
                  if (constraints.maxWidth > 900) {
                    return Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Expanded(child: _buildPolicyOverview(context)),
                        const SizedBox(width: AppTheme.spacing24),
                        Expanded(child: _buildCoverageDetails(context)),
                      ],
                    );
                  } else {
                    return Column(
                      children: [
                        _buildPolicyOverview(context),
                        const SizedBox(height: AppTheme.spacing24),
                        _buildCoverageDetails(context),
                      ],
                    );
                  }
                },
              ),
              const SizedBox(height: AppTheme.spacing32),
              
              // Bottom Action Buttons
              _buildActionButtons(context),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSummaryHeader(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final isCompact = constraints.maxWidth < 650;
        
        final content = [
          // Large Icon Container
          Container(
            padding: EdgeInsets.all(isCompact ? AppTheme.spacing12 : AppTheme.spacing16),
            decoration: BoxDecoration(
              color: Colors.white,
              shape: BoxShape.circle,
              border: Border.all(color: Colors.black, width: 2),
            ),
            child: Icon(
              Icons.favorite_outline,
              size: isCompact ? 36 : 48,
              color: Colors.black,
            ),
          ),
          SizedBox(
            width: isCompact ? 0 : AppTheme.spacing24,
            height: isCompact ? AppTheme.spacing16 : 0,
          ),
          // Text Content
          isCompact 
            ? Column(
                children: [
                  Text(
                    'Policy Details : ${policy.name}',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                      fontSize: 18,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Policy Number : ${policy.policyId}',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      fontSize: 13,
                      color: AppTheme.textGrey,
                    ),
                  ),
                ],
              )
            : Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Policy Details : ${policy.name}',
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                        fontSize: 20,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Policy Number : ${policy.policyId}',
                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        fontSize: 14,
                        color: AppTheme.textGrey,
                      ),
                    ),
                  ],
                ),
              ),
          SizedBox(
            width: isCompact ? 0 : AppTheme.spacing24,
            height: isCompact ? AppTheme.spacing16 : 0,
          ),
          // Due Date Badge (from real endDate if available)
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
            decoration: BoxDecoration(
              color: AppTheme.primaryBlue,
              borderRadius: BorderRadius.circular(AppTheme.radiusPill),
            ),
            child: Text(
              'Due Date : ${_formatDate(policy.endDate)}',
              style: const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.bold,
                fontSize: 14,
              ),
            ),
          ),
        ];

        return Container(
          width: double.infinity,
          padding: const EdgeInsets.all(AppTheme.spacing24),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(AppTheme.radiusLarge),
            border: Border.all(color: AppTheme.textGrey.withValues(alpha: 0.2)),
            boxShadow: AppTheme.softShadow,
          ),
          child: isCompact 
            ? Column(
                mainAxisSize: MainAxisSize.min,
                children: content,
              )
            : Row(children: content),
        );
      },
    );
  }

  Widget _buildPolicyOverview(BuildContext context) {
    return _buildDetailSection(
      context,
      title: 'Policy Overview',
      items: [
        _DetailItem('Status', policy.status == PolicyStatus.active ? 'Active' : (policy.status == PolicyStatus.due ? 'Due' : 'Expired')),
        _DetailItem(
          'Coverage',
          policy.category == PolicyCategory.life
              ? 'Life Insurance'
              : policy.category == PolicyCategory.health
                  ? 'Health Insurance'
                  : policy.category == PolicyCategory.motor
                      ? 'Motor Insurance'
                      : 'Others',
        ),
        _DetailItem('Start Date', _formatDate(policy.startDate)),
        _DetailItem('Expiration Date', _formatDate(policy.endDate)),
        _DetailItem('Premium', '₹ ${NumberFormat('#,##,###').format(policy.annualPremium)}/year'),
        const _DetailItem('Policy Term', '-'),
        const _DetailItem('Payment Term', '-'),
        const _DetailItem('Payment Method', '-'),
      ],
    );
  }

  Widget _buildCoverageDetails(BuildContext context) {
    return _buildDetailSection(
      context,
      title: 'Coverage Details',
      items: [
        _DetailItem('Sum Assured', '₹ ${NumberFormat('#,##,###').format(policy.sumInsured)}'),
        const _DetailItem('Nominee', '-'),
        const _DetailItem('Grace Period', '-'),
        const _DetailItem('Death Benefit', '-'),
        const _DetailItem('Critical Illness', '-'),
        const _DetailItem('Maturity Benefit', '-'),
        const _DetailItem('Payout options', '-'),
      ],
    );
  }

  Widget _buildDetailSection(BuildContext context, {required String title, required List<_DetailItem> items}) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(AppTheme.radiusLarge),
        boxShadow: AppTheme.softShadow,
      ),
      clipBehavior: Clip.antiAlias,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Section Header
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(AppTheme.spacing16),
            color: AppTheme.primaryBlue,
            child: Text(
              title,
              style: const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.bold,
                fontSize: 16,
              ),
            ),
          ),
          // Items
          ...items.map((item) => Container(
            padding: const EdgeInsets.symmetric(horizontal: AppTheme.spacing20, vertical: AppTheme.spacing12),
            decoration: BoxDecoration(
              border: Border(bottom: BorderSide(color: AppTheme.textGrey.withValues(alpha: 0.1))),
            ),
            child: Row(
              children: [
                Expanded(
                  flex: 1,
                  child: Text(
                    item.label,
                    style: const TextStyle(fontWeight: FontWeight.w500, fontSize: 14),
                  ),
                ),
                const Text(' : '),
                Expanded(
                  flex: 1,
                  child: Text(
                    item.value,
                    style: const TextStyle(fontSize: 14),
                  ),
                ),
              ],
            ),
          )),
        ],
      ),
    );
  }

  Widget _buildActionButtons(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final isCompact = constraints.maxWidth < 800;
        
        final buttons = [
          _ActionButton(
            icon: Icons.file_download_outlined,
            label: 'Download Policy',
            onTap: () {},
          ),
          SizedBox(
            width: isCompact ? 0 : AppTheme.spacing16,
            height: isCompact ? AppTheme.spacing16 : 0,
          ),
          _ActionButton(
            icon: Icons.description_outlined,
            label: 'File a Claim',
            onTap: () {},
          ),
          SizedBox(
            width: isCompact ? 0 : AppTheme.spacing16,
            height: isCompact ? AppTheme.spacing16 : 0,
          ),
          _ActionButton(
            icon: Icons.shield_outlined,
            label: 'Manage Policy',
            onTap: () {},
          ),
        ];

        return isCompact 
            ? Column(
                mainAxisSize: MainAxisSize.min,
                children: buttons,
              )
            : Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: buttons.map((widget) => widget is _ActionButton 
                  ? Expanded(child: widget) 
                  : widget).toList(),
              );
      },
    );
  }

  String _formatDate(DateTime? date) {
    if (date == null) return '-';
    return DateFormat('dd/MM/yy').format(date);
  }
}

class _DetailItem {
  final String label;
  final String value;
  const _DetailItem(this.label, this.value);
}

class _ActionButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _ActionButton({
    required this.icon,
    required this.label,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 56,
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(AppTheme.radiusPill),
        boxShadow: AppTheme.cardShadow,
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(AppTheme.radiusPill),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(icon, size: 24, color: Colors.black),
                const SizedBox(width: 12),
                Flexible(
                  child: Text(
                    label,
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 14,
                      color: Colors.black,
                    ),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
