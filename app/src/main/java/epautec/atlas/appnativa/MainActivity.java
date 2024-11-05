package epautec.atlas.appnativa;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import epautec.atlas.appnativa.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'appnativa' library on application startup.
    static {
        System.loadLibrary("appnativa");
    }

    private ActivityMainBinding binding;
    private android.widget.Button boton;
    private android.widget.ImageView original, bordes;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText("Hello from C++"); // Dejamos fijo el texto en inglés para luego cambiar con el botón

        original = findViewById(R.id.imageView);
        bordes = findViewById(R.id.imageView2);

        boton = findViewById(R.id.button);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv.setText(stringFromJNI());
                android.graphics.Bitmap bIn = BitmapFactory.decodeResource(getResources(),R.drawable.post);
                android.graphics.Bitmap bOut = bIn.copy(bIn.getConfig(), true);
                detectorBordes(bIn, bOut);
                bordes.setImageBitmap(bOut);
            }
        });

    }

    /**
     * A native method that is implemented by the 'appnativa' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native void detectorBordes(android.graphics.Bitmap in, android.graphics.Bitmap out);
}