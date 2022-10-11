package com.example.deco3801project;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class WriteNFC extends AppCompatActivity {
    private PendingIntent pendingIntent;
    private NfcAdapter adapter;
    private IntentFilter[] writeFilter;
    private String[][] writeTechList;
    private Tag tag;
    private SharedPreferences pref;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent startIntent = getIntent();

        String masterKeyAlias = null;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.notify();
        }

        try {
            assert masterKeyAlias != null;
            pref = EncryptedSharedPreferences.create(
                    "secret_shared_prefs",
                    masterKeyAlias,
                    getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.notify();
        }

        setContentView(R.layout.activity_write_nfc);
        adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter == null) {
            Intent i = new Intent(WriteNFC.this, MainActivity2.class);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Toast.makeText(this, "Device not supported", Toast.LENGTH_LONG).show();
            startActivity(i);
        }
        writeFilter = new IntentFilter[]{};
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_MUTABLE
        );
        writeTechList = new String[][] {
                {Ndef.class.getName()},
                {NdefFormatable.class.getName()}
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.enableForegroundDispatch(this, pendingIntent,
                    writeFilter, writeTechList);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(this, "Tag detected", Toast.LENGTH_LONG).show();
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

    }

    public void handleWrite(View v) {
        NdefRecord mimeRecord =
                NdefRecord.createMime("application/vnd.com.example.deco3801project",
                "Android NFC".getBytes(StandardCharsets.US_ASCII));
        SwitchCompat switchView = findViewById(R.id.switch1);
        NdefRecord[] records;
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean("Bluetooth", switchView.isChecked());
        edit.apply();
        if (switchView.isChecked()) {
            TextView bluetooth = findViewById(R.id.editTextBluetooth);
            NdefRecord address = NdefRecord.createTextRecord("en",
                    bluetooth.getText().toString());
            records = new NdefRecord[]{
                    address,
                    mimeRecord
            };
        } else {
            records = new NdefRecord[]{
                    mimeRecord
            };
        }
        NdefMessage message = new NdefMessage(records);
        if (tag != null) {
            try {
                Ndef ndef = Ndef.get(tag);
                if (ndef == null) {
                    NdefFormatable ndefFormatable = NdefFormatable.get(tag);
                    if (ndefFormatable != null) {
                        ndefFormatable.connect();
                        ndefFormatable.format(message);
                        ndefFormatable.close();
                        Toast.makeText(this, "Formatted and written",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "This tag is not supported",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    ndef.connect();
                    ndef.writeNdefMessage(message);
                    ndef.close();
                    Toast.makeText(this, "Written",
                            Toast.LENGTH_LONG).show();
                }
            } catch (IOException | FormatException e) {
                Toast.makeText(this, "Exception!",
                        Toast.LENGTH_LONG).show();
                throw new RuntimeException();
            }
        } else {
            Toast.makeText(this, "Tag is null", Toast.LENGTH_LONG).show();
        }
    }
}