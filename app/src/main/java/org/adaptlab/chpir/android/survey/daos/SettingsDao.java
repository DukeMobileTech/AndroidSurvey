package org.adaptlab.chpir.android.survey.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.Settings;

import java.util.List;

@Dao
public abstract class SettingsDao extends BaseDao<Settings> {

    @Query("SELECT * from Settings WHERE Id = 1 ORDER BY Id ASC LIMIT 1")
    public abstract LiveData<Settings> getInstance();

    @Query("SELECT * from Settings WHERE Id = 1 ORDER BY Id ASC LIMIT 1")
    public abstract Settings getInstanceSync();

    @Query("SELECT DISTINCT language FROM QuestionTranslations")
    public abstract LiveData<List<String>> languages();
}
