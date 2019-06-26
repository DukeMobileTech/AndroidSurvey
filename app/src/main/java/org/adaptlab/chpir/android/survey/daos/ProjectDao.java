package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.Project;

@Dao
public abstract class ProjectDao extends BaseDao<Project> {
    @Query("SELECT * FROM Projects WHERE RemoteId=:id")
    public abstract LiveData<Project> findById(Long id);
}
