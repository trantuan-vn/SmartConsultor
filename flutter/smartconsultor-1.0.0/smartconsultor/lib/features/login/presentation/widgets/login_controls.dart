import 'package:flutter/material.dart';
import 'package:responsive_framework/responsive_framework.dart';


class LoginControls extends StatefulWidget {
  const LoginControls({
    super.key,
  });

  @override
  State<LoginControls> createState() => _LoginControlsState();
}

class _LoginControlsState extends State<LoginControls> {
  /// Controller
final TextEditingController usernameController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: ResponsiveRowColumn(
          rowMainAxisAlignment: MainAxisAlignment.center,
          rowPadding: const EdgeInsets.all(30),
          columnPadding: const EdgeInsets.all(30),
          layout: ResponsiveBreakpoints.of(context).largerThan(MOBILE) ? ResponsiveRowColumnType.COLUMN: ResponsiveRowColumnType.ROW,
          children: [
            ResponsiveRowColumnItem(
              rowFlex: 1,
              child: Image.asset('assets/login_image.png'), // Thay đổi đường dẫn ảnh
            ),
            ResponsiveRowColumnItem(
              rowFlex: 2,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    TextField(
                      controller: usernameController,
                      decoration: const InputDecoration(labelText: 'Username (Email)'),
                      keyboardType: TextInputType.emailAddress,
                      // Add email validation logic if needed
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      controller: passwordController,
                      decoration: const InputDecoration(labelText: 'Password'),
                      obscureText: true,
                      // Add strong password validation logic if needed
                    ),
                    const SizedBox(height: 32),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        ElevatedButton(
                          onPressed: () {
                            // Add login logic here
                          },
                          child: const Text('OK'),
                        ),
                        ElevatedButton(
                          onPressed: () {
                            usernameController.clear();
                            passwordController.clear();
                          },
                          child: const Text('Cancel'),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}