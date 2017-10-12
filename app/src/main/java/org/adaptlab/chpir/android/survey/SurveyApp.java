package org.adaptlab.chpir.android.survey;

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

}