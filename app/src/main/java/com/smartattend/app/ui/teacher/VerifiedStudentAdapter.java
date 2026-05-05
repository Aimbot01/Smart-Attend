package com.smartattend.app.ui.teacher;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.smartattend.app.R;
import com.smartattend.app.databinding.ItemVerifiedStudentBinding;
import com.smartattend.app.model.AttendanceRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VerifiedStudentAdapter extends RecyclerView.Adapter<VerifiedStudentAdapter.ViewHolder> {

    private final List<AttendanceRecord> students;

    public VerifiedStudentAdapter(List<AttendanceRecord> students) {
        this.students = students;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemVerifiedStudentBinding binding;
        public ViewHolder(ItemVerifiedStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVerifiedStudentBinding binding = ItemVerifiedStudentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord student = students.get(position);
        holder.binding.tvStudentName.setText(student.getStudentName());
        holder.binding.tvStudentId.setText(student.getStudentId());
        holder.binding.tvFaceScore.setText(String.format(Locale.getDefault(), "Face: %.0f%%", student.getFaceMatchScore() * 100));

        if (student.getSelfieUrl() != null && !student.getSelfieUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(student.getSelfieUrl())
                    .placeholder(R.drawable.ic_face_placeholder)
                    .into(holder.binding.ivStudent);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        holder.binding.tvTime.setText(sdf.format(new Date(student.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return students != null ? students.size() : 0;
    }
}
