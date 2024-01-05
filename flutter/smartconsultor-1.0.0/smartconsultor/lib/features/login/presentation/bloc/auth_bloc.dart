import 'package:flutter/widgets.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:smartconsultor/core/utils/validator.dart';
import 'package:smartconsultor/features/login/domain/usecases/auth_use_case.dart';

part 'auth_event.dart';
part 'auth_state.dart';

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final AuthUseCase authUseCase;

  AuthBloc({required this.authUseCase}) : super(AuthInitialState()){
    on<AuthEvent>((event, emit) async {
    emit(AuthLoadingState());

    if (event is SignInEvent) {
      // Xử lý sự kiện đăng nhập
      
      try {
        // Thực hiện kiểm tra username và password ở đây trước khi gọi use case
        if (!Validator.isEmail(event.username)) {
          emit(AuthErrorState(errorMessage: 'Invalid email format'));
          return;
        }

        if (!Validator.isStrongPassword(event.password)) {
          emit(AuthErrorState(errorMessage: 'Weak password'));
          return;
        }

        final result = await authUseCase(Params(username: event.username,password: event.password));
        
        if (result.isRight()) {
          emit(AuthAuthenticatedState(username: event.username));
        } else {
          emit(AuthUnauthenticatedState(errorMessage: 'Invalid credentials'));
        }
      } catch (e) {
        emit(AuthUnauthenticatedState(errorMessage: 'An error occurred'));
      }
    } else if (event is SignOutEvent) {
      // Xử lý sự kiện đăng xuất
      // Thực hiện đăng xuất
      emit(AuthUnauthenticatedState(errorMessage: 'SignOut'));
    } else if (event is RefreshTokenEvent) {
      // Xử lý sự kiện làm mới token

      // Thực hiện làm mới token và kiểm tra thành công hay không
      // Giả sử refreshToken là hợp lệ
      emit(AuthAuthenticatedState(username: 'tuanta'));
    }      
    });
  }
}