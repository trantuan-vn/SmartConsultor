import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:smartconsultor/features/login/data/datasources/user_local_data_source.dart';
import 'package:smartconsultor/features/login/data/models/user_model.dart';



class MockUserLocalDataSource extends Mock implements UserLocalDataSource {}

void main() {
  group('UserLocalDataSource', () {
    late MockUserLocalDataSource mockDataSource;
    const testUser = UserModel(id: '1', username: 'tuanta2021@gmail.com', email: 'tuanta2021@gmail.com');

    setUp(() {
      mockDataSource = MockUserLocalDataSource();
    });

    test('getLoggedInUser should return null initially', () async {
      when(() => mockDataSource.getLoggedInUser()).thenAnswer((_) async => null);

      final loggedInUser = await mockDataSource.getLoggedInUser();

      expect(loggedInUser, isNull);
    });

    test('saveLoggedInUser should save user', () async {
      
      when(() => mockDataSource.getLoggedInUser()).thenAnswer((_) async => testUser);
      when(() => mockDataSource.saveLoggedInUser(testUser)).thenAnswer((_) async => Future.value(null));

      await mockDataSource.saveLoggedInUser(testUser);

      final loggedInUser = await mockDataSource.getLoggedInUser();

      expect(loggedInUser, equals(testUser));
    });
  });
}
