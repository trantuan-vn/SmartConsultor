import 'package:bloc_test/bloc_test.dart';
import 'package:dartz/dartz.dart'; 
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:smartconsultor/core/error/failures.dart';
import 'package:smartconsultor/features/login/data/models/user_model.dart';
import 'package:smartconsultor/features/login/domain/usecases/auth_use_case.dart';
import 'package:smartconsultor/features/login/presentation/bloc/auth_bloc.dart';

// Mock your AuthUseCase
class MockAuthUseCase extends Mock implements AuthUseCase {}

void main() {
  group('AuthBloc', () {
    const params = Params(username: 'tuanta2021@gmail.com', password: 'P@ssw0rd');
    const invalidEmailParams = Params(username: 'tuanta2021gmail.com', password: 'P@ssw0rd');
    const weakPassParams = Params(username: 'tuanta2021@gmail.com', password: 'Passw0rd');
    const user = UserModel(id: '1', username: 'tuanta2021@gmail.com', email: 'tuanta2021@gmail.com');
    
    late AuthBloc authBloc;
    late MockAuthUseCase mockAuthUseCase;

    setUp(() {
      mockAuthUseCase = MockAuthUseCase();
      authBloc = AuthBloc(authUseCase: mockAuthUseCase);
    });

    tearDown(() {
      authBloc.close();
    });

    test('initial state is AuthInitialState', () {
      expect(authBloc.state, AuthInitialState());
    });

    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoadingState, AuthAuthenticatedState] when SignInEvent is added successfully',
      build: () {
        when(() => mockAuthUseCase(params))
            .thenAnswer((_) async => const Right(user));
        return authBloc;
      },
      act: (bloc) => bloc.add(SignInEvent(username: params.username, password: params.password)),
      expect: () => [
        AuthLoadingState(),
        const AuthAuthenticatedState(user: user),
      ],
    );
    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoadingState, AuthErrorState] when Invalid email format',
      build: () {
        return authBloc;
      },
      act: (bloc) => bloc.add(SignInEvent(username: invalidEmailParams.username, password: invalidEmailParams.password)),
      expect: () => [
        AuthLoadingState(),
        const AuthErrorState(errorMessage: 'Invalid email format'),
      ],
    );
    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoadingState, AuthErrorState] when Weak password',
      build: () {
        return authBloc;
      },
      act: (bloc) => bloc.add(SignInEvent(username: weakPassParams.username, password: weakPassParams.password)),
      expect: () => [
        AuthLoadingState(),
        const AuthErrorState(errorMessage: 'Weak password'),
      ],
    );        

    blocTest<AuthBloc, AuthState>(
      'emits [AuthLoadingState, AuthUnauthenticatedState] when SignInEvent has invalid credentials',
      build: () {
        when(() => mockAuthUseCase(params))
            .thenAnswer((_) async => Left(ServerFailure()));        
        return authBloc;
      },
      act: (bloc) => bloc.add(SignInEvent(username: params.username, password: params.password)),
      expect: () => [
        AuthLoadingState(),
        const AuthUnauthenticatedState(errorMessage: 'Invalid credentials'),
      ],
    );

    // Add more tests for SignOutEvent, RefreshTokenEvent, and error cases as needed.
  });
}
