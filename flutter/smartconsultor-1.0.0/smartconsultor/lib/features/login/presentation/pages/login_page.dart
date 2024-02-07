import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:smartconsultor/core/appearance/device_size.dart';
import 'package:smartconsultor/features/dashboard/presentation/pages/dashboard.dart';
import 'package:smartconsultor/features/login/presentation/bloc/auth_bloc.dart';
import 'package:smartconsultor/features/login/presentation/widgets/quote_view.dart';
import 'package:smartconsultor/features/login/presentation/widgets/widgets.dart';
import 'package:smartconsultor/core/di/injection_container.dart';

class LoginPage extends StatelessWidget {
  // ignore: constant_identifier_names
  static const LOGIN_ROUTE = '/login';

  const LoginPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: BlocProvider(
        create: (context) => sl<AuthBloc>(),
        child: LoginForm(),
      ),
    );
  }
}

class LoginForm extends StatelessWidget {
  const LoginForm({super.key});

  @override
  Widget build(BuildContext context) {
    bool isMobileLayout = DeviceSize.isMobileLayout(context);
    
    return BlocListener<AuthBloc, AuthState>(
      listener: (context, state) {
        if (state is AuthAuthenticatedState) {
          // Navigate to the next page on successful login
          Navigator.pushReplacementNamed(context, Dashboard.DASHBOARD_ROUTE);  
        } else if (state is AuthUnauthenticatedState) {
          // Show an error message if login fails
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(state.errorMessage),
              duration: const Duration(seconds: 2),
            ),
          );
        } else if (state is AuthErrorState) {
          // Show an error message if login fails
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(state.errorMessage),
              duration: const Duration(seconds: 2),
            ),
          );
        }        
      },
      child: BlocBuilder<AuthBloc, AuthState>(
        builder: (context, state) {
          return Scaffold(
            body: Center(
                child: isMobileLayout ? 
                  LoginControls()
                  :
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      QuoteView(),
                      LoginControls(),
                    ],
                  ),
            ),
          );
        },
      ),
    );
  }
}