package com.ioteducloud.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DeviceControlActivity extends AppCompatActivity {

    private TextView tvDeviceName, tvStatus;
    private Switch switchDevice;
    private Button btnReadings, btnLogout;

    private DatabaseReference deviceRef;
    private FirebaseAuth mAuth;

    private boolean isSwitchChangingProgrammatically = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        //DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference("devices").child("esp32_01");

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Referencias UI
        tvDeviceName = findViewById(R.id.tvDeviceName);
        tvStatus = findViewById(R.id.tvStatus);
        switchDevice = findViewById(R.id.switchDevice);
        btnReadings = findViewById(R.id.btnReadings);
        btnLogout = findViewById(R.id.btnLogout);

        // Referencia a Firebase Database del ESP32
        deviceRef = FirebaseDatabase.getInstance().getReference("devices/esp32_01");

        // Obtener nombre del dispositivo
        deviceRef.child("info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                if (name != null) {
                    tvDeviceName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeviceControlActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Escuchar estado del dispositivo
        deviceRef.child("control/state").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String state = snapshot.getValue(String.class);
                if (state != null) {
                    tvStatus.setText("Estado: " + state);
                    isSwitchChangingProgrammatically = true;
                    switchDevice.setChecked(state.equals("ON"));
                    isSwitchChangingProgrammatically = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeviceControlActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Cambiar estado al tocar switch
        switchDevice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isSwitchChangingProgrammatically) return;
                String newState = isChecked ? "ON" : "OFF";
                deviceRef.child("control/state").setValue(newState)
                        .addOnSuccessListener(aVoid -> Toast.makeText(DeviceControlActivity.this, "Dispositivo " + newState, Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(DeviceControlActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        // Botón ver lecturas
        btnReadings.setOnClickListener(v -> {
            Intent intent = new Intent(DeviceControlActivity.this, ReadingsActivity.class);
            startActivity(intent);
        });

        // Botón cerrar sesión
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(DeviceControlActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
