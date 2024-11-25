package epautec.atlas.appnativa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("appnativa"); // Nombre del archivo de biblioteca .so
    }

    private ImageView imageView;
    private Button startStream;

    private Button navigatebtn;
    private String cameraUrl = "http://192.168.0.105:81/stream"; // Dirección de tu cámara IP
    private static final String TAG = "MJPEGStreamTask";
    public native Bitmap procesarFrame(Bitmap bitmap);
    private Bitmap currentFrame; // Para almacenar el frame actual
    private ExecutorService executorService; // Para ejecutar tareas en segundo plano

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        startStream = findViewById(R.id.btnStream);
        navigatebtn = findViewById(R.id.navigate);

        executorService = Executors.newSingleThreadExecutor(); // Inicializa el Executor

        startStream.setOnClickListener(v -> {
            // Inicia la tarea de captura de flujo MJPEG
            Toast.makeText(this, "Iniciando flujo", Toast.LENGTH_SHORT).show();
            startMJPEGStream();
        });

        Button applyFilterButton = findViewById(R.id.applyFilterButton);

        applyFilterButton.setOnClickListener(v -> {
            if (currentFrame != null) {
                // Convierte el Bitmap actual en byte[]
                Bitmap processedFrame = procesarFrame(currentFrame); // Llamar al filtro nativo

                if (processedFrame != null) {
                    // Muestra el frame procesado
                    imageView.setImageBitmap(processedFrame);
                } else {
                    Log.e(TAG, "Error al procesar el frame");
                }
            }
        });

        navigatebtn.setOnClickListener(v -> {
            // Redirigir a SecondActivity
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
        });
    }

    private void startMJPEGStream() {
        executorService.execute(() -> {
            try {
                // Conexión HTTP para obtener el flujo MJPEG de la cámara
                HttpURLConnection connection = (HttpURLConnection) new URL(cameraUrl).openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                InputStream inputStream = connection.getInputStream();

                int bytesRead;
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);

                    // Aquí es donde puedes decodificar el flujo MJPEG para obtener los frames
                    byte[] frameData = byteArrayOutputStream.toByteArray();

                    // Decodificar el frame como un Bitmap
                    Bitmap frame = decodeMJPEGFrame(frameData);

                    if (frame != null) {
                        currentFrame = frame;
                        runOnUiThread(() -> imageView.setImageBitmap(frame)); // Actualiza la UI en el hilo principal
                    }

                    byteArrayOutputStream.reset(); // Resetea el buffer para el siguiente frame
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al procesar el flujo MJPEG", e);
            }
        });
    }

    // Decodifica los bytes del flujo MJPEG en un Bitmap
    private Bitmap decodeMJPEGFrame(byte[] frameData) {
        // Utiliza alguna librería para decodificar el flujo MJPEG a un Bitmap
        // Aquí un ejemplo de decodificación (puedes usar una librería como Android's BitmapFactory)
        return BitmapFactory.decodeByteArray(frameData, 0, frameData.length);
    }
}



/**
 * A native method that is implemented by the 'appnativa' native library,
 * which is packaged with this application.
 * // Configuración del botón "Ir a SecondActivity"
 *         navigateButton.setOnClickListener(v -> {
 *             // Redirigir a SecondActivity
 *             Intent intent = new Intent(MainActivity.this, SecondActivity.class);
 *             startActivity(intent);
 *         });
 */
