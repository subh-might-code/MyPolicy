import 'package:flutter/material.dart';
import 'theme/app_theme.dart';
import 'screens/login_screen.dart';

void main() {
  runApp(const HDFCInsuranceApp());
}

/// Main application widget
class HDFCInsuranceApp extends StatelessWidget {
  const HDFCInsuranceApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'HDFC Insurance Dashboard',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      home: const LoginScreen(),
    );
  }
}
