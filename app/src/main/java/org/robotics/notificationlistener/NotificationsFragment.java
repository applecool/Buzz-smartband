package org.robotics.notificationlistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Created by Cameron on 2/20/2016.
 */
public class NotificationsFragment extends Fragment {

    TableLayout tab;
    private String TAG = "NotificationFragment";
    private Handler handler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        View v = inflater.inflate(R.layout.scrolling_notifications, container, false);
        tab = (TableLayout) v.findViewById(R.id.tab);
        final IntentFilter mIntentFilter = new IntentFilter(MyAccessibilityService.Constants.ACTION_CATCH_NOTIFICATION);
        mIntentFilter.addAction(MyAccessibilityService.Constants.ACTION_CATCH_TOAST);
        //mIntentFilter.addAction("Msg");
        getActivity().registerReceiver(onNotice, mIntentFilter);
        Log.v(TAG, "Receiver registered.");
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, mIntentFilter /*new IntentFilter("Msg")*/);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(onNotice);
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

            // Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT).show();

            final PackageManager pm = getActivity().getPackageManager();
            ApplicationInfo ai;
            try {
                ai = pm.getApplicationInfo( pack, 0);
            } catch (final PackageManager.NameNotFoundException e) {
                ai = null;
            }
            final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

            TableRow tr = new TableRow(getActivity());
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            TextView textview = new TextView(getActivity());
            textview.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            textview.setTextSize(20);
            textview.setTextColor(Color.parseColor("#0B0719"));
            textview.setText(Html.fromHtml(applicationName + "<br><b>" + title + " : </b>" + text));
            tr.addView(textview);
            tab.addView(tr);

            ((MainActivity) getActivity()).handleMessageReceived(applicationName, text);

            Log.v(TAG, "Received message");
            Log.v(TAG, "intent.getAction() :: " + intent.getAction());
            Log.v(TAG, "intent.getStringExtra(Constants.EXTRA_PACKAGE) :: " + intent.getStringExtra(MyAccessibilityService.Constants.EXTRA_PACKAGE));
            Log.v(TAG, "intent.getStringExtra(Constants.EXTRA_MESSAGE) :: " + intent.getStringExtra(MyAccessibilityService.Constants.EXTRA_MESSAGE));
        }
    };

}
