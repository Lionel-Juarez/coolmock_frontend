package com.example.hamacav1.initialmenus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hamacav1.MainActivity;
import com.example.hamacav1.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private FirebaseAuth auth;
    TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        Button loginButton = findViewById(R.id.login_button);
        TextView signupRedirectText = findViewById(R.id.signUpRedirectText);
        forgotPassword = findViewById(R.id.forgot_password);
        auth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString();
            String pass = loginPassword.getText().toString();
            if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!pass.isEmpty()) {
                    auth.signInWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(authResult -> {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    user.getIdToken(true).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            String idToken = task.getResult().getToken();
                                            // Guardar el token en SharedPreferences
                                            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("idToken", idToken);
                                            editor.putString("userId", user.getUid()); // Guardar el userId también
                                            editor.apply();

                                            Log.d("Token", "Firebase ID Token: " + idToken);

                                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            finish(); // Finalizar actividad para evitar regresar
                                        } else {
                                            Log.e("TokenError", "Failed to get token", task.getException());
                                            Toast.makeText(LoginActivity.this, "Failed to get token: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(e -> {
                                Log.e("LoginError", "Login Failed", e);
                                Toast.makeText(LoginActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    loginPassword.setError("Empty fields are not allowed");
                }
            } else if (email.isEmpty()) {
                loginEmail.setError("Empty fields are not allowed");
            } else {
                loginEmail.setError("Please enter correct email");
            }
        });

        signupRedirectText.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
    }
}
