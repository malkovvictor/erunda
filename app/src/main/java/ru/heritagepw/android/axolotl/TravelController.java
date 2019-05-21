package ru.heritagepw.android.axolotl;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TravelController {
    public static final int STEP = 10;

    private Context mContext;
    private QuizDatabaseHelper dbh;


    public TravelController(Context context) {
        mContext = context;
        dbh = new QuizDatabaseHelper(context);
    }

    public String getCityName(int id) {
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor cur = db.rawQuery("select name from cities where id=?", new String[] {Integer.toString(id)});
        if (cur.moveToFirst()) {
            return cur.getString(cur.getColumnIndex("name"));
        }
        return "";
    }

    public int getSource() {
        return mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).getInt("source", 1);
    }

    public int getDest() {
        return mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).getInt("dest", -1);
    }

    public int getSourceScore() {
        return mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).getInt("from", 0);
    }

    public int getDestScore() {
        return mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).getInt("to", getSourceScore() + STEP);
    }

    public CityPhoto getCityViewByCity() {
        return CityPhoto.loadRandom(dbh.getReadableDatabase(), this);
    }

    public CityPhoto getCityView(int id, int factId) {
        return CityPhoto.load(dbh.getReadableDatabase(), this, id, factId);
    }

    public void chooseRoad() {
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor cur = db.rawQuery("select * from roads where cityA=? order by random() limit 1", new String[] {Integer.toString(getSource())});
        cur.moveToFirst();
        int cityB = cur.getInt(cur.getColumnIndex("cityB"));
        mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).edit().putInt("dest", cityB).apply();
        Integer step = cur.getInt(cur.getColumnIndex("cost"));
        if (step == null || step == 0) {
            step = STEP;
        }
        int toScore = getSourceScore() + step;
        mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).edit().putInt("to", toScore).apply();
    }

    public void arrive() {
        SharedPreferences sp = mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE);

        int dest = sp.getInt("dest", 1);
        int to = sp.getInt("to", 1);
        sp.edit()
                .putInt("source", dest)
                .putInt("dest", -1)
                .putInt("from", to)
                .putInt("to", -1)
                .apply();

    }
}
