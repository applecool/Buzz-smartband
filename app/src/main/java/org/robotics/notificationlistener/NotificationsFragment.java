package org.robotics.notificationlistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Created by Cameron on 2/20/2016.
 */
public class NotificationsFragment extends Fragment {

    TableLayout tab;
    CheckBox maps, fb, sms, groupme;
    SharedPreferences pref;
    private String TAG = "NotificationFragment";
    private Handler handler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View v = inflater.inflate(R.layout.scrolling_notifications, container, false);
        pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = pref.edit();
        tab = (TableLayout) v.findViewById(R.id.tab);
        maps = (CheckBox) v.findViewById(R.id.checkBoxMaps);
        maps.setChecked(pref.getBoolean("maps",false));
        maps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                edit.putBoolean("maps", isChecked);
                edit.apply();
            }
        });
        fb = (CheckBox) v.findViewById(R.id.checkBoxFacebook);
        fb.setChecked(pref.getBoolean("fb",false));
        fb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                edit.putBoolean("messenger", isChecked);
                edit.apply();
            }
        });
        sms = (CheckBox) v.findViewById(R.id.checkBoxSMS);
        sms.setChecked(pref.getBoolean("messenger", false));
        sms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                edit.putBoolean("messenger", isChecked);
                edit.apply();
            }
        });
        groupme = (CheckBox) v.findViewById(R.id.checkBoxGroupMe);
        groupme.setChecked(pref.getBoolean("groupme",false));
        groupme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                edit.putBoolean("groupme", isChecked);
                edit.apply();
            }
        });
        return v;
    }

    public void addNotificationRow(String sender, String message){
        TableRow tr = new TableRow(getActivity());
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        TextView textview = new TextView(getActivity());
        textview.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
        textview.setTextSize(20);
        textview.setTextColor(Color.parseColor("#0B0719"));
        textview.setText(Html.fromHtml(sender + "<br><b>" + "Data" + " : </b>" + message));
        tr.addView(textview);
        tab.addView(tr);
    }


}
