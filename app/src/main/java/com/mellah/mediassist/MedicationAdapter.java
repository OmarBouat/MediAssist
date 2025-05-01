package com.mellah.mediassist;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
    public interface OnMedicationActionListener {
        void onEdit(int medId, String name, String dosage,
                    String timesJson, String startDate, String endDate, String notes);
        void onDelete(int medId);
    }

    private final Context context;
    private final Cursor cursor;
    private final OnMedicationActionListener listener;
    private final Gson gson = new Gson();

    public MedicationAdapter(Context context, Cursor cursor, OnMedicationActionListener listener) {
        this.context = context;
        this.cursor = cursor;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_medication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) return;

        int id = cursor.getInt(cursor.getColumnIndexOrThrow(
                MediAssistDatabaseHelper.COLUMN_MED_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(
                MediAssistDatabaseHelper.COLUMN_MED_NAME));
        String dosage = cursor.getString(cursor.getColumnIndexOrThrow(
                MediAssistDatabaseHelper.COLUMN_MED_DOSAGE));
        String timesJson = cursor.getString(cursor.getColumnIndexOrThrow(
                MediAssistDatabaseHelper.COLUMN_MED_TIMES_JSON));
        String startDate = cursor.getString(cursor.getColumnIndexOrThrow(
                MediAssistDatabaseHelper.COLUMN_MED_START_DATE));
        String endDate = cursor.getString(cursor.getColumnIndexOrThrow(
                MediAssistDatabaseHelper.COLUMN_MED_END_DATE));
        String notes = cursor.getString(cursor.getColumnIndexOrThrow(
                MediAssistDatabaseHelper.COLUMN_MED_NOTES));

        List<String> times = gson.fromJson(timesJson,
                new TypeToken<List<String>>(){}.getType());
        String timesDisplay = TextUtils.join(", ", times);

        holder.tvName.setText(name);
        holder.tvDosage.setText(dosage);
        holder.tvTimes.setText(timesDisplay);

        holder.btnEdit.setOnClickListener(v ->
                listener.onEdit(id, name, dosage, timesJson, startDate, endDate, notes)
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
        TextView tvName, tvDosage, tvTimes;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvMedDosage);
            tvTimes = itemView.findViewById(R.id.tvMedTimes);
            btnEdit = itemView.findViewById(R.id.btnEditMed);
            btnDelete = itemView.findViewById(R.id.btnDeleteMed);
        }
    }
}
