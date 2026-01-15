package com.ioteducloud.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReadingsActivity extends AppCompatActivity {

    private TextView tvTemperature, tvHumidity, tvPressure;
    private RecyclerView recyclerReadings;
    private DatabaseReference readingsRef;
    private List<Reading> readingList;
    private ReadingsAdapter readingsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readings);

        // Referencias UI
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvPressure = findViewById(R.id.tvPressure);
        recyclerReadings = findViewById(R.id.recyclerReadings);

        recyclerReadings.setLayoutManager(new LinearLayoutManager(this));
        readingList = new ArrayList<>();
        readingsAdapter = new ReadingsAdapter(readingList);
        recyclerReadings.setAdapter(readingsAdapter);

        // Conexión a Firebase
        readingsRef = FirebaseDatabase.getInstance().getReference("devices/esp32_01/readings");

        // Escucha cambios en tiempo real
        readingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    readingList.clear();
                    Map<String, Reading> sortedMap = new TreeMap<>();

                    for (DataSnapshot child : snapshot.getChildren()) {
                        String timestamp = child.getKey();

                        double temp = parseDoubleSafe(child.child("temperature").getValue());
                        double hum = parseDoubleSafe(child.child("humidity").getValue());
                        double pres = parseDoubleSafe(child.child("pressure").getValue());

                        Reading r = new Reading(timestamp, temp, hum, pres);
                        sortedMap.put(timestamp, r);
                    }

                    readingList.addAll(sortedMap.values());
                    Collections.reverse(readingList); // muestra la más reciente arriba
                    readingsAdapter.notifyDataSetChanged();

                    // Mostrar la lectura más reciente
                    if (!readingList.isEmpty()) {
                        Reading last = readingList.get(0);
                        tvTemperature.setText("Temperatura: " + last.getTemperature() + " °C");
                        tvHumidity.setText("Humedad: " + last.getHumidity() + " %");
                        tvPressure.setText("Presión: " + last.getPressure() + " hPa");
                    }
                } else {
                    Toast.makeText(ReadingsActivity.this, "No hay lecturas registradas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReadingsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Convierte cualquier tipo de valor (String, Long, Double, etc.) a Double de forma segura.
     */
    private double parseDoubleSafe(Object value) {
        if (value == null) return 0.0;
        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                return Double.parseDouble((String) value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
