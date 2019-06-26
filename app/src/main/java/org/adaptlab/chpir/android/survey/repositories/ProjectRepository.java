package org.adaptlab.chpir.android.survey.repositories;

import android.app.Application;

import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.adaptlab.chpir.android.survey.daos.ProjectDao;
import org.adaptlab.chpir.android.survey.entities.Project;
import org.adaptlab.chpir.android.survey.entities.SurveyEntity;
import org.adaptlab.chpir.android.survey.tasks.EntityDownloadTask;

public class ProjectRepository extends Repository {
    private ProjectDao mProjectDao;

    public ProjectRepository(Application application) {
        SurveyRoomDatabase db = SurveyRoomDatabase.getDatabase(application);
        mProjectDao = db.projectDao();
    }

    @Override
    public void download() {
        new EntityDownloadTask(this).execute();
    }

    @Override
    public String getRemoteTableName() {
        return "projects";
    }

    @Override
    public BaseDao getDao() {
        return mProjectDao;
    }

    @Override
    public SurveyEntity getEntity() {
        return new Project();
    }
}
