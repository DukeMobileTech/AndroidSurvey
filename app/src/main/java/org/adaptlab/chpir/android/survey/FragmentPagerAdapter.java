package org.adaptlab.chpir.android.survey;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.adaptlab.chpir.android.survey.models.AdminSettings;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.viewpagerfragments.InstrumentViewPagerFragment;
import org.adaptlab.chpir.android.survey.viewpagerfragments.SubmittedSurveyPagerFragment;
import org.adaptlab.chpir.android.survey.viewpagerfragments.SurveyViewPagerFragment;

import java.util.ArrayList;
import java.util.Objects;

public class FragmentPagerAdapter extends androidx.fragment.app.FragmentPagerAdapter {
    private Context mContext;
    private ArrayList<String> mTabs;

    FragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
        setTabs();
    }

    private void setTabs() {
        mTabs = new ArrayList<>();
        mTabs.add(mContext.getString(R.string.instruments));
        AdminSettings adminSettings = AppUtil.getAdminSettingsInstance();
        if (adminSettings.getShowSurveys()) {
            mTabs.add(mContext.getString(R.string.ongoing_surveys));
            mTabs.add(mContext.getString(R.string.submitted_surveys));
        }
        if (adminSettings.getShowRosters()) mTabs.add(mContext.getString(R.string.rosters));
        if (adminSettings.getShowScores()) mTabs.add(mContext.getString(R.string.rosters));
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return Objects.requireNonNull(createPagerFragment(mTabs.get(position)));
    }

    private Fragment createPagerFragment(String name) {
        if (name.equals(mContext.getString(R.string.instruments))) {
            return new InstrumentViewPagerFragment();
        } else if (name.equals(mContext.getString(R.string.ongoing_surveys))) {
            return new SurveyViewPagerFragment();
        } else if (name.equals(mContext.getString(R.string.submitted_surveys))) {
            return new SubmittedSurveyPagerFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs.get(position);
    }

}