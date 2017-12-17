package com.axiom.cookmate.adapter;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.axiom.cookmate.R;
import com.axiom.cookmate.fragment.IntroPagerFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private String[] mHeading;
    private String[] mText;
    private static int PAGE_NUM = 5;
    private Context mContext;

    public ViewPagerAdapter(FragmentManager fragmentManager,Context context) {
        super(fragmentManager);
        mContext = context;
        mHeading = mContext.getResources().getStringArray(R.array.heading_text);
        mText = mContext.getResources().getStringArray(R.array.extra_text);
    }

    @Override
    public int getCount() {
        return PAGE_NUM;
    }
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return IntroPagerFragment.newInstance(mHeading[position],mText[position]);
            case 1:
                return IntroPagerFragment.newInstance(mHeading[position],mText[position]);
            case 2:
                return IntroPagerFragment.newInstance(mHeading[position],mText[position]);
            case 3:
                return IntroPagerFragment.newInstance(mHeading[position],mText[position]);
            case 4:
                return IntroPagerFragment.newInstance(mHeading[position],mText[position]);
            default:
                return null;
        }
        }
}
