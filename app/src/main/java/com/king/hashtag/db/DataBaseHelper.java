package com.king.hashtag.db;

/**
 * Created by asus on 05/04/2018.
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.king.hashtag.module.Categorie;
import com.king.hashtag.module.Tags;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String DBNAME = "odb.sqlite";
    public static String DBLOCATION ;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public DataBaseHelper(Context context) {
        super(context, DBNAME, null, 1);
        this.mContext = context;
    }

    public static void setmDatabase(Context context){
        DBLOCATION = "/data/data/"+context.getPackageName()+"/databases/";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void openDatabase() {
        String dbPath = mContext.getDatabasePath(DBNAME).getPath();
        if(mDatabase != null && mDatabase.isOpen()) {
            return;
        }
        mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public void closeDatabase() {
        if(mDatabase!=null) {
            mDatabase.close();
        }
    }

    public List<Categorie> getCategories() {
        Categorie item = null;
        List<Categorie> itemList = new ArrayList<>();
        openDatabase();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM categorie", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            item = new Categorie(cursor.getInt(0), cursor.getString(1));
            itemList.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        closeDatabase();
        return itemList;
    }

    public List<Tags> getTagsGroupeFilterByIdCat(int id_cat) {
        Tags item = null;
        List<Tags> itemList = new ArrayList<>();
        openDatabase();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM tags where id_cat="+id_cat, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            item = new Tags(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
            itemList.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        closeDatabase();
        return itemList;
    }


}