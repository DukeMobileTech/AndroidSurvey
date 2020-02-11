package org.adaptlab.chpir.android.survey.daos;

import androidx.room.Dao;
import androidx.room.Query;

import org.adaptlab.chpir.android.survey.entities.InstructionTranslation;

@Dao
public abstract class InstructionTranslationDao extends BaseDao<InstructionTranslation> {
    @Query("SELECT * FROM InstructionTranslations WHERE RemoteId=:id")
    public abstract InstructionTranslation instructionTranslationSync(Long id);
}
