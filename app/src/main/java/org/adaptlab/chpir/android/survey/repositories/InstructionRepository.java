package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.InstructionDao;
import org.adaptlab.chpir.android.survey.daos.InstructionTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.InstructionTranslation;
import org.adaptlab.chpir.android.survey.tasks.TranslatableEntityDownloadTask;

public class InstructionRepository implements Downloadable {
    private InstructionDao mInstructionDao;
    private InstructionTranslationDao mInstructionTranslationDao;

    public InstructionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mInstructionDao = db.instructionDao();
        mInstructionTranslationDao = db.instructionTranslationDao();
    }

    @Override
    public void download() {
        new TranslatableEntityDownloadTask(mInstructionDao, mInstructionTranslationDao, getRemoteTableName(),
                Instruction.class, InstructionTranslation.class).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "instructions";
    }
}
