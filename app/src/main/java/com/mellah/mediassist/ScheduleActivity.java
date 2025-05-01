package com.mellah.mediassist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ScheduleActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView rvSchedule;
    private MediAssistDatabaseHelper dbHelper;
    private int currentUserId;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        dbHelper     = new MediAssistDatabaseHelper(this);
        calendarView = findViewById(R.id.calendarView);
        rvSchedule   = findViewById(R.id.rvSchedule);
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));

        currentUserId = getSharedPreferences("MediAssistPrefs", MODE_PRIVATE)
                .getInt("currentUserId", -1);
        if (currentUserId < 0) {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load today
        LocalDate today = LocalDate.now();
        loadForDate(today);

        // On date change
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            LocalDate selected = LocalDate.of(year, month + 1, dayOfMonth);
            loadForDate(selected);
        });
    }

    private void loadForDate(LocalDate date) {
        List<ScheduleItem> items = new ArrayList<>();
        String dateStr = date.toString(); // "YYYY-MM-DD"

        // 1️⃣ Medications: those active on 'date'
        try (Cursor c = dbHelper.getAllMedications(currentUserId)) {
            int idxId    = c.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_MED_ID);
            int idxName  = c.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_MED_NAME);
            int idxStart = c.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_MED_START_DATE);
            int idxEnd   = c.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_MED_END_DATE);
            int idxTimes = c.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_MED_TIMES_JSON);
            while (c.moveToNext()) {
                int    medId     = c.getInt(idxId);
                String name      = c.getString(idxName);
                LocalDate start  = LocalDate.parse(c.getString(idxStart));
                LocalDate end    = LocalDate.parse(c.getString(idxEnd));
                if (!date.isBefore(start) && !date.isAfter(end)) {
                    List<String> times = gson.fromJson(
                            c.getString(idxTimes),
                            new TypeToken<List<String>>(){}.getType()
                    );
                    for (String t : times) {
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

        // 2️⃣ Appointments: those on 'date'
        try (Cursor c2 = dbHelper.getAllAppointments(currentUserId)) {
            int idxId    = c2.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_ID);
            int idxTitle = c2.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_TITLE);
            int idxDate  = c2.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_DATE);
            int idxTime  = c2.getColumnIndexOrThrow(MediAssistDatabaseHelper.COLUMN_APPT_TIME);
            while (c2.moveToNext()) {
                String d = c2.getString(idxDate);
                if (dateStr.equals(d)) {
                    items.add(new ScheduleItem(
                            c2.getInt(idxId),
                            ScheduleItem.Type.APPOINTMENT,
                            c2.getString(idxTitle),
                            c2.getString(idxTime)
                    ));
                }
            }
        }

        // 3️⃣ Bind to adapter
        DailyScheduleAdapter.OnItemClickListener listener = item -> {
            if (item.type == ScheduleItem.Type.MEDICATION) {
                Intent i = new Intent(this, AddMedicationActivity.class);
                i.putExtra("medId", item.id);
                startActivity(i);
            } else {
                Intent i = new Intent(this, AddAppointmentActivity.class);
                i.putExtra("apptId", item.id);
                startActivity(i);
            }
        };
        rvSchedule.setAdapter(new DailyScheduleAdapter(this, items, listener));

        if (items.isEmpty()) {
            Toast.makeText(this, "No items on " + dateStr, Toast.LENGTH_SHORT).show();
        }
    }
}
