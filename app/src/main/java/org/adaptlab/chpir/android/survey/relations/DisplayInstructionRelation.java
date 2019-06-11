package org.adaptlab.chpir.android.survey.relations;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import org.adaptlab.chpir.android.survey.entities.DisplayInstruction;
import org.adaptlab.chpir.android.survey.entities.Instruction;

import java.util.List;

public class DisplayInstructionRelation {
    @Embedded
    public DisplayInstruction displayInstruction;
    @Relation(parentColumn = "InstructionRemoteId", entityColumn = "RemoteId", entity = Instruction.class)
    public List<Instruction> instructions;
}
