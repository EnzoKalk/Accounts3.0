package com.example.accounts;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;


public class ViewActivity extends AppCompatActivity {
    private String path;
    private String owner;
    private ManageUser mngUsr;
    private ArrayList<User> listUser = new ArrayList<>();
    private ArrayList<Account> listAccount;
    private ManageAccount mngAcc;
    private LogApp log;
    private ManageApp mngApp;
    private TableLayout viewAcc;
    private TableRow row;
    private TableRow.LayoutParams lp;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        Toolbar toolbarView = findViewById(R.id.toolbarView);
        setSupportActionBar(toolbarView);

        owner = getIntent().getExtras().getString("owner");
        path = getIntent().getExtras().getString("path");
        mngApp = new ManageApp();
        mngUsr = new ManageUser();
        mngAcc = new ManageAccount();

        listAccount = mngAcc.deserializationListAccount(path, owner);
        listUser = mngUsr.deserializationListUser(path);
        log = mngApp.deserializationFlag(path);

        toolbarView.setTitle("Benvenuto " + log.getUser());

        viewAcc = findViewById(R.id.viewAccount);
        row = new TableRow(this);
        lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        lp.setMargins(40, 10, 20, 10);
        row.setLayoutParams(lp);
        Button b = new Button(ViewActivity.this);
        String rounded  =  "drawable/rounded.xml";

        for (Account acc : listAccount) {
            row = new TableRow(this);
            row.setLayoutParams(lp);
            b.setText(acc.getName());
            b.setLayoutParams(lp);
            b.setTextSize(getResources().getDimension(R.dimen.list_names));
            b.setBackground (Drawable.createFromPath(rounded));
            row.addView(b);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {

        }
        if (id == R.id.action_sort) {

        }
        if (id == R.id.action_setting) {

        }
        if (id == R.id.action_exit) {
            log = new LogApp(false, " ");
            mngApp.serializationFlag(log, path);
            Intent intent = new Intent(ViewActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("path", path);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            System.exit(0);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Per favore clicca di nuovo BACK per uscire", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


}
