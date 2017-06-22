package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "OptionTranslations")
public class OptionTranslation extends Model {
    @Column(name = "Option")
    private Option mOption;
    @Column(name = "Language")
    private String mLanguage;
    @Column(name = "Text")
    private String mText;
    @Column(name = "InstrumentTranslation")
    private InstrumentTranslation mInstrumentTranslation;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    
    public OptionTranslation() {
        super();
    }
    
    /*
     * Finders
     */    
    public static OptionTranslation findByLanguage(String language) {
        return new Select().from(OptionTranslation.class).where("Language = ?", language).executeSingle();
    }

    public static OptionTranslation findByRemoteId(Long id) {
        return new Select().from(OptionTranslation.class).where("RemoteId = ?", id).executeSingle();
    }
    
    /*
     * Getters/Setters
     */
    public Option getOption() {
        return mOption;
    }
    public void setOption(Option option) {
        mOption = option;
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

    public void setInstrumentTranslation(InstrumentTranslation translation) {
        mInstrumentTranslation = translation;
    }
}
