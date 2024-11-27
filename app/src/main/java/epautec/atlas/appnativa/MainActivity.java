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
    private String cameraUrl = "http://192.168.0.104:81/stream"; // URL del flujo MJPEG
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
                    URL url = new URL(cameraUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(1000 * 5);
                    connection.setReadTimeout(1000 * 5);
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

                        InputStream in = connection.getInputStream();
                        InputStreamReader isr = new InputStreamReader(in);
                        BufferedReader br = new BufferedReader(isr);

                        String data;
                        int len;
                        byte[] buffer;

                        while ((data = br.readLine()) != null) {
                            if (data.contains("Content-Type:")) {

                                data = br.readLine();
                                len = Integer.parseInt(data.split(":")[1].trim());
                                bis = new BufferedInputStream(in);
                                buffer = new byte[len];

                                int t = 0;
                                while (t < len) {
                                    t += bis.read(buffer, t, len - t);
                                }

                                //System.out.println("-->" + Arrays.toString(buffer));

                                Bytes2ImageFile(buffer, getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");
                                final Bitmap bitmap = BitmapFactory.decodeFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/0A.jpg");
                                //byte[] byteArray = bitmapToByteArray(bitmap);

                                //processImageInCpp(byteArray);
                                android.graphics.Bitmap bOut = bitmap.copy(bitmap.getConfig(), true);
                                //detectorBordes(bitmap, bOut);
                                Log.e("MainActivity", "Filtro: Ingreso ...");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //connectButton.setText("OFF");
                                        camara.setImageBitmap(bOut);
                                    }
                                });
                            }
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
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
