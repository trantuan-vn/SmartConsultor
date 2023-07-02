Dưới đây là mã code hoàn chỉnh cho ứng dụng OCR trong Flutter:

`pubspec.yaml`:

yaml

Copy code
name: ocr_app
description: OCR Demo App
version: 1.0.0

environment:
  sdk: ">=2.12.0 <3.0.0"

dependencies:
  flutter:
    sdk: flutter
  flutter_ocr: ^2.4.6
  flutter_mlkit: ^2.0.2
  image_picker: ^0.8.4+2

dev_dependencies:
  flutter_test:
    sdk: flutter
`main.dart`:

dart

Copy code
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_ocr/flutter_ocr.dart';
import 'package:flutter_mlkit/flutter_mlkit.dart';
import 'package:image_picker/image_picker.dart';

import 'id_card_model.dart';

void main() {
  runApp(OCRApp());
}

class OCRApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'OCR Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: OCRScreen(),
    );
  }
}

class OCRScreen extends StatefulWidget {
  @override
  _OCRScreenState createState() => _OCRScreenState();
}

class _OCRScreenState extends State<OCRScreen> {
  String errorText = '';
  List<IDCardModel> idCardInfo = [];
  File idImage;

  Future<void> classifyAndRecognizeText() async {
    final imagePicker = ImagePicker();
    final pickedImage = await imagePicker.pickImage(source: ImageSource.gallery);

    idCardInfo.clear();
    errorText = '';

    final visionText = await FlutterMlkit.textRecognizerProcessImage(pickedImage.path);
    final lines = visionText.replaceAll('\n\n', '\n').split('\n');

    final firstLine = lines[0].toLowerCase();

    if (firstLine.contains('giấy chứng minh thư nhân dân')) {
      final ocrResult = await recognizeText(pickedImage);

      final line1 = lines[1].substring(0, lines[1].indexOf(':')).trim();
      final line2 = lines[2].substring(0, lines[2].indexOf(':')).trim();
      final line3 = lines[3].substring(0, lines[3].indexOf(':')).trim();
      final line4 = lines[4].substring(0, lines[4].indexOf(':')).trim();
      final line5 = lines[5].substring(0, lines[5].indexOf(':')).trim();

      if (line1 != 'giấy chứng minh thư nhân dân' ||
          line2 != 'số' ||
          line3 != 'sinh ngày' ||
          line4 != 'nguyên quán' ||
          line5 != 'nơi đăng ký hộ khẩu thường trú') {
        errorText = 'Không thể nhận diện đầy đủ thông tin chứng minh thư.';
      } else {
        final idNumber = lines[2].substring(lines[2].indexOf(':') + 1).trim();
        final dob = lines[3].substring(lines[3].indexOf(':') + 1).trim();
        final hometown = lines[4].substring(lines[4].indexOf(':') + 1).trim();
        final permanentAddress = lines[5].substring(lines[5].indexOf(':') + 1).trim();

        idCardInfo.add(
          IDCardModel(
            type: 'Chứng minh thư (mặt trước)',
            identificationNumber: idNumber,
            dateOfBirth: dob,
            hometown: hometown,
            permanentAddress: permanentAddress,
            religion: '',
            ethnic: '',
          ),
        );
      }
    } else if (firstLine.contains('căn cước công dân')) {
      final ocrResult = await recognizeText(pickedImage);

      final line1 = lines[1].substring(0, lines[1].indexOf(':')).trim();
      final line3 = lines[3].substring(0, lines[3].indexOf(':')).trim();
      final line5 = lines[5].trim();
      final line6 = lines[6].substring(0, lines[6].indexOf(':')).trim();
      final line7 = lines[7].substring(0, lines[7].indexOf(':')).trim();
      final line9 = lines[9].trim();
      final line11 = lines[11].substring(0, lines[11].indexOf(':')).trim();
      final line12 = lines[12].substring(0, lines[12].indexOf(':')).trim();

      if (line1 != 'căn cước công dân' ||
          line3 != 'số/no' ||
          line5 != '' ||
          line6 != 'ngày sinh/date of birth' ||
          line7 != 'giới tính/sex' ||
          line9 != '' ||
          line11 != 'nơi thường trú/place of residence' ||
          line12 != '') {
        errorText = 'Không thể nhận diện đầy đủ thông tin căn cước công dân.';
      } else {
        final idNumber = lines[3].substring(lines[3].indexOf(':') + 1).trim();
        final data5 = lines[5];
        final dob = lines[6].substring(lines[6].indexOf(':') + 1).trim();
        final sex = lines[7].substring(lines[7].indexOf(':') + 1, lines[7].indexOf('quốc tịch:')).trim();
        final nationality = lines[7].substring(lines[7].indexOf('quốc tịch:') + 9).trim();
        final data9 = lines[9];
        final residence = lines[11].substring(lines[11].indexOf(':') + 1).trim() + ' ' + lines[12].trim();

        idCardInfo.add(
          IDCardModel(
            type: 'Căn cước công dân (mặt trước)',
            identificationNumber: idNumber,
            data5: data5,
            dateOfBirth: dob,
            sex: sex,
            nationality: nationality,
            data9: data9,
            residence: residence,
            personalIdentification: '',
            date: '',
          ),
        );
      }
    } else if (firstLine.contains('đặc điểm nhận dạng/personal identification') && lines[3].contains('ngày,tháng,năm/date,month,year')) {
      final ocrResult = await recognizeText(pickedImage);

      final personalIdentification = lines[1].substring(lines[1].indexOf(':') + 1);
      final dob = lines[3].substring(lines[3].indexOf(':') + 1);
      final date = lines[4].substring(lines[4].indexOf(':') + 1);
      final residence = lines[6].substring(lines[6].indexOf(':') + 1);

      idCardInfo.add(
        IDCardModel(
          type: 'Căn cước công dân (mặt sau)',
          identificationNumber: '',
          dateOfBirth: dob,
          hometown: '',
          permanentAddress: '',
          religion: '',
          ethnic: '',
          personalIdentification: personalIdentification,
          date: date,
        ),
      );
    } else {
      errorText = 'Không thể nhận diện loại giấy tờ.';
    }

    setState(() {});
  }

