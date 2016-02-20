package org.robotics.notificationlistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TableLayout tab;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tab = (TableLayout) findViewById(R.id.tab);
        final IntentFilter mIntentFilter = new IntentFilter(MyAccessibilityService.Constants.ACTION_CATCH_NOTIFICATION);
        mIntentFilter.addAction(MyAccessibilityService.Constants.ACTION_CATCH_TOAST);
        //mIntentFilter.addAction("Msg");
        registerReceiver(onNotice, mIntentFilter);
        Log.v(TAG, "Receiver registered.");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, mIntentFilter /*new IntentFilter("Msg")*/);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onNotice);
    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String pack = intent.getStringExtra("package");
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");

            if (pack == null)
                    pack = intent.getStringExtra(MyAccessibilityService.Constants.EXTRA_PACKAGE);
            if (text == null)
                    text =  intent.getStringExtra(MyAccessibilityService.Constants.EXTRA_MESSAGE);

            Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT).show();

            TableRow tr = new TableRow(getApplicationContext());
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            TextView textview = new TextView(getApplicationContext());
            textview.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            textview.setTextSize(20);
            textview.setTextColor(Color.parseColor("#0B0719"));
            textview.setText(Html.fromHtml(pack + "<br><b>" + title + " : </b>" + text));
            tr.addView(textview);
            tab.addView(tr);
            Toast.makeText(getApplicationContext(),"TEESTSETESTSET",Toast.LENGTH_SHORT);

            Log.v(TAG, "Received message");
            Log.v(TAG, "intent.getAction() :: " + intent.getAction());
            Log.v(TAG, "intent.getStringExtra(Constants.EXTRA_PACKAGE) :: " + intent.getStringExtra(MyAccessibilityService.Constants.EXTRA_PACKAGE));
            Log.v(TAG, "intent.getStringExtra(Constants.EXTRA_MESSAGE) :: " + intent.getStringExtra(MyAccessibilityService.Constants.EXTRA_MESSAGE));
        }
    };
}


