package ru.victormalkov.android.axolotl;

import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

public class TravelController {
    public static final int ROAD_TYPE_TRAIN = 1;
    public static final int ROAD_TYPE_SHIP = 2;
    public static final int ROAD_TYPE_BUS = 3;
    public static final int ROAD_TYPE_DEER = 4;
    public static final int ROAD_TYPE_AIRPLANE = 5;


    private int defaultStep;

    private Context mContext;
    private QuizDatabaseHelper dbh;

    private Boolean eulaAccepted = null;
    private static TravelController mInstance = null;

    public static synchronized TravelController getInstance(Context context) {
        Log.v("TravelController", "Get tc for context " + context.toString());
        if (mInstance == null) {
            mInstance = new TravelController(context);
        }
        return mInstance;
    }

    private TravelController(Context context) {
        mContext = context;
        dbh = QuizDatabaseHelper.getInstance(context);
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

    public RoadImage getRoadView(int id) {
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
            return new RoadImage(d, credits);
        }
        cur.close();
        return null;
    }

    public RoadImage getRoadView() {
        return getRoadView(this.getTerrain());
    }

    public String getRoadSymbol() {
        int roadType = mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).getInt("roadType", 1);
        switch (roadType) {
            case ROAD_TYPE_TRAIN: return mContext.getString(R.string.train);
            case ROAD_TYPE_SHIP: return mContext.getString(R.string.ship);
            case ROAD_TYPE_DEER: return mContext.getString(R.string.deer);
            case ROAD_TYPE_BUS: return mContext.getString(R.string.bus);
            case ROAD_TYPE_AIRPLANE: return mContext.getString(R.string.airplane);
            default: return mContext.getString(R.string.train);
        }
    }

    public void chooseRoad() {
        SQLiteDatabase db = dbh.getReadableDatabase();
        SharedPreferences.Editor spe = mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE).edit();

        Cursor cur = db.rawQuery("select min(visits) as mv from roads inner join cities on roads.cityB=cities.id where roads.cityA=?", new String[] {Integer.toString(getSource())});
        cur.moveToFirst();
        int minimum = cur.getInt(cur.getColumnIndex("mv"));
        cur.close();

        //cur = db.rawQuery("select * from roads where cityA=? order by random() limit 1", new String[] {Integer.toString(getSource())});
        cur = db.rawQuery("select * from roads inner join cities on roads.cityB=cities.id where roads.cityA=? and visits=? order by random() limit 1", new String[] {Integer.toString(getSource()), Integer.toString(minimum)});
        cur.moveToFirst();
        int cityB = cur.getInt(cur.getColumnIndex("cityB"));
        spe.putInt("dest", cityB);
        int terrain = cur.getInt(cur.getColumnIndex("terrainType"));
        spe.putInt("terrain", terrain);
        int roadType = cur.getInt(cur.getColumnIndex("roadType"));
        spe.putInt("roadType", roadType);
        Integer step = cur.getInt(cur.getColumnIndex("cost"));
        if (step == null || step == 0) {
            step = defaultStep;
        }
        int toScore = getSourceScore() + step;
        spe.putInt("to", toScore);

        spe.apply();
        cur.close();
    }

    public void arrive() {
        SharedPreferences sp = mContext.getSharedPreferences(mContext.getPackageName() + ".travel", Context.MODE_PRIVATE);
        int dest = sp.getInt("dest", 1);
        int source = sp.getInt("source", 1);

        SQLiteDatabase db = dbh.getReadableDatabase();
        db.execSQL("update cities set visits=visits+1 where id=?", new Object[] {dest});

        int star = mContext.getSharedPreferences(mContext.getPackageName() + ".score", Context.MODE_PRIVATE).getInt("stars", getSourceScore());
        sp.edit()
                .putInt("previous", source)
                .putInt("source", dest)
                .putInt("dest", -1)
                .putInt("from", star)
                .putInt("to", -1)
                .apply();

    }

    public @Nullable Boolean isEulaAccepted() {
        if (eulaAccepted == null) {
            SharedPreferences sp = mContext.getSharedPreferences(mContext.getPackageName() + ".eula", Context.MODE_PRIVATE);
            if (sp != null && sp.contains("accepted")) {
                eulaAccepted = sp.getBoolean("accepted", false);
            }
        }
        return eulaAccepted;
    }

    public void doAcceptEula(boolean accept) {
        // must commit, not apply
        mContext.getSharedPreferences(mContext.getPackageName() + ".eula", Context.MODE_PRIVATE).edit().putBoolean("accepted", accept).commit();
        eulaAccepted = accept;
        Log.v("TravelController", String.format("eula accept set to %B", mContext.getSharedPreferences(mContext.getPackageName() + ".eula", Context.MODE_PRIVATE).getBoolean("accepted", false) ));
    }

    public void doLegalStuff(FragmentManager fragmentManager) {
        Boolean ea = this.isEulaAccepted();
        if (ea != null && !ea) {
            new EulaDialog().show(fragmentManager, "eula");
        }
    }
}
