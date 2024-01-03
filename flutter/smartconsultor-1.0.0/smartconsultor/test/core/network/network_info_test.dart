import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:smartconsultor/core/network/network_info.dart';

class MockConnectivity extends Mock implements Connectivity {}

void main(){
  
  MockConnectivity mockConnectivity=MockConnectivity();
  NetworkInfo networkInfo=NetworkInfo(mockConnectivity);

  group('isConnected', () { 
    test('should forward the call to Connectivity.checkConnectivity()', () async {
      //arrange
      when(() => mockConnectivity.checkConnectivity()).thenAnswer((_) async => ConnectivityResult.mobile);
      //act
      final result= await networkInfo.isConnected();
      //assert
      verify(() => mockConnectivity.checkConnectivity());
      expect(result, true);
    });
  });
}