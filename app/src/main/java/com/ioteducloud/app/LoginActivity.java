package com.ioteducloud.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton;
    private TextView registerText;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.editEmail);
        passwordField = findViewById(R.id.editPassword);
        loginButton = findViewById(R.id.btnLogin);
        registerText = findViewById(R.id.tvRegister);
        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verificando credenciales...");
        progressDialog.setCancelable(false);

        loginButton.setOnClickListener(v -> attemptLogin());
        registerText.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) checkUserRole(user.getUid());
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void checkUserRole(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (!snapshot.exists()) {
                    Toast.makeText(LoginActivity.this, "Usuario sin rol definido", Toast.LENGTH_LONG).show();
                    return;
                }

                String role = snapshot.child("role").getValue(String.class);
                String name = snapshot.child("name").getValue(String.class);

                if (role == null) role = "viewer";

                Toast.makeText(LoginActivity.this,
                        "Bienvenido, " + name + " (" + role + ")", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, DeviceControlActivity.class);
                intent.putExtra("role", role);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Error al verificar rol", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
