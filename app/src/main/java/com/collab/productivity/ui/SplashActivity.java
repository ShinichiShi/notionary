package com.collab.productivity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.collab.productivity.R;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 1000; // 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Use Handler to delay the start of MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Start MainActivity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);

            // Close splash activity
            finish();
        }, SPLASH_DELAY);
    }
}