  Future<String> recognizeText(File imageFile) async {
    final text = await FlutterOCR.recognizeText(imageFile.path);
    return text;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('OCR Demo'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            FlatButton(
              onPressed: () {
                classifyAndRecognizeText();
              },
              child: Text('Choose Image'),
            ),
            if (idCardInfo.isNotEmpty)
              Column(
                children: <Widget>[
                  Text(errorText),
                  ListView.builder(
                    shrinkWrap: true,
                    itemCount: idCardInfo.length,
                    itemBuilder: (context, index) {
                      final idCard = idCardInfo[index];
                      return ListTile(
                        title: Text(idCard.type),
                        subtitle: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: <Widget>[
                            if (idCard.identificationNumber.isNotEmpty) Text(idCard.identificationNumber),
                            if (idCard.dateOfBirth.isNotEmpty) Text(idCard.dateOfBirth),
                            if (idCard.hometown.isNotEmpty) Text(idCard.hometown),
                            if (idCard.permanentAddress.isNotEmpty) Text(idCard.permanentAddress),
                            if (idCard.religion.isNotEmpty) Text(idCard.religion),
                            if (idCard.ethnic.isNotEmpty) Text(idCard.ethnic),
                            if (idCard.personalIdentification.isNotEmpty) Text(idCard.personalIdentification),
                            if (idCard.date.isNotEmpty) Text(idCard.date),
                          ],
                        ),
                      );
                    },
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }
}

`id_card_model.dart`:

dart

Copy code
class IDCardModel {
  String type; // Loại chứng minh thư: "Chứng minh thư (mặt trước)" hoặc "Căn cước công dân (mặt sau)"
  String identificationNumber;
  String dateOfBirth;
  String hometown;
  String permanentAddress;
  String religion;
  String ethnic;
  String personalIdentification;
  String date;

  IDCardModel({
    this.type,
    this.identificationNumber,
    this.dateOfBirth,
    this.hometown,
    this.permanentAddress,
    this.religion,
    this.ethnic,
    this.personalIdentification,
    this.date,
  });
}
Nhớ import `id_card_model.dart` trong `main.dart`:

dart

Copy code
import 'id_card_model.dart';
Hy vọng rằng mã code này sẽ giúp bạn triển khai ứng dụng OCR thành công. Nếu cần thêm trợ giúp, đừng ngần ngại để tôi biết!