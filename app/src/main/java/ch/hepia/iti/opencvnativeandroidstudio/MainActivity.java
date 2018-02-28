package ch.hepia.iti.opencvnativeandroidstudio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private CameraBridgeViewBase _cameraBridgeViewBase;
    private int filterLevel = 0;
    private final int MAX_FILTER = 4;
    private Mat kernelMedian;

    private Mat kernelLaPlace;
    private Boolean blockedCamera = false;
    private float[] lastTouchDownXY = new float[2];
    private Mat lastMat;
    private float matrixHeight;
    private float matrixWidth;
    private float surfaceWidth;
    private float surfaceHeight;
    private float matrixWidthOnView;
    private float matrixHeightOnView;


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
                    kernelMedian = new Mat(3, 3, CvType.CV_32F) {
                        {
                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {
                                    put(i, j, 1.0 / 9.0);
                                }
                            }
                        }
                    };

                    kernelLaPlace = new Mat(3, 3, CvType.CV_32F) {
                        {
                            for (int i = 0; i < 3; i++) {
                                for (int j = 0; j < 3; j++) {
                                    put(i, j, (i == 1 && j == 1) ? 8 : -1);
                                }
                            }
                        }
                    };
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA},
                1);

        _cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.main_surface);
        _cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        _cameraBridgeViewBase.setCvCameraViewListener(this);

        Button toggleFilterBtn = findViewById(R.id.toggle_filter_btn);
        Button blockCameraBtn = findViewById(R.id.block_picture_btn);

        toggleFilterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterLevel++;
                filterLevel %= MAX_FILTER;
                String toDisplay = "Everything activated";
                switch (filterLevel) {
                    case 0:
                        toDisplay = "Nothing activated";
                        break;
                    case 1:
                        toDisplay = "Median blur activated";
                        break;
                    case 2:
                        toDisplay = "Edge detection activated";
                        break;
                    case 3:
                        toDisplay = "Binary treshold activated";
                        break;
                    default:
                        break;
                }
                Toast.makeText(getApplicationContext(), toDisplay, Toast.LENGTH_LONG).show();
            }
        });

        blockCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blockedCamera = !blockedCamera;
            }
        });

        _cameraBridgeViewBase.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // save the X,Y coordinates
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    lastTouchDownXY[0] = event.getX();
                    lastTouchDownXY[1] = event.getY();
                }

                // let the touch event pass on to whoever needs it
                return false;
            }
        });
        _cameraBridgeViewBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float x = lastTouchDownXY[0];
                float y = lastTouchDownXY[1];

                Log.v("COORDINATES", "Matrix on surface(" + matrixWidthOnView + "," + matrixHeightOnView + ")Matrix size(" + matrixWidth + "," + matrixHeight + "),surface size(" + surfaceWidth + "," + surfaceHeight + ")" + "=>(" + x + "," + y + ")");

                float xOnMatrix = x - (surfaceWidth - matrixWidthOnView) / 2;
                float yOnMatrix = y;

                if (xOnMatrix < 0 || xOnMatrix >= matrixWidthOnView) {
                    Log.v("TOUCH", "Touch outside image, ignoring");
                } else {
                    xOnMatrix = xOnMatrix / (matrixWidthOnView / matrixWidth);
                    yOnMatrix = yOnMatrix / (matrixHeightOnView / matrixHeight);
                    Log.v("TOUCH", "Touch inside image");
                    applyGrey(lastMat.getNativeObjAddr(), Math.round(xOnMatrix), Math.round(yOnMatrix));
                }
            }
        });
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
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
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

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat matGray = inputFrame.gray();
        int channels = matGray.channels();

        matrixHeight = matGray.height();
        matrixWidth = matGray.width();

        surfaceWidth = _cameraBridgeViewBase.getWidth();
        surfaceHeight = _cameraBridgeViewBase.getHeight();

        matrixHeightOnView = surfaceHeight;
        matrixWidthOnView = (surfaceHeight / matrixHeight) * matrixWidth;

        if (blockedCamera) {
            return lastMat;
        }


        if (filterLevel == 0) {
            lastMat = matGray;
            return matGray;
        }
        Mat mat = new Mat(matGray.rows(), matGray.cols(), matGray.type());


        applyKernel(matGray.getNativeObjAddr(), mat.getNativeObjAddr(), kernelMedian.getNativeObjAddr());
        if (filterLevel == 1) {
            lastMat = mat;
            return mat;
        }


        applyKernel(mat.getNativeObjAddr(), matGray.getNativeObjAddr(), kernelLaPlace.getNativeObjAddr());
        if (filterLevel == 2) {
            lastMat = matGray;
            return matGray;
        }
        binaryThreshold(matGray.getNativeObjAddr(), mat.getNativeObjAddr());
        lastMat = mat;
        return mat;
    }


    public native void binary(long matAddrGray);

    public native void salt(long matAddrGray, int nbrElem);

    public native void reduceColors(long matAddr, int level);

    public native void sharpen(long matAddr, long returnMatAddr);

    public native void applyKernel(long matAddr, long returnMatAddr, long kernelAddr);

    public native void binaryThreshold(long matAddr, long returnMatAddr);

    public native void applyGrey(long matAddr, int x, int y);
}

