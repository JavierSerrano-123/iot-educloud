package com.ioteducloud.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReadingsAdapter extends RecyclerView.Adapter<ReadingsAdapter.ViewHolder> {

    private List<Reading> readingList;

    public ReadingsAdapter(List<Reading> readingList) {
        this.readingList = readingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reading, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reading r = readingList.get(position);
        holder.tvTimestamp.setText(r.getTimestamp());
        holder.tvTemp.setText("Temp: " + r.getTemperature() + " °C");
        holder.tvHum.setText("Hum: " + r.getHumidity() + " %");
        holder.tvPres.setText("Pres: " + r.getPressure() + " hPa");
    }

    @Override
    public int getItemCount() {
        return readingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp, tvTemp, tvHum, tvPres;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvHum = itemView.findViewById(R.id.tvHum);
            tvPres = itemView.findViewById(R.id.tvPres);
        }
    }
}
