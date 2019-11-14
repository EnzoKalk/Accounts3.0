package com.example.accounts;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;

    private RelativeLayout lay;
    private EditText userApp;
    private ImageView userError;
    private EditText passApp;
    private ImageView passError;
    private Button login;
    private TextView sign;
    private Switch flagApp;
    private String path;
    private ManageApp mngApp;
    private ManageUser mngUsr;
    private ArrayList<User> listUser = new ArrayList<>();
    private CancellationSignal cancellationSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lay = findViewById(R.id.relLayMain);
        path = getExternalStorageDirectory().getAbsolutePath();
        userApp = findViewById(R.id.userApp);
        userError = findViewById(R.id.userError);
        passApp = findViewById(R.id.passApp);
        passError = findViewById(R.id.passError);
        login = findViewById(R.id.authButton);
        sign = findViewById(R.id.signText);
        flagApp = findViewById(R.id.flagApp);

        if (!checkPermission()) {
            openActivity();
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                openActivity();
            }
        }


        mngApp = new ManageApp();
        LogApp log = mngApp.deserializationFlag(path);

        mngUsr = new ManageUser();
        listUser = mngUsr.deserializationListUser(path);

        if (log.getFlagApp() == true) {
            flagApp.setChecked(true);
            User usr = new User();
            for (User u : listUser) {
                if (u.getPriority() == true) usr = u;
            }
            if (usr.getFinger() == true) {
                if (!checkBiometricSupport()) {
                    return;
                }
                ;
            }
            Intent intent = new Intent(MainActivity.this, ViewActivity.class);
            intent.putExtra("path", path);
            intent.putExtra("owner", usr.getUser());
            startActivity(intent);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View view) {
                userError.setVisibility(View.INVISIBLE);
                passError.setVisibility(View.INVISIBLE);
                User usr = new User(userApp.getText().toString(), passApp.getText().toString(), false, false);
                if (!fieldCheck(usr)) return;
                if (mngUsr.login(usr, listUser)) {
                    for (User u : listUser) if (u.getUser().equals(usr.getUser())) usr = u;
                    if (usr.getFinger() == true) {
                        if (!checkBiometricSupport()) {
                            return;
                        }
                        authenticateUser(lay);
                    }

                } else {
                    userError.setVisibility(View.VISIBLE);
                    passError.setVisibility(View.VISIBLE);
                    Snackbar.make(view, "Autenticazione errata", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignActivity.class);
                intent.putExtra("path", path);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean fieldCheck(User usr) {
        if (!isValidWord(usr.getUser()) && !isValidWord(usr.getPassword())) {
            userError.setVisibility(View.VISIBLE);
            passError.setVisibility(View.VISIBLE);
            Snackbar.make(lay, "Campi Utente e Password non validi !!!", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (!isValidWord(usr.getUser())) {
            userError.setVisibility(View.VISIBLE);
            Snackbar.make(lay, "Campo Utente non valido !!!", Snackbar.LENGTH_LONG).show();
            return false;
        } else if (!isValidWord(usr.getPassword())) {
            passError.setVisibility(View.VISIBLE);
            Snackbar.make(lay, "Campo Password non valido !!!", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean isValidWord(String word) {
        return ((word.matches("[A-Za-z0-9?!_.-]*")) && (!word.isEmpty()));
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle(getString(R.string.permission_necessary));
                alertBuilder.setMessage(R.string.storage_permission_is_encessary_to_wrote_event);
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        } else {
            openActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults.length > 0) {
                boolean flag = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        flag = false;
                    }
                }
                if (flag) {
                    openActivity();
                } else {
                    finish();
                }
            } else {
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openActivity() {
        //add your further process after giving permission or to download images from remote server.
    }

    private void notifyUser(String message) {
        Toast.makeText(this,
                message,
                Toast.LENGTH_LONG).show();
    }

    private Boolean checkBiometricSupport() {
        KeyguardManager keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        PackageManager packageManager = this.getPackageManager();
        if (!keyguardManager.isKeyguardSecure()) {
            notifyUser("Lock screen security not enabled in Settings");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            notifyUser("Fingerprint authentication permission not enabled");
            return false;
        }
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            return true;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private BiometricPrompt.AuthenticationCallback getAuthenticationCallback() {

        return new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                notifyUser("Autenticazione errore: " + errString);
                super.onAuthenticationError(errorCode, errString);
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                notifyUser("Autenticazione effettuata");
                super.onAuthenticationSucceeded(result);
                if (flagApp.isChecked()) {
                    for (User u : listUser) {
                        if (u.getPriority() && (!u.getUser().equals(userApp.getText().toString()))) {
                            User us = u;
                            us.setPriority(false);
                            listUser.remove(u);
                            listUser.add(us);
                        }
                        if (u.getUser().equals(userApp.getText().toString())) {
                            User usr = new User(userApp.getText().toString(), passApp.getText().toString(), true, true);
                            listUser.remove(u);
                            listUser.add(usr);
                        }
                    }
                }
                mngUsr.serializationListUser(listUser, path);
                LogApp log = new LogApp(flagApp.isChecked());
                mngApp.serializationFlag(log, path);
                Intent intent = new Intent(MainActivity.this, ViewActivity.class);
                intent.putExtra("path", path);
                intent.putExtra("owner", userApp.getText().toString());
                startActivity(intent);
            }
        };
    }

    private CancellationSignal getCancellationSignal() {
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                notifyUser("Cancelled via signal");
            }
        });
        return cancellationSignal;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void authenticateUser(View view) {
        BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(this)
                .setTitle("Lettura impronta digitale")
                .setSubtitle("Autenticazione richiesta per continuare")
                .setDescription("Account con autenticazione biometrica per proteggere i dati.")
                .setNegativeButton("Annulla", this.getMainExecutor(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                notifyUser("Autenticazione annullata");
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                .build();
        biometricPrompt.authenticate(getCancellationSignal(), getMainExecutor(), getAuthenticationCallback());
    }

}

