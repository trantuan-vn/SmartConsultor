// test.dart
import 'package:dartz/dartz.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:mocktail/mocktail.dart';
import 'package:smartconsultor/core/error/exceptions.dart';
import 'package:smartconsultor/core/error/failures.dart';
import 'package:smartconsultor/core/network/network_info.dart';
import 'package:smartconsultor/features/login/data/datasources/user_local_data_source.dart';
import 'package:smartconsultor/features/login/data/datasources/user_remote_data_source.dart';
import 'package:smartconsultor/features/login/data/models/user_model.dart';
import 'package:smartconsultor/features/login/data/repositories/auth_repository_impl.dart';
import 'package:smartconsultor/features/login/domain/entities/user.dart';

class MockUserRemoteDataSource extends Mock implements UserRemoteDataSource {}
class MockUserLocalDataSource extends Mock implements UserLocalDataSource {}
class MockConnectivity extends Mock implements Connectivity {}

void main() {
  MockConnectivity mockConnectivity= MockConnectivity();
  NetworkInfo networkInfo=NetworkInfo(mockConnectivity);  
  MockUserRemoteDataSource mockUserRemoteDataSource=MockUserRemoteDataSource();
  MockUserLocalDataSource mockUserLocalDataSource=MockUserLocalDataSource();
  AuthRepositoryImpl authRepositoryImpl=AuthRepositoryImpl(localDataSource: mockUserLocalDataSource,remoteDataSource: mockUserRemoteDataSource, networkInfo: networkInfo);
  
  final userModel = UserModel(id: '1', username: 'tuanta', email: 'tuanta2021@gmail.com');
  
  group('Auth', () {
    test("login successfully", () async {
      // arrange
      when(() => mockConnectivity.checkConnectivity()).thenAnswer((_) async => ConnectivityResult.ethernet);      
      when(() => mockUserRemoteDataSource.login('tuanta2021@gmail.com', 'abcd@1234')).thenAnswer((_) async => userModel);
      // act
      final result=await authRepositoryImpl.login('tuanta2021@gmail.com','abcd@1234');
      // assert
      verify(() => networkInfo.isConnected());
      expect(result, Right(userModel));
    });
    test("ServerException", () async {
      // arrange
      when(() => mockConnectivity.checkConnectivity()).thenAnswer((_) async => ConnectivityResult.ethernet);      
      when(() => mockUserRemoteDataSource.login('tuanta2021@gmail.com', 'abcd@1234')).thenThrow(ServerException());
      // act
      final result=await authRepositoryImpl.login('tuanta2021@gmail.com','abcd@1234');
      // assert
      expect(result, isA<Left<Failure,User>>());
    });
    test("NetworkException", () async {
      // arrange
      when(() => mockConnectivity.checkConnectivity()).thenAnswer((_) async => ConnectivityResult.none);      
      // act
      final result=await authRepositoryImpl.login('tuanta2021@gmail.com','abcd@1234');
      // assert
      expect(result, isA<Left<Failure,User>>());
    });

  });
}
