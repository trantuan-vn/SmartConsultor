// ignore_for_file: deprecated_member_use

import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import '../appearance/app_colors.dart';

class AppTheme {
  static ThemeData build() => ThemeData(
      primaryColor: const Color(0xffffffff),
      primaryColorDark: const Color(0xffffffff),
      primaryColorLight: const Color(0xffffffff),
      disabledColor: AppColors.greyMedium,
      dividerColor: AppColors.divider,
      brightness: Brightness.light,
      fontFamily: GoogleFonts.roboto().fontFamily,
      primaryIconTheme: const IconThemeData(size: 24),
      //accentIconTheme: IconThemeData(size: 28),
      textTheme: TextTheme(
        headline1: GoogleFonts.roboto(fontWeight: FontWeight.bold, fontSize: 16, color: const Color(0xff383838)),//title store en ListStore
        headline2: GoogleFonts.roboto(fontWeight: FontWeight.w500, fontSize: 16, color: const Color(0xff383838)),//title popUp
        headline3: GoogleFonts.roboto(fontWeight: FontWeight.normal, fontSize: 16, color: const Color(0xFF3D9FE6)), //en buscador blue
        headline4: GoogleFonts.roboto(fontWeight: FontWeight.normal, fontSize: 16, color: const Color(0xff383838)),//direcciones y resultados busqueda
        headline5: GoogleFonts.roboto(fontWeight: FontWeight.normal, fontSize: 16, color: const Color(0xff868686)),//#resultados y sug busqueda
        headline6: GoogleFonts.roboto(fontWeight: FontWeight.normal, fontSize: 16, color: const Color(0xffFFAC0C)), //edit y chance, adress payment

        subtitle1:  GoogleFonts.roboto(fontWeight: FontWeight.bold, fontSize: 13, color: const Color(0xFF3D9FE6)),//envio gratis list_store
        subtitle2: GoogleFonts.roboto(fontWeight: FontWeight.w500, fontSize: 13, color: const Color(0xFF3D9FE6)),//botones en popUp
        overline: GoogleFonts.roboto(fontWeight: FontWeight.normal, fontSize: 13, color: const Color(0xff383838)),//horario Store
        button: GoogleFonts.roboto(fontWeight: FontWeight.normal, fontSize: 13, color: const Color(0xff868686)), //subtitle store
        caption: GoogleFonts.roboto(fontWeight: FontWeight.w500, fontSize: 13, color: const Color(0xffFFAC0C))
      ),
      //colorScheme: ColorScheme(background: Color(0xffffffff)),
      );

  /// Wrap a widget to be affected for this theme
  ///
  /// Usage: AppTheme.darkTheme(child: Column(...))
  static Theme darkTheme({required Widget child}) => Theme(
        data: build().copyWith(),
        child: child,
      );
}