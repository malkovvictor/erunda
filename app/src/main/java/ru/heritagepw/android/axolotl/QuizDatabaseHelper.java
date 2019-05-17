package ru.heritagepw.android.axolotl;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class QuizDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "questions.db";
    private static final int DB_VERSION = 18;
    private final Context myContext;

    public QuizDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.myContext = context;
    }

    //Copies your database from your local assets-folder to the just created empty database in the system folder
    private void installDatabase() throws IOException
    {
        Log.v("DB Create", "begin copy database");
        InputStream mInput = myContext.getAssets().open(DB_NAME);
        Log.v("DB Create", myContext.getDatabasePath(DB_NAME).getPath());
        OutputStream mOutput = new FileOutputStream(myContext.getDatabasePath(DB_NAME).getPath());
        byte[] mBuffer = new byte[2024];
        int mLength;
        int total = 0;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
            total += mLength;
        }
        Log.v("DB create", "Total bytes: " + total);
        mOutput.flush();
        mOutput.close();
        mInput.close();
        getPref().edit().putInt(DB_NAME, DB_VERSION).apply();
        Log.v("DB Create", "end copy database, db version: " + DB_VERSION);
    }

    private SharedPreferences getPref() {
        return  myContext.getSharedPreferences(myContext.getPackageName() + ".database_version", Context.MODE_PRIVATE);
    }

    private boolean isOutdated() {
        Log.v("DB create", "Current db version: " + getPref().getInt(DB_NAME, 0) + ", desired: " + DB_VERSION);
        return getPref().getInt(DB_NAME, 0) < DB_VERSION;
    }

    synchronized private void installOrUpdateIfNecessary() {
        if (isOutdated()) {
            try {
                myContext.deleteDatabase(DB_NAME);
                installDatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        installOrUpdateIfNecessary();
        return super.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        installOrUpdateIfNecessary();
        return super.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // nothing
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing
    }
}
