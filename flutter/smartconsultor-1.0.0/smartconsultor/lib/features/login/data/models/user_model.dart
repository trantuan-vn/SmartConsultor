import 'package:smartconsultor/features/login/domain/entities/User.dart';

class UserModel extends User {
  UserModel({
    required String id,
    required String username,
    required String email
  }) : super(id: id, username: username, email: email);

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'],
      username: json['username'],
      email: json['email'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'email': email,
    };
  }
}