import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:get_it/get_it.dart';
import 'package:smartconsultor/core/network/network_info.dart';
import 'package:smartconsultor/features/login/data/datasources/user_local_data_source.dart';
import 'package:smartconsultor/features/login/data/datasources/user_remote_data_source.dart';
import 'package:smartconsultor/features/login/data/repositories/auth_repository_impl.dart';
import 'package:smartconsultor/features/login/domain/repositories/auth_repository.dart';
import 'package:smartconsultor/features/login/domain/usecases/auth_use_case.dart';
import 'package:smartconsultor/features/login/presentation/bloc/auth_bloc.dart';
import 'package:http/http.dart' as http;


final sl = GetIt.instance;

Future<void> init() async {
  //core
  sl.registerLazySingleton<Connectivity>(() => Connectivity());
  sl.registerLazySingleton<NetworkInfo>(() => NetworkInfo(sl()));
  // datasource
  sl.registerLazySingleton<UserLocalDataSource>(() => UserLocalDataSourceImpl());
  sl.registerLazySingleton<UserRemoteDataSource>(() => UserRemoteDataSourceImpl(client: sl()));
  // repository
  sl.registerLazySingleton<AuthRepository>(() => AuthRepositoryImpl(localDataSource: sl(),remoteDataSource: sl(), networkInfo: sl()));
  // use case
  sl.registerLazySingleton(() => AuthUseCase(sl()));
  // bloc
  sl.registerFactory(() => AuthBloc(authUseCase: sl()));
  //external
  sl.registerLazySingleton(() => http.Client());
}

