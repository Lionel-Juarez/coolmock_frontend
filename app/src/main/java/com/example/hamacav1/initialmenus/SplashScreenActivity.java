package com.example.hamacav1.initialmenus;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.hamacav1.MainActivity;
import com.example.hamacav1.R;


@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    public static int SPLASH_TIMER = 2000;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        FrameLayout logoContainer = findViewById(R.id.logo_container);
        ImageView splashLogo = findViewById(R.id.splash_logo);

        logoContainer.setBackground(getDrawable(R.drawable.circle));
        splashLogo.setBackground(getDrawable(R.drawable.circle));

        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logoContainer.startAnimation(logoAnimation);

        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            String idToken = sharedPreferences.getString("idToken", null);

            if (idToken != null) {
                Log.d("SplashScreen", "Token found, redirecting to MainActivity");
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Log.d("SplashScreen", "No token found, redirecting to LoginActivity");
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            finish();
        }, SPLASH_TIMER);
    }
}


