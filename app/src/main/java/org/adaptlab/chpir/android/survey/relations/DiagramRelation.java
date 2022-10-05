package org.adaptlab.chpir.android.survey.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Diagram;
import org.adaptlab.chpir.android.survey.entities.Option;

import java.util.List;

public class DiagramRelation {
    @Embedded
    public Diagram diagram;
    @Relation(parentColumn = "OptionId", entityColumn = "RemoteId", entity = Option.class)
    public List<OptionRelation> options;
}
