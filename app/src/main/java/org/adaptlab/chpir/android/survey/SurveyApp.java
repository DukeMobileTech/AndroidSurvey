package org.adaptlab.chpir.android.survey;

import android.content.Context;

public class SurveyApp extends com.activeandroid.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Context getContext() {
        return this;
    }

}