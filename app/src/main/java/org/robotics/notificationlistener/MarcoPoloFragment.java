package org.robotics.notificationlistener;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by Cameron on 2/20/2016.
 */
public class MarcoPoloFragment extends Fragment{

    ImageButton left, right;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.marco_polo, container, false);
        left = (ImageButton) v.findViewById(R.id.leftButton);
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN)
                    ((MainActivity) getActivity()).sendBTMessage("10");
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                    ((MainActivity) getActivity()).sendBTMessage("00");
                return true;
            }
        });
        right = (ImageButton) v.findViewById(R.id.rightButton);
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN)
                    ((MainActivity) getActivity()).sendBTMessage("01");
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                    ((MainActivity) getActivity()).sendBTMessage("00");
                return true;
            }
        });
        return v;
    }



}
