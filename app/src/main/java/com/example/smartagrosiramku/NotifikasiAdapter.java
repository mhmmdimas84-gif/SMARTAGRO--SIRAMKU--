package com.example.smartagrosiramku;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotifikasiAdapter extends RecyclerView.Adapter<NotifikasiAdapter.ViewHolder> {

    private List<NotifikasiLog> notifList;

    public NotifikasiAdapter(List<NotifikasiLog> notifList) {
        this.notifList = notifList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notifikasi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotifikasiLog notif = notifList.get(position);

        holder.tvTitle.setText(notif.title);
        holder.tvMessage.setText(notif.message);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(notif.timestamp)));

        if (notif.isRead) {
            holder.badgeStatus.setVisibility(View.GONE);
            holder.btnTandaiDibaca.setVisibility(View.GONE);
            holder.cardNotifikasi.setBackgroundResource(R.drawable.bg_white_rounded_top); // Or just plain white
        } else {
            holder.badgeStatus.setVisibility(View.VISIBLE);
            holder.btnTandaiDibaca.setVisibility(View.VISIBLE);
            
            // Set style based on type
            if ("error".equals(notif.type)) {
                holder.cardNotifikasi.setBackgroundResource(R.drawable.bg_pump_card_error);
                holder.tvTitle.setTextColor(Color.parseColor("#B71C1C"));
                holder.imgIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                // We'd use setTint if we were coding in Kotlin or API 21+, but here we just rely on the XML default tint
            } else if ("warning".equals(notif.type)) {
                holder.cardNotifikasi.setBackgroundResource(R.drawable.bg_pump_card_amber);
                holder.tvTitle.setTextColor(Color.parseColor("#E65100"));
                holder.imgIcon.setImageResource(android.R.drawable.ic_dialog_info);
            } else {
                holder.cardNotifikasi.setBackgroundResource(R.drawable.bg_pump_card_green);
                holder.tvTitle.setTextColor(Color.parseColor("#1B5E20"));
                holder.imgIcon.setImageResource(R.drawable.ic_water_drop);
            }
        }

        holder.btnTandaiDibaca.setOnClickListener(v -> {
            if (notif.id != null) {
                FirebaseDatabase.getInstance().getReference("Sensors/notifications").child(notif.id).child("isRead").setValue(true);
            }
        });

        holder.btnHapus.setOnClickListener(v -> {
            if (notif.id != null) {
                FirebaseDatabase.getInstance().getReference("Sensors/notifications").child(notif.id).removeValue();
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardNotifikasi;
        ImageView imgIcon;
        TextView tvTitle, tvTime, badgeStatus, tvMessage, btnTandaiDibaca, btnHapus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNotifikasi = itemView.findViewById(R.id.cardNotifikasi);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            badgeStatus = itemView.findViewById(R.id.badgeStatus);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            btnTandaiDibaca = itemView.findViewById(R.id.btnTandaiDibaca);
            btnHapus = itemView.findViewById(R.id.btnHapus);
        }
    }
}
