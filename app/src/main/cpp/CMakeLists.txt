cmake_minimum_required(VERSION 3.22.1)

project("appnativa")

# Configuración de OpenCV (asegúrate de que OpenCV esté bien configurado)
set(OpenCV_STATIC on)
set(OpenCV_DIR /home/atlas/aplicaciones/OpenCV-android-sdk/sdk/native/jni)
find_package(OpenCV REQUIRED)
include_directories(/home/atlas/aplicaciones/OpenCV-android-sdk/sdk/native/jni/ include)

# Buscar la biblioteca log
find_library(
        log-lib
        log
)

# Buscar la biblioteca jnigraphics
find_library(
        jnigraphics-lib jnigraphics
)

# Crear la biblioteca compartida (SHARED) con el código fuente native-lib.cpp
add_library(${CMAKE_PROJECT_NAME} SHARED
        native-lib.cpp)

# Vincular bibliotecas necesarias
target_link_libraries(${CMAKE_PROJECT_NAME}
        ${OpenCV_LIBS}
        ${jnigraphics-lib}
        ${log-lib}
        android)
