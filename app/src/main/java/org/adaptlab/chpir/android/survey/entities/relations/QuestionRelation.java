package org.adaptlab.chpir.android.survey.entities.relations;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.Instruction;
import org.adaptlab.chpir.android.survey.entities.MultipleSkip;
import org.adaptlab.chpir.android.survey.entities.NextQuestion;
import org.adaptlab.chpir.android.survey.entities.OptionSet;
import org.adaptlab.chpir.android.survey.entities.Question;

import java.util.List;

public class QuestionRelation {
    @Embedded
    public Question question;
    @Relation(parentColumn = "InstructionId", entityColumn = "RemoteId", entity = Instruction.class)
    public List<Instruction> instructions;
    @Relation(parentColumn = "RemoteOptionSetId", entityColumn = "RemoteId", entity = OptionSet.class)
    public List<OptionSetRelation> optionSets;
    @Relation(parentColumn = "RemoteSpecialOptionSetId", entityColumn = "RemoteId", entity = OptionSet.class)
    public List<OptionSetRelation> specialOptionSets;
    @Relation(parentColumn = "DisplayId", entityColumn = "RemoteId", entity = Display.class)
    public List<DisplayRelation> displays;
    @Relation(parentColumn = "QuestionIdentifier", entityColumn = "QuestionIdentifier", entity = NextQuestion.class)
    public List<NextQuestion> nextQuestions;
    @Relation(parentColumn = "QuestionIdentifier", entityColumn = "QuestionIdentifier", entity = MultipleSkip.class)
    public List<MultipleSkip> multipleSkips;

}
