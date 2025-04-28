package com.mellah.mediassist;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    private final Context context;
    private final List<ScheduleItem> items;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ScheduleAdapter(Context context, Cursor medsCursor, Cursor apptsCursor) {
        this.context = context;
        items = new ArrayList<>();
        // Medications
        if (medsCursor != null) {
            while (medsCursor.moveToNext()) {
                String name = medsCursor.getString(medsCursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_MED_NAME));
                String time = medsCursor.getString(medsCursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_MED_TIME));
                items.add(new ScheduleItem("Medication: " + name, time));
            }
        }
        // Appointments
        if (apptsCursor != null) {
            while (apptsCursor.moveToNext()) {
                String title = apptsCursor.getString(apptsCursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_TITLE));
                String date  = apptsCursor.getString(apptsCursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_DATE));
                String time  = apptsCursor.getString(apptsCursor.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_TIME));
                items.add(new ScheduleItem("Appointment: " + title, date + " " + time));
            }
        }
        // Sort by datetime
        Collections.sort(items, (a, b) -> {
            try {
                Date da = sdf.parse(a.dateTime);
                Date db = sdf.parse(b.dateTime);
                return da.compareTo(db);
            } catch (ParseException e) {
                return 0;
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesc, tvDateTime;
        public ViewHolder(View itemView) {
            super(itemView);
            tvDesc     = itemView.findViewById(R.id.tvScheduleDesc);
            tvDateTime = itemView.findViewById(R.id.tvScheduleDateTime);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ScheduleItem item = items.get(position);
        holder.tvDesc.setText(item.description);
        holder.tvDateTime.setText(item.dateTime);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static class ScheduleItem {
        String description, dateTime;
        ScheduleItem(String description, String dateTime) {
            this.description = description;
            this.dateTime    = dateTime;
        }
    }
}
