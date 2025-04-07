package com.inmo.arsdksample.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.inmo.arsdksample.model.Interaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SQLite database helper for storing interaction history and cached responses
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "valentine_assistant.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table names
    private static final String TABLE_INTERACTIONS = "interactions";
    private static final String TABLE_CACHED_RESPONSES = "cached_responses";
    
    // Common columns
    private static final String KEY_ID = "id";
    private static final String KEY_TIMESTAMP = "timestamp";
    
    // Interactions table columns
    private static final String KEY_EMOTION = "emotion";
    private static final String KEY_PICKUP_LINE = "pickup_line";
    private static final String KEY_SUCCESSFUL = "was_successful";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_IMAGE_PATH = "image_path";
    
    // Cached responses table columns
    private static final String KEY_IMAGE_HASH = "image_hash";
    private static final String KEY_RESPONSE = "response";
    
    // Date format for SQLite storage
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    
    // Singleton instance
    private static DatabaseHelper instance;
    
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create interactions table
        String CREATE_INTERACTIONS_TABLE = "CREATE TABLE " + TABLE_INTERACTIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TIMESTAMP + " TEXT,"
                + KEY_EMOTION + " TEXT,"
                + KEY_PICKUP_LINE + " TEXT,"
                + KEY_SUCCESSFUL + " INTEGER,"
                + KEY_NOTES + " TEXT,"
                + KEY_IMAGE_PATH + " TEXT"
                + ")";
        
        // Create cached responses table
        String CREATE_CACHED_RESPONSES_TABLE = "CREATE TABLE " + TABLE_CACHED_RESPONSES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_IMAGE_HASH + " TEXT UNIQUE,"
                + KEY_RESPONSE + " TEXT,"
                + KEY_TIMESTAMP + " TEXT"
                + ")";
        
        db.execSQL(CREATE_INTERACTIONS_TABLE);
        db.execSQL(CREATE_CACHED_RESPONSES_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INTERACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CACHED_RESPONSES);
        
        // Create tables again
        onCreate(db);
    }
    
    // --- INTERACTION METHODS ---
    
    /**
     * Add a new interaction to the database
     */
    public long addInteraction(Interaction interaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_TIMESTAMP, DATE_FORMAT.format(interaction.getTimestamp()));
        values.put(KEY_EMOTION, interaction.getDetectedEmotion());
        values.put(KEY_PICKUP_LINE, interaction.getPickupLine());
        values.put(KEY_SUCCESSFUL, interaction.isWasSuccessful() ? 1 : 0);
        values.put(KEY_NOTES, interaction.getNotes());
        values.put(KEY_IMAGE_PATH, interaction.getImageFilePath());
        
        // Insert row
        long id = db.insert(TABLE_INTERACTIONS, null, values);
        db.close();
        
        return id;
    }
    
    /**
     * Get a single interaction by ID
     */
    public Interaction getInteraction(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
                TABLE_INTERACTIONS,
                new String[]{KEY_ID, KEY_TIMESTAMP, KEY_EMOTION, KEY_PICKUP_LINE, KEY_SUCCESSFUL, KEY_NOTES, KEY_IMAGE_PATH},
                KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);
        
        if (cursor != null)
            cursor.moveToFirst();
        
        Interaction interaction = null;
        
        if (cursor != null && !cursor.isAfterLast()) {
            interaction = new Interaction();
            interaction.setId(cursor.getLong(0));
            
            try {
                interaction.setTimestamp(DATE_FORMAT.parse(cursor.getString(1)));
            } catch (ParseException e) {
                interaction.setTimestamp(new Date());
            }
            
            interaction.setDetectedEmotion(cursor.getString(2));
            interaction.setPickupLine(cursor.getString(3));
            interaction.setWasSuccessful(cursor.getInt(4) == 1);
            interaction.setNotes(cursor.getString(5));
            interaction.setImageFilePath(cursor.getString(6));
            
            cursor.close();
        }
        
        db.close();
        return interaction;
    }
    
    /**
     * Get all interactions
     */
    public List<Interaction> getAllInteractions() {
        List<Interaction> interactionList = new ArrayList<>();
        
        String selectQuery = "SELECT * FROM " + TABLE_INTERACTIONS + " ORDER BY " + KEY_TIMESTAMP + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                Interaction interaction = new Interaction();
                interaction.setId(cursor.getLong(0));
                
                try {
                    interaction.setTimestamp(DATE_FORMAT.parse(cursor.getString(1)));
                } catch (ParseException e) {
                    interaction.setTimestamp(new Date());
                }
                
                interaction.setDetectedEmotion(cursor.getString(2));
                interaction.setPickupLine(cursor.getString(3));
                interaction.setWasSuccessful(cursor.getInt(4) == 1);
                interaction.setNotes(cursor.getString(5));
                interaction.setImageFilePath(cursor.getString(6));
                
                interactionList.add(interaction);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return interactionList;
    }
    
    /**
     * Update an interaction
     */
    public int updateInteraction(Interaction interaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, DATE_FORMAT.format(interaction.getTimestamp()));
        values.put(KEY_EMOTION, interaction.getDetectedEmotion());
        values.put(KEY_PICKUP_LINE, interaction.getPickupLine());
        values.put(KEY_SUCCESSFUL, interaction.isWasSuccessful() ? 1 : 0);
        values.put(KEY_NOTES, interaction.getNotes());
        values.put(KEY_IMAGE_PATH, interaction.getImageFilePath());
        
        // Update row
        int result = db.update(TABLE_INTERACTIONS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(interaction.getId())});
        
        db.close();
        return result;
    }
    
    /**
     * Delete an interaction
     */
    public void deleteInteraction(Interaction interaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_INTERACTIONS, KEY_ID + " = ?",
                new String[]{String.valueOf(interaction.getId())});
        db.close();
    }
    
    /**
     * Get count of successful interactions
     */
    public int getSuccessfulInteractionsCount() {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_INTERACTIONS + " WHERE " + KEY_SUCCESSFUL + " = 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        
        int count = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            count = cursor.getInt(0);
            cursor.close();
        }
        
        db.close();
        return count;
    }
    
    // --- CACHED RESPONSE METHODS ---
    
    /**
     * Add a cached response
     */
    public long addCachedResponse(String imageHash, String response) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_IMAGE_HASH, imageHash);
        values.put(KEY_RESPONSE, response);
        values.put(KEY_TIMESTAMP, DATE_FORMAT.format(new Date()));
        
        // Insert row
        long id = db.insert(TABLE_CACHED_RESPONSES, null, values);
        db.close();
        
        return id;
    }
    
    /**
     * Get cached response by image hash
     */
    public String getCachedResponse(String imageHash) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
                TABLE_CACHED_RESPONSES,
                new String[]{KEY_RESPONSE},
                KEY_IMAGE_HASH + "=?",
                new String[]{imageHash},
                null, null, null, null);
        
        String response = null;
        
        if (cursor != null && cursor.moveToFirst()) {
            response = cursor.getString(0);
            cursor.close();
        }
        
        db.close();
        return response;
    }
    
    /**
     * Clean up old cached responses (older than 30 days)
     */
    public void cleanupOldCachedResponses() {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Get timestamp 30 days ago
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        String thirtyDaysAgo = DATE_FORMAT.format(cal.getTime());
        
        db.delete(TABLE_CACHED_RESPONSES, KEY_TIMESTAMP + " < ?", new String[]{thirtyDaysAgo});
        db.close();
    }
}