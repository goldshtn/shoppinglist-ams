package net.sashag.shoppinglist;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int EDIT_ITEM_REQUEST_CODE = 42;

    private CartItemAdapter itemsAdapter;
    private int currentContextualPosition = -1;
    private BroadcastReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupList();
        findViewById(R.id.add_to_list).setOnClickListener(this);
        countLaunches();
        handleSearchIntent(getIntent());
    }

    private void countLaunches() {
        SharedPreferences prefs = getSharedPreferences("launches", MODE_PRIVATE);
        int launchCount = prefs.getInt("launch_count", 0);
        if (launchCount == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Welcome!")
                    .setMessage("Thank you for using our amazing app!")
                    .setPositiveButton("Dismiss", null)
                    .create()
                    .show();
        } else if (launchCount % 5 == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Hey...")
                    .setMessage("Would you mind rating us on Google Play? We are really desperate.")
                    .setPositiveButton("Okay", null)
                    .create()
                    .show();
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("launch_count", launchCount + 1);
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkForPermission();
    }

    private void registerSmsReceiver() {
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                parseSmsAndAddItem(intent);
            }
        };
        registerReceiver(smsReceiver,
                new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    private void checkForPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECEIVE_SMS },
                    42);
        } else {
            registerSmsReceiver();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 42 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                permissions[0].equals(Manifest.permission.RECEIVE_SMS)) {
            registerSmsReceiver();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void parseSmsAndAddItem(Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras == null)
            return;

        Object[] smses = (Object[]) intentExtras.get("pdus");
        if (smses == null)
            return;

        for (Object sms : smses) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms);
            String smsBody = smsMessage.getMessageBody();
            itemsAdapter.add(new CartItem(smsBody, 1));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
    }

    private ActionMode.Callback contextModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.context_menu, menu);
            mode.setTitle(itemsAdapter.getItem(currentContextualPosition).getTitle());
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            CartItem cartItem = itemsAdapter.getItem(currentContextualPosition);
            switch (item.getItemId()) {
                case R.id.menu_item_mark_purchased:
                    itemsAdapter.markPurchased(cartItem);
                    break;
                case R.id.menu_item_delete:
                    itemsAdapter.remove(cartItem);
                    break;
                case R.id.menu_item_edit:
                    Intent intent = new Intent(MainActivity.this, EditCartItemActivity.class);
                    intent.putExtra(EditCartItemActivity.EXTRA_ITEM, cartItem);
                    startActivityForResult(intent, EDIT_ITEM_REQUEST_CODE);
                    break;
            }
            mode.finish();
            currentContextualPosition = -1;
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_ITEM_REQUEST_CODE && resultCode == RESULT_OK) {
            CartItem item = (CartItem) data.getSerializableExtra(EditCartItemActivity.EXTRA_ITEM);
            itemsAdapter.update(item);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupList() {
        ListView shoppingList = (ListView) findViewById(R.id.shopping_list);
        itemsAdapter = new CartItemAdapter(this);
        shoppingList.setAdapter(itemsAdapter);
        // To learn more about using the contextual action mode, consult
        // the developer guide: http://developer.android.com/guide/topics/ui/menus.html
        shoppingList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                currentContextualPosition = position;
                startSupportActionMode(contextModeCallback);
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        EditText itemToBuy = (EditText) findViewById(R.id.item_to_buy);
        String itemTitle = itemToBuy.getText().toString();
        CartItem newItem = new CartItem(itemTitle, 1);
        itemsAdapter.add(newItem);
        itemToBuy.getText().clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        setupSearch(menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setupSearch(Menu menu) {
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        SearchView searchView =
                (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                itemsAdapter.setFilter(null);
                return true;
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSearchIntent(intent);
    }

    private void handleSearchIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            itemsAdapter.setFilter(query);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                // Will be implemented in a later lab
                break;
            case R.id.menu_item_mark_all_purchased:
                markAllPurchasedAfterConfirming();
                break;
            case R.id.menu_item_share:
                startShare();
                break;
            case R.id.menu_item_about:
                startActivity(new Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://www.hpe.com")));
        }
        return super.onOptionsItemSelected(item);
    }

    private void markAllPurchasedAfterConfirming() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.are_you_sure)
                .setMessage(R.string.confirm_mark_all_purchased)
                .setIcon(android.R.drawable.ic_menu_edit)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemsAdapter.markAllPurchased();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    private void startShare() {
        StringBuilder shareString = new StringBuilder();
        for (int i = 0; i < itemsAdapter.getCount(); ++i) {
            CartItem item = itemsAdapter.getItem(i);
            shareString.append(item.toString()).append("\n");
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareString.toString());
        startActivity(Intent.createChooser(
                intent, getString(R.string.share_shopping_list)));
    }
}
