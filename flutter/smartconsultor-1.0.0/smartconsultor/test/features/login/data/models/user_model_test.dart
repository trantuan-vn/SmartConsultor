import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:smartconsultor/features/login/data/models/user_model.dart';

import '../../../../fixtures/fixtures_reader.dart';



void main() {
  final userModel = UserModel(id: '1', username: 'tuanta', email: 'tuanta2021@gmail.com');

  test(
    'should be a subclass of User entity',
    () async {
      // assert
      expect(userModel, isA<UserModel>());
    },
  );

  group('fromJson', () {
    test(
      'should return a valid model when the JSON number is an User',
      () async {
        // arrange
        final Map<String, dynamic> jsonMap =
            json.decode(fixture('user.json'));
        // act
        final result = UserModel.fromJson(jsonMap);
        // assert
        expect(result, userModel);
      },
    );
  });

  group('toJson', () {
    test(
      'should return a JSON map containing the proper data',
      () async {
        // act
        final result = userModel.toJson();
        // assert
        final expectedMap = {
          "id": "1",
          "username": "tuanta",
          "email": "tuanta2021@gmail.com"
        };
        expect(result, expectedMap);
      },
    );
  });
}