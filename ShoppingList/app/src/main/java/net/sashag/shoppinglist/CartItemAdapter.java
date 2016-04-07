package net.sashag.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class CartItemAdapter extends BaseAdapter {

    private Context context;
    private SQLiteDatabase db;
    private Cursor cursor;
    private String filter;

    public CartItemAdapter(Context context) {
        this.context = context;
        db = new ShoppingListDBHelper(context).getWritableDatabase();
        reload();
    }

    private void reload() {
        String where = null;
        if (filter != null && filter.length() > 0) {
            where = "title LIKE '%" + filter + "%'";
        }

        cursor = db.query("shopping_list",
                new String[]{"_id", "title", "amount", "due_date", "purchased"},
                where, null, null, null, null);
        notifyDataSetChanged();
    }

    public void add(CartItem object) {
        ContentValues values = new ContentValues();
        values.put("title", object.getTitle());
        values.put("amount", object.getAmount());
        values.put("due_date", object.getDueDate().getTime());
        values.put("purchased", object.isPurchased() ? 1 : 0);
        long id = db.insert("shopping_list", null, values);
        object.setDatabaseId(id);
        reload();
    }

    public void remove(CartItem object) {
        db.delete("shopping_list", "_id = ?",
                new String[]{Long.toString(object.getDatabaseId())});
        reload();
    }

    public void clear() {
        db.delete("shopping_list", null, null);
        reload();
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public CartItem getItem(int position) {
        cursor.moveToPosition(position);
        CartItem item = new CartItem(
                cursor.getString(cursor.getColumnIndexOrThrow("title")),
                cursor.getInt(cursor.getColumnIndexOrThrow("amount"))
        );
        item.setDueDate(new Date(
                cursor.getLong(cursor.getColumnIndexOrThrow("due_date"))
        ));
        item.setPurchased(
                cursor.getInt(cursor.getColumnIndexOrThrow("purchased")) == 1
        );
        item.setDatabaseId(
                cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
        );
        return item;
    }

    @Override
    public long getItemId(int position) {
        cursor.moveToPosition(position);
        return cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = LayoutInflater.from(context).inflate(
                    R.layout.cart_item_row, parent, false
            );
        }
        populateRow(row, getItem(position));
        return row;
    }

    public void markPurchased(CartItem item) {
        item.setPurchased(true);
        ContentValues values = new ContentValues();
        values.put("purchased", 1);
        db.update("shopping_list", values, "_id = ?",
                new String[] { Long.toString(item.getDatabaseId()) });
        reload();
    }

    private void populateRow(View row, final CartItem item) {
        CheckBox titleAndPurchased = (CheckBox) row.findViewById(R.id.title_and_purchased);
        titleAndPurchased.setOnCheckedChangeListener(null);
        String format = context.getResources().getString(R.string.item_row_format);
        titleAndPurchased.setText(String.format(format, item.getAmount(), item.getTitle()));
        titleAndPurchased.setChecked(item.isPurchased());
        titleAndPurchased.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                markPurchased(item);
            }
        });

        TextView dueDate = (TextView) row.findViewById(R.id.due_date);
        dueDate.setText(
                DateFormat.getDateInstance(DateFormat.SHORT).format(item.getDueDate()));
        if (item.getDueDate().before(new Date())) {
            dueDate.setTextColor(Color.RED);
        } else {
            dueDate.setTextColor(Color.DKGRAY);
        }
    }

    public void markAllPurchased() {
        ContentValues values = new ContentValues();
        values.put("purchased", 1);
        db.update("shopping_list", values, null, null);
        reload();
    }

    public void update(CartItem object) {
        ContentValues values = new ContentValues();
        values.put("title", object.getTitle());
        values.put("amount", object.getAmount());
        values.put("due_date", object.getDueDate().getTime());
        values.put("purchased", object.isPurchased() ? 1 : 0);
        db.update("shopping_list", values, "_id = ?",
                new String[]{Long.toString(object.getDatabaseId())});
        reload();
    }

    public void setFilter(String query) {
        this.filter = query;
        reload();
    }
}
