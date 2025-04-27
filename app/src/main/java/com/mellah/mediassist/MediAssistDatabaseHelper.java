package com.mellah.mediassist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class MediAssistDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mediassist.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USERS              = "users";
    public static final String TABLE_MEDICATIONS        = "medications";
    public static final String TABLE_APPOINTMENTS       = "appointments";
    public static final String TABLE_PRESCRIPTIONS      = "prescriptions";
    public static final String TABLE_EMERGENCY_CONTACTS = "emergency_contacts";

    // Users columns
    public static final String COLUMN_USER_ID         = "id";
    public static final String COLUMN_USER_USERNAME   = "username";
    public static final String COLUMN_USER_PASSWORD   = "password";
    public static final String COLUMN_USER_CREATED_AT = "created_at";

    // Medications columns
    public static final String COLUMN_MED_ID           = "id";
    public static final String COLUMN_MED_NAME         = "name";
    public static final String COLUMN_MED_DOSAGE       = "dosage";
    public static final String COLUMN_MED_FREQUENCY    = "frequency";
    public static final String COLUMN_MED_TIME         = "time";
    public static final String COLUMN_MED_START_DATE   = "start_date";
    public static final String COLUMN_MED_END_DATE     = "end_date";
    public static final String COLUMN_MED_NOTES        = "notes";
    public static final String COLUMN_MED_CREATED_AT   = "created_at";

    // Appointments columns
    public static final String COLUMN_APPT_ID           = "id";
    public static final String COLUMN_APPT_TITLE        = "title";
    public static final String COLUMN_APPT_DATE         = "date";
    public static final String COLUMN_APPT_TIME         = "time";
    public static final String COLUMN_APPT_OFFSET       = "reminder_offset";
    public static final String COLUMN_APPT_NOTES        = "notes";
    public static final String COLUMN_APPT_CREATED_AT   = "created_at";

    // Prescriptions columns
    public static final String COLUMN_RX_ID             = "id";
    public static final String COLUMN_RX_IMAGE_PATH     = "image_path";
    public static final String COLUMN_RX_DESCRIPTION    = "description";
    public static final String COLUMN_RX_DATE_ADDED     = "date_added";

    // Emergency Contacts columns
    public static final String COLUMN_EC_ID             = "id";
    public static final String COLUMN_EC_NAME           = "name";
    public static final String COLUMN_EC_PHONE          = "phone";
    public static final String COLUMN_EC_RELATION       = "relation";
    public static final String COLUMN_EC_CREATED_AT     = "created_at";

    public MediAssistDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Users
        String CREATE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_USERNAME + " TEXT NOT NULL UNIQUE,"
                + COLUMN_USER_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_USER_CREATED_AT + " TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now'))"
                + ");";
        db.execSQL(CREATE_USERS);

        // 2. Medications
        String CREATE_MED = "CREATE TABLE " + TABLE_MEDICATIONS + " ("
                + COLUMN_MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MED_NAME + " TEXT NOT NULL,"
                + COLUMN_MED_DOSAGE + " TEXT NOT NULL,"
                + COLUMN_MED_FREQUENCY + " TEXT NOT NULL,"
                + COLUMN_MED_TIME + " TEXT NOT NULL,"
                + COLUMN_MED_START_DATE + " TEXT NOT NULL,"
                + COLUMN_MED_END_DATE + " TEXT,"
                + COLUMN_MED_NOTES + " TEXT,"
                + COLUMN_MED_CREATED_AT + " TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now'))"
                + ");";
        db.execSQL(CREATE_MED);
        db.execSQL("CREATE INDEX idx_medications_time ON " + TABLE_MEDICATIONS + "(" + COLUMN_MED_TIME + ");");

        // 3. Appointments
        String CREATE_APPT = "CREATE TABLE " + TABLE_APPOINTMENTS + " ("
                + COLUMN_APPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_APPT_TITLE + " TEXT NOT NULL,"
                + COLUMN_APPT_DATE + " TEXT NOT NULL,"
                + COLUMN_APPT_TIME + " TEXT NOT NULL,"
                + COLUMN_APPT_OFFSET + " INTEGER NOT NULL,"
                + COLUMN_APPT_NOTES + " TEXT,"
                + COLUMN_APPT_CREATED_AT + " TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now'))"
                + ");";
        db.execSQL(CREATE_APPT);
        db.execSQL("CREATE INDEX idx_appointments_datetime ON " + TABLE_APPOINTMENTS + "(" + COLUMN_APPT_DATE + ", " + COLUMN_APPT_TIME + ");");

        // 4. Prescriptions
        String CREATE_RX = "CREATE TABLE " + TABLE_PRESCRIPTIONS + " ("
                + COLUMN_RX_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_RX_IMAGE_PATH + " TEXT NOT NULL,"
                + COLUMN_RX_DESCRIPTION + " TEXT,"
                + COLUMN_RX_DATE_ADDED + " TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d','now'))"
                + ");";
        db.execSQL(CREATE_RX);
        db.execSQL("CREATE INDEX idx_prescriptions_date ON " + TABLE_PRESCRIPTIONS + "(" + COLUMN_RX_DATE_ADDED + ");");

        // 5. Emergency Contacts
        String CREATE_EC = "CREATE TABLE " + TABLE_EMERGENCY_CONTACTS + " ("
                + COLUMN_EC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EC_NAME + " TEXT NOT NULL,"
                + COLUMN_EC_PHONE + " TEXT NOT NULL,"
                + COLUMN_EC_RELATION + " TEXT,"
                + COLUMN_EC_CREATED_AT + " TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now'))"
                + ");";
        db.execSQL(CREATE_EC);
        db.execSQL("CREATE INDEX idx_contacts_name ON " + TABLE_EMERGENCY_CONTACTS + "(" + COLUMN_EC_NAME + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tables on upgrade
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESCRIPTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMERGENCY_CONTACTS);
        onCreate(db);
    }

    // --- User table methods ---
    public long addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USER_USERNAME, username);
        cv.put(COLUMN_USER_PASSWORD, password); // TODO: hash password before storing
        long id = db.insert(TABLE_USERS, null, cv);
        db.close();
        return id;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { COLUMN_USER_ID };
        String selection = COLUMN_USER_USERNAME + " = ? AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selArgs = { username, password };
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selArgs, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    public Cursor getUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, COLUMN_USER_USERNAME + " = ?", new String[]{username}, null, null, null);
    }

    // --- Medication table methods ---
    public long addMedication(String name, String dosage, String frequency, String time,
                              String startDate, String endDate, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MED_NAME, name);
        cv.put(COLUMN_MED_DOSAGE, dosage);
        cv.put(COLUMN_MED_FREQUENCY, frequency);
        cv.put(COLUMN_MED_TIME, time);
        cv.put(COLUMN_MED_START_DATE, startDate);
        cv.put(COLUMN_MED_END_DATE, endDate);
        cv.put(COLUMN_MED_NOTES, notes);
        long id = db.insert(TABLE_MEDICATIONS, null, cv);
        db.close();
        return id;
    }

    public Cursor getAllMedications() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MEDICATIONS, null, null, null, null, null, COLUMN_MED_TIME + " ASC");
    }

    public int updateMedication(int id, String name, String dosage, String frequency, String time,
                                String startDate, String endDate, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MED_NAME, name);
        cv.put(COLUMN_MED_DOSAGE, dosage);
        cv.put(COLUMN_MED_FREQUENCY, frequency);
        cv.put(COLUMN_MED_TIME, time);
        cv.put(COLUMN_MED_START_DATE, startDate);
        cv.put(COLUMN_MED_END_DATE, endDate);
        cv.put(COLUMN_MED_NOTES, notes);
        int rows = db.update(TABLE_MEDICATIONS, cv, COLUMN_MED_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public int deleteMedication(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_MEDICATIONS, COLUMN_MED_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // --- Appointment table methods ---
    public long addAppointment(String title, String date, String time, int reminderOffset, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_APPT_TITLE, title);
        cv.put(COLUMN_APPT_DATE, date);
        cv.put(COLUMN_APPT_TIME, time);
        cv.put(COLUMN_APPT_OFFSET, reminderOffset);
        cv.put(COLUMN_APPT_NOTES, notes);
        long id = db.insert(TABLE_APPOINTMENTS, null, cv);
        db.close();
        return id;
    }

    public Cursor getAllAppointments() {
        SQLiteDatabase db = this.getReadableDatabase();
        String order = COLUMN_APPT_DATE + " ASC, " + COLUMN_APPT_TIME + " ASC";
        return db.query(TABLE_APPOINTMENTS, null, null, null, null, null, order);
    }

    public int updateAppointment(int id, String title, String date, String time, int reminderOffset, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_APPT_TITLE, title);
        cv.put(COLUMN_APPT_DATE, date);
        cv.put(COLUMN_APPT_TIME, time);
        cv.put(COLUMN_APPT_OFFSET, reminderOffset);
        cv.put(COLUMN_APPT_NOTES, notes);
        int rows = db.update(TABLE_APPOINTMENTS, cv, COLUMN_APPT_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public int deleteAppointment(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_APPOINTMENTS, COLUMN_APPT_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // --- Prescription table methods ---
    public long addPrescription(String imagePath, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_RX_IMAGE_PATH, imagePath);
        cv.put(COLUMN_RX_DESCRIPTION, description);
        long id = db.insert(TABLE_PRESCRIPTIONS, null, cv);
        db.close();
        return id;
    }

    public Cursor getAllPrescriptions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRESCRIPTIONS, null, null, null, null, null, COLUMN_RX_DATE_ADDED + " DESC");
    }

    public int deletePrescription(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_PRESCRIPTIONS, COLUMN_RX_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // --- Emergency Contact table methods ---
    public long addEmergencyContact(String name, String phone, String relation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_EC_NAME, name);
        cv.put(COLUMN_EC_PHONE, phone);
        cv.put(COLUMN_EC_RELATION, relation);
        long id = db.insert(TABLE_EMERGENCY_CONTACTS, null, cv);
        db.close();
        return id;
    }

    public Cursor getAllEmergencyContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EMERGENCY_CONTACTS, null, null, null, null, null, COLUMN_EC_NAME + " ASC");
    }

    public int updateEmergencyContact(int id, String name, String phone, String relation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_EC_NAME, name);
        cv.put(COLUMN_EC_PHONE, phone);
        cv.put(COLUMN_EC_RELATION, relation);
        int rows = db.update(TABLE_EMERGENCY_CONTACTS, cv, COLUMN_EC_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public int deleteEmergencyContact(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_EMERGENCY_CONTACTS, COLUMN_EC_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }
}
