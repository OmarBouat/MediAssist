package com.mellah.mediassist;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {
    private Context context;
    private Cursor cursor;
    private Gson gson = new Gson();

    public MedicationAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
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
        String name = cursor.getString(cursor.getColumnIndexOrThrow(
                MediAssistDatabaseHelper.COLUMN_MED_NAME));
        String dosage = cursor.getString(cursor.getColumnIndexOrThrow(
                MediAssistDatabaseHelper.COLUMN_MED_DOSAGE));
        String freq = cursor.getString(cursor.getColumnIndexOrThrow(
                MediAssistDatabaseHelper.COLUMN_MED_FREQUENCY));
        String timesJson = cursor.getString(cursor.getColumnIndexOrThrow(
                MediAssistDatabaseHelper.COLUMN_MED_TIMES_JSON));

        // Parse JSON array and format for display
        List<String> times = gson.fromJson(timesJson,
                new TypeToken<List<String>>(){}.getType());
        String timesDisplay = TextUtils.join(", ", times);

        holder.tvName.setText(name);
        holder.tvDosage.setText(dosage);
        holder.tvFrequency.setText(freq);
        holder.tvTimes.setText(timesDisplay);
    }

    @Override
    public int getItemCount() {
        return (cursor == null) ? 0 : cursor.getCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvFrequency, tvTimes;
        ViewHolder(View itemView) {
            super(itemView);
            tvName      = itemView.findViewById(R.id.tvMedName);
            tvDosage    = itemView.findViewById(R.id.tvMedDosage);
            tvFrequency = itemView.findViewById(R.id.tvMedFrequency);
            tvTimes     = itemView.findViewById(R.id.tvMedTimes);
        }
    }
}

