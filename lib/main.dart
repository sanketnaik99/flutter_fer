import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:flutter_fer/pages/captureImage.dart';
import 'package:flutter_fer/pages/imageResult.dart';
import 'package:flutter_fer/routes.dart';

List<CameraDescription> cameras;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  cameras = await availableCameras();
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'FER',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
      ),
      initialRoute: CAPTURE_IMAGE,
      routes: {
        CAPTURE_IMAGE: (context) => CaptureImage(),
        IMAGE_RESULT: (context) => ImageResult(),
      },
    );
  }
}
