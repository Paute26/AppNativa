#include <jni.h>
#include <vector>
#include <jni.h>
#include <android/bitmap.h> // Esto es necesario para trabajar con Bitmap
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/highgui.hpp>
#include <android/bitmap.h>
#include <cstring>  // Para memcpy()
#include <android/log.h>

using namespace cv;

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

#include <jni.h>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>
using namespace cv;

extern "C"
JNIEXPORT jobject JNICALL
Java_epautec_atlas_appnativa_MainActivity_procesarFrame(JNIEnv *env, jobject thiz, jobject bitmap) {
    // Verifica si el bitmap es nulo
    if (bitmap == nullptr) {
        return nullptr;
    }

    AndroidBitmapInfo info;
    void* pixels;

    // Obtén la información sobre el bitmap (como tamaño y tipo)
    AndroidBitmap_getInfo(env, bitmap, &info);

    // Bloquea los píxeles del Bitmap para manipularlos
    AndroidBitmap_lockPixels(env, bitmap, &pixels);

    // Convierte los píxeles bloqueados del Bitmap en una imagen de OpenCV (Mat)
    Mat img(info.height, info.width, CV_8UC4, pixels);

    // Aplica el filtro a la imagen
    Mat result = filtroBordesArcoiris(img);

    // Obtén la clase Bitmap y el método estático createBitmap
    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapClass,
                                                          "createBitmap",
                                                          "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    // Obtén el valor del campo estático ARGB_8888 de Bitmap.Config
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID argb8888Field = env->GetStaticFieldID(bitmapConfigClass,
                                                   "ARGB_8888",
                                                   "Landroid/graphics/Bitmap$Config;");
    jobject argb8888 = env->GetStaticObjectField(bitmapConfigClass, argb8888Field);

    // Crea un nuevo Bitmap utilizando el método estático createBitmap
    jobject resultBitmap = env->CallStaticObjectMethod(bitmapClass,
                                                       createBitmapMethod,
                                                       result.cols, result.rows,
                                                       argb8888);

    // Copia los datos de la Mat a la nueva imagen Bitmap
    void* resultPixels;
    AndroidBitmap_lockPixels(env, resultBitmap, &resultPixels);

    // Copia de los datos del Mat al Bitmap
    memcpy(resultPixels, result.data, result.total() * result.elemSize());

    AndroidBitmap_unlockPixels(env, resultBitmap);
    AndroidBitmap_unlockPixels(env, bitmap); // Desbloquea el bitmap original

    return resultBitmap;
}

//TEST -----------Aprendizaje

extern "C"
JNIEXPORT jobject JNICALL
Java_epautec_atlas_appnativa_SecondActivity_filtroBA(JNIEnv *env, jobject thiz, jobject bitmap) {
    // Convierte el objeto Bitmap de Android a un objeto Mat de OpenCV
    AndroidBitmapInfo info;
    void* pixels;

    // Obtén la información sobre el bitmap (como tamaño y tipo)
    AndroidBitmap_getInfo(env, bitmap, &info);

    // Bloquea los píxeles del Bitmap para manipularlos
    AndroidBitmap_lockPixels(env, bitmap, &pixels);

    // Convierte los píxeles bloqueados del Bitmap en una imagen de OpenCV (Mat)
    Mat img(info.height, info.width, CV_8UC4, pixels);

    // Aplica el filtro a la imagen
    Mat result = filtroBordesArcoiris(img);

    // Obtén la clase Bitmap y el método estático createBitmap
    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapClass,
                                                          "createBitmap",
                                                          "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    // Obtén el valor del campo estático ARGB_8888 de Bitmap.Config
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID argb8888Field = env->GetStaticFieldID(bitmapConfigClass,
                                                   "ARGB_8888",
                                                   "Landroid/graphics/Bitmap$Config;");
    jobject argb8888 = env->GetStaticObjectField(bitmapConfigClass, argb8888Field);

    // Crea un nuevo Bitmap utilizando el método estático createBitmap
    jobject resultBitmap = env->CallStaticObjectMethod(bitmapClass,
                                                       createBitmapMethod,
                                                       result.cols, result.rows,
                                                       argb8888);

    // Copia los datos de la Mat a la nueva imagen Bitmap
    void* resultPixels;
    AndroidBitmap_lockPixels(env, resultBitmap, &resultPixels);
    memcpy(resultPixels, result.data, result.total() * result.elemSize());
    AndroidBitmap_unlockPixels(env, resultBitmap);

    // Desbloquea el bitmap original
    AndroidBitmap_unlockPixels(env, bitmap);

    return resultBitmap;
}

