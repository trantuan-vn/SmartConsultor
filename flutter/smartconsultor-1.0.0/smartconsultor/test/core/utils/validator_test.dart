import 'package:flutter_test/flutter_test.dart';
import 'package:smartconsultor/core/utils/validator.dart';


void main() {
  group('Validator', () {
    test('isEmail should return true for valid emails', () {
      expect(Validator.isEmail('test@example.com'), isTrue);
      expect(Validator.isEmail('user@domain.co'), isTrue);
      expect(Validator.isEmail('someone123@sub.domain.org'), isTrue);
    });

    test('isEmail should return false for invalid emails', () {
      expect(Validator.isEmail('invalid-email'), isFalse);
      expect(Validator.isEmail('test@com'), isFalse);
      expect(Validator.isEmail('user@domain@co'), isFalse);
    });

    test('isStrongPassword should return true for strong passwords', () {
      expect(Validator.isStrongPassword('StrongP@ss1'), isTrue);
      expect(Validator.isStrongPassword('Secure123!'), isTrue);
      expect(Validator.isStrongPassword('P@ssw0rd'), isTrue);
    });

    test('isStrongPassword should return false for weak passwords', () {
      expect(Validator.isStrongPassword('weak'), isFalse);
      expect(Validator.isStrongPassword('onlyletters'), isFalse);
      expect(Validator.isStrongPassword('12345678'), isFalse);
      expect(Validator.isStrongPassword('NoSpecialCharacter'), isFalse);
    });
  });
}
