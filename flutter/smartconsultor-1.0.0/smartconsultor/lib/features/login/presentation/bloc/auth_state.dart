import 'package:equatable/equatable.dart';
import 'package:smartconsultor/features/login/domain/entities/User.dart';

abstract class AuthState extends Equatable {
  const AuthState();

  @override
  List<Object?> get props => [];
}

class AuthInitial extends AuthState {}

class AuthLoading extends AuthState {}

class AuthSuccess extends AuthState {
  final User user;

  const AuthSuccess(this.user);

  @override
  List<Object?> get props => [user];
}

class AuthFailure extends AuthState {
  final AuthError error;

  const AuthFailure(this.error);

  @override
  List<Object?> get props => [error];
}

// Các lớp lỗi cụ thể
abstract class AuthError extends Equatable {
  const AuthError();

  @override
  List<Object?> get props => [];
}

class InvalidCredentials extends AuthError {}

class NetworkError extends AuthError {}
