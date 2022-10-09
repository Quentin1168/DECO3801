package com.example.deco3801project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;

public class WriteNFC extends AppCompatActivity {
    private PendingIntent pendingIntent;
    private NfcAdapter adapter;
    private IntentFilter[] writeFilter;
    private String[][] writeTechList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_nfc);
        writeFilter = new IntentFilter[]{};
        Intent intent =new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                0
        );
        adapter = NfcAdapter.getDefaultAdapter(this);

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
        adapter.disableForegroundDispatch(this);
    }
}