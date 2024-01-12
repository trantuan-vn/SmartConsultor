import 'package:flutter/material.dart';
import 'package:smartconsultor/features/login/domain/entities/user.dart';


class UserDisplay extends StatelessWidget {
  
  final User user;
  
  const UserDisplay({
    super.key,
    required this.user,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
        height: MediaQuery.of(context).size.height / 3,
        child: Column(
          children: <Widget>[
            const Text(
              'Login successfully',
              style: TextStyle(fontSize: 25),
              textAlign: TextAlign.center,              
            ),
            Expanded(
              child: Center(
                child: SingleChildScrollView(
                  child: Text(
                    user.toString(),
                    style: const TextStyle(fontSize: 25),
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
            ),
          ],
        ));
  }
}