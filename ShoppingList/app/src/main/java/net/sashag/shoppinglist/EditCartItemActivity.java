package net.sashag.shoppinglist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;

import java.util.Calendar;

public class EditCartItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM = "item";

    private CartItem editedCartItem;
    private EditText itemTitle;
    private SeekBar itemAmount;
    private DatePicker itemDueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cart_item);

        itemTitle = (EditText) findViewById(R.id.item_title);
        itemAmount = (SeekBar) findViewById(R.id.item_amount);
        itemDueDate = (DatePicker) findViewById(R.id.due_date);

        Intent intent = getIntent();
        editedCartItem = (CartItem) intent.getSerializableExtra(EXTRA_ITEM);

        modelToView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_save:
                viewToModel();
                Intent intent = new Intent();
                intent.putExtra(EXTRA_ITEM, editedCartItem);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void modelToView() {
        itemTitle.setText(editedCartItem.getTitle());
        itemAmount.setProgress(editedCartItem.getAmount());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(editedCartItem.getDueDate());
        itemDueDate.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void viewToModel() {
        editedCartItem.setTitle(itemTitle.getText().toString());
        editedCartItem.setAmount(itemAmount.getProgress());

        Calendar calendar = Calendar.getInstance();
        calendar.set(
                itemDueDate.getYear(),
                itemDueDate.getMonth(),
                itemDueDate.getDayOfMonth()
        );
        editedCartItem.setDueDate(calendar.getTime());
    }
}
