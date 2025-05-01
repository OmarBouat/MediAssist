package com.mellah.mediassist;

import android.content.Context;
import android.database.Cursor;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    private Context context;
    private List<ScheduleItem> items;
    private Gson gson = new Gson();
    private SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public ScheduleAdapter(Context context, Cursor medsCursor, Cursor apptsCursor) {
        this.context = context;
        items = new ArrayList<>();

        // 1. Expand medication schedules
        if (medsCursor != null) {
            int idxName   = medsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_MED_NAME);
            int idxStart  = medsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_MED_START_DATE);
            int idxEnd    = medsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_MED_END_DATE);
            int idxTimes  = medsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_MED_TIMES_JSON);

            while (medsCursor.moveToNext()) {
                String name = medsCursor.getString(idxName);
                String startDate = medsCursor.getString(idxStart);
                String endDate   = medsCursor.getString(idxEnd);
                String timesJson  = medsCursor.getString(idxTimes);

                List<String> times = gson.fromJson(timesJson,
                        new TypeToken<List<String>>(){}.getType());
                LocalDate d = LocalDate.parse(startDate);
                LocalDate e = LocalDate.parse(endDate);
                while (!d.isAfter(e)) {
                    for (String t : times) {
                        items.add(new ScheduleItem(
                                "Medication: " + name,
                                d + " " + t
                        ));
                    }
                    d = d.plusDays(1);
                }
            }
        }

        // 2. Add appointments (unchanged)
        if (apptsCursor != null) {
            int idxTitle = apptsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_APPT_TITLE);
            int idxDate  = apptsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_APPT_DATE);
            int idxTime  = apptsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_APPT_TIME);
            while (apptsCursor.moveToNext()) {
                String title = apptsCursor.getString(idxTitle);
                String date  = apptsCursor.getString(idxDate);
                String time  = apptsCursor.getString(idxTime);
                items.add(new ScheduleItem(
                        "Appointment: " + title,
                        date + " " + time
                ));
            }
        }

        // 3. Sort by parsed datetime
        Collections.sort(items, Comparator.comparing(item -> {
            try { return dtFormat.parse(item.dateTime); }
            catch (Exception e) { return new Date(0); }
        }));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        ScheduleItem si = items.get(pos);
        holder.tvLabel.setText(si.label);
        holder.tvDateTime.setText(si.dateTime);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel, tvDateTime;
        ViewHolder(View v) {
            super(v);
            tvLabel    = v.findViewById(R.id.tvSchedLabel);
            tvDateTime = v.findViewById(R.id.tvSchedDateTime);
        }
    }
}
