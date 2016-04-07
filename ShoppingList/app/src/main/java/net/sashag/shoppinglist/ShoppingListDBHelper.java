package net.sashag.shoppinglist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ShoppingListDBHelper extends SQLiteOpenHelper {

    public ShoppingListDBHelper(Context context) {
        super(context, "shopping_db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table shopping_list (_id integer primary key autoincrement, " +
                   "title text not null, amount integer not null, " +
                   "due_date integer not null, purchased integer not null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do yet
    }
}
