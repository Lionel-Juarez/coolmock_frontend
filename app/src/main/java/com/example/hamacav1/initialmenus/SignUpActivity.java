package com.example.hamacav1.initialmenus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hamacav1.R;

import com.example.hamacav1.util.Internetop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword, signupNombreCompleto, signupTelefono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupNombreCompleto = findViewById(R.id.signup_nombre_completo);
        signupTelefono = findViewById(R.id.signup_telefono);
        EditText signupPasswordConfirm = findViewById(R.id.signup_password_confirm);
        Button signupButton = findViewById(R.id.signup_button);
        TextView loginRedirectText = findViewById(R.id.loginRedirectText);

        signupButton.setOnClickListener(view -> {
            String email = signupEmail.getText().toString().trim();
            String pass = signupPassword.getText().toString().trim();
            String confirmPass = signupPasswordConfirm.getText().toString().trim();
            String nombreCompleto = signupNombreCompleto.getText().toString().trim();
            String telefono = signupTelefono.getText().toString().trim();

            if (email.isEmpty()) {
                signupEmail.setError("Email cannot be empty");
            } else if (pass.isEmpty()) {
                signupPassword.setError("Password cannot be empty");
            } else if (!pass.equals(confirmPass)) {
                signupPasswordConfirm.setError("Passwords do not match");
            } else if (nombreCompleto.isEmpty()) {
                signupNombreCompleto.setError("Nombre completo cannot be empty");
            } else if (telefono.isEmpty()) {
                signupTelefono.setError("Teléfono cannot be empty");
            } else {
                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.getIdToken(true).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    String idToken = task1.getResult().getToken();
                                    String uid = user.getUid();

                                    SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("idToken", idToken);
                                    editor.apply();

                                    createOrUpdateCliente(nombreCompleto, telefono, email, idToken, uid);

                                    Toast.makeText(SignUpActivity.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(SignUpActivity.this, "Failed to get token: " + Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "SignUp Failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        loginRedirectText.setOnClickListener(view -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));

        // Toggle password visibility
        togglePasswordVisibility(signupPassword);
        togglePasswordVisibility(signupPasswordConfirm);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void togglePasswordVisibility(EditText passwordField) {
        passwordField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = 2; // index of the drawable end
                Drawable drawable = passwordField.getCompoundDrawables()[drawableEnd];
                if (drawable != null && event.getRawX() >= (passwordField.getRight() - drawable.getBounds().width())) {
                    int selection = passwordField.getSelectionEnd();
                    if (passwordField.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_password_24_principal_color, 0, R.drawable.baseline_visibility_24, 0);
                    } else {
                        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_password_24_principal_color, 0, R.drawable.baseline_visibility_off_24, 0);
                    }
                    passwordField.setSelection(selection);
                    return true;
                }
            }
            return false;
        });
    }

    private void createOrUpdateCliente(String nombreCompleto, String telefono, String email, String idToken, String uid) {
        if (!Internetop.getInstance(this).isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject json = new JSONObject();
            try {
                json.put("nombreCompleto", nombreCompleto);
                json.put("numeroTelefono", telefono);
                json.put("email", email);
                json.put("rol", "CLIENTE");
                json.put("uid", uid);

                String url = getResources().getString(R.string.url_clientes) + "crearOActualizar";
                RequestBody body = RequestBody.create(json.toString(), MEDIA_TYPE_JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + idToken)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        Log.d("createOrUpdateCliente", "Cliente guardado con éxito");
                    } else {
                        String errorMessage = response.body() != null ? response.body().string() : "Respuesta vacía";
                        Log.e("createOrUpdateCliente", "Error al guardar cliente, respuesta del servidor: " + errorMessage);
                    }
                } catch (Exception e) {
                    Log.e("createOrUpdateCliente", "Excepción al enviar datos del cliente: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                Log.e("createOrUpdateCliente", "Excepción al crear JSON del cliente: " + e.getMessage(), e);
            }
        });
    }
}
