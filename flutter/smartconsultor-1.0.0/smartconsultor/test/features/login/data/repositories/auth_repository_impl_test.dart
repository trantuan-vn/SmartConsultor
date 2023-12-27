import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import 'package:connectivity/connectivity.dart';
import 'package:smartconsultor/core/network/network_info.dart';
void main(){
  
  Connectivity connectivity=Connectivity();
  NetworkInfo networkInfo= NetworkInfo(connectivity);

  group('isConnected', () { 
     test('should return true when ConnectivityResult is not none', () async {
      // Arrange
      when(connectivity.checkConnectivity())
          .thenAnswer((_) async => ConnectivityResult.wifi);
      // Act
      final result = await networkInfo.isConnected();
      // Assert
      expect(result, true);        
     });
  });
}
