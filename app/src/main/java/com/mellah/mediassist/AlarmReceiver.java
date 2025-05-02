package com.mellah.mediassist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int itemId     = intent.getIntExtra("itemId", -1);
        String itemType= intent.getStringExtra("itemType");
        String label   = intent.getStringExtra("label");
        String time    = intent.getStringExtra("time");

        showNotification(context, itemId, label, time);

        Intent i = new Intent(context, FullScreenAlarmActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("itemId",     itemId);
        i.putExtra("itemType",  itemType);
        i.putExtra("label",      label);
        i.putExtra("time",       time);
        context.startActivity(i);
    }

    private void playAlarmSound(Context context) {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
        if (ringtone != null && !ringtone.isPlaying()) {
            ringtone.play();
        }

        // Optional: vibration
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void showNotification(Context context, int id, String label, String time) {
        String channelId = "alarm_channel";
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel (for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Alarm Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminders for appointments and medications");
            channel.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
            );
            nm.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, FullScreenAlarmActivity.class);
        openIntent.putExtra("itemId", id);
        openIntent.putExtra("label", label);
        openIntent.putExtra("time", time);
        PendingIntent pi = PendingIntent.getActivity(
                context,
                id,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Reminder: " + label)
                .setContentText("It's time: " + time)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(0)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(pi, true);

        nm.notify(id, builder.build());
    }

}
