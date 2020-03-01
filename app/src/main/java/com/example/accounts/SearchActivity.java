package com.example.accounts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

@SuppressWarnings("SameParameterValue")
public class SearchActivity extends AppCompatActivity {
    private ArrayList<Account> listAccount;
    private User usr;
    private LinearLayout lay;
    private TextView elemFind;
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        User owner = (User) (Objects.requireNonNull(getIntent().getExtras())).get("owner");
        lay = findViewById(R.id.contentSearchLayout);
        EditText name = findViewById(R.id.nameSerched);
        elemFind = findViewById(R.id.elemFind);
        ManageUser mngUsr = new ManageUser();
        ArrayList<User> listUser = mngUsr.deserializationListUser(this);
        usr = mngUsr.findUser(listUser, Objects.requireNonNull(owner).getUser());
        if (usr != null) {
            listAccount = null;
            name.addTextChangedListener(new TextWatcher() {
                @SuppressLint("SetTextI18n")
                @Override
                public void afterTextChanged(Editable s) {
                    lay.removeAllViews();
                    i = 0;
                    if (!s.toString().equals("")) {
                        for (final Account a : increasing(listAccount)) {
                            if (a.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                                i++;
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                lp.setMargins(0, 10, 0, 10);
                                Button myButton = new Button(SearchActivity.this);
                                myButton.setText(a.getName());
                                myButton.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                                myButton.setBackground(getDrawable(R.drawable.rounded_color));
                                lay.addView(myButton, lp);
                                myButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        goToShowElementActivity(usr, a);
                                    }
                                });
                            }
                        }
                    }
                    elemFind.setText("Numero account trovati: " + i);

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });

        } else {
            notifyUser("Utente non rilevato. Impossibile visualizzare la lista degli account.");
            goToMainActivity();
        }
    }

    public void goToMainActivity() {
        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void goToShowElementActivity(User usr, Account acc) {
        Intent intent = new Intent(SearchActivity.this, ShowElementActivity.class);
        intent.putExtra("account", acc);
        intent.putExtra("owner", usr);
        startActivity(intent);
    }

    private void notifyUser(String message) {
        Toast.makeText(this,
                message,
                Toast.LENGTH_LONG).show();
    }

    public ArrayList<Account> increasing(ArrayList<Account> list) {
        Collections.sort(list, new Comparator<Account>() {
            @Override
            public int compare(Account lhs, Account rhs) {
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            }
        });
        return list;
    }
}
