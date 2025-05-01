package com.mellah.mediassist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DailyScheduleAdapter
        extends RecyclerView.Adapter<DailyScheduleAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(ScheduleItem item);
    }

    private final Context context;
    private final List<ScheduleItem> items;
    private final OnItemClickListener listener;

    public DailyScheduleAdapter(Context ctx,
                                List<ScheduleItem> items,
                                OnItemClickListener listener) {
        this.context  = ctx;
        this.items    = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        ScheduleItem si = items.get(pos);
        holder.tvLabel.setText(si.label);
        holder.tvTime .setText(si.time);

        // Color-code by type
        int colorRes = (si.type == ScheduleItem.Type.MEDICATION)
                ? R.color.schedule_medication_color
                : R.color.schedule_appointment_color;
        int color = ContextCompat.getColor(context, colorRes);
        holder.tvLabel.setTextColor(color);
        holder.tvTime .setTextColor(color);

        holder.itemView.setOnClickListener(v -> listener.onClick(si));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel, tvTime;
        ViewHolder(View v) {
            super(v);
            tvLabel = v.findViewById(R.id.tvSchedLabel);
            tvTime  = v.findViewById(R.id.tvSchedDateTime);
        }
    }
}
