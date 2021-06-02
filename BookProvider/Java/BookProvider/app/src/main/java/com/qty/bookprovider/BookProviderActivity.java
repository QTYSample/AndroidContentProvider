package com.qty.bookprovider;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class BookProviderActivity extends AppCompatActivity implements IReportBack {

    private static final String TAG = "BookProvider";
    private ProviderTester providerTester;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        providerTester = new ProviderTester(this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        appendMenuItemText(item);
        switch (item.getItemId()) {
            case R.id.menu_add:
                providerTester.addBook();
                break;

            case R.id.menu_display_table:
                providerTester.showBooks();
                break;

            case R.id.menu_delete:
                providerTester.removeBook();
                break;

            case R.id.menu_update:
                providerTester.updateBook();
                break;

            case R.id.menu_da_clear:
                emptyText();
                break;
        }
        return true;
    }

    private TextView getTextView() {
        return (TextView) this.findViewById(R.id.text1);
    }

    protected void appendMenuItemText(MenuItem menuItem) {
        String title = menuItem.getTitle().toString();
        TextView tv = getTextView();
        tv.setText(tv.getText() + "\n" + title);
    }

    protected void emptyText() {
        TextView tv = getTextView();
        tv.setText("");
    }

    private void appendText(String s) {
        TextView tv = getTextView();
        tv.setText(tv.getText() + "\n" + s);
        Log.d(TAG, s);
    }

    public void reportBack(String tag, String message) {
        this.appendText(tag + ":" + message);
        Log.d(tag, message);
    }

    //Implement save/restore
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String st = savedInstanceState.getString("debugViewText");
        if (st == null) return;
        TextView tv = getTextView();
        tv.setText(st);
        Log.d(TAG, "Restored state");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //save state
        TextView tv = getTextView();
        String t = tv.getText().toString();
        outState.putString("debugViewText", t);
        Log.d(TAG, "Saved state");
    }

}