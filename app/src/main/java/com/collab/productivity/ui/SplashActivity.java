package com.collab.productivity.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import com.collab.productivity.utils.Logger;
import com.collab.productivity.utils.FirebaseManager;
import com.collab.productivity.utils.CloudinaryManager;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate - Start");

        // Initialize Firebase and Cloudinary
        initializeServices();

        // Keep the splash screen visible for some time
        splashScreen.setKeepOnScreenCondition(() -> true);

        // Add fade out animation when splash screen is dismissed
        splashScreen.setOnExitAnimationListener(splashScreenView -> {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.getView(),
                View.ALPHA,
                1f,
                0f
            );
            fadeOut.setInterpolator(new AnticipateInterpolator());
            fadeOut.setDuration(300L);

            fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    splashScreenView.remove();
                    // Check authentication and navigate
                    navigateToNextScreen();
                }
            });

            fadeOut.start();
        });
    }

    private void initializeServices() {
        // Initialize Cloudinary
        CloudinaryManager.getInstance().init(this);
        Logger.d(TAG, "Services initialized");
    }

    private void navigateToNextScreen() {
        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        Intent intent;
        if (firebaseManager.isUserLoggedIn()) {
            Logger.d(TAG, "User logged in, navigating to MainActivity");
            intent = new Intent(this, MainActivity.class);
        } else {
            Logger.d(TAG, "User not logged in, navigating to LoginActivity");
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
