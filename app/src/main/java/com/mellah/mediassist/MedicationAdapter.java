package com.mellah.mediassist;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
    private final Context context;
    private Cursor cursor;

    public MedicationAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvTime;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName   = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvMedDosage);
            tvTime   = itemView.findViewById(R.id.tvMedTime);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) return;
        String name   = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_MED_NAME));
        String dosage = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_MED_DOSAGE));
        String time   = cursor.getString(cursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_MED_TIME));
        holder.tvName.setText(name);
        holder.tvDosage.setText(dosage);
        holder.tvTime.setText(time);
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
