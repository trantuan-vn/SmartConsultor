import 'package:flutter/material.dart';

class AppLocalizations {
  AppLocalizations(this.locale);

  final Locale locale;


  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations) ?? AppLocalizations(const Locale('en'));
  }

  static final Map<String, Map<String, String>> _localizedValues = {
    'en': {
      'loginTitle': 'Login',
    },
    'vn': {
      'loginTitle': 'Đăng nhập',
    },
  };

  String getText(String value) => _localizedValues[locale.languageCode]?[value] ?? '';

  String get loginTitle => _localizedValues[locale.languageCode]?['loginTitle'] ?? '';

}