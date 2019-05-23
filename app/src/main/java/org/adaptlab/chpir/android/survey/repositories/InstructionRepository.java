package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.InstructionDao;
import org.adaptlab.chpir.android.survey.daos.InstructionTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.InstructionTranslation;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class InstructionRepository extends Repository {
    private InstructionDao mInstructionDao;
    private InstructionTranslationDao mInstructionTranslationDao;

    public InstructionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mInstructionDao = db.instructionDao();
        mInstructionTranslationDao = db.instructionTranslationDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "instructions";
    }

    @Override
    public BaseDao getDao() {
        return mInstructionDao;
    }

    @Override
    public BaseDao getTranslationDao() {
        return mInstructionTranslationDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new Instruction();
    }

    @Override
    public SurveyEntity getTranslationEntity() {
        return new InstructionTranslation();
    }
}
