package epautec.atlas.appnativa;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.hardware.camera2.*;

import java.util.Collections;
public class SecondActivity extends AppCompatActivity {

    static {
        System.loadLibrary("appnativa");  // Asegúrate de que el nombre coincida con el de tu biblioteca nativa
    }

    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CameraManager cameraManager;
    private String cameraId;

    private TextureView textureView;
    private ImageView imageView;
    private Button startCameraButton;
    private Button applyFilterButton;
    private Button showfilter;

    public native Bitmap filtroRainbow(Bitmap bitmap);
    public native void filters(Bitmap bitmapIn, Bitmap bitmapOut, int hMin, int sMin, int vMin, int hMax, int sMax, int vMax);
    private void applyFilter() {
        if (bitmapI != null && bitmapO != null) {
            filters(bitmapI, bitmapO,
                    seekBarHMin.getProgress(), seekBarSMin.getProgress(), seekBarVMin.getProgress(),
                    seekBarHMax.getProgress(), seekBarSMax.getProgress(), seekBarVMax.getProgress());
            imageView.setImageBitmap(bitmapO);
        } else {
            Toast.makeText(this, "Bitmap no disponible.", Toast.LENGTH_SHORT).show();
        }
    }
    private Bitmap bitmapI;
    private Bitmap bitmapO;
    private boolean isCameraActive = false;
    private boolean isFilterActive = false; // Indica si el filtro está activo
    private boolean isSecondFilter = false;
    private android.widget.SeekBar seekBarHMin;
    private android.widget.SeekBar seekBarSMin;
    private android.widget.SeekBar seekBarVMin;

    private android.widget.SeekBar seekBarHMax;
    private android.widget.SeekBar seekBarSMax;
    private android.widget.SeekBar seekBarVMax;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        textureView = findViewById(R.id.textureView);
        imageView = findViewById(R.id.imageViewFiltered); // ImageView para mostrar la imagen filtrada
        startCameraButton = findViewById(R.id.startCameraButton);
        applyFilterButton = findViewById(R.id.buttonApplyFilter);
        showfilter = findViewById(R.id.verfiltro);

        //bitmapI = textureView.getBitmap();
        //bitmapO = bitmapI.copy(bitmapI.getConfig(),true);

        seekBarHMin = findViewById(R.id.sbHMin);
        seekBarSMin = findViewById(R.id.sbSMin);
        seekBarVMin = findViewById(R.id.sbVMin);

        seekBarHMax = findViewById(R.id.sbHMax);
        seekBarSMax = findViewById(R.id.sbSMax);
        seekBarVMax = findViewById(R.id.sbVMax);

        seekBarHMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                applyFilter();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarSMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                applyFilter();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarVMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                applyFilter();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarHMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                applyFilter();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarSMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                applyFilter();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarVMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                applyFilter();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        applyFilter();

        //--------------

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        startCameraButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(SecondActivity.this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                ActivityCompat.requestPermissions(SecondActivity.this,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        });

        applyFilterButton.setOnClickListener(v -> {
            isFilterActive = !isFilterActive; // Alternar el estado del filtro
            if (isFilterActive){
                Toast.makeText(this, "Filtro 1 Activado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Filtro 1 Desactivado", Toast.LENGTH_SHORT).show();
                runOnUiThread(() -> {
                    textureView.setVisibility(View.VISIBLE); // Mostrar vista previa de la cámara
                    imageView.setVisibility(View.INVISIBLE); // Ocultar imagen filtrada
                });
            }
        });

        showfilter.setOnClickListener(v -> {
            // Alternar el estado del filtro
            isSecondFilter = !isSecondFilter;
            if (isFilterActive == false && isSecondFilter==true) {
                Toast.makeText(this, "Filtro 2 Activado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Filtro 2 Desactivado", Toast.LENGTH_SHORT).show();
                runOnUiThread(() -> {
                    textureView.setVisibility(View.VISIBLE); // Mostrar vista previa de la cámara
                    imageView.setVisibility(View.INVISIBLE); // Ocultar imagen filtrada
                });
            }
        });

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                // La cámara se abrirá solo cuando se presione el botón
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                // Opcionalmente puedes ajustar la cámara aquí
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                closeCamera();
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
                Bitmap bitmap = textureView.getBitmap();

                if (bitmap == null) {
                    runOnUiThread(() -> imageView.setVisibility(View.INVISIBLE));
                    return;
                }

                if (isFilterActive && !isSecondFilter) {
                    // Aplica el primer filtro
                    Bitmap filteredBitmap = filtroRainbow(bitmap);

                    runOnUiThread(() -> {
                        imageView.setImageBitmap(filteredBitmap); // Muestra la imagen filtrada
                        imageView.setVisibility(View.VISIBLE);    // Asegúrate de que sea visible
                        //textureView.setVisibility(View.INVISIBLE); // Oculta la vista previa de la cámara
                    });

                } else if (!isFilterActive && isSecondFilter) {
                    // Aplica el segundo filtro
                    if (bitmapI == null || bitmapO == null) {
                        bitmapI = bitmap.copy(bitmap.getConfig(), true);
                        bitmapO = bitmap.copy(bitmap.getConfig(), true);
                    }

                    filters(bitmapI, bitmapO,
                            seekBarHMin.getProgress(), seekBarSMin.getProgress(), seekBarVMin.getProgress(),
                            seekBarHMax.getProgress(), seekBarSMax.getProgress(), seekBarVMax.getProgress());

                    runOnUiThread(() -> {
                        imageView.setImageBitmap(bitmapO);         // Muestra la imagen filtrada
                        imageView.setVisibility(View.VISIBLE);     // Asegúrate de que sea visible
                        //textureView.setVisibility(View.INVISIBLE); // Oculta la vista previa de la cámara
                    });

                } else {
                    // Ningún filtro activo
                    runOnUiThread(() -> {
                        imageView.setVisibility(View.INVISIBLE); // Oculta la imagen filtrada
                        textureView.setVisibility(View.VISIBLE); // Muestra la vista previa de la cámara
                    });
                }
            }


        });
    }
/*
    private void applyFilter() {
        Bitmap bitmap = textureView.getBitmap(); // Obtener cuadro actual
        Bitmap filteredBitmap = filtroRainbow(bitmap); // Aplicar filtro nativo

        // Mostrar el Bitmap filtrado en el ImageView
        runOnUiThread(() -> {
            textureView.setVisibility(View.INVISIBLE); // Ocultar vista previa original
            imageView.setImageBitmap(filteredBitmap); // Mostrar resultado filtrado
            imageView.setVisibility(View.VISIBLE);
        });
    }*/

    private void startCamera() {
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            if (cameraIds.length == 0) {
                Toast.makeText(this, "No se encontraron cámaras", Toast.LENGTH_SHORT).show();
                return;
            }

            cameraId = null;
            for (String id : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id;
                    isCameraActive = true;
                    break;
                }
            }

            if (cameraId == null) {
                Toast.makeText(this, "No se encontró la cámara trasera", Toast.LENGTH_SHORT).show();
                return;
            }

            openCamera();
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al acceder a la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createCameraPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    cameraDevice.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    cameraDevice.close();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture == null) {
                return;
            }

            texture.setDefaultBufferSize(1920, 1080); // Ajusta según sea necesario
            Surface surface = new Surface(texture);

            CaptureRequest.Builder captureRequestBuilder;
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) return;
                            cameraCaptureSession = session;
                            try {
                                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(SecondActivity.this, "Configuración fallida", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
        }
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }
}
