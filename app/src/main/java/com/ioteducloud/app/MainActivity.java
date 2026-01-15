package com.ioteducloud.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Map;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTemperature, tvHumidity, tvPressure, tvDeviceState;
    private Button btnToggle, btnLogout;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    private String deviceId = "esp32_01";
    private String currentState = "OFF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvPressure = findViewById(R.id.tvPressure);
        tvDeviceState = findViewById(R.id.tvDeviceState);
        btnToggle = findViewById(R.id.btnToggle);
        btnLogout = findViewById(R.id.btnLogout);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        String userEmail = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getEmail() : "";
        tvWelcome.setText("Bienvenido: " + userEmail);

        // Escuchar lecturas del sensor
        dbRef.child("devices").child(deviceId).child("readings")
                .limitToLast(1) // última lectura
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot reading : snapshot.getChildren()) {
                            Map<String, Object> data = (Map<String, Object>) reading.getValue();
                            if (data != null) {
                                double temp = Double.parseDouble(data.get("temperature").toString());
                                double hum = Double.parseDouble(data.get("humidity").toString());
                                double pres = Double.parseDouble(data.get("pressure").toString());

                                DecimalFormat df = new DecimalFormat("#.##");
                                tvTemperature.setText("Temperatura: " + df.format(temp) + " °C");
                                tvHumidity.setText("Humedad: " + df.format(hum) + " %");
                                tvPressure.setText("Presión: " + df.format(pres) + " hPa");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Error al leer datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Escuchar estado ON/OFF
        dbRef.child("devices").child(deviceId).child("control").child("state")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            currentState = snapshot.getValue(String.class);
                            tvDeviceState.setText("Estado del dispositivo: " + currentState);
                            btnToggle.setText(currentState.equals("ON") ? "Apagar" : "Encender");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

        // Cambiar estado ON/OFF
        btnToggle.setOnClickListener(v -> {
            String newState = currentState.equals("ON") ? "OFF" : "ON";
            dbRef.child("devices").child(deviceId).child("control").child("state")
                    .setValue(newState)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Dispositivo " + newState, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Cerrar sesión
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
