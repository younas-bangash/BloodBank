package com.blood.donor.patient.bank;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

class DatabaseHandler extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 10;
    // Database Name
    private static final String DATABASE_NAME = "BloodDonationApp";
    // Contacts table name
    private static final String TABLE_NAME = "blooddonationdata";
    // Contacts Table Columns names
    private static final String KEY_ID    = "id";
    private static final String KEY_FNAME = "fname";
    private static final String KEY_LNAME = "lname";
    private static final String KEY_PH_NO = "phone_number";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_BG    = "bloodgroup";
    private static final String KEY_CAT   = "category";
    private static final String KEY_LOC   = "location";
    private static final String KEY_MAPID   = "mapid";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_FNAME + " TEXT,"
                + KEY_LNAME + " TEXT,"
                + KEY_PH_NO + " TEXT,"
                + KEY_EMAIL + " TEXT,"
                + KEY_BG + " TEXT,"
                + KEY_CAT + " TEXT,"
                + KEY_LOC + " TEXT,"
                + KEY_MAPID + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);

    }
    // Adding new contact
    void addContact(GetSetData contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FNAME, contact.getmFirstName());
        values.put(KEY_LNAME, contact.getmLastName());
        values.put(KEY_PH_NO, contact.getmContact());
        values.put(KEY_EMAIL, contact.getmEmail());
        values.put(KEY_BG, contact.getmBloodGroup());
        values.put(KEY_CAT, contact.getmCatagory());
        values.put(KEY_MAPID, contact.getmMapID());
        values.put(KEY_LOC, contact.getmLoc());
        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }


    // Getting All Contacts
    public List<GetSetData> getAllContacts(int mSearchKey) {
        List<GetSetData> contactList = new ArrayList<>();
        // Select All Query
        String selectQuery=null;
        if(mSearchKey==2) //select all user
           selectQuery = "SELECT  * FROM " + TABLE_NAME ;
        else if(mSearchKey==1) //select all user patient
            selectQuery = "SELECT  * FROM " + TABLE_NAME +" where "+ KEY_CAT +"='Blood Patient'";
        else if(mSearchKey==0) //select all user donar
            selectQuery = "SELECT  * FROM " + TABLE_NAME +" where "+ KEY_CAT +"='Blood Donor'";

        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                GetSetData contact = new GetSetData();
                contact.setmRecodeID(""+Integer.parseInt(cursor.getString(0)));
                contact.setmFirstName(cursor.getString(1));
                contact.setmLastName(cursor.getString(2));
                contact.setmContact(cursor.getString(3));
                contact.setmEmail(cursor.getString(4));
                contact.setmBloodGroup(cursor.getString(5));
                contact.setmCatagory(cursor.getString(6));
                contact.setmLoc(cursor.getString(7));
                contact.setmMapID(cursor.getString(8));
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        return contactList;
    }
}
