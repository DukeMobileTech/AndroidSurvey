package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.DisplayInstructionDao;
import org.adaptlab.chpir.android.survey.entities.DisplayInstruction;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class DisplayInstructionRepository extends Repository {
    private DisplayInstructionDao mDisplayInstructionDao;

    public DisplayInstructionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mDisplayInstructionDao = db.displayInstructionDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "display_instructions";
    }

    @Override
    public BaseDao getDao() {
        return mDisplayInstructionDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new DisplayInstruction();
    }
}
