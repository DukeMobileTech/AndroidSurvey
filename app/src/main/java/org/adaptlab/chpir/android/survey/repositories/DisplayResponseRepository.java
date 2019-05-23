package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.relations.DisplayResponseDao;

public class DisplayResponseRepository {
    private DisplayResponseDao displayResponseDao;

    public DisplayResponseRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        displayResponseDao = db.displayResponseDao();
    }

    public DisplayResponseDao getDisplayResponseDao() {
        return displayResponseDao;
    }
}
