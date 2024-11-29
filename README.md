# AppNativa
Titulo: Aplicaciones-Nativas-Android-OpenCV

> [!NOTE]
> Codigo de desarrollo en rama **"MASTER"**

La aplicacion se encuentra desarrollada en Adroid Studio junto a la aplicacion del Modulo ESP32-CAM, junto a la aplicacion de filtros y mejoras de imagen con manipulacion de pixeles para la camra del dispositivo y el video capturado del modulo

# Uso / Instalacion
1. Se clona el Repositorio
   
   ```git clone -b master --single-branch https://github.com/Paute26/AppNativa.git```
3. En Android Studio abre el proyecto mediante el navegador "FILE>OPEN..."
4. Modificar en ```MAinActivity.java``` la URL por la URL de la web dond esta alojado el servidor de la camara del modulo

   ```private String cameraUrl = "http://192.168.0.102:8080/stream"; // URL del flujo MJPEG```
6. Ejecutar con un dispositvo emulado o fisico
>Hay que tener en cuenta que el dispostivo debe de encontrarse en modo desarrollador
>con las opciones activadas de: Depuracion USB, Instalar via USB,Depuracion USB(ajustes de seguridad)
5.La APlicacion esta lista para usarse

> [!WARNING]
> Se uso Android Studio Iguana | 2023.2.1
> Tener en cuenta los puertos que se maneje
> Cuando se usa la camara del Modulo, este solo permite un ingreso
>   ES decir que solo un servicio a la vez obtendra la informacion de la camara
> EL servicio de la camara ya debe de estar en linea para poder usar la aplicacion
> Guia de webservice cam: https://randomnerdtutorials.com/esp32-cam-video-streaming-web-server-camera-home-assistant/


# Estructura
Se usa elsiguiente diagrma para tener en cuenta omo se lleva a cabo el proyecto
![image](https://github.com/user-attachments/assets/70acba5f-5145-4a7b-b19e-1398a8850ac9)

