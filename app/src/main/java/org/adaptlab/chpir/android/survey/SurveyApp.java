package org.adaptlab.chpir.android.survey;

import android.content.Context;
import android.content.res.Configuration;

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
        super.onCreate();
        mInstance = this;
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