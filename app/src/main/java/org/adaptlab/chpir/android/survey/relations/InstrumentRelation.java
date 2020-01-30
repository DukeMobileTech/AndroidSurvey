package org.adaptlab.chpir.android.survey.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Section;

import java.util.List;

public class InstrumentRelation {
    @Embedded
    public Instrument instrument;
    @Relation(parentColumn = "RemoteId", entityColumn = "InstrumentRemoteId", entity = Section.class)
    public List<SectionRelation> sections;
    @Relation(parentColumn = "RemoteId", entityColumn = "InstrumentRemoteId", entity = Question.class)
    public List<QuestionTranslationRelation> questions;
}
