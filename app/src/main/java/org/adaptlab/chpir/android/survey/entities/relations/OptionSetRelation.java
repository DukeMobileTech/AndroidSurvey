package org.adaptlab.chpir.android.survey.entities.relations;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.OptionSet;
import org.adaptlab.chpir.android.survey.entities.OptionSetOption;

import java.util.List;

public class OptionSetRelation {
    @Embedded
    public OptionSet optionSet;
    @Relation(parentColumn = "InstructionRemoteId", entityColumn = "RemoteId", entity = Instruction.class)
    public List<Instruction> instructions;
    @Relation(parentColumn = "RemoteId", entityColumn = "OptionSetRemoteId", entity = OptionSetOption.class)
    public List<OptionSetOptionRelation> optionSetOptions;
}
