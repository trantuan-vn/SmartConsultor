// test.dart
import 'package:flutter_test/flutter_test.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:mocktail/mocktail.dart';


class MockConnectivity extends Mock implements Connectivity {}

void main() {
  MockConnectivity connectivity = MockConnectivity();

  group('isConnected', () {
    test('should return true when ConnectivityResult is not none', () async {
      // Arrange
      when(() => connectivity.checkConnectivity()).thenAnswer((_) async => ConnectivityResult.ethernet);
      // Act
      final result = await connectivity.checkConnectivity();
      // Assert
      expect(result, ConnectivityResult.ethernet);
    });
  });
}
