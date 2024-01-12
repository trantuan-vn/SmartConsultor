import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:smartconsultor/features/login/presentation/bloc/auth_bloc.dart';
import 'package:smartconsultor/features/login/presentation/widgets/widgets.dart';
import 'package:smartconsultor/injection_container.dart';

class LoginPage extends StatelessWidget {
  const LoginPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Login'),
      ),
      body: buildBody(context),
    );
  }

  BlocProvider<AuthBloc> buildBody(BuildContext context) {
    return BlocProvider(
        create: (_) => sl<AuthBloc>(),
        child: Padding(
          padding: const EdgeInsets.all(10),
          child: Column(
            children: <Widget>[
              const SizedBox(height: 10),
              //top half
              BlocBuilder<AuthBloc, AuthState>(
                builder: (context, state) {
                  if (state is AuthErrorState) {
                    return MessageDisplay(
                      message: state.errorMessage,
                    );
                  } else if (state is AuthAuthenticatedState) {
                    return UserDisplay(
                      user: state.user,
                    );
                  } else if (state is AuthUnauthenticatedState) {
                    return MessageDisplay(
                      message: state.errorMessage,
                    );
                  } else if (state is AuthLoadingState) {
                    return const LoadingWidget();
                  }
                  return const MessageDisplay(
                    message: 'Start loginning',
                  );
                },
              ),
              const SizedBox(height: 20),
              // bottom
              const LoginControls(),
            ],
          ),
        ));
  }
}


