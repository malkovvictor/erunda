package ru.victormalkov.android.axolotl;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;

import java.io.IOException;

public class TravelController {
    private int defaultStep;

    private Context mContext;
    private QuizDatabaseHelper dbh;


    public TravelController(Context context) {
        mContext = context;
        dbh = new QuizDatabaseHelper(context);
        defaultStep = mContext.getResources().getInteger(R.integer.default_step);
    }

    public String getCityName(int id) {
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor cur = db.rawQuery("select name from cities where id=?", new String[] {Integer.toString(id)});
        if (cur.moveToFirst()) {
            String name = cur.getString(cur.getColumnIndex("name"));
            cur.close();
            return name;
        }
        cur.close();
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
        return mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).getInt("to", getSourceScore() + defaultStep);
    }

    public int getTerrain() {
        return mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).getInt("terrain", 1);
    }

    public CityPhoto getCityViewByCity() {
        return CityPhoto.loadRandom(dbh.getReadableDatabase(), this);
    }

    public CityPhoto getCityView(int id, int factId) {
        return CityPhoto.load(dbh.getReadableDatabase(), this, id, factId);
    }

    public RoadView getRoadView(int id) {
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor cur = db.rawQuery("select * from terrain where id=?", new String[] {Integer.toString(id)});
        if (cur.moveToFirst()) {
            String fileName = String.format("terrain/%s", cur.getString(cur.getColumnIndex("photo")));
            String credits = cur.getString(cur.getColumnIndex("credits"));
            Drawable d = null;
            try {
                d = Drawable.createFromStream(
                        mContext.getAssets().open(fileName),
                        fileName
                );
            } catch (IOException e) {

                e.printStackTrace();
            }
            cur.close();
            return new RoadView(d, credits);
        }
        cur.close();
        return null;
    }

    public RoadView getRoadView() {
        return getRoadView(this.getTerrain());
    }

    public void chooseRoad() {
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor cur = db.rawQuery("select * from roads where cityA=? order by random() limit 1", new String[] {Integer.toString(getSource())});
        cur.moveToFirst();
        int cityB = cur.getInt(cur.getColumnIndex("cityB"));
        mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).edit().putInt("dest", cityB).apply();
        int terrain = cur.getInt(cur.getColumnIndex("terrainType"));
        mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).edit().putInt("terrain", terrain).apply();
        Integer step = cur.getInt(cur.getColumnIndex("cost"));
        if (step == null || step == 0) {
            step = defaultStep;
        }
        int toScore = getSourceScore() + step;
        mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).edit().putInt("to", toScore).apply();
        cur.close();
    }

    public void arrive() {
        SharedPreferences sp = mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE);

        int dest = sp.getInt("dest", 1);
        int star = mContext.getSharedPreferences(mContext.getPackageName() + ".score", Context.MODE_PRIVATE).getInt("stars", getSourceScore());
        sp.edit()
                .putInt("source", dest)
                .putInt("dest", -1)
                .putInt("from", star)
                .putInt("to", -1)
                .apply();

    }
}