//TEST---------------------------------------------------------------------------------------------

extern "C"
JNIEXPORT jobject JNICALL
Java_epautec_atlas_appnativa_SecondActivity_applyFilterNative(JNIEnv *env, jobject thiz, jobject bitmap) {
    AndroidBitmapInfo info;
    void* pixels;

    // Obtener información del bitmap
    AndroidBitmap_getInfo(env, bitmap, &info);

    // Verifica si el formato es el esperado
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        __android_log_print(ANDROID_LOG_ERROR, "JNI", "Formato de bitmap no soportado: %d", info.format);
        return nullptr;
    }

    // Bloquear los píxeles del Bitmap para manipularlos
    AndroidBitmap_lockPixels(env, bitmap, &pixels);

    // Convertir los píxeles del Bitmap en una imagen Mat de OpenCV
    Mat img(info.height, info.width, CV_8UC4, pixels); // 8 bits por canal, 4 canales (RGBA)

    __android_log_print(ANDROID_LOG_INFO, "JNI", "Dimensiones de la imagen original: %d x %d", img.cols, img.rows);

    // Aplicar el filtro (convertir a escala de grises)
    Mat result;
    cvtColor(img, result, COLOR_RGBA2GRAY);  // Convierte a escala de grises

    __android_log_print(ANDROID_LOG_INFO, "JNI", "Dimensiones después del FILTRO: %d x %d", result.cols, result.rows);
    __android_log_print(ANDROID_LOG_INFO, "JNI", "Número de canales después del filtro: %d", result.channels());

    // Crear un nuevo Bitmap para el resultado con el formato adecuado
    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                          "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    // Usamos ARGB_8888 ya que es el formato de 4 canales que se maneja bien en OpenCV
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID argb8888FieldID = env->GetStaticFieldID(bitmapConfigClass, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
    jobject argb8888Config = env->GetStaticObjectField(bitmapConfigClass, argb8888FieldID);

    // Crear el Bitmap resultante con las mismas dimensiones de la imagen original
    jobject resultBitmap = env->CallStaticObjectMethod(bitmapClass, createBitmapMethod,
                                                       result.cols, result.rows, argb8888Config);

    // Bloquear los píxeles del Bitmap resultante
    void* resultPixels;
    AndroidBitmap_lockPixels(env, resultBitmap, &resultPixels);

    // Asegurarnos de que los datos de la imagen resultante se copien correctamente
    // La clave aquí es asegurarse de que los datos no se copien múltiples veces
    if (result.channels() == 1) {
        // Si la imagen está en escala de grises, copiar los datos correctamente.
        // Asegurarse de que el número de píxeles y su alineación sea correcta
        uint8_t* ptr = (uint8_t*)resultPixels;
        for (int y = 0; y < result.rows; ++y) {
            for (int x = 0; x < result.cols; ++x) {
                uint8_t pixel_value = result.at<uchar>(y, x);
                // Asignar un valor gris en los tres canales (R, G, B) y mantener el canal alfa
                ptr[(y * result.cols + x) * 4 + 0] = pixel_value; // R
                ptr[(y * result.cols + x) * 4 + 1] = pixel_value; // G
                ptr[(y * result.cols + x) * 4 + 2] = pixel_value; // B
                ptr[(y * result.cols + x) * 4 + 3] = 255; // A (totalmente opaco)
            }
        }
    } else {
        __android_log_print(ANDROID_LOG_ERROR, "JNI", "La imagen no tiene 1 canal después del filtro");
        return nullptr;
    }

    // Liberar los píxeles del Bitmap resultante
    AndroidBitmap_unlockPixels(env, resultBitmap);

    // Liberar los recursos del bitmap original
    AndroidBitmap_unlockPixels(env, bitmap);

    return resultBitmap;
}


