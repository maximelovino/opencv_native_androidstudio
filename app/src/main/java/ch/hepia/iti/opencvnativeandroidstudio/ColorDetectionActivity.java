package ch.hepia.iti.opencvnativeandroidstudio;

import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ColorDetectionActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG = "ColorDetect::Activity";
    private CameraBridgeViewBase _cameraBridgeViewBase;
    private SeekBar hueMin, hueMax, satMin, satMax, lightnessMin, lightnessMax;

    private BaseLoaderCallback _baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Load ndk built module, as specified in moduleName in build.gradle
                    // after opencv initialization
                    System.loadLibrary("native-lib");
                    _cameraBridgeViewBase.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_color_detection);

        _cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.camera_surface_colorDetect);
        _cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        _cameraBridgeViewBase.setCvCameraViewListener(this);

        hueMax = findViewById(R.id.hue_max);
        hueMin = findViewById(R.id.hue_min);
        satMin = findViewById(R.id.saturation_min);
        satMax = findViewById(R.id.saturation_max);
        lightnessMax = findViewById(R.id.lightness_max);
        lightnessMin = findViewById(R.id.lightness_min);
    }

    public void disableCamera() {
        if (_cameraBridgeViewBase != null) {
            _cameraBridgeViewBase.disableView();
        }
    }

    public void enableCamera() {
        if (_cameraBridgeViewBase != null) {
            _cameraBridgeViewBase.enableView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        disableCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, _baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            _baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(ColorDetectionActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onDestroy() {
        super.onDestroy();
        disableCamera();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mat = inputFrame.rgba();

        int hueMinVal = hueMin.getProgress();
        int satMinVal = satMin.getProgress();
        int lightnessMinVal = lightnessMin.getProgress();
        int hueMaxVal = hueMax.getProgress();
        int satMaxVal = satMax.getProgress();
        int lightnessMaxVal = lightnessMax.getProgress();

        hsvDetection(mat.getNativeObjAddr(),hueMinVal,hueMaxVal,satMinVal,satMaxVal,lightnessMinVal,lightnessMaxVal);
        return mat;
    }

    public native void hsvDetection(long mataddr, int hueMin, int hueMax, int satMin, int satMax, int valMin, int valMax);


}
