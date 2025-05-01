package com.mellah.mediassist;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Adapter showing a single day's ScheduleItems,
 * each with just an HH:mm in its `time` field.
 */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(ScheduleItem item);
    }

    private final Context context;
    private final List<ScheduleItem> items;
    private final OnItemClickListener listener;
    private final Gson gson = new Gson();

    /**
     * @param context     Activity context
     * @param medsCursor  Cursor from getAllMedications(userId)
     * @param apptsCursor Cursor from getAllAppointments(userId)
     * @param forDate     The LocalDate for which to show items
     * @param listener    Callback for click/edit
     */
    public ScheduleAdapter(Context context,
                           Cursor medsCursor,
                           Cursor apptsCursor,
                           LocalDate forDate,
                           OnItemClickListener listener) {
        this.context  = context;
        this.listener = listener;
        this.items    = new ArrayList<>();

        String dateStr = forDate.toString(); // "YYYY-MM-DD"
        // 1) Medications active on forDate
        if (medsCursor != null) {
            int idxId    = medsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_MED_ID);
            int idxName  = medsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_MED_NAME);
            int idxStart = medsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_MED_START_DATE);
            int idxEnd   = medsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_MED_END_DATE);
            int idxTimes = medsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_MED_TIMES_JSON);

            while (medsCursor.moveToNext()) {
                int medId       = medsCursor.getInt(idxId);
                String name     = medsCursor.getString(idxName);
                LocalDate start = LocalDate.parse(medsCursor.getString(idxStart));
                LocalDate end   = LocalDate.parse(medsCursor.getString(idxEnd));

                if (!forDate.isBefore(start) && !forDate.isAfter(end)) {
                    List<String> times = gson.fromJson(
                            medsCursor.getString(idxTimes),
                            new TypeToken<List<String>>(){}.getType()
                    );
                    for (String t : times) {
                        // only the time portion
                        items.add(new ScheduleItem(
                                medId,
                                ScheduleItem.Type.MEDICATION,
                                name,
                                t
                        ));
                    }
                }
            }
        }

        // 2) Appointments exactly on forDate
        if (apptsCursor != null) {
            int idxId    = apptsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_APPT_ID);
            int idxTitle = apptsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_APPT_TITLE);
            int idxDate  = apptsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_APPT_DATE);
            int idxTime  = apptsCursor.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_APPT_TIME);

            while (apptsCursor.moveToNext()) {
                String d = apptsCursor.getString(idxDate);
                if (dateStr.equals(d)) {
                    String t = apptsCursor.getString(idxTime);
                    items.add(new ScheduleItem(
                            apptsCursor.getInt(idxId),
                            ScheduleItem.Type.APPOINTMENT,
                            apptsCursor.getString(idxTitle),
                            t
                    ));
                }
            }
        }

        // 3) Sort by HH:mm
        Collections.sort(items, Comparator.comparing(si -> si.time));
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
        final TextView tvLabel, tvTime;
        ViewHolder(View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvSchedLabel);
            tvTime  = itemView.findViewById(R.id.tvSchedDateTime);
        }
    }
}
