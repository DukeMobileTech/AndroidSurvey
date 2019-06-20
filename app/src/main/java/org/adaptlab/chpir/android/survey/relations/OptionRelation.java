package org.adaptlab.chpir.android.survey.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.OptionTranslation;

import java.util.List;

public class OptionRelation {
    @Embedded
    public Option option;
    @Relation(parentColumn = "RemoteId", entityColumn = "OptionRemoteId", entity = OptionTranslation.class)
    public List<OptionTranslation> translations;
}
