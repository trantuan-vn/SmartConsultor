
import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';
import 'package:smartconsultor/core/error/failures.dart';
import 'package:smartconsultor/core/usecases/use_case.dart';
import 'package:smartconsultor/features/login/domain/entities/user.dart';
import 'package:smartconsultor/features/login/domain/repositories/auth_repository.dart';

class AuthUseCase implements UseCase<User, Params>{
  final AuthRepository auth_repository;

  AuthUseCase(this.auth_repository);

  @override
  Future<Either<Failure, User>> call(Params params) async {
    return await auth_repository.login(params.username, params.password);
  }
}

class Params extends Equatable {
  final String username;
  final String password;

  Params({required this.username, required this.password});

  @override
  List<Object> get props => [username, password];
}