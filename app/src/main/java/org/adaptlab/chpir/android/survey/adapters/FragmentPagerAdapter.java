package org.adaptlab.chpir.android.survey.adapters;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.adaptlab.chpir.android.survey.InstrumentActivity;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.viewmodels.SettingsViewModel;
import org.adaptlab.chpir.android.survey.viewpagerfragments.InstrumentViewPagerFragment;
import org.adaptlab.chpir.android.survey.viewpagerfragments.SurveyViewPagerFragment;

import java.util.ArrayList;

public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
    private Context mContext;
    private ArrayList<String> mTabs;

    public FragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
        setTabs();
    }

    private void setTabs() {
        SettingsViewModel settingsViewModel = ViewModelProviders.of((InstrumentActivity) mContext).get(SettingsViewModel.class);
        settingsViewModel.getSettings().observe((InstrumentActivity) mContext, new Observer<Settings>() {
            @Override
            public void onChanged(@Nullable Settings settings) {
                mTabs = new ArrayList<>();
                mTabs.add(mContext.getString(R.string.instruments));
                if (settings != null) {
                    if (settings.isShowSurveys()) mTabs.add(mContext.getString(R.string.surveys));
                    if (settings.isShowRosters()) mTabs.add(mContext.getString(R.string.rosters));
                    if (settings.isShowScores()) mTabs.add(mContext.getString(R.string.rosters));
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public Fragment getItem(int position) {
        return createPagerFragment(mTabs.get(position));
    }

    private Fragment createPagerFragment(String name) {
        if (name.equals(mContext.getString(R.string.instruments))) {
            return new InstrumentViewPagerFragment();
        } else if (name.equals(mContext.getString(R.string.surveys))) {
            return new SurveyViewPagerFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return mTabs == null ? 0 : mTabs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs.get(position);
    }

}