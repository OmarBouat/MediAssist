package com.mellah.mediassist;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AddAppointmentActivity extends AppCompatActivity {
    private EditText etTitle, etNotes;
    private TextView tvDate, tvTime;
    private Button btnPickDate, btnPickTime, btnSave;
    private MediAssistDatabaseHelper dbHelper;

    private boolean isEditMode = false;
    private int apptId = -1;
    private int apptYear, apptMonth, apptDay, apptHour, apptMinute, apptOffset = 60;

    private boolean waitingForPermission = false;
    private int pendingScheduleId;
    private String pendingDate, pendingTime, pendingLabel;
    private int pendingOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);

        etTitle      = findViewById(R.id.etApptTitle);
        etNotes      = findViewById(R.id.etApptNotes);
        tvDate       = findViewById(R.id.tvApptDate);
        tvTime       = findViewById(R.id.tvApptTime);
        btnPickDate  = findViewById(R.id.btnPickDate);
        btnPickTime  = findViewById(R.id.btnPickTime);
        btnSave      = findViewById(R.id.btnSaveAppt);
        dbHelper     = new MediAssistDatabaseHelper(this);

        Calendar cal = Calendar.getInstance();
        apptYear   = cal.get(Calendar.YEAR);
        apptMonth  = cal.get(Calendar.MONTH);
        apptDay    = cal.get(Calendar.DAY_OF_MONTH);
        apptHour   = cal.get(Calendar.HOUR_OF_DAY);
        apptMinute = cal.get(Calendar.MINUTE);

        updateDateDisplay();
        updateTimeDisplay();

        btnPickDate.setOnClickListener(v ->
                new DatePickerDialog(this,
                        (view,y,m,d)->{ apptYear=y; apptMonth=m; apptDay=d; updateDateDisplay(); },
                        apptYear, apptMonth, apptDay
                ).show()
        );
        btnPickTime.setOnClickListener(v ->
                new TimePickerDialog(this,
                        (view,h,min)->{ apptHour=h; apptMinute=min; updateTimeDisplay(); },
                        apptHour, apptMinute, true
                ).show()
        );
        btnSave.setOnClickListener(v-> saveAppointment());

        Intent i = getIntent();
        if (i.hasExtra("apptId")) {
            isEditMode = true;
            apptId   = i.getIntExtra("apptId", -1);
            etTitle. setText(i.getStringExtra("title"));
            String[] dp = i.getStringExtra("date").split("-");
            apptYear  = Integer.parseInt(dp[0]);
            apptMonth = Integer.parseInt(dp[1]) - 1;
            apptDay   = Integer.parseInt(dp[2]);
            updateDateDisplay();
            String[] tp = i.getStringExtra("time").split(":");
            apptHour   = Integer.parseInt(tp[0]);
            apptMinute = Integer.parseInt(tp[1]);
            updateTimeDisplay();
            apptOffset= i.getIntExtra("offset", 60);
            etNotes.  setText(i.getStringExtra("notes"));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (waitingForPermission) {
            AlarmManager am = getSystemService(AlarmManager.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && am.canScheduleExactAlarms()) {
                waitingForPermission = false;
                scheduleStaticAppointmentAlarm(
                        this,
                        pendingScheduleId,
                        pendingDate, pendingTime,
                        pendingOffset, pendingLabel
                );
                finish();
            }
        }
    }

    private void saveAppointment() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
            return;
        }
        String notes = etNotes.getText().toString().trim();
        String date  = String.format("%04d-%02d-%02d", apptYear, apptMonth+1, apptDay);
        String time  = String.format("%02d:%02d", apptHour, apptMinute);
        int userId   = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                .getInt("currentUserId", -1);

        int scheduleId;
        boolean ok;
        if (isEditMode) {
            ok = dbHelper.updateAppointment(
                    apptId, title, date, time, apptOffset, notes
            );
            scheduleId = apptId;
        } else {
            long id = dbHelper.addAppointment(
                    userId, title, date, time, apptOffset, notes
            );
            ok = id > 0;
            scheduleId = (int)id;
        }
        if (!ok) {
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
            return;
        }

        AlarmManager am = getSystemService(AlarmManager.class);
        pendingScheduleId = scheduleId;
        pendingDate       = date;
        pendingTime       = time;
        pendingOffset     = apptOffset;
        pendingLabel      = title;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !am.canScheduleExactAlarms()) {
            waitingForPermission = true;
            Toast.makeText(this,
                    "Enable exact alarms in settings",
                    Toast.LENGTH_LONG
            ).show();
            startActivity(new Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            ));
        } else {
            scheduleStaticAppointmentAlarm(
                    this, scheduleId, date, time, apptOffset, title
            );
            finish();
        }
    }

    /**
     * Shared scheduling logic for appointments
     */
    public static void scheduleStaticAppointmentAlarm(
            Context ctx,
            int scheduleId,
            String date,
            String time,
            int offsetMinutes,
            String label
    ) {
        String[] dp = date.split("-");
        String[] tp = time.split(":");
        Calendar c = Calendar.getInstance();
        c.set(
                Integer.parseInt(dp[0]),
                Integer.parseInt(dp[1]) - 1,
                Integer.parseInt(dp[2]),
                Integer.parseInt(tp[0]),
                Integer.parseInt(tp[1]),
                0
        );
        c.add(Calendar.MINUTE, -offsetMinutes);
        long when = c.getTimeInMillis();

        AlarmManager am = ctx.getSystemService(AlarmManager.class);
        Intent fire = new Intent(ctx, AlarmReceiver.class);
        fire.putExtra("itemId",   scheduleId);
        fire.putExtra("itemType", "APPOINTMENT");
        fire.putExtra("label",    label);
        fire.putExtra("time",     time);
        PendingIntent piFire = PendingIntent.getBroadcast(
                ctx,
                scheduleId,
                fire,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        Intent show = new Intent(ctx, ScheduleActivity.class);
        PendingIntent piShow = PendingIntent.getActivity(
                ctx,
                scheduleId * 1000,
                show,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmClockInfo info = new AlarmClockInfo(when, piShow);
        am.setAlarmClock(info, piFire);
    }

    private void updateDateDisplay() {
        tvDate.setText(String.format("%04d-%02d-%02d",
                apptYear, apptMonth+1, apptDay));
    }

    private void updateTimeDisplay() {
        tvTime.setText(String.format("%02d:%02d",
                apptHour, apptMinute));
    }
}
