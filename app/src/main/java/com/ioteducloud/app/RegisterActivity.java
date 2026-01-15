package com.ioteducloud.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword, etRole;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas con los IDs CORRECTOS del XML
        etName = findViewById(R.id.editName);
        etEmail = findViewById(R.id.editEmail);
        etPassword = findViewById(R.id.editPassword);
        etConfirmPassword = findViewById(R.id.editConfirmPassword);
        etRole = findViewById(R.id.editRole);  // Cambiado de Spinner a EditText
        btnRegister = findViewById(R.id.btnRegister);

        // El TextView tvGoToLogin no existe en tu XML, así que lo removemos
        // tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Botón registrar
        btnRegister.setOnClickListener(v -> registerUser());

        // Agregar funcionalidad para ir al login (sin el TextView)
        // Puedes agregar un TextView en tu XML o usar otra forma
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();
        String role = etRole.getText().toString().trim().toLowerCase(); // Convertir a minúsculas

        // Validaciones
        if (TextUtils.isEmpty(name)) {
            etName.setError("Ingresa tu nombre");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Ingresa tu correo");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(role)) {
            etRole.setError("Ingresa un rol (admin/operator/viewer)");
            etRole.requestFocus();
            return;
        }

        // Validar que el rol sea uno de los permitidos
        if (!role.equals("admin") && !role.equals("operator") && !role.equals("viewer")) {
            etRole.setError("Rol debe ser: admin, operator o viewer");
            etRole.requestFocus();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Registrando...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Registrar");

                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("email", email);
                            userData.put("role", role);
                            userData.put("createdAt", System.currentTimeMillis());

                            mDatabase.child("users").child(uid).setValue(userData)
                                    .addOnCompleteListener(saveTask -> {
                                        if (saveTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Usuario registrado correctamente",
                                                    Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this,
                                                    "Error al guardar datos: " + saveTask.getException().getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Error al registrar: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Método para volver al login (puedes llamarlo desde un botón o gesto)
    public void goToLogin(View view) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}