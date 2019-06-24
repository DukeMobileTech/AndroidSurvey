package org.adaptlab.chpir.android.survey.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.QuestionTranslation;

import java.util.List;

public class QuestionTranslationRelation {
    @Embedded
    public Question question;
    @Relation(parentColumn = "RemoteId", entityColumn = "QuestionRemoteId", entity = QuestionTranslation.class)
    public List<QuestionTranslation> translations;
}
