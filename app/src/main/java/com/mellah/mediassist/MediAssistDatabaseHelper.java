package com.mellah.mediassist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.io.File;

public class MediAssistDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mediassist.db";
    private static final int DATABASE_VERSION = 3;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_MEDICATIONS = "medications";
    public static final String TABLE_APPOINTMENTS = "appointments";
    public static final String TABLE_PRESCRIPTIONS = "prescriptions";
    public static final String TABLE_EMERGENCY_CONTACTS = "emergency_contacts";

    // Common foreign key
    public static final String COLUMN_USER_REF = "user_id";

    // Users columns
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_USERNAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_BLOOD_TYPE = "blood_type";
    public static final String COLUMN_USER_AGE = "age";
    public static final String COLUMN_USER_GENDER = "gender";
    public static final String COLUMN_USER_WEIGHT = "weight";
    public static final String COLUMN_USER_ALLERGIES = "allergies";
    public static final String COLUMN_USER_CREATED_AT = "created_at";

    // Medications columns
    public static final String COLUMN_MED_ID = "id";
    public static final String COLUMN_MED_TIMES_JSON = "times_json"; // renamed
    public static final String COLUMN_MED_NAME = "name";
    public static final String COLUMN_MED_DOSAGE = "dosage";
    public static final String COLUMN_MED_FREQUENCY = "frequency";
    public static final String COLUMN_MED_START_DATE = "start_date";
    public static final String COLUMN_MED_END_DATE = "end_date";
    public static final String COLUMN_MED_NOTES = "notes";
    public static final String COLUMN_MED_CREATED_AT = "created_at";

    // Appointments
    public static final String COLUMN_APPT_ID = "id";
    public static final String COLUMN_APPT_TITLE = "title";
    public static final String COLUMN_APPT_DATE = "date";
    public static final String COLUMN_APPT_TIME = "time";
    public static final String COLUMN_APPT_OFFSET = "reminder_offset";
    public static final String COLUMN_APPT_NOTES = "notes";
    public static final String COLUMN_APPT_CREATED_AT = "created_at";

    // Prescriptions
    public static final String COLUMN_RX_ID = "id";
    public static final String COLUMN_RX_IMAGE_PATH = "image_path";
    public static final String COLUMN_RX_DESCRIPTION = "description";
    public static final String COLUMN_RX_DATE_ADDED = "date_added";

    // Emergency Contacts
    public static final String COLUMN_EC_ID = "id";
    public static final String COLUMN_EC_NAME = "name";
    public static final String COLUMN_EC_PHONE = "phone";
    public static final String COLUMN_EC_RELATION = "relation";
    public static final String COLUMN_EC_CREATED_AT = "created_at";

    public MediAssistDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Users table with medical info
        String CREATE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_USERNAME + " TEXT NOT NULL UNIQUE,"
                + COLUMN_USER_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_USER_BLOOD_TYPE + " TEXT,"
                + COLUMN_USER_AGE + " INTEGER,"
                + COLUMN_USER_GENDER + " TEXT,"
                + COLUMN_USER_WEIGHT + " REAL,"
                + COLUMN_USER_ALLERGIES + " TEXT,"
                + COLUMN_USER_CREATED_AT + " TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now'))"
                + ");";
        db.execSQL(CREATE_USERS);

        // Medications table with JSON times
        String CREATE_MED = "CREATE TABLE " + TABLE_MEDICATIONS + " ("
                + COLUMN_MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_REF + " INTEGER NOT NULL,"
                + COLUMN_MED_NAME + " TEXT NOT NULL,"
                + COLUMN_MED_DOSAGE + " TEXT NOT NULL,"
                + COLUMN_MED_FREQUENCY + " TEXT NOT NULL,"
                + COLUMN_MED_TIMES_JSON + " TEXT NOT NULL,"
                + COLUMN_MED_START_DATE + " TEXT NOT NULL,"
                + COLUMN_MED_END_DATE + " TEXT,"
                + COLUMN_MED_NOTES + " TEXT,"
                + COLUMN_MED_CREATED_AT + " TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now')),"
                + "FOREIGN KEY(" + COLUMN_USER_REF + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE"
                + ");";
        db.execSQL(CREATE_MED);
        db.execSQL("CREATE INDEX idx_medications_user_times ON " + TABLE_MEDICATIONS
                + "(" + COLUMN_USER_REF + ", " + COLUMN_MED_TIMES_JSON + ");");

        // 3. Appointments
        String CREATE_APPT = "CREATE TABLE " + TABLE_APPOINTMENTS + " ("
                + COLUMN_APPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_REF + " INTEGER NOT NULL,"
                + COLUMN_APPT_TITLE + " TEXT NOT NULL,"
                + COLUMN_APPT_DATE + " TEXT NOT NULL,"
                + COLUMN_APPT_TIME + " TEXT NOT NULL,"
                + COLUMN_APPT_OFFSET + " INTEGER NOT NULL,"
                + COLUMN_APPT_NOTES + " TEXT,"
                + COLUMN_APPT_CREATED_AT + " TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now')),"
                + "FOREIGN KEY(" + COLUMN_USER_REF + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE"
                + ");";
        db.execSQL(CREATE_APPT);
        db.execSQL("CREATE INDEX idx_appointments_user_date ON " + TABLE_APPOINTMENTS + "(" + COLUMN_USER_REF + ", " + COLUMN_APPT_DATE + ", " + COLUMN_APPT_TIME + ");");

        // 4. Prescriptions
        String CREATE_RX = "CREATE TABLE " + TABLE_PRESCRIPTIONS + " ("
                + COLUMN_RX_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_REF + " INTEGER NOT NULL,"
                + COLUMN_RX_IMAGE_PATH + " TEXT NOT NULL,"
                + COLUMN_RX_DESCRIPTION + " TEXT,"
                + COLUMN_RX_DATE_ADDED + " TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d','now')),"
                + "FOREIGN KEY(" + COLUMN_USER_REF + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE"
                + ");";
        db.execSQL(CREATE_RX);
        db.execSQL("CREATE INDEX idx_prescriptions_user_date ON " + TABLE_PRESCRIPTIONS + "(" + COLUMN_USER_REF + ", " + COLUMN_RX_DATE_ADDED + ");");

        // 5. Emergency Contacts
        String CREATE_EC = "CREATE TABLE " + TABLE_EMERGENCY_CONTACTS + " ("
                + COLUMN_EC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_REF + " INTEGER NOT NULL,"
                + COLUMN_EC_NAME + " TEXT NOT NULL,"
                + COLUMN_EC_PHONE + " TEXT NOT NULL,"
                + COLUMN_EC_RELATION + " TEXT,"
                + COLUMN_EC_CREATED_AT + " TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now')),"
                + "FOREIGN KEY(" + COLUMN_USER_REF + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE"
                + ");";
        db.execSQL(CREATE_EC);
        db.execSQL("CREATE INDEX idx_contacts_user_name ON " + TABLE_EMERGENCY_CONTACTS + "(" + COLUMN_USER_REF + ", " + COLUMN_EC_NAME + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For simplicity, drop & recreate
        db.execSQL("PRAGMA foreign_keys=OFF;");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMERGENCY_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESCRIPTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    // --- User methods updated for profile ---
    public long addUser(String username, String password,
                        String bloodType, int age, String gender,
                        double weight, String allergies) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USER_USERNAME, username);
        cv.put(COLUMN_USER_PASSWORD, password); // TODO: hash
        cv.put(COLUMN_USER_BLOOD_TYPE, bloodType);
        cv.put(COLUMN_USER_AGE, age);
        cv.put(COLUMN_USER_GENDER, gender);
        cv.put(COLUMN_USER_WEIGHT, weight);
        cv.put(COLUMN_USER_ALLERGIES, allergies);
        long id = db.insert(TABLE_USERS, null, cv);
        db.close();
        return id;
    }

    public boolean checkUser(String username, String password) {
        // unchanged; returns existence
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USER_USERNAME + "=? AND " + COLUMN_USER_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USER_USERNAME + "=?",
                new String[]{username}, null, null, null);
        int id = -1;
        if (cursor.moveToFirst()) id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        cursor.close();
        db.close();
        return id;
    }

    /**
     * Fetch a single user’s row by ID.
     */
    public Cursor getUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS,
                null,
                COLUMN_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) },
                null, null, null);
    }

    /**
     * Update a user’s profile fields.
     */
    public boolean updateUser(int userId,
                              String bloodType,
                              int age,
                              String gender,
                              double weight,
                              String allergies) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USER_BLOOD_TYPE, bloodType);
        cv.put(COLUMN_USER_AGE,         age);
        cv.put(COLUMN_USER_GENDER,      gender);
        cv.put(COLUMN_USER_WEIGHT,      weight);
        cv.put(COLUMN_USER_ALLERGIES,   allergies);
        int rows = db.update(
                TABLE_USERS,
                cv,
                COLUMN_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) }
        );
        db.close();
        return rows > 0;
    }


    public long addMedication(int userId,
                              String name,
                              String dosage,
                              String frequency,
                              String timesJson,
                              String startDate,
                              String endDate,
                              String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USER_REF, userId);
        cv.put(COLUMN_MED_NAME, name);
        cv.put(COLUMN_MED_DOSAGE, dosage);
        cv.put(COLUMN_MED_FREQUENCY, frequency);
        cv.put(COLUMN_MED_TIMES_JSON, timesJson);
        cv.put(COLUMN_MED_START_DATE, startDate);
        cv.put(COLUMN_MED_END_DATE, endDate);
        cv.put(COLUMN_MED_NOTES, notes);
        long id = db.insert(TABLE_MEDICATIONS, null, cv);
        db.close();
        return id;
    }

    public Cursor getAllMedications(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MEDICATIONS,
                null,
                COLUMN_USER_REF + " = ?",
                new String[]{String.valueOf(userId)},
                null, null,
                COLUMN_MED_NAME + " ASC");
    }

    /**
     * Update an existing medication entry.
     * @return true if at least one row was updated
     */
    public boolean updateMedication(int medId,
                                    String name,
                                    String dosage,
                                    String frequency,
                                    String timesJson,
                                    String startDate,
                                    String endDate,
                                    String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_MED_NAME, name);
        cv.put(COLUMN_MED_DOSAGE, dosage);
        cv.put(COLUMN_MED_FREQUENCY, frequency);
        cv.put(COLUMN_MED_TIMES_JSON, timesJson);
        cv.put(COLUMN_MED_START_DATE, startDate);
        cv.put(COLUMN_MED_END_DATE, endDate);
        cv.put(COLUMN_MED_NOTES, notes);
        int rows = db.update(TABLE_MEDICATIONS,
                cv,
                COLUMN_MED_ID + " = ?",
                new String[]{String.valueOf(medId)});
        db.close();
        return rows > 0;
    }

    /**
     * Delete a medication by its ID.
     */
    public boolean deleteMedication(int medId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_MEDICATIONS,
                COLUMN_MED_ID + " = ?",
                new String[]{String.valueOf(medId)});
        db.close();
        return rows > 0;
    }

    // Similarly update appointments, prescriptions, and contacts:
    public long addAppointment(int userId, String title, String date,
                               String time, int offset, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USER_REF, userId);
        cv.put(COLUMN_APPT_TITLE, title);
        cv.put(COLUMN_APPT_DATE, date);
        cv.put(COLUMN_APPT_TIME, time);
        cv.put(COLUMN_APPT_OFFSET, offset);
        cv.put(COLUMN_APPT_NOTES, notes);
        long id = db.insert(TABLE_APPOINTMENTS, null, cv);
        db.close();
        return id;
    }

    public Cursor getAllAppointments(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_APPOINTMENTS, null,
                COLUMN_USER_REF + "=?", new String[]{String.valueOf(userId)},
                null, null, COLUMN_APPT_DATE + " ASC, " + COLUMN_APPT_TIME + " ASC");
    }

    /**
     * Update an existing appointment.
     * @return true if at least one row was updated
     */
    public boolean updateAppointment(int apptId,
                                     String title,
                                     String date,
                                     String time,
                                     int offset,
                                     String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_APPT_TITLE, title);
        cv.put(COLUMN_APPT_DATE, date);
        cv.put(COLUMN_APPT_TIME, time);
        cv.put(COLUMN_APPT_OFFSET, offset);
        cv.put(COLUMN_APPT_NOTES, notes);
        int rows = db.update(TABLE_APPOINTMENTS,
                cv,
                COLUMN_APPT_ID + " = ?",
                new String[]{String.valueOf(apptId)});
        db.close();
        return rows > 0;
    }

    /**
     * Delete an appointment by its ID.
     */
    public boolean deleteAppointment(int apptId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_APPOINTMENTS,
                COLUMN_APPT_ID + " = ?",
                new String[]{String.valueOf(apptId)});
        db.close();
        return rows > 0;
    }


    public long addPrescription(int userId, String imagePath, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USER_REF, userId);
        cv.put(COLUMN_RX_IMAGE_PATH, imagePath);
        cv.put(COLUMN_RX_DESCRIPTION, description);
        long id = db.insert(TABLE_PRESCRIPTIONS, null, cv);
        db.close();
        return id;
    }

    public Cursor getAllPrescriptions(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRESCRIPTIONS, null,
                COLUMN_USER_REF + "=?", new String[]{String.valueOf(userId)},
                null, null, COLUMN_RX_DATE_ADDED + " DESC");
    }

    /**
     * Update an existing prescription.
     * Deletes the previous image file (if different) before updating.
     * @return true if at least one row was updated
     */
    public boolean updatePrescription(int rxId, String newImagePath, String description) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Look up the existing imagePath
        String oldImagePath = null;
        Cursor c = db.query(
                TABLE_PRESCRIPTIONS,
                new String[]{ COLUMN_RX_IMAGE_PATH },
                COLUMN_RX_ID + " = ?",
                new String[]{ String.valueOf(rxId) },
                null, null, null
        );
        if (c != null) {
            if (c.moveToFirst()) {
                oldImagePath = c.getString(
                        c.getColumnIndexOrThrow(COLUMN_RX_IMAGE_PATH)
                );
            }
            c.close();
        }

        // If we're changing to a different image, delete the old file
        if (oldImagePath != null
                && !oldImagePath.isEmpty()
                && !oldImagePath.equals(newImagePath)) {
            File oldFile = new File(Uri.parse(oldImagePath).getPath());
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }

        // Perform the DB update
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_RX_IMAGE_PATH, newImagePath);
        cv.put(COLUMN_RX_DESCRIPTION, description);
        int rows = db.update(
                TABLE_PRESCRIPTIONS,
                cv,
                COLUMN_RX_ID + " = ?",
                new String[]{ String.valueOf(rxId) }
        );
        db.close();

        return rows > 0;
    }


    /**
     * Delete a prescription by its ID.
     */
    public boolean deletePrescription(int rxId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(TABLE_PRESCRIPTIONS,
                new String[]{COLUMN_RX_IMAGE_PATH},
                COLUMN_RX_ID + "=?",
                new String[]{ String.valueOf(rxId) },
                null,null,null);
        if (c != null && c.moveToFirst()) {
            String uriStr = c.getString(0);
            c.close();
            // 2) delete the file
            if (uriStr != null) {
                File f = new File(Uri.parse(uriStr).getPath());
                if (f.exists()) f.delete();
            }
        }

        int rows = db.delete(
                TABLE_PRESCRIPTIONS,
                COLUMN_RX_ID + " = ?",
                new String[]{ String.valueOf(rxId) }
        );
        db.close();
        return rows > 0;
    }


    public long addEmergencyContact(int userId, String name, String phone, String relation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USER_REF, userId);
        cv.put(COLUMN_EC_NAME, name);
        cv.put(COLUMN_EC_PHONE, phone);
        cv.put(COLUMN_EC_RELATION, relation);
        long id = db.insert(TABLE_EMERGENCY_CONTACTS, null, cv);
        db.close();
        return id;
    }

    public Cursor getAllEmergencyContacts(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EMERGENCY_CONTACTS, null,
                COLUMN_USER_REF + "=?", new String[]{String.valueOf(userId)},
                null, null, COLUMN_EC_NAME + " ASC");

    }

    /**
     * Update an existing emergency contact.
     * @return true if at least one row was updated
     */
    public boolean updateEmergencyContact(int ecId, String name, String phone, String relation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_EC_NAME, name);
        cv.put(COLUMN_EC_PHONE, phone);
        cv.put(COLUMN_EC_RELATION, relation);
        int rows = db.update(
                TABLE_EMERGENCY_CONTACTS,
                cv,
                COLUMN_EC_ID + " = ?",
                new String[]{ String.valueOf(ecId) }
        );
        db.close();
        return rows > 0;
    }

    /**
     * Delete an emergency contact by its ID.
     */
    public boolean deleteEmergencyContact(int ecId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(
                TABLE_EMERGENCY_CONTACTS,
                COLUMN_EC_ID + " = ?",
                new String[]{ String.valueOf(ecId) }
        );
        db.close();
        return rows > 0;
    }

}
