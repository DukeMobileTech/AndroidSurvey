package org.adaptlab.chpir.android.survey.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Collage;
import org.adaptlab.chpir.android.survey.entities.OptionCollage;

import java.util.List;

public class OptionCollageRelation {
    @Embedded
    public OptionCollage optionCollage;
    @Relation(parentColumn = "CollageId", entityColumn = "RemoteId", entity = Collage.class)
    public List<CollageRelation> collages;
}
