import 'dart:async';

import 'package:smartconsultor/core/appearance/app_colors.dart';
import 'package:flutter/material.dart';
import 'package:smartconsultor/core/hive/main_box_mixin.dart';
import 'package:smartconsultor/features/dashboard/presentation/pages/dashboard.dart';
import 'package:smartconsultor/features/login/presentation/pages/login_page.dart';

class SplashPage extends StatefulWidget {
  // ignore: constant_identifier_names
  static const SPLASH_PAGE_ROUTE = '/splash_page';

  const SplashPage({super.key});

  @override
  State<StatefulWidget> createState() {
    return SplashPageState();
  }
}

class SplashPageState extends State<SplashPage> with TickerProviderStateMixin, MainBoxMixin  {
  late AnimationController controller;
  late Animation<double> animation;

  @override
  void initState() {
    super.initState();
    Future.delayed(const Duration(seconds: 1), () => loadData());

    controller = AnimationController(
      duration: const Duration(milliseconds: 1000),
      vsync: this,
    );
    
    animation = Tween(begin: 0.5, end: 1.5).animate(controller)..addListener(() => loadData());
    controller.forward();
  }

  @override
  void dispose() {
    super.dispose();
    controller.dispose();
  }

  void loadData() async {
    Future.delayed(const Duration(seconds: 3), onDoneLoading);
  }

  onDoneLoading() async {
      const userKey='user_';
      await MainBoxMixin.initHive(userKey); // Thay đổi 'user_' thành tiền tố phù hợp
      final userJson = getData<Map<String, dynamic>>(MainBoxKeys.isLogin);
      await closeBox();

    // ignore: unnecessary_null_comparison, use_build_context_synchronously
    Navigator.popAndPushNamed(context, userJson == null ? LoginPage.LOGIN_PAGE : Dashboard.DASHBOARD);
  }

  @override
  Widget build(BuildContext context) {
    //final strings = AppLocalizations.of(context);
    // strings.splashTitle
    return Scaffold(
      body: Container(
        decoration: BoxDecoration(
          color: AppColors.accentColor,
          image: const DecorationImage(
            image: AssetImage('assets/images/app_logo.svg'),
            fit: BoxFit.scaleDown,
          ),
        ),
      ),
    );
  }
}