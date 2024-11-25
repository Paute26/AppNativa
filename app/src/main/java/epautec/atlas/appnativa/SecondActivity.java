package epautec.atlas.appnativa;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

    public native Bitmap ADDFiltro(Bitmap bitmap);

    private boolean isCameraActive = false;
    private boolean isFilterActive = false; // Indica si el filtro está activo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        textureView = findViewById(R.id.textureView);
        imageView = findViewById(R.id.imageViewFiltered); // ImageView para mostrar la imagen filtrada
        startCameraButton = findViewById(R.id.startCameraButton);
        applyFilterButton = findViewById(R.id.buttonApplyFilter);

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
            if (isFilterActive) {
                Toast.makeText(this, "Filtro Activado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Filtro Desactivado", Toast.LENGTH_SHORT).show();
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
                if (isFilterActive) {
                    // Captura el fotograma actual como un Bitmap
                    Bitmap bitmap = textureView.getBitmap();

                    if (bitmap != null) {
                        // Aplica el filtro nativo
                        Bitmap filteredBitmap = ADDFiltro(bitmap);

                        // Muestra el resultado en el ImageView
                        runOnUiThread(() -> {
                            imageView.setImageBitmap(filteredBitmap); // Actualiza la imagen filtrada
                            imageView.setVisibility(View.VISIBLE);   // Asegúrate de que sea visible
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        imageView.setVisibility(View.INVISIBLE); // Oculta la imagen filtrada
                    });
                }
            }

        });
    }

    private void applyFilter() {
        Bitmap bitmap = textureView.getBitmap(); // Obtener cuadro actual
        Bitmap filteredBitmap = ADDFiltro(bitmap); // Aplicar filtro nativo

        // Mostrar el Bitmap filtrado en el ImageView
        runOnUiThread(() -> {
            textureView.setVisibility(View.INVISIBLE); // Ocultar vista previa original
            imageView.setImageBitmap(filteredBitmap); // Mostrar resultado filtrado
            imageView.setVisibility(View.VISIBLE);
        });
    }

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
