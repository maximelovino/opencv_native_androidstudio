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
Java_ch_hepia_iti_opencvnativeandroidstudio_MainActivity_sharpen(JNIEnv *env, jobject instance,
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
            currentNew[j] = saturate_cast<uchar>(5 * current[j] - current[j - channels] - next[j] - current[j + channels] - precedent[j]);
        }
    }
}

}