extern "C"
JNIEXPORT jobject JNICALL
Java_epautec_atlas_appnativa_SecondActivity_ADDFiltro(JNIEnv *env, jobject thiz, jobject bitmap) {
    // Convierte el objeto Bitmap de Android a un objeto Mat de OpenCV
    AndroidBitmapInfo info;
    void* pixels;

    // Obtén la información sobre el bitmap (como tamaño y tipo)
    AndroidBitmap_getInfo(env, bitmap, &info);

    // Verifica si el formato es el esperado
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        __android_log_print(ANDROID_LOG_ERROR, "JNI", "Formato de bitmap no soportado: %d", info.format);
        return nullptr;
    }
    // Bloquea los píxeles del Bitmap para manipularlos
    AndroidBitmap_lockPixels(env, bitmap, &pixels);

    // Convierte los píxeles bloqueados del Bitmap en una imagen de OpenCV (Mat)
    Mat img(info.height, info.width, CV_8UC4, pixels);

    // Aplica el filtro a la imagen
    Mat result = filtroBordesArcoiris(img);
    //__android_log_print(ANDROID_LOG_ERROR, "ATLAS", "CANALES: %d", result.channels());

    // Obtén la clase Bitmap y el método estático createBitmap
    jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapClass,
                                                          "createBitmap",
                                                          "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    // Usamos ARGB_8888 ya que es el formato de 4 canales que se maneja bien en OpenCV
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jfieldID argb8888FieldID = env->GetStaticFieldID(bitmapConfigClass, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
    jobject argb8888Config = env->GetStaticObjectField(bitmapConfigClass, argb8888FieldID);

    // Crear el Bitmap resultante con las mismas dimensiones de la imagen original
    jobject resultBitmap = env->CallStaticObjectMethod(bitmapClass, createBitmapMethod,
                                                       result.cols, result.rows, argb8888Config);

    // Copia los datos de la Mat a la nueva imagen Bitmap
    void* resultPixels;
    AndroidBitmap_lockPixels(env, resultBitmap, &resultPixels);

    // Asegurarnos de que los datos de la imagen resultante se copien correctamente
    // La clave aquí es asegurarse de que los datos no se copien múltiples veces
    if (result.channels() == 3) {
        uint8_t* ptr = (uint8_t*)resultPixels;
        for (int y = 0; y < result.rows; ++y) {
            for (int x = 0; x < result.cols; ++x) {
                cv::Vec3b pixel = result.at<cv::Vec3b>(y, x);
                ptr[(y * result.cols + x) * 4 + 0] = pixel[2]; // R
                ptr[(y * result.cols + x) * 4 + 1] = pixel[1]; // G
                ptr[(y * result.cols + x) * 4 + 2] = pixel[0]; // B
                ptr[(y * result.cols + x) * 4 + 3] = 255;      // A
            }
        }
    }
    else {
        __android_log_print(ANDROID_LOG_ERROR, "JNI", "La imagen no tiene 1 canal después del filtro");
        return nullptr;
    }

    //memcpy(resultPixels, result.data, result.total() * result.elemSize());
    AndroidBitmap_unlockPixels(env, resultBitmap);

    // Desbloquea el bitmap original
    AndroidBitmap_unlockPixels(env, bitmap);

    return resultBitmap;
}