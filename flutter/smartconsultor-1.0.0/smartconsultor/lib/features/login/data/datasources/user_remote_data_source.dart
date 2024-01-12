import 'dart:convert';

import 'package:smartconsultor/core/error/exceptions.dart';
import 'package:smartconsultor/features/login/data/models/user_model.dart';
import 'package:http/http.dart' as http;
import 'package:smartconsultor/fixture/fixture.dart';


abstract class UserRemoteDataSource {
  /// Calls the http://numbersapi.com/{number} endpoint.
  ///
  /// Throws a [ServerException] for all error codes.
  Future<UserModel> login(String username, String password);

}

class UserRemoteDataSourceImpl implements UserRemoteDataSource {
  final http.Client client;
  UserRemoteDataSourceImpl({required this.client});
  @override
  Future<UserModel> login(String username, String password) => _getUserFromUrl('http://localhost:8081/api/login');
  
  Future<UserModel> _getUserFromUrl(String url) async {
    /*
    final uri=Uri.parse(url);
    final response = await client.get(
      uri,
      headers: {
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return UserModel.fromJson(json.decode(response.body));
    } else {
      throw ServerException();
    }
    */
    return UserModel.fromJson(json.decode(fixture('user.json')));
  }

}
