package com.mellah.mediassist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int itemId     = intent.getIntExtra("itemId", -1);
        String itemType= intent.getStringExtra("itemType");
        String label   = intent.getStringExtra("label");
        String time    = intent.getStringExtra("time");

        Intent i = new Intent(context, FullScreenAlarmActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("itemId",     itemId);
        i.putExtra("itemType",  itemType);
        i.putExtra("label",      label);
        i.putExtra("time",       time);
        context.startActivity(i);
    }
}
