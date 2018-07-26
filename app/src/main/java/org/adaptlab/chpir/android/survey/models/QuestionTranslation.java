package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "QuestionTranslations")
public class QuestionTranslation extends Model {
    private static final String TAG = "QuestionTranslation";

    @Column(name = "Question")
    private Question mQuestion;
    @Column(name = "Language")
    private String mLanguage;
    @Column(name = "Text")
    private String mText;
    @Column(name = "InstrumentTranslation")
    private InstrumentTranslation mInstrumentTranslation;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Instructions")
    private String mInstructions;

    public QuestionTranslation() {
        super();
    }

    public static QuestionTranslation findByLanguage(String language) {
        return new Select().from(QuestionTranslation.class).where("Language = ?", language)
                .executeSingle();
    }

    public static QuestionTranslation findByRemoteId(Long id) {
        return new Select().from(QuestionTranslation.class).where("RemoteId = ?", id)
                .executeSingle();
    }

    public Question getQuestion() {
        return mQuestion;
    }

    public void setQuestion(Question question) {
        mQuestion = question;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String language) {
        mLanguage = language;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public InstrumentTranslation getInstrumentTranslation() {
        return mInstrumentTranslation;
    }

    public void setInstrumentTranslation(InstrumentTranslation translation) {
        mInstrumentTranslation = translation;
    }

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }

    public String getInstructions() {
        return mInstructions;
    }

    public void setInstructions(String instructions) {
        mInstructions = instructions;
    }
}