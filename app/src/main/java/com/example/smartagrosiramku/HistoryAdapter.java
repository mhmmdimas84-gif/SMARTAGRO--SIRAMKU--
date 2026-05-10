package com.example.smartagrosiramku;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryLog> historyList;

    public HistoryAdapter(List<HistoryLog> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryLog log = historyList.get(position);
        holder.tvTDS.setText(String.format(Locale.getDefault(), "%.0f", log.tds));
        holder.tvAir.setText(String.valueOf(log.waterLevel));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd · HH:mm:ss", Locale.getDefault());
        String dateStr = sdf.format(new Date(log.timestamp));
        holder.tvWaktu.setText(dateStr);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWaktu, tvTDS, tvAir;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWaktu = itemView.findViewById(R.id.tvWaktuHistory);
            tvTDS = itemView.findViewById(R.id.tvTDSHistory);
            tvAir = itemView.findViewById(R.id.tvAirHistory);
        }
    }
}
