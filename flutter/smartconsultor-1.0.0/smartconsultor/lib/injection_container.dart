import 'package:get_it/get_it.dart';
import 'package:smartconsultor/core/network/network_info.dart';
import 'package:smartconsultor/features/login/data/datasources/user_local_data_source.dart';
import 'package:smartconsultor/features/login/data/datasources/user_remote_data_source.dart';
import 'package:smartconsultor/features/login/data/repositories/auth_repository_impl.dart';
import 'package:smartconsultor/features/login/domain/repositories/auth_repository.dart';
import 'package:smartconsultor/features/login/domain/usecases/auth_use_case.dart';
import 'package:smartconsultor/features/login/presentation/bloc/auth_bloc.dart';


final sl = GetIt.instance;

void init(){
  // bloc
  sl.registerFactory(() => AuthBloc(authUseCase: sl()));
  // use case
  sl.registerLazySingleton(() => AuthUseCase(sl()));
  // repository
  sl.registerLazySingleton<AuthRepository>(() => AuthRepositoryImpl(localDataSource: sl(),remoteDataSource: sl(), networkInfo: sl()));
  // datasource
  sl.registerLazySingleton<UserLocalDataSource>(() => UserLocalDataSourceImpl());
  sl.registerLazySingleton<UserRemoteDataSource>(() => UserRemoteDataSourceImpl(client: sl()));
  //core
  sl.registerLazySingleton<NetworkInfo>(() => NetworkInfo(sl()));
  //external
 
}

