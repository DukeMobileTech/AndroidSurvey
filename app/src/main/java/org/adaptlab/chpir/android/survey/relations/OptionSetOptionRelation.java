package org.adaptlab.chpir.android.survey.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Option;
import org.adaptlab.chpir.android.survey.entities.OptionSetOption;

import java.util.List;

public class OptionSetOptionRelation {
    @Embedded
    public OptionSetOption optionSetOption;
    @Relation(parentColumn = "OptionRemoteId", entityColumn = "RemoteId", entity = Option.class)
    public List<OptionRelation> options;
}
