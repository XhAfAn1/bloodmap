package edu.ewubd.bloodmap.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ewubd.bloodmap.ClassModels.BloodBankModel;
import edu.ewubd.bloodmap.ClassModels.HospitalContactModel;
import edu.ewubd.bloodmap.ClassModels.UserModel;

public class LocalDatabaseManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "rokto_dhara_cache.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_DONORS = "donors";
    private static final String TABLE_HOSPITALS = "hospitals";
    private static final String TABLE_BLOOD_BANKS = "blood_banks";

    private final Gson gson;

    public LocalDatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.gson = new Gson();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Donors Table
        String createDonors = "CREATE TABLE " + TABLE_DONORS + " (" +
                "uid TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "bloodGroup TEXT, " +
                "locationArea TEXT, " +
                "contactNumber TEXT, " +
                "latitude REAL, " +
                "longitude REAL, " +
                "totalDonations INTEGER, " +
                "availableToDonate INTEGER, " +
                "subscriptionPlan TEXT)";
        db.execSQL(createDonors);

        // Hospitals Table
        String createHospitals = "CREATE TABLE " + TABLE_HOSPITALS + " (" +
                "hospitalId TEXT PRIMARY KEY, " +
                "hospitalName TEXT, " +
                "contactNumber TEXT, " +
                "address TEXT, " +
                "latitude REAL, " +
                "longitude REAL, " +
                "facilities TEXT, " +
                "hasBloodBank INTEGER)";
        db.execSQL(createHospitals);

        // Blood Banks Table
        String createBloodBanks = "CREATE TABLE " + TABLE_BLOOD_BANKS + " (" +
                "bloodBankId TEXT PRIMARY KEY, " +
                "bankName TEXT, " +
                "contactNumber TEXT, " +
                "address TEXT, " +
                "latitude REAL, " +
                "longitude REAL, " +
                "stock TEXT, " +
                "isOpen24Hours INTEGER)";
        db.execSQL(createBloodBanks);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DONORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOSPITALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOOD_BANKS);
        onCreate(db);
    }

    // Donors Operations 

    public void syncDonors(List<UserModel> donors) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_DONORS, null, null);
            for (UserModel donor : donors) {
                ContentValues values = new ContentValues();
                values.put("uid", donor.getUid());
                values.put("name", donor.getName());
                values.put("bloodGroup", donor.getBloodGroup());
                values.put("locationArea", donor.getLocationArea());
                values.put("contactNumber", donor.getContactNumber());
                values.put("latitude", donor.getLatitude());
                values.put("longitude", donor.getLongitude());
                values.put("totalDonations", donor.getTotalDonations());
                values.put("availableToDonate", donor.isAvailableToDonate() ? 1 : 0);
                values.put("subscriptionPlan", donor.getSubscriptionPlan());
                db.insert(TABLE_DONORS, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<UserModel> getCachedDonors() {
        List<UserModel> donors = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DONORS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                UserModel user = new UserModel();
                user.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                user.setBloodGroup(cursor.getString(cursor.getColumnIndexOrThrow("bloodGroup")));
                user.setLocationArea(cursor.getString(cursor.getColumnIndexOrThrow("locationArea")));
                user.setContactNumber(cursor.getString(cursor.getColumnIndexOrThrow("contactNumber")));
                user.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
                user.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
                user.setTotalDonations(cursor.getInt(cursor.getColumnIndexOrThrow("totalDonations")));
                user.setAvailableToDonate(cursor.getInt(cursor.getColumnIndexOrThrow("availableToDonate")) == 1);
                user.setSubscriptionPlan(cursor.getString(cursor.getColumnIndexOrThrow("subscriptionPlan")));
                donors.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return donors;
    }

    // Hospitals Operations 

    public void syncHospitals(List<HospitalContactModel> hospitals) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_HOSPITALS, null, null);
            for (HospitalContactModel hospital : hospitals) {
                ContentValues values = new ContentValues();
                values.put("hospitalId", hospital.getHospitalId());
                values.put("hospitalName", hospital.getHospitalName());
                values.put("contactNumber", hospital.getContactNumber());
                values.put("address", hospital.getAddress());
                values.put("latitude", hospital.getLatitude());
                values.put("longitude", hospital.getLongitude());
                values.put("hasBloodBank", hospital.isHasBloodBank() ? 1 : 0);
                values.put("facilities", gson.toJson(hospital.getAvailableFacilities()));
                db.insert(TABLE_HOSPITALS, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<HospitalContactModel> getCachedHospitals() {
        List<HospitalContactModel> hospitals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HOSPITALS, null, null, null, null, null, null);

        Type listType = new TypeToken<ArrayList<String>>(){}.getType();

        if (cursor.moveToFirst()) {
            do {
                HospitalContactModel hospital = new HospitalContactModel();
                hospital.setHospitalId(cursor.getString(cursor.getColumnIndexOrThrow("hospitalId")));
                hospital.setHospitalName(cursor.getString(cursor.getColumnIndexOrThrow("hospitalName")));
                hospital.setContactNumber(cursor.getString(cursor.getColumnIndexOrThrow("contactNumber")));
                hospital.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                hospital.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
                hospital.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
                hospital.setHasBloodBank(cursor.getInt(cursor.getColumnIndexOrThrow("hasBloodBank")) == 1);
                
                String facilitiesJson = cursor.getString(cursor.getColumnIndexOrThrow("facilities"));
                hospital.setAvailableFacilities(gson.fromJson(facilitiesJson, listType));
                
                hospitals.add(hospital);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return hospitals;
    }

    //Blood Banks Operations

    public void syncBloodBanks(List<BloodBankModel> banks) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_BLOOD_BANKS, null, null);
            for (BloodBankModel bank : banks) {
                ContentValues values = new ContentValues();
                values.put("bloodBankId", bank.getBloodBankId());
                values.put("bankName", bank.getBankName());
                values.put("contactNumber", bank.getContactNumber());
                values.put("address", bank.getAddress());
                values.put("latitude", bank.getLatitude());
                values.put("longitude", bank.getLongitude());
                values.put("isOpen24Hours", bank.isOpen24Hours() ? 1 : 0);
                values.put("stock", gson.toJson(bank.getAvailableStock()));
                db.insert(TABLE_BLOOD_BANKS, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<BloodBankModel> getCachedBloodBanks() {
        List<BloodBankModel> banks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BLOOD_BANKS, null, null, null, null, null, null);

        Type mapType = new TypeToken<HashMap<String, Integer>>(){}.getType();

        if (cursor.moveToFirst()) {
            do {
                BloodBankModel bank = new BloodBankModel();
                bank.setBloodBankId(cursor.getString(cursor.getColumnIndexOrThrow("bloodBankId")));
                bank.setBankName(cursor.getString(cursor.getColumnIndexOrThrow("bankName")));
                bank.setContactNumber(cursor.getString(cursor.getColumnIndexOrThrow("contactNumber")));
                bank.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                bank.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
                bank.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
                bank.setOpen24Hours(cursor.getInt(cursor.getColumnIndexOrThrow("isOpen24Hours")) == 1);
                
                String stockJson = cursor.getString(cursor.getColumnIndexOrThrow("stock"));
                bank.setAvailableStock(gson.fromJson(stockJson, mapType));
                
                banks.add(bank);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return banks;
    }
}
