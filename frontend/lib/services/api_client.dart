import 'dart:convert';

import 'package:http/http.dart' as http;

/// Simple API client for talking to the MyPolicy BFF backend.
class ApiClient {
  ApiClient._();

  static final ApiClient instance = ApiClient._();

  /// Base URL for the backend.
  ///
  /// For local dev with `flutter run -d chrome`, this should point to
  /// the BFF port (8090). When the Flutter app is hosted by the BFF
  /// (same origin), this can be set to an empty string.
  static const String _defaultBaseUrl = 'http://localhost:8090';

  /// Compile-time override support, so we can switch to same-origin later:
  /// flutter run -d chrome --dart-define=API_BASE_URL=http://localhost:8090
  /// flutter build web --dart-define=API_BASE_URL=
  static const String _baseUrl =
      String.fromEnvironment('API_BASE_URL', defaultValue: _defaultBaseUrl);

  Uri _uri(String path) {
    if (_baseUrl.isEmpty) {
      // Same-origin deployment: use relative paths.
      return Uri.parse(path);
    }
    return Uri.parse('$_baseUrl$path');
  }

  /// Login against BFF `/api/bff/auth/login`.
  ///
  /// `userId` maps to backend `customerIdOrUserId`.
  Future<LoginResult> login(String userId, String password) async {
    final uri = _uri('/api/bff/auth/login');

    final response = await http.post(
      uri,
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode({
        'customerIdOrUserId': userId,
        'password': password,
      }),
    );

    if (response.statusCode != 200) {
      String message = 'Login failed (HTTP ${response.statusCode})';
      try {
        final body = jsonDecode(response.body) as Map<String, dynamic>;
        final backendMessage =
            body['message'] ?? body['error'] ?? body['detail'];
        if (backendMessage is String && backendMessage.isNotEmpty) {
          message = backendMessage;
        }
      } catch (_) {
        // Ignore parse errors and keep generic message.
      }
      throw ApiException(message);
    }

    final json = jsonDecode(response.body) as Map<String, dynamic>;
    return LoginResult.fromJson(json);
  }

  /// Fetch portfolio for a given customerId from BFF `/api/bff/portfolio/{customerId}`.
  Future<PortfolioResult> getPortfolio(String customerId) async {
    final uri = _uri('/api/bff/portfolio/$customerId');

    final response = await http.get(uri);

    if (response.statusCode != 200) {
      String message = 'Failed to load portfolio (HTTP ${response.statusCode})';
      try {
        final body = jsonDecode(response.body) as Map<String, dynamic>;
        final backendMessage =
            body['message'] ?? body['error'] ?? body['detail'];
        if (backendMessage is String && backendMessage.isNotEmpty) {
          message = backendMessage;
        }
      } catch (_) {
        // ignore
      }
      throw ApiException(message);
    }

    final json = jsonDecode(response.body) as Map<String, dynamic>;
    return PortfolioResult.fromJson(json);
  }
}

class LoginResult {
  final String token;
  final String customerId;
  final String fullName;

  LoginResult({
    required this.token,
    required this.customerId,
    required this.fullName,
  });

  factory LoginResult.fromJson(Map<String, dynamic> json) {
    // Support both flat and nested customer structures defensively.
    final customer = json['customer'] as Map<String, dynamic>? ?? {};

    String? extractString(dynamic value) {
      if (value == null) return null;
      if (value is String) return value;
      return value.toString();
    }

    final token = extractString(json['token']) ?? '';
    final customerId = extractString(
          json['customerId'],
        ) ??
        extractString(customer['customerId']) ??
        '';

    final fullName = extractString(json['fullName']) ??
        extractString(customer['fullName']) ??
        extractString(customer['name']) ??
        '';

    if (token.isEmpty || customerId.isEmpty) {
      throw ApiException('Invalid login response from server.');
    }

    return LoginResult(
      token: token,
      customerId: customerId,
      fullName: fullName,
    );
  }
}

/// Portfolio result from BFF `/api/bff/portfolio/{customerId}`.
class PortfolioResult {
  final List<BackendPolicy> policies;

  PortfolioResult({
    required this.policies,
  });

  factory PortfolioResult.fromJson(Map<String, dynamic> json) {
    final List<dynamic> rawPolicies = (json['policies'] as List<dynamic>? ?? []);
    final policies = rawPolicies
        .map((p) => BackendPolicy.fromJson(p as Map<String, dynamic>))
        .toList();
    return PortfolioResult(policies: policies);
  }
}

class BackendPolicy {
  final String id;
  final String policyNumber;
  final String policyType;
  final double premiumAmount;
  final double sumAssured;
  final String insurerId;
  final DateTime? startDate;
  final DateTime? endDate;

  BackendPolicy({
    required this.id,
    required this.policyNumber,
    required this.policyType,
    required this.premiumAmount,
    required this.sumAssured,
    required this.insurerId,
    required this.startDate,
    required this.endDate,
  });

  factory BackendPolicy.fromJson(Map<String, dynamic> json) {
    double toDouble(dynamic v) {
      if (v == null) return 0;
      if (v is num) return v.toDouble();
      return double.tryParse(v.toString()) ?? 0;
    }

    DateTime? parseDate(dynamic v) {
      if (v == null) return null;
      if (v is String) {
        return DateTime.tryParse(v);
      }
      return null;
    }

    return BackendPolicy(
      id: (json['id'] ?? json['policyId'] ?? '').toString(),
      policyNumber: (json['policyNumber'] ?? '').toString(),
      policyType: (json['policyType'] ?? '').toString(),
      premiumAmount: toDouble(json['premiumAmount']),
      sumAssured: toDouble(json['sumAssured']),
      insurerId: (json['insurerId'] ?? '').toString(),
      startDate: parseDate(json['startDate']),
      endDate: parseDate(json['endDate']),
    );
  }
}


class ApiException implements Exception {
  final String message;

  ApiException(this.message);

  @override
  String toString() => 'ApiException: $message';
}

