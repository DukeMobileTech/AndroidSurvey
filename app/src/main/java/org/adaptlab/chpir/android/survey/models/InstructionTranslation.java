package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "InstructionTranslations")
public class InstructionTranslation extends Model {
	@Column(name = "Instruction")
    private Instruction mInstruction;
    @Column(name = "Language")
    private String mLanguage;
    @Column(name = "Text")
    private String mText;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;

    public InstructionTranslation() {
        super();
    }
    
    public static InstructionTranslation findByLanguage(String language) {
        return new Select().from(InstructionTranslation.class).where("Language = ?", language).executeSingle();
    }

    public static InstructionTranslation findByRemoteId(Long id) {
        return new Select().from(InstructionTranslation.class).where("RemoteId = ?", id).executeSingle();
    }

    public void setInstruction(Instruction instruction) {
        mInstruction = instruction;
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

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }
}
