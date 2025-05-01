package com.mellah.mediassist;

public class ScheduleItem {
    public enum Type { MEDICATION, APPOINTMENT }

    public final int    id;
    public final Type   type;
    public final String label;
    public final String time;  // e.g. "08:00" or "14:30"

    public ScheduleItem(int id, Type type, String label, String time) {
        this.id    = id;
        this.type  = type;
        this.label = label;
        this.time  = time;
    }
}
