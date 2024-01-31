import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';


import 'package:smartconsultor/core/appearance/app_colors.dart';
import 'package:smartconsultor/core/di/injection_container.dart';
import 'package:smartconsultor/features/dashboard/presentation/pages/dashboard.dart';
import 'package:smartconsultor/features/login/presentation/pages/login_page.dart';
import 'package:smartconsultor/features/splash/presentation/bloc/splash_bloc.dart';

class SplashPage extends StatelessWidget {
  // ignore: constant_identifier_names
  static const SPLASH_PAGE_ROUTE = '/splash_page';

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (context) => sl<SplashBloc>()..add(SplashLoadData()),
      child: SplashPageContent(),
    );
  }
}

class SplashPageContent extends StatefulWidget {
  @override
  _SplashPageContentState createState() => _SplashPageContentState();
}

class _SplashPageContentState extends State<SplashPageContent>
    with SingleTickerProviderStateMixin {
  late AnimationController controller;
  
  @override
  void initState() {
    super.initState();
    controller = AnimationController(
      duration: const Duration(milliseconds: 1000),
      vsync: this,
    );
    controller.forward();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: BlocListener<SplashBloc, SplashState>(
        listener: (context, state) {
          if (state is SplashLoadingError) {
            // Show an error message if login fails
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(state.errorMessage),
                duration: const Duration(seconds: 2),
              ),
            );
          }
          if (state is SplashDoneLoading) {
            onDoneLoading(context,state);
          }
        },
        child: AnimatedBuilder(
          animation: controller,
          builder: (context, child) {
            return Transform.scale(
              scale: Tween(begin: 0.5, end: 1.5).animate(controller).value,
              child: Container(
                decoration: BoxDecoration(
                  color: AppColors.accentColor,
                  image: const DecorationImage(
                    image: AssetImage('assets/images/app_logo.svg'),
                    fit: BoxFit.scaleDown,
                  ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }

  void onDoneLoading(BuildContext context, SplashDoneLoading state) {
    Navigator.popAndPushNamed(
      context,
      state.splashUser==null ? LoginPage.LOGIN_ROUTE : Dashboard.DASHBOARD_ROUTE,
    );
  }

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }
}
