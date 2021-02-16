package dev.sanketnaik.flutter_fer

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs.imread
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import org.opencv.core.Core.*
import org.opencv.imgcodecs.Imgcodecs.imwrite
import org.opencv.imgproc.Imgproc.rectangle
import org.opencv.imgproc.Imgproc.resize
import org.pytorch.Module
import org.pytorch.PyTorchAndroid
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.IOException

class MainActivity: FlutterActivity() {
    private val CHANNEL = "dev.sanketnaik.flutter_fer/fer"

    init {
        if(OpenCVLoader.initDebug()){
            Log.i("OPEN_CV", "LOADED OPENCV");
        }else{
            Log.e("OPEN_CV", "FAILED TO LOAD OPENCV")
        }
    }


    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if(call.method == "predict"){
                val args = call.arguments as Map<String, Any>
                val imagePath = predict(args["imagePath"] as String)
                if(imagePath != null){
                    result.success(imagePath)
                }else{
                    result.error("ERROR", "Something went wrong", null);
                }
            } else{
                result.error("ERROR", "Something went wrong", null)
            }
        }
    }

    private fun predict(imagePath: String): MutableList<String> {
        if(OpenCVLoader.initDebug()){
            Log.i("OPEN_CV", "LOADED OPENCV");
        }else{
            Log.e("OPEN_CV", "FAILED TO LOAD OPENCV")
        }

        val originalImage: Mat = imread(imagePath)
        val image: Mat = originalImage.clone()
        val pathList = imagePath.split(".").toMutableList()
        pathList[pathList.size - 2] = pathList[pathList.size - 2] + "-new"
        val newPath = pathList.joinToString(".")

        val inputStream: InputStream = resources.openRawResource(R.raw.haarcascade_frontalface_default)
        val cascadeDir = getDir("cascade", Context.MODE_PRIVATE)
        val mCascadeFile = File(cascadeDir, "haarcascade_frontalface_default.xml")
        val os: FileOutputStream = FileOutputStream(mCascadeFile)

        val buffer = ByteArray(4096 )
        var byteRead = inputStream.read(buffer)
        while (byteRead != -1) {
            os.write(buffer, 0, byteRead)
            byteRead = inputStream.read(buffer)
        }

        inputStream.close()
        os.close()

        val faceDetector = CascadeClassifier(mCascadeFile.absolutePath)
        var detections: MatOfRect = MatOfRect()

        faceDetector.detectMultiScale(image, detections, 1.2)

        Log.i("FACE_DETECTION", "${detections.toList().size}")
        Log.i("FACE_DETECTION", detections.toString())

        val rects :List<Rect> = detections.toList()
        val rect = rects.get(0)
        rectangle(image, Point(rect.x.toDouble(), rect.y.toDouble()), Point((rect.x + rect.width).toDouble(), (rect.y + rect.height).toDouble()), Scalar(0.0,255.0,0.0, 255.0), 5, 4, 0)

        val cropped: Mat = Mat(image, rect)
        resize(cropped, cropped, Size(48.0, 48.0))
        val bitmap: Bitmap = Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(cropped, bitmap)
        val resizedImage = Bitmap.createScaledBitmap(bitmap, 48, 48, true)

//        PyTorchAndroid.setNumThreads(4)
        val inputTensor: Tensor = TensorImageUtils.bitmapToFloat32Tensor(resizedImage, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        Log.i("INPUT TENSOR", "${inputTensor.dtype()}")



        var module: Module
        try {
            module = PyTorchAndroid.loadModuleFromAsset(assets, "convnet-quantized-full.pt")
            Log.i("PYTORCH", "MODULE LOADED SUCCESSFULLY")
        }catch (e: IOException){
            Log.i("IO_EXCEPTION", "IO EXCEPTION WHILE LOADING THE MODEL")
        }


        imwrite(newPath, image)
        val result: MutableList<String> = ArrayList()
        result.add(newPath)
        result.add("Time Taken: 10 seconds")
        return result
    }
}