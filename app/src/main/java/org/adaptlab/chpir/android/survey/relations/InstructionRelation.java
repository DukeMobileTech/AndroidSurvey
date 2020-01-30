package org.adaptlab.chpir.android.survey.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.InstructionTranslation;

import java.util.List;

public class InstructionRelation {
    @Embedded
    public Instruction instruction;
    @Relation(parentColumn = "RemoteId", entityColumn = "InstructionRemoteId", entity = InstructionTranslation.class)
    public List<InstructionTranslation> translations;
}
