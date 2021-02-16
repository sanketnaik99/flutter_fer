import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ImageResultArguments {
  final String imagePath;
  ImageResultArguments({this.imagePath});
}

class ImageResult extends StatefulWidget {
  @override
  _ImageResultState createState() => _ImageResultState();
}

class _ImageResultState extends State<ImageResult> {
  String _imagePath = "";
  static const platform = const MethodChannel('dev.sanketnaik.flutter_fer/fer');

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    Future.delayed(Duration.zero, () {
      final ImageResultArguments args =
          ModalRoute.of(context).settings.arguments;
      setState(() {
        _imagePath = args.imagePath;
      });
      predict(_imagePath);
    });
  }

  predict(String imagePath) async {
    List<dynamic> result =
        await platform.invokeMethod('predict', {"imagePath": _imagePath});
    setState(() {
      _imagePath = result[0];
    });
    print("RESULT => ${result}");
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Image Result'),
      ),
      body: _imagePath == ""
          ? CircularProgressIndicator()
          : Image.file(
              File(_imagePath),
            ),
    );
  }
}
