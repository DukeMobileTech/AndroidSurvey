package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.Task;

import java.util.List;

@Dao
public abstract class TaskDao extends BaseDao<Task> {
    @Query("SELECT * FROM Tasks")
    public abstract LiveData<List<Task>> getAllTasks();

}
