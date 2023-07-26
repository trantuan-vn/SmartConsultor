Để tạo mã QR cho ứng dụng Google Authenticator trong ứng dụng Flutter của bạn, bạn có thể sử dụng gói Flutter `qr_flutter`. Dưới đây là một đoạn mã cơ bản để tạo mã QR cho một chuỗi bí mật (secret key):

Thêm gói `qr_flutter` vào file `pubspec.yaml` của dự án Flutter:
yaml

Copy code
dependencies:
  flutter:
    sdk: flutter

  qr_flutter: ^5.0.0
Sử dụng mã QR trong Widget của bạn:
dart

Copy code
import 'package:flutter/material.dart';
import 'package:qr_flutter/qr_flutter.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  final String secretKey = 'ABCD1234EFGH5678'; // Chuỗi bí mật (secret key) cho xác thực hai yếu tố

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Tạo mã QR cho Google Authenticator'),
        ),
        body: Center(
          child: QrImage(
            data: 'otpauth://totp/MyApp?secret=$secretKey',
            version: QrVersions.auto,
            size: 200.0,
          ),
        ),
      ),
    );
  }
}

import 'package:google_authenticator/google_authenticator.dart';

bool verifyOTP(String otp, String secret) {
  final googleAuthenticator = GoogleAuthenticator();

  return googleAuthenticator.isCodeValid(
    secret,
    otp,
    currentTime: DateTime.now().millisecondsSinceEpoch,
  );
}

void main() {
  final otpFromUser = '123456'; // Mã OTP nhập từ người dùng
  final secretKeyFromUser = 'ABCD1234EFGH5678'; // Chuỗi mã QR hoặc mã QR công cộng được lưu trữ từ người dùng

  final isOTPValid = verifyOTP(otpFromUser, secretKeyFromUser);

  if (isOTPValid) {
    print('Mã OTP hợp lệ!');
  } else {
    print('Mã OTP không hợp lệ!');
  }
}
