import 'package:dartz/dartz.dart';
import 'package:smartconsultor/core/error/exceptions.dart';
import 'package:smartconsultor/core/error/failures.dart';
import 'package:smartconsultor/core/network/network_info.dart';
import 'package:smartconsultor/features/login/data/datasources/user_remote_data_source.dart';
import 'package:smartconsultor/features/login/domain/entities/user.dart';
import 'package:smartconsultor/features/login/domain/repositories/auth_repository.dart';

class AuthRepositoryImpl implements AuthRepository {
  final UserRemoteDataSource remoteDataSource;
  final NetworkInfo networkInfo;
  
  AuthRepositoryImpl({required this.remoteDataSource, required this.networkInfo});

  @override
  Future<Either<Failure, User>> login(String username, String password) async {
    if (await networkInfo.isConnected()) {
      try {
        return Right(await remoteDataSource.login(username, password));    
      } on ServerException {
        return Left(ServerFailure());
      }
    } else {
      return Left(NetworkError());
    }
  }
}