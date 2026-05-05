package com.smartattend.app.ui.student;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartattend.app.R;
import com.smartattend.app.model.ScheduleItem;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private final List<ScheduleItem> items;

    public ScheduleAdapter(List<ScheduleItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleItem item = items.get(position);
        
        String[] timeParts = item.getTime().split(" ");
        holder.tvTime.setText(timeParts[0]);
        holder.tvAmPm.setText(timeParts[1]);
        
        holder.tvSubject.setText(item.getSubject());
        holder.tvDetail.setText(item.getRoom() + (item.getType().isEmpty() ? "" : " • " + item.getType()));
        
        // Color coding based on subject
        int color = Color.parseColor("#4F46E5"); // Default Indigo
        if (item.getSubject().contains("LAB")) color = Color.parseColor("#059669"); // Green for Labs
        else if (item.getSubject().contains("Minor")) color = Color.parseColor("#D97706"); // Orange
        else if (item.getSubject().contains("Lunch")) color = Color.parseColor("#9CA3AF"); // Grey
        
        holder.indicator.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvAmPm, tvSubject, tvDetail;
        View indicator;

        ViewHolder(View v) {
            super(v);
            tvTime = v.findViewById(R.id.tvTime);
            tvAmPm = v.findViewById(R.id.tvAmPm);
            tvSubject = v.findViewById(R.id.tvSubject);
            tvDetail = v.findViewById(R.id.tvDetail);
            indicator = v.findViewById(R.id.indicator);
        }
    }
}
