package ru.heritagepw.android.axolotl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CityPhoto {
    int id;
    int factId = -1;
    String filename;
    String copyrightLink;
    String copyrightAuthor;
    String copyrightLicense;
    String name;
    String fact;

    public CityPhoto(Integer id, String filename, String name, String copyrightLink, String copyrightAuthor, String copyrightLicense) {
        this.id = id;
        this.filename = filename;
        this.copyrightLink = copyrightLink;
        this.copyrightAuthor = copyrightAuthor;
        this.copyrightLicense = copyrightLicense;
        this.name = name;
    }

    public CityPhoto(Cursor cur) {
        this(
                cur.getInt(cur.getColumnIndex("id")),
                cur.getString(cur.getColumnIndex("photo")),
                cur.getString(cur.getColumnIndex("name")),
                cur.getString(cur.getColumnIndex("copyrightLink")),
                cur.getString(cur.getColumnIndex("copyrightAuthor")),
                cur.getString(cur.getColumnIndex("copyrightLicense"))
        );
    }

    public static CityPhoto loadRandom(SQLiteDatabase db, TravelController travelController) {
        Cursor cur = db.rawQuery("select * from cityPOI where city=? order by random() limit 1", new String[] {Integer.toString(travelController.getSource())});
        CityPhoto res = null;
        if (cur.moveToFirst()) {
            res = new CityPhoto(cur);
            Cursor cur2 = db.rawQuery("select * from poiFact where poi=?", new String[] {Integer.toString(res.id)});
            if (cur2.moveToFirst()) {
                res.fact = cur2.getString(cur2.getColumnIndex("fact"));
                res.factId = cur2.getInt(cur2.getColumnIndex("id"));
            }
        }
        return res;
    }

    public static CityPhoto load(SQLiteDatabase db,  TravelController tc, int id, int factId) {
        Cursor cur = db.rawQuery("select * from cityPOI where id=?", new String[] {Integer.toString(id)});
        CityPhoto res = null;
        if (cur.moveToFirst()) {
            res = new CityPhoto(cur);
            Cursor cur2 = db.rawQuery("select * from poiFact where id=?", new String[] {Integer.toString(factId)});
            if (cur2.moveToFirst()) {
                res.fact = cur2.getString(cur2.getColumnIndex("fact"));
                res.factId = cur2.getInt(cur2.getColumnIndex("id"));
            }
        }
        return res;
    }

    public String getCopyright() {
        return String.format("<a href=\"%s\">Image</a> © %s / %s", copyrightLink, copyrightAuthor, copyrightLicense);
    }

    public String getFilename() {
        return String.format("poi/%s", filename);
    }
}
