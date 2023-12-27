import 'package:connectivity/connectivity.dart';

class NetworkInfo {
  final Connectivity connectivity;

  NetworkInfo(this.connectivity);

  Future<bool> isConnected() async {
    try {
      var result = await connectivity.checkConnectivity();
      return result != ConnectivityResult.none;
    } catch (e) {
      // Handle exceptions if necessary
      return false;
    }
  }

  Stream<bool> get onConnectivityChanged {
    return connectivity.onConnectivityChanged.map((result) {
      return result != ConnectivityResult.none;
    });
  }
}
