// test.dart
import 'package:flutter_test/flutter_test.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:mocktail/mocktail.dart';
import 'package:smartconsultor/core/network/network_info.dart';
import 'package:smartconsultor/features/login/data/datasources/user_remote_data_source.dart';
import 'package:smartconsultor/features/login/data/repositories/auth_repository_impl.dart';

class MockUserRemoteDataSource extends Mock implements UserRemoteDataSource {}
class MockConnectivity extends Mock implements Connectivity {}

void main() {
  MockConnectivity mockConnectivity= MockConnectivity();
  NetworkInfo networkInfo=NetworkInfo(mockConnectivity);  
  MockUserRemoteDataSource mockUserRemoteDataSource=MockUserRemoteDataSource();
  AuthRepositoryImpl authRepositoryImpl=AuthRepositoryImpl(remoteDataSource: mockUserRemoteDataSource, networkInfo: networkInfo);

  group('isConnected', () {
    test('should return true when ConnectivityResult is not none', () async {
      // Arrange
      when(() => mockConnectivity.checkConnectivity()).thenAnswer((_) async => ConnectivityResult.ethernet);
      // Act
      final result = await mockConnectivity.checkConnectivity();
      // Assert
      expect(result, ConnectivityResult.ethernet);
    });
  });

  

}
