package epautec.atlas.appnativa;

import android.annotation.SuppressLint;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("appnativa"); // Cargar la biblioteca nativa
    }
    //private native Bitmap getFrameFromCamera(String url);
    private ImageView camara;
    private Button siguienteBtn;
    private Button capturarBtn;
    private String cameraUrl = "http://192.168.0.102:8080/stream"; // URL del flujo MJPEG
    public native Bitmap procesarFrame(Bitmap bitmap);
    private HttpURLConnection connection;
    private boolean connected = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camara = findViewById(R.id.pantalla);
        siguienteBtn = findViewById(R.id.navigate);
        capturarBtn = findViewById(R.id.btnStream);

        //Capturar imagen desde Boton
        capturarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connected) {
                    disconnect();
                    Log.e("MainActivity", "Finalizado: cámara");
                } else {
                    connect();
                    //Toast.makeText(this,"Activado", Toast.LENGTH_SHORT).show();
                }
            }
        });
        siguienteBtn.setOnClickListener(v -> {
             // Redirigir a SecondActivity
              Intent intent = new Intent(MainActivity.this, SecondActivity.class);
              startActivity(intent);
          });

        //
    }

    private void connect() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedInputStream bis = null;
                try {
                    // Establecer la conexión
                    URL url = new URL(cameraUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000 * 5);
                    connection.setReadTimeout(5000 * 5);
                    connection.setDoInput(true);
                    connection.connect();

                    if (connection.getResponseCode() == 200) {
                        connected = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                capturarBtn.setText("Desconectar");
                            }
                        });

                        // Leer los datos de la respuesta
                        InputStream in = connection.getInputStream();
                        InputStreamReader isr = new InputStreamReader(in);
                        BufferedReader br = new BufferedReader(isr);

                        String data;
                        int len;
                        byte[] buffer;

                        while ((data = br.readLine()) != null) {
                            if (data.contains("Content-Type:")) {
                                // Leer el tamaño de la imagen
                                data = br.readLine();
                                len = Integer.parseInt(data.split(":")[1].trim());
                                bis = new BufferedInputStream(in);
                                buffer = new byte[len];

                                int t = 0;
                                while (t < len) {
                                    t += bis.read(buffer, t, len - t);
                                }

                                // Guardar la imagen como archivo
                                Bytes2ImageFile(buffer, getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");

                                // Decodificar la imagen desde el archivo
                                final Bitmap bitmap = BitmapFactory.decodeFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");

                                // Verificar si la imagen fue decodificada correctamente
                                if (bitmap != null) {
                                    android.graphics.Bitmap bOut = bitmap.copy(bitmap.getConfig(), true);
                                    Bitmap filtro = procesarFrame(bOut);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Mostrar la imagen en el ImageView
                                            camara.setImageBitmap(filtro);
                                        }
                                    });
                                } else {
                                    Log.e("MainActivity", "La imagen no se pudo decodificar.");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Error al obtener la imagen", LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                    } else {
                        Log.e("MainActivity", "Error de conexión: " + connection.getResponseCode());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Error de conexión", LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                    Log.e("MainActivity", "URL mal formada", e);
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e("MainActivity", "Error de E/S al leer la imagen", e);
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Error de E/S", LENGTH_SHORT).show();
                        }
                    });
                } finally {
                    // Desconectar
                    disconnect();
                }
            }
        });
        thread.start();
    }


    private void disconnect() {
        if (connection != null) {
            connection.disconnect();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                capturarBtn.setText("Conectar");
            }
        });

        connected = false;
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void Bytes2ImageFile(byte[] bytes, String fileName) {
        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes, 0, bytes.length);
            fos.flush();
            fos.close();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //public native void getFrameFromCamera(android.graphics.Bitmap in, android.graphics.Bitmap out);

}
