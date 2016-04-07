package net.sashag.shoppinglist;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.ExecutableQuery;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CartItemAdapter extends BaseAdapter {

    private Context context;
    private MobileServiceTable<CartItem> table;
    private MobileServiceList<CartItem> results;
    private String filter;

    public CartItemAdapter(Context context, MobileServiceClient mobileServiceClient) {
        this.context = context;
        this.table = mobileServiceClient.getTable(CartItem.class);
        reload();
    }

    private void showError(String error, Throwable throwable) {
        Toast.makeText(context, error + "\n" + throwable.toString(), Toast.LENGTH_LONG).show();
    }

    private void reload() {
        ExecutableQuery<CartItem> query;
        if (filter != null && filter.length() > 0) {
            query = table.where()
                         .startsWith("title", filter)
                         .orderBy("dueDate", QueryOrder.Ascending);
        } else {
            query = table.orderBy("dueDate", QueryOrder.Ascending);
        }
        Futures.addCallback(
                query.execute(),
                new FutureCallback<MobileServiceList<CartItem>>() {
                    @Override
                    public void onSuccess(MobileServiceList<CartItem> result) {
                        results = result;
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        showError("Error executing query", t);
                    }
                }
        );
    }

    public void add(CartItem object) {
        Futures.addCallback(
                table.insert(object),
                new FutureCallback<CartItem>() {
                    @Override
                    public void onSuccess(CartItem result) {
                        reload();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        showError("Error inserting item", t);
                    }
                }
        );
    }

    public void remove(CartItem object) {
        Futures.addCallback(
                table.delete(object),
                new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        reload();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        showError("Error deleting item", t);
                    }
                }
        );
    }

    @Override
    public int getCount() {
        if (results == null) {
            return 0;
        }
        return results.getTotalCount();
    }

    @Override
    public CartItem getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
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
        update(item);
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
        ArrayList<ListenableFuture<CartItem>> futures = new ArrayList<>();
        for (CartItem item : results) {
            item.setPurchased(true);
            futures.add(table.update(item));
        }
        Futures.addCallback(
                Futures.allAsList(futures),
                new FutureCallback<List<CartItem>>() {
                    @Override
                    public void onSuccess(List<CartItem> result) {
                        reload();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        showError("Error marking all as purchased", t);
                    }
                }
        );
    }

    public void update(CartItem object) {
        Futures.addCallback(
                table.update(object),
                new FutureCallback<CartItem>() {
                    @Override
                    public void onSuccess(CartItem result) {
                        reload();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        showError("Error updating item", t);
                    }
                }
        );
    }

    public void setFilter(String query) {
        this.filter = query;
        reload();
    }
}
