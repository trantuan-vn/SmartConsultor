import 'package:flutter/foundation.dart';
import 'package:url_launcher/url_launcher.dart'; 

Future<bool> openUrl(String url, {bool newWindow = false}) async {
  // ignore: deprecated_member_use
  if (await canLaunch(url)) {
    // ignore: deprecated_member_use
    return await launch(
      url,
    );
  } else {
    debugPrint("Could not launch $url");
    return false;
  }
}