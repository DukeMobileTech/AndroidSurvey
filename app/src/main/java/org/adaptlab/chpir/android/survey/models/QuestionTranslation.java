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
    @Column(name = "RegExValidationMessage")
    private String mRegExValidationMessage;
    @Column(name = "InstrumentTranslation")
    private InstrumentTranslation mInstrumentTranslation;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Instructions")
    private String mInstructions;
    
    /*
     * Finders
     */   
    public static QuestionTranslation findByLanguage(String language) {
        return new Select().from(QuestionTranslation.class).where("Language = ?", language).executeSingle();
    }

    public static QuestionTranslation findByRemoteId(Long id) {
        return new Select().from(QuestionTranslation.class).where("RemoteId = ?", id).executeSingle();
    }
    
    /*
     * Getters/Setters
     */
    public QuestionTranslation() {
        super();
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
    
    public String getRegExValidationMessage() {
        return mRegExValidationMessage;
    }
    
    public void setRegExValidationMessage(String message) {
        if (message.equals("null") || message.equals(""))
            mRegExValidationMessage = null;
         else
             mRegExValidationMessage = message;
    }

    public void setInstrumentTranslation(InstrumentTranslation translation) {
        mInstrumentTranslation = translation;
    }

    public InstrumentTranslation getInstrumentTranslation() {
        return mInstrumentTranslation;
    }

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }

    public void setInstructions(String instructions) {
        mInstructions = instructions;
    }

    public String getInstructions() {
        return mInstructions;
    }
}