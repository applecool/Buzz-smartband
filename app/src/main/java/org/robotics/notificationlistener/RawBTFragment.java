package org.robotics.notificationlistener;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static org.robotics.notificationlistener.R.layout.raw_bluetooth_send;

/**
 * Created by Cameron on 2/20/2016.
 */
public class RawBTFragment extends Fragment{

    ListView messages;
    EditText input;
    Button sendButton;
    ArrayAdapter<String> adapter;

    ArrayList<String> values;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(raw_bluetooth_send, container, false);
        messages = (ListView) v.findViewById(R.id.messages);
        input = (EditText) v.findViewById(R.id.input);
        sendButton = (Button) v.findViewById(R.id.send);
        values = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, values);
        messages.setAdapter(adapter);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).sendClick(input.getText().toString());
            }
        });
        return v;
    }

    public void addMessage(String message){
        if(messages != null) {
            values.add(0,message);
            //adapter.insert(message, 0);
            if(adapter.getCount() > 10){
                values.remove(10);
            }
            //messages.(message + "\n");
            messages.invalidate();
        }
    }

}
