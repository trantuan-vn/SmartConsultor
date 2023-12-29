import 'package:dartz/dartz.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import 'package:smartconsultor/features/login/domain/entities/user.dart';
import 'package:smartconsultor/features/login/domain/repositories/auth_repository.dart';
import 'package:smartconsultor/features/login/domain/usecases/auth_use_case.dart';


class MockAuthRepository extends Mock implements AuthRepository {}

void main() {
  late AuthUseCase authUseCase;
  late MockAuthRepository mockAuthRepository;

  setUp(() {
    mockAuthRepository = MockAuthRepository();
    authUseCase = AuthUseCase(mockAuthRepository);
  });

  group('AuthUseCase', () {
    test('should return a User when login is successful', () async {
      // Arrange
      const params = Params(username: 'test_username', password: 'test_password');
      final user = User(id: '1', username: 'test_username', email: 'test@example.com');

      // Set up the mock behavior
      when(() => mockAuthRepository.login(params.username, params.password))
          .thenAnswer((_) async => Right(user));

      // Act
      final result = await authUseCase(params);

      // Assert
      expect(result, Right(user));

      // Verify that the login method was called with the correct parameters
      verify(() => mockAuthRepository.login(params.username, params.password));
      // Verify that no other method of the mockAuthRepository was called
      verifyNoMoreInteractions(mockAuthRepository);
    });

    // Add more tests as needed, for example, handling failure cases
  });
}