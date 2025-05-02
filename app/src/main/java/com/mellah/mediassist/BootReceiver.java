package com.mellah.mediassist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import java.time.LocalDate;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        MediAssistDatabaseHelper db = new MediAssistDatabaseHelper(context);
        Gson gson = new Gson();

        // 1) Reschedule medication alarms
        int userId = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                .getInt("currentUserId", -1);
        Cursor mc = db.getAllMedications(userId);
        while (mc.moveToNext()) {
            int medId     = mc.getInt(mc.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_MED_ID));
            String name   = mc.getString(mc.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_MED_NAME));
            String timesJson = mc.getString(mc.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_MED_TIMES_JSON));
            List<String> times = gson.fromJson(timesJson,
                    new TypeToken<List<String>>(){}.getType());
            // reuse your existing scheduling helper
            AddMedicationActivity.scheduleStaticMedicationAlarms(
                    context, medId, name, times
            );
        }
        mc.close();

        // 2) Reschedule appointment alarms
        Cursor ac = db.getAllAppointments(userId);
        while (ac.moveToNext()) {
            int apptId   = ac.getInt(ac.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_APPT_ID));
            String date  = ac.getString(ac.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_APPT_DATE));
            String time  = ac.getString(ac.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_APPT_TIME));
            int offset   = ac.getInt(ac.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_APPT_OFFSET));
            String title = ac.getString(ac.getColumnIndexOrThrow(
                    MediAssistDatabaseHelper.COLUMN_APPT_TITLE));
            AddAppointmentActivity.scheduleStaticAppointmentAlarm(
                    context, apptId, date, time, offset, title
            );
        }
        ac.close();
        db.close();
    }
}
