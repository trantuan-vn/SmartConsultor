import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:smartconsultor/core/appearance/app_theme.dart';
import 'package:smartconsultor/core/appearance/device_type.dart';
import 'package:smartconsultor/core/env/environment_configuration.dart';
import 'package:smartconsultor/core/localization/app_localizations_delegate.dart';
import 'package:smartconsultor/features/dashboard/presentation/pages/dashboard.dart';
import 'package:smartconsultor/features/login/presentation/pages/login_page.dart';
import 'package:smartconsultor/features/splash/presentation/papes/spash_page.dart';
import 'core/di/injection_container.dart' as di;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await di.init();
  runApp(const MainApp());
}

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
Widget build(BuildContext context) {
    bool touchMode = DeviceType.isMobile == true;
    double densityAmt = touchMode ? 0.0 : -1.0;
    VisualDensity density = VisualDensity(horizontal: densityAmt, vertical: densityAmt);    

    return MaterialApp(
      supportedLocales: const [
        Locale('en', ''),
        Locale('vn', ''),
      ],
      locale: const Locale('en'),
      
      localizationsDelegates: const [
        AppLocalizationsDelegate(),
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ],
      
      theme: AppTheme.build(density),
      initialRoute: "/",
      onGenerateRoute: (settings) {
        // ignore: prefer_const_constructors
        Widget destinationRoute = SplashPage();

        switch (settings.name) {
          case LoginPage.LOGIN_ROUTE:
            // ignore: prefer_const_constructors
            destinationRoute = LoginPage();
            break;
          case Dashboard.DASHBOARD_ROUTE:
            // ignore: prefer_const_constructors
            destinationRoute = Dashboard();
            break;            
        }
        return MaterialPageRoute(builder: (context) => destinationRoute, settings: settings);
      },
      routes: {
        // ignore: prefer_const_constructors
        '/': (BuildContext context) => SplashPage(),
        // ignore: prefer_const_constructors
        LoginPage.LOGIN_ROUTE: (BuildContext context) => LoginPage(),
        // ignore: prefer_const_constructors
        Dashboard.DASHBOARD_ROUTE: (BuildContext context) => Dashboard(),
      },
    );
  }
}
