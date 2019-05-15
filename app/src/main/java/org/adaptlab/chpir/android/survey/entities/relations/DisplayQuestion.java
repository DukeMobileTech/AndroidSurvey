package org.adaptlab.chpir.android.survey.entities.relations;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.Question;

import java.util.List;

public class DisplayQuestion {
    @Embedded
    public Display display;
    @Relation(parentColumn = "RemoteId", entityColumn = "DisplayId", entity = Question.class)
    public List<Question> questions;
}
