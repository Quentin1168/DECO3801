package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;

public class WriteNFC extends AppCompatActivity {
    private PendingIntent pendingIntent;
    private NfcAdapter adapter;
    private IntentFilter[] writeFilter;
    private String[][] writeTechList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                PendingIntent.FLAG_IMMUTABLE
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
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        NdefRecord aar = NdefRecord.createApplicationRecord("com.example.deco3801project");
        NdefRecord[] records = new NdefRecord[]{
                aar
        };
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
                }
            } catch (IOException | FormatException e) {
                throw new RuntimeException();
            }
        }
    }
}