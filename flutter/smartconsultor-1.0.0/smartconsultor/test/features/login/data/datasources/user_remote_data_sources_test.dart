import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:http/http.dart' as http;
import 'package:smartconsultor/core/error/exceptions.dart';
import 'package:smartconsultor/features/login/data/datasources/user_remote_data_source.dart';
import 'package:smartconsultor/features/login/data/models/user_model.dart';

import '../../../../fixtures/fixtures_reader.dart';

class MockHttpClient extends Mock implements http.Client {}


void main() {
  MockHttpClient mockHttpClient=MockHttpClient();
  UserRemoteDataSourceImpl userRemoteDataSourceImpl=UserRemoteDataSourceImpl(client: mockHttpClient);

  group('auth', () { 
    UserModel userModel=UserModel.fromJson(json.decode(fixture('user.json')));
    test('should perform a get request on a URL', () async {
      //arrange
      final url=Uri.parse('http://numbersapi.com/random');

      when(() => mockHttpClient.get(url,headers: {'Content-Type': 'application/json'})).thenAnswer((_) async => http.Response(fixture('user.json'),200));
      //act

      final result= await userRemoteDataSourceImpl.login('tuanta', 'abcd1234');
      //assert
      verify(() => mockHttpClient.get(
        url,
        headers: {
        'Content-Type': 'application/json',
        }
      ));
      
      expect(result, userModel);

    });
    test('should throw a ServerException', () async {
      //arrange
      final url=Uri.parse('http://numbersapi.com/random');

      when(() => mockHttpClient.get(url,headers: {'Content-Type': 'application/json'})).thenAnswer((_) async => http.Response('Somthing go wrong',404));
      //act
      final call= userRemoteDataSourceImpl.login;
      //assert
      expect(() => call('tuanta','abcd1234'), throwsA(TypeMatcher<ServerException>()));
    });
  });

}

