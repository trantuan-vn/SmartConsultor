part of 'auth_bloc.dart';

sealed class AuthState extends Equatable {
  const AuthState();
  
  @override
  List<Object> get props => [];
}
@immutable
class AuthInitialState extends AuthState {}
@immutable
class AuthLoadingState extends AuthState {}
@immutable
class AuthAuthenticatedState extends AuthState {
  final String username;

  AuthAuthenticatedState({required this.username});

  @override
  List<Object> get props => [username];
}
@immutable
class AuthUnauthenticatedState extends AuthState {
  final String errorMessage;

  AuthUnauthenticatedState({required this.errorMessage});

  @override
  List<Object> get props => [errorMessage];
}
@immutable
class AuthErrorState extends AuthState {
  final String errorMessage;

  AuthErrorState({required this.errorMessage});

  @override
  List<Object> get props => [errorMessage];
}
