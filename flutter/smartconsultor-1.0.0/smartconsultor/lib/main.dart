import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:smartconsultor/core/appearance/app_theme.dart';
import 'package:smartconsultor/core/env/environment_configuration.dart';
import 'package:smartconsultor/core/localization/app_localizations_delegate.dart';
import 'package:smartconsultor/features/dashboard/presentation/pages/dashboard.dart';
import 'package:smartconsultor/features/login/presentation/pages/login_page.dart';
import 'package:smartconsultor/features/splash/presentation/papes/spash.dart';
import 'core/di/injection_container.dart' as di;
import 'package:responsive_framework/responsive_framework.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await EnvironmentConfiguration.run(); // Init env configuration
  await di.init();
  runApp(const MainApp());
}

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      builder: (context, child) => ResponsiveBreakpoints.builder(
        child: child!,
        breakpoints: [
          const Breakpoint(start: 0, end: 450, name: MOBILE),
          const Breakpoint(start: 451, end: 800, name: TABLET),
          const Breakpoint(start: 801, end: 1920, name: DESKTOP),
          const Breakpoint(start: 1921, end: double.infinity, name: '4K'),
        ],
      ),
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
      


      theme: AppTheme.build(),
      initialRoute: "/",
      onGenerateRoute: (settings) {
        // ignore: prefer_const_constructors
        Widget destinationRoute = SplashPage();

        switch (settings.name) {
          case '/login':
            // ignore: prefer_const_constructors
            destinationRoute = LoginPage();
            break;
          case '/dashboard':
            // ignore: prefer_const_constructors
            destinationRoute = Dashboard();
            break;            
        }
        return MaterialPageRoute(builder: (context) {
          // The following code implements the legacy ResponsiveWrapper AutoScale functionality
          // using the new ResponsiveScaledBox. The ResponsiveScaledBox widget preserves
          // the legacy ResponsiveWrapper behavior, scaling the UI instead of resizing.
          //
          // **MaxWidthBox** - A widget that limits the maximum width.
          // This is used to create a gutter area on either side of the content.
          //
          // **ResponsiveScaledBox** - A widget that  renders its child
          // with a FittedBox set to the `width` value. Set the fixed width value
          // based on the active breakpoint.
          return MaxWidthBox(
            maxWidth: 1200,
            background: Container(color: const Color(0xFFF5F5F5)),
            child: ResponsiveScaledBox(
              width: ResponsiveValue<double>(context, conditionalValues: [
                Condition.equals(name: MOBILE, value: 450),
                Condition.between(start: 800, end: 1100, value: 800),
                Condition.between(start: 1000, end: 1200, value: 1000),
                // There are no conditions for width over 1200
                // because the `maxWidth` is set to 1200 via the MaxWidthBox.
              ]).value,
              child: BouncingScrollWrapper.builder(
                  context, destinationRoute,
                  dragWithMouse: true),
            ),
          );
        });
      },
      routes: {
        // ignore: prefer_const_constructors
        '/': (BuildContext context) => SplashPage(),
        // ignore: prefer_const_constructors
        '/login': (BuildContext context) => LoginPage(),
        // ignore: prefer_const_constructors
        '/dashboard': (BuildContext context) => Dashboard(),
      },
    );
  }
}
