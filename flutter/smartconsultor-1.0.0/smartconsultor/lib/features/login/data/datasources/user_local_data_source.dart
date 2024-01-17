import 'package:smartconsultor/core/error/exceptions.dart';
import 'package:smartconsultor/core/hive/main_box_mixin.dart';
import 'package:smartconsultor/features/login/data/models/user_model.dart';


abstract class UserLocalDataSource {
  Future<UserModel?> getLoggedInUser();
  Future<void> saveLoggedInUser(UserModel user);
}

class UserLocalDataSourceImpl with MainBoxMixin implements UserLocalDataSource {
  static const String _userKey = 'user_';

  @override
  Future<UserModel?> getLoggedInUser() async {
    try {
      await MainBoxMixin.initHive(_userKey); // Thay đổi 'user_' thành tiền tố phù hợp
      final userJson = getData<Map<String, dynamic>>(MainBoxKeys.isLogin);
      await closeBox();
      
      // ignore: unnecessary_null_comparison
      if (userJson != null) {
        return UserModel.fromJson(userJson);
      } else {
        return null;
      }
    } catch (e) {
      throw CacheException(); // hoặc bất kỳ ngoại lệ tùy chỉnh nào khác
    }
  }

  @override
  Future<void> saveLoggedInUser(UserModel user) async {
    try {
      await MainBoxMixin.initHive(_userKey); // Thay đổi 'user_' thành tiền tố phù hợp

      await addData<Map<String, dynamic>>(MainBoxKeys.isLogin, user.toJson());
      await closeBox();
    } catch (e) {
      throw CacheException(); // hoặc bất kỳ ngoại lệ tùy chỉnh nào khác
    }
  }
}
