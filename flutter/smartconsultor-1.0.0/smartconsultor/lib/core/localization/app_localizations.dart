import 'package:flutter/material.dart';

class AppLocalizations {
  AppLocalizations(this.locale);

  final Locale locale;


  static AppLocalizations? of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }

  static final Map<String, Map<String, String>> _localizedValues = {
    'en': {
      'login': 'Login',
    },
    'vn': {
      'login': 'Đăng nhập',
    },
  };

  String getText(String value) => _localizedValues[locale.languageCode]?[value] ?? '';

  String? get login => _localizedValues[locale.languageCode]?['login'];

}