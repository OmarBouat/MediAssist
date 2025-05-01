package com.mellah.mediassist;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    public interface OnAppointmentActionListener {
        void onEdit(int apptId, String title, String date, String time, int offset, String notes);
        void onDelete(int apptId);
    }

    private final Context context;
    private final Cursor cursor;
    private final OnAppointmentActionListener listener;

    public AppointmentAdapter(Context context, Cursor cursor, OnAppointmentActionListener listener) {
        this.context = context;
        this.cursor = cursor;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        if (!cursor.moveToPosition(pos)) return;

        int id     = cursor.getInt(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_ID));
        String title  = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_TITLE));
        String date   = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_DATE));
        String time   = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_TIME));
        int offset    = cursor.getInt(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_OFFSET));
        String notes  = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_NOTES));

        holder.tvTitle.setText(title);
        holder.tvDateTime.setText(date + " " + time);

        holder.btnEdit.setOnClickListener(v ->
                listener.onEdit(id, title, date, time, offset, notes)
        );
        holder.btnDelete.setOnClickListener(v ->
                listener.onDelete(id)
        );
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDateTime;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tvApptTitle);
            tvDateTime = itemView.findViewById(R.id.tvApptDateTime);
            btnEdit    = itemView.findViewById(R.id.btnEditAppt);
            btnDelete  = itemView.findViewById(R.id.btnDeleteAppt);
        }
    }
}
