package com.example.tushar.greedygames.provider;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tushar on 19/4/17.
 */

public class Database extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 123;
    public static final String DATABASE_NAME = "app.db";
    private Context mContext;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + Tables.LOCATIONS + "(" +
                AccountContract.LocationColumns.LATITUDE + " TEXT DEFAULT '0'," +
                AccountContract.LocationColumns.LONGITUDE + " TEXT DEFAULT '0'," +
                AccountContract.LocationColumns.TIME_STAMP + " TEXT DEFAULT '0'" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public interface Tables {
        String LOCATIONS = "loations";
    }
}
