package org.adaptlab.chpir.android.survey.daos.relations;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import org.adaptlab.chpir.android.survey.entities.relations.QuestionRelation;

import java.util.List;

@Dao
public abstract class DisplayQuestionDao {
    @Transaction
    @Query("SELECT Questions.* FROM Questions INNER JOIN Displays ON Displays.RemoteId=Questions.DisplayId " +
            "WHERE Questions.DisplayId=:displayId AND Questions.InstrumentRemoteId=:instrumentId AND Questions.Deleted=0")
    public abstract LiveData<List<QuestionRelation>> displayQuestions(Long instrumentId, Long displayId);

//    @Transaction
//    @Query("SELECT * FROM Displays WHERE RemoteId=:id AND InstrumentRemoteId=:instrumentId LIMIT 1")
//    public abstract LiveData<DisplayQuestion> findById(long instrumentId, long id);

//    @Query("SELECT Questions.Text AS questionText, Questions.QuestionIdentifier AS questionIdentifier, " +
//            "Questions.RemoteId AS questionRemoteId, Questions.NumberInInstrument AS numberInInstrument, " +
//            "Instructions.Text AS questionInstructionText " +
//            "FROM Questions " +
//            "INNER JOIN Displays ON Displays.RemoteId=Questions.DisplayId " +
//            "LEFT JOIN Instructions ON Questions.InstructionId=Instructions.RemoteId " +
//            "WHERE Questions.DisplayId=:displayId AND Questions.InstrumentRemoteId=:instrumentId AND Questions.Deleted=0")
//    public abstract LiveData<List<QuestionInstruction>> displayQuestions(long instrumentId, long displayId);

//    @Query("SELECT Options.Text AS optionText, Options.RemoteId AS optionId, Options.Identifier AS optionIdentifier, " +
//            "OptionSetOptions.Position AS numberInQuestion " +
//            "FROM Options, OptionSetOptions " +
//            "INNER JOIN Questions ON Questions.RemoteOptionSetId=OptionSetOptions.OptionSetRemoteId AND  " +
//            "Questions.RemoteId=:questionId AND Questions.Deleted=0" +
//            "WHERE OptionSetOptions.OptionRemoteId=Options.RemoteId")
//    public abstract LiveData<List<QuestionOption>> questionOptions(Long questionId);


//    @Query("SELECT * " +
//            "FROM Questions " +
//            "INNER JOIN Displays ON Displays.RemoteId=Questions.DisplayId " +
//            "WHERE Questions.DisplayId=:displayId AND Questions.InstrumentRemoteId=:instrumentId AND Questions.Deleted=0")
//    public abstract LiveData<List<QuestionOption>> questions(long instrumentId, long displayId);

//    @Query("SELECT * FROM Questions INNER JOIN Displays ON Displays.RemoteId=Questions.DisplayId " +
//            "WHERE Questions.DisplayId=:displayId AND Questions.InstrumentRemoteId=:instrumentId AND Questions.Deleted=0")
//    public abstract LiveData<List<Question>> displayQuestions(Long instrumentId, Long displayId);

//    @Query("SELECT * FROM Instructions INNER JOIN Questions ON Questions.InstructionId=Instructions.RemoteId " +
//            "WHERE Questions.DisplayId=:displayId AND Questions.InstrumentRemoteId=:instrumentId AND Questions.Deleted=0")
//    public abstract LiveData<List<Instruction>> questionInstructions(Long instrumentId, Long displayId);
}
