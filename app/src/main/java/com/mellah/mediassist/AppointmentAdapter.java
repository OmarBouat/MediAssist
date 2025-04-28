package com.mellah.mediassist;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    private final Context context;
    private Cursor cursor;

    public AppointmentAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDateTime;
        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tvApptTitle);
            tvDateTime = itemView.findViewById(R.id.tvApptDateTime);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) return;
        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_TITLE));
        String date  = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_DATE));
        String time  = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_TIME));
        holder.tvTitle.setText(title);
        holder.tvDateTime.setText(date + " " + time);
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }
}