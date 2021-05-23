package tech.fedorov.fedchatclient.Memory;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

import tech.fedorov.fedchatclient.Messages.Message;

public class DBManager {
    /*
     * TABLES: ------- MESSAGES
     */
    private Context context;
    private String DB_NAME = "messages.db";

    private SQLiteDatabase db;

    private static DBManager dbManager;

    public static DBManager getInstance(Context context) {
        if (dbManager == null) {
            dbManager = new DBManager(context);
        }
        return dbManager;
    }

    private DBManager(Context context) {
        this.context = context;
        db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        createTablesIfNeedBe();
    }

    void addMessage(String username, String text, String time) {
        db.execSQL("INSERT INTO MESSAGES VALUES ('" + username + "', " + text + "', " + time
                + ");");
    }



    ArrayList<Message> getAllMessages() {
        ArrayList<Message> data = new ArrayList<Message>();
        Cursor cursor = db.rawQuery("SELECT * FROM MESSAGES;", null);
        boolean hasMoreData = cursor.moveToFirst();

        while (hasMoreData) {
            String name = cursor.getString(cursor.getColumnIndex("USERNAME"));
            String text = cursor.getString(cursor.getColumnIndex("TEXT"));
            String time = cursor.getString(cursor.getColumnIndex("TIME"));
            data.add(new Message(text, name, time));
            hasMoreData = cursor.moveToNext();
        }

        return data;
    }

    private void createTablesIfNeedBe() {
        db.execSQL("CREATE TABLE IF NOT EXISTS MESSAGES (USERNAME TEXT, TEXT TEXT, TIME TEXT);");
    }

    private boolean dbExist() {
        File dbFile = context.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }
}
