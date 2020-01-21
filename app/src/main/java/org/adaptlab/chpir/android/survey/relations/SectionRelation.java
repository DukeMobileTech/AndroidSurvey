package org.adaptlab.chpir.android.survey.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.Section;

import java.util.List;

public class SectionRelation {
    @Embedded
    public Section section;
    @Relation(parentColumn = "RemoteId", entityColumn = "SectionId", entity = Display.class)
    public List<Display> displays;
}
