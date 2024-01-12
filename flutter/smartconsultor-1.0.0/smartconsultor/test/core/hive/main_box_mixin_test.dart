import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:smartconsultor/core/hive/main_box_mixin.dart';

// Import the code you want to test


class MockBox extends Mock implements Box {}

void main() {
  group('MainBoxMixin', () {
    late MainBoxMixin mainBoxMixin;
    late MockBox mockBox;

    setUp(() {
      mockBox = MockBox();
      mainBoxMixin = MainBoxMixin();
      MainBoxMixin.mainBox = mockBox;
    });

    tearDown(() async {
      await mainBoxMixin.closeBox(isUnitTest: true);
    });

    test('Init Hive', () async {
      // Arrange
      when(() => Hive.initFlutter()).thenAnswer((_) async {});

      // Act
      await MainBoxMixin.initHive('prefix_');

      // Assert
      verify(() => Hive.initFlutter()).called(1);
      verify(() => Hive.openBox('prefix_smart_consultor')).called(1);
    });

    test('Add data to box', () async {
      // Arrange
      when(() => mockBox.put(any(), any())).thenAnswer((_) async {});

      // Act
      await mainBoxMixin.addData(MainBoxKeys.token, 'your_token');

      // Assert
      verify(() => mockBox.put('token', 'your_token')).called(1);
    });

    test('Remove data from box', () async {
      // Arrange
      when(() => mockBox.delete(any())).thenAnswer((_) async {});

      // Act
      await mainBoxMixin.removeData(MainBoxKeys.token);

      // Assert
      verify(() => mockBox.delete('token')).called(1);
    });

    test('Get data from box', () {
      // Arrange
      when(() => mockBox.get(any())).thenReturn('your_token');

      // Act
      final result = mainBoxMixin.getData<String>(MainBoxKeys.token);

      // Assert
      expect(result, 'your_token');
    });

    test('Close box', () async {
      // Arrange
      when(() => mockBox.close()).thenAnswer((_) async {});
      when(() => mockBox.deleteFromDisk()).thenAnswer((_) async {});

      // Act
      await mainBoxMixin.closeBox();

      // Assert
      verify(() => mockBox.close()).called(1);
      verify(() => mockBox.deleteFromDisk()).called(1);
    });
  });
}
