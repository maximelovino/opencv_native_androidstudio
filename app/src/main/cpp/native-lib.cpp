#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/core/core.hpp>
#include <opencv2/objdetect.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

using namespace std;
using namespace cv;

extern "C"
{
void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_EdgeDetectionActivity_salt(JNIEnv *env,
                                                                       jobject instance,
                                                                       jlong matAddrGray,
                                                                       jint nbrElem) {
    Mat &mGr = *(Mat *) matAddrGray;
    for (int k = 0; k < nbrElem; k++) {
        int i = rand() % mGr.cols;
        int j = rand() % mGr.rows;
        mGr.at<uchar>(j, i) = 255;
    }
}

void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_EdgeDetectionActivity_binary(JNIEnv *env,
                                                                         jobject instance,
                                                                         jlong matAddrGray) {
    Mat &mGr = *(Mat *) matAddrGray;
    for (int i = 0; i < mGr.cols; ++i) {
        for (int j = 0; j < mGr.rows; ++j) {
            mGr.at<uchar>(j, i) = mGr.at<uchar>(j, i) < 127 ? 0 : 255;
        }
    }
}

void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_EdgeDetectionActivity_reduceColors(JNIEnv *env,
                                                                               jobject instance,
                                                                               jlong matAddr,
                                                                               jint level) {
    Mat &mat = *(Mat *) matAddr;
    const int channels = mat.channels();
    switch (channels) {
        case 1: {
            MatIterator_<uchar> it, end;
            for (it = mat.begin<uchar>(), end = mat.end<uchar>(); it != end; ++it)
                *it = ((*it / level) * level) + level / 2;
            break;
        }
        case 4: {
            MatIterator_<Vec4b> it, end;
            for (it = mat.begin<Vec4b>(), end = mat.end<Vec4b>(); it != end; ++it) {
                (*it)[0] = (((*it)[0] / level) * level) + level / 2;
                (*it)[1] = (((*it)[1] / level) * level) + level / 2;
                (*it)[2] = (((*it)[2] / level) * level) + level / 2;
            }
            break;
        }
        default:
            break;
    }
}

void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_EdgeDetectionActivity_sharpen(JNIEnv *env,
                                                                          jobject instance,
                                                                          jlong matAddr,
                                                                          jlong returnMatAddr) {

    Mat &mat = *(Mat *) matAddr;
    Mat &returnMat = *(Mat *) returnMatAddr;


    int channels = mat.channels();
    int nRows = mat.rows;
    int nCols = mat.cols * channels;
    /*if (mat.isContinuous()) {
        nCols *= nRows;
        nRows = 1;
    }*/
    int i, j;
    uchar *precedent, *current, *next, *currentNew;
    for (i = 1; i < nRows - 1; ++i) {
        precedent = mat.ptr<uchar>(i - 1);
        current = mat.ptr<uchar>(i);
        next = mat.ptr<uchar>(i + 1);
        currentNew = returnMat.ptr<uchar>(i);

        for (j = channels; j < nCols - channels; ++j) {
            currentNew[j] = saturate_cast<uchar>(
                    5 * current[j] - current[j - channels] - next[j] - current[j + channels] -
                    precedent[j]);
        }
    }
}

void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_EdgeDetectionActivity_applyKernel(JNIEnv *env,
                                                                              jobject instance,
                                                                              jlong matAddr,
                                                                              jlong returnMatAddr,
                                                                              jlong kernelAddr) {
    Mat &mat = *(Mat *) matAddr;
    Mat &matReturn = *(Mat *) returnMatAddr;
    Mat &kernelMat = *(Mat *) kernelAddr;

    filter2D(mat, matReturn, -1, kernelMat);
}


void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_EdgeDetectionActivity_binaryThreshold(JNIEnv *env,
                                                                                  jobject instance,
                                                                                  jlong matAddr,
                                                                                  jlong returnMatAddr) {
    Mat &mat = *(Mat *) matAddr;
    Mat &matReturn = *(Mat *) returnMatAddr;

    threshold(mat, matReturn, 32, 255, 1);
}


void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_EdgeDetectionActivity_lab3__JJI(JNIEnv *env,
                                                                            jobject instance,
                                                                            jlong matAddr,
                                                                            jlong returnMatAddr,
                                                                            jint filterLevel) {

    Mat &mat = *(Mat *) matAddr;
    Mat &matReturn = *(Mat *) returnMatAddr;
    Mat detected_edges;

    if (filterLevel == 0) {
        mat.copyTo(matReturn);
        return;
    }


    blur(mat, detected_edges, Size(3, 3));

    Canny(detected_edges, detected_edges, 50, 200);


    if (filterLevel == 1) {
        matReturn = Scalar::all(0);
        mat.copyTo(matReturn, detected_edges);
    }

    if (filterLevel == 2) {
        Mat dst;
        cvtColor(detected_edges, dst, CV_GRAY2BGR);
        vector<Vec4i> lines;
        HoughLinesP(detected_edges, lines, 1, CV_PI / 180, 50, 50, 10);

        for (size_t i = 0; i < lines.size(); i++) {
            Vec4i l = lines[i];
            line(dst, Point(l[0], l[1]), Point(l[2], l[3]), Scalar(0, 0, 255), 3, CV_AA);
        }
        matReturn = Scalar::all(0);
        dst.copyTo(matReturn);
        return;
    }

    if (filterLevel == 3) {
        Mat dst;

        vector<Vec3f> circles;

        HoughCircles(mat, circles, CV_HOUGH_GRADIENT, 1, mat.rows / 8, 200, 100, 0, 0);
        cvtColor(mat, dst, CV_GRAY2BGR);

        for (size_t i = 0; i < circles.size(); i++) {
            Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
            int radius = cvRound(circles[i][2]);
            // circle center
            circle(dst, center, 3, Scalar(0, 255, 0), -1, 8, 0);
            // circle outline
            circle(dst, center, radius, Scalar(0, 0, 255), 3, 8, 0);
        }
        matReturn = Scalar::all(0);
        dst.copyTo(matReturn);
    }
}


void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_EdgeDetectionActivity_applyGrey(JNIEnv *env,
                                                                            jobject instance,
                                                                            jlong matAddr,
                                                                            jint x, jint y) {
    Mat &mat = *(Mat *) matAddr;


    if (x < 0 || x >= mat.cols || y < 0 || y >= mat.rows) {
        return;
    }


    if (mat.at<uchar>(x, y) != 255) {
        return;
    }
    __android_log_print(ANDROID_LOG_VERBOSE, "RECURSION", "(%d,%d)", x, y);
    mat.at<uchar>(x, y) = 127;


    for (int i = -1; i <= 1; ++i) {
        for (int j = -1; j <= 1; ++j) {
            if (i == 0 && j == 0) {
                continue;
            }
            Java_ch_hepia_iti_opencvnativeandroidstudio_EdgeDetectionActivity_applyGrey(env,
                                                                                        instance,
                                                                                        matAddr,
                                                                                        x + i,
                                                                                        y + j);
        }
    }
}


void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_ColorDetectionActivity_hsvDetection(JNIEnv *env,
                                                                                jobject instance,
                                                                                jlong matAddr,
                                                                                jint hueMin,
                                                                                jint hueMax,
                                                                                jint satMin,
                                                                                jint satMax,
                                                                                jint valMin,
                                                                                jint valMax) {
    Mat &mat = *(Mat *) matAddr;
    cvtColor(mat, mat, COLOR_RGB2HSV);
    inRange(mat, Scalar(hueMin, satMin, valMin), Scalar(hueMax, satMax, valMax), mat);
}

void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_FaceDetectionActivity_detectFaces(JNIEnv *env,
                                                                              jobject instance,
                                                                              jlong matAddr) {
    Mat &mat = *(Mat *) matAddr;
    Mat matGray;
    cvtColor(mat,matGray,COLOR_RGBA2GRAY);
    //These files are also stored in res/classifierAssets
    Mat hat = imread("/sdcard/ClassifierApp/cowboy_hat.png", IMREAD_UNCHANGED);
    CascadeClassifier classifier("/sdcard/ClassifierApp/faceClassifier.xml");
    vector<Rect> faces;
    classifier.detectMultiScale(matGray, faces, 1.1, 3, CV_HAAR_SCALE_IMAGE, Size(300, 300));

    for (size_t i = 0; i < faces.size(); i++) {
        //rectangle(mat, faces[i], Scalar(255));
        int baseX = faces[i].x;
        int baseY = faces[i].y;
        double factor = (double)faces[i].width / hat.cols;
        resize(hat, hat,Size(faces[i].width,(int)(hat.rows*factor)));

        //We check if the hat fits in the image
        if (baseY - hat.rows >= 0 && baseX - hat.cols >= 0 && baseX + hat.cols < mat.cols) {
            //We copy and avoid all transparent pixels
            for (int j = 0; j < hat.rows; ++j) {
                for (int k = 0; k < hat.cols; ++k) {
                    if (hat.at<Vec4b>(j,k)[3] > 100){
                        mat.at<Vec4b>(baseY-hat.rows + j, baseX + k) = hat.at<Vec4b>(j,k);
                    }
                }
            }
        }
    }
}

}
