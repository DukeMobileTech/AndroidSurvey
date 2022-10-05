package org.adaptlab.chpir.android.survey.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Collage;
import org.adaptlab.chpir.android.survey.entities.Diagram;

import java.util.List;

public class CollageRelation {
    @Embedded
    public Collage collage;
    @Relation(parentColumn = "RemoteId", entityColumn = "CollageId", entity = Diagram.class)
    public List<DiagramRelation> diagrams;
}
