#include <jni.h>
#include <string>
#include <sstream>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/dnn.hpp>
#include <opencv2/video.hpp>
#include <opencv2/highgui/highgui.hpp>


#include "android/bitmap.h"

using namespace std;
using namespace cv;

void bitmapToMat(JNIEnv * env, jobject bitmap, cv::Mat &dst, jboolean
needUnPremultiplyAlpha){
    AndroidBitmapInfo info;
    void* pixels = 0;
    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        dst.create(info.height, info.width, CV_8UC4);
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(needUnPremultiplyAlpha) cvtColor(tmp, dst, cv::COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, cv::COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //jclass je = env->FindClass("org/opencv/core/CvException");
        jclass je = env->FindClass("java/lang/Exception");
        //if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}

void matToBitmap(JNIEnv * env, cv::Mat src, jobject bitmap, jboolean needPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void* pixels = 0;
    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( src.dims == 2 && info.height == (uint32_t)src.rows && info.width ==
                                                                         (uint32_t)src.cols );
        CV_Assert( src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, cv::COLOR_GRAY2RGBA);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, cv::COLOR_RGB2RGBA);
            } else if(src.type() == CV_8UC4){
                if(needPremultiplyAlpha) cvtColor(src, tmp, cv::COLOR_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, cv::COLOR_GRAY2BGR565);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, cv::COLOR_RGB2BGR565);
            } else if(src.type() == CV_8UC4){
                cvtColor(src, tmp, cv::COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //jclass je = env->FindClass("org/opencv/core/CvException");
        jclass je = env->FindClass("java/lang/Exception");
        //if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}

// Filtro de bordes arcoíris en C++
Mat filtroBordesArcoiris(const Mat& frame) {
    Mat imaGris;
    cvtColor(frame, imaGris, COLOR_BGR2GRAY);
    Ptr<CLAHE> clahe = createCLAHE();
    clahe->setClipLimit(2.0);
    clahe->setTilesGridSize(Size(8, 8));
    clahe->apply(imaGris, imaGris);
    medianBlur(imaGris, imaGris, 5);
    int lowerThreshold = 70;
    int upperThreshold = 180;
    Canny(imaGris, imaGris, lowerThreshold, upperThreshold);

    Mat imaArcoirisHSV = Mat::zeros(frame.size(), CV_8UC3);
    int ancho = frame.cols;

    for (int y = 0; y < frame.rows; y++) {
        for (int x = 0; x < ancho; x++) {
            if (imaGris.at<uchar>(y, x) != 0) {
                int hue = static_cast<int>((x / static_cast<float>(ancho)) * 180);
                imaArcoirisHSV.at<Vec3b>(y, x) = Vec3b(hue, 255, 255);
            }
        }
    }

    Mat imaArcoirisBGR;
    cvtColor(imaArcoirisHSV, imaArcoirisBGR, COLOR_HSV2BGR);

    return imaArcoirisBGR;
}



extern "C" JNIEXPORT void JNICALL
Java_epautec_atlas_appnativa_MainActivity_detectorBordes(
        JNIEnv* env,
        jobject /*this*/,
        jobject bitmapIn,
        jobject bitmapOut){

    cv::Mat src;
    cv::Mat tmp;
    cv::Mat bordes;
    bitmapToMat(env, bitmapIn, src, false);

    cv:cvtColor(src, tmp, cv::COLOR_BGR2GRAY);
    cv::Laplacian(tmp, bordes, CV_16S, 3);
    cv::convertScaleAbs(bordes, bordes);
    matToBitmap(env, bordes, bitmapOut, false);
}



extern "C" JNIEXPORT jstring

JNICALL
Java_epautec_atlas_appnativa_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    int a = 0;
    int b = 1;
    int c = 0;
    stringstream ss;
    ss << a << "," << b << ",";
    for (int i=0;i<10;i++){
        c = a + b;
        a = b;
        b = c;
        ss << c << ",";
    }
    return env->NewStringUTF(ss.str().c_str());
}

extern "C"
JNIEXPORT jlong JNICALL
Java_epautec_atlas_appnativa_SecondActivity_applyRainbowEdgeFilter(JNIEnv *env, jobject thiz, jlong matAddr) {
    // Obtener la dirección de la Mat que se pasa desde Java
    Mat &frame = *(Mat *) matAddr;

    // Crear una nueva Mat para almacenar la imagen con el filtro
    Mat filteredImage = filtroBordesArcoiris(frame);

    // Devolver la dirección de la imagen procesada
    return (jlong) &filteredImage;
}



