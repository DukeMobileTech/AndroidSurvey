package org.adaptlab.chpir.android.survey.daos;

import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.DisplayTranslation;

import java.util.List;

@Dao
public abstract class DisplayTranslationDao extends BaseDao<DisplayTranslation> {
    @Query("SELECT * FROM DisplayTranslations WHERE DisplayRemoteId=:displayId")
    public abstract List<DisplayTranslation> displayTranslationsSync(Long displayId);

    @Query("SELECT * FROM DisplayTranslations WHERE Text=:text AND DisplayRemoteId=:displayId")
    public abstract DisplayTranslation findByTitleAndDisplayIdSync(String text, Long displayId);
}
