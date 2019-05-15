package org.adaptlab.chpir.android.survey.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.viewpagerfragments.DisplayPagerFragment;

import java.util.List;

public class DisplayPagerAdapter extends FragmentStatePagerAdapter {
    private int mDisplayPosition;
    private Survey mSurvey;
    private List<Display> mDisplays;

    public DisplayPagerAdapter(FragmentManager fm, int position) {
        super(fm);
        mDisplayPosition = position;
    }

    public void setDisplays(List<Display> displays) {
        this.mDisplays = displays;
        notifyDataSetChanged();
    }

    public void setSurvey(Survey survey) {
        mSurvey = survey;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int i) {
        if (mDisplays != null && mDisplays.get(i) != null) {
            mDisplayPosition = mDisplays.get(i).getPosition();
        }
        DisplayPagerFragment fragment = new DisplayPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DisplayPagerFragment.EXTRA_DISPLAY_POSITION, mDisplayPosition);
        bundle.putLong(DisplayPagerFragment.EXTRA_INSTRUMENT_ID, mDisplays.get(i).getInstrumentRemoteId());
        bundle.putLong(DisplayPagerFragment.EXTRA_DISPLAY_ID, mDisplays.get(i).getRemoteId());
        if (mSurvey != null)
            bundle.putString(DisplayPagerFragment.EXTRA_SURVEY_UUID, mSurvey.getUUID());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return mDisplays == null ? 0 : mDisplays.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mDisplays == null || mDisplays.get(position) == null) return "";
        return mDisplays.get(position).getPosition() + ": " + mDisplays.get(position).getTitle();
    }

}
