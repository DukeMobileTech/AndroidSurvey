package org.adaptlab.chpir.android.survey;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatDelegate;

import com.newrelic.agent.android.NewRelic;

import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.LocaleManager;

public class SurveyApp extends com.activeandroid.app.Application {
    private static SurveyApp mInstance;

    public static SurveyApp getInstance() {
        if (mInstance == null) {
            mInstance = new SurveyApp();
        }
        return mInstance;
    }

    @Override
    public void onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate();
        mInstance = this;
        NewRelic.withApplicationToken(getString(R.string.new_relic_application_token)).start(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }

}