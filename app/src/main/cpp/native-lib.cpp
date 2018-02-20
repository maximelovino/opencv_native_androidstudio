#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

using namespace std;
using namespace cv;

extern "C"
{
void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_MainActivity_salt(JNIEnv *env, jobject instance,
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
Java_ch_hepia_iti_opencvnativeandroidstudio_MainActivity_binary(JNIEnv *env, jobject instance,
                                                                jlong matAddrGray) {
    Mat &mGr = *(Mat *) matAddrGray;
    for (int i = 0; i < mGr.cols; ++i) {
        for (int j = 0; j < mGr.rows; ++j) {
            mGr.at<uchar>(j, i) = mGr.at<uchar>(j, i) < 127 ? 0 : 255;
        }
    }
}

void JNICALL
Java_ch_hepia_iti_opencvnativeandroidstudio_MainActivity_reduceColors(JNIEnv *env, jobject instance,
                                                                      jlong matAddr, jint level) {
    Mat &mGr = *(Mat *) matAddr;
    const int channels = mGr.channels();
    switch (channels) {
        case 1: {
            MatIterator_<uchar> it, end;
            for (it = mGr.begin<uchar>(), end = mGr.end<uchar>(); it != end; ++it)
                *it = ((*it / level) * level) + level / 2;
            break;
        }
        case 3:
            break;
        default:
            break;
    }
}
}
