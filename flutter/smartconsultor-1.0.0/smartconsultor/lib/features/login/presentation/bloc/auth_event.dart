part of 'auth_bloc.dart';

sealed class AuthEvent extends Equatable {
  const AuthEvent();

  @override
  List<Object> get props => [];
}

@immutable
class SignInEvent extends AuthEvent {
  final String username;
  final String password;

  SignInEvent({required this.username, required this.password});

  @override
  List<Object> get props => [username, password];
}
@immutable
class SignOutEvent extends AuthEvent {
  @override
  List<Object> get props => [];
}
@immutable
class RefreshTokenEvent extends AuthEvent {
  final String refreshToken;

  RefreshTokenEvent({required this.refreshToken});

  @override
  List<Object> get props => [refreshToken];
}
