package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.QuestionDao;
import org.adaptlab.chpir.android.survey.daos.QuestionTranslationDao;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.QuestionTranslation;
import org.adaptlab.chpir.android.survey.tasks.TranslatableEntityDownloadTask;

public class QuestionRepository implements Downloadable {
    private static final String TAG = "QuestionRepository";
    private QuestionDao mQuestionDao;
    private QuestionTranslationDao mQuestionTranslationDao;

    public QuestionRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mQuestionDao = db.questionDao();
        mQuestionTranslationDao = db.questionTranslationDao();
    }

    public void download() {
        new TranslatableEntityDownloadTask(mQuestionDao, mQuestionTranslationDao, getRemoteTableName(),
                Question.class, QuestionTranslation.class).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "questions";
    }

}
