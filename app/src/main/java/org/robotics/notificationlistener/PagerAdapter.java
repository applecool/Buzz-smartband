package org.robotics.notificationlistener;

/**
 * Created by Cameron on 2/20/2016.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Cameron on 2/20/2016.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                NotificationsFragment tab1 = new NotificationsFragment();
                return tab1;
            case 1:
                RawBTFragment tab2 = new RawBTFragment();
                return tab2;
            case 2:
                MarcoPoloFragment tab3 = new MarcoPoloFragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
