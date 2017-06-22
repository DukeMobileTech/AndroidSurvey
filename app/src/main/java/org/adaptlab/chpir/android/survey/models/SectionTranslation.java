package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "SectionTranslations")
public class SectionTranslation extends Model {
	@Column(name = "Section")
    private Section mSection;
    @Column(name = "Language")
    private String mLanguage;
    @Column(name = "Text")
    private String mText;
    @Column(name = "InstrumentTranslation")
    private InstrumentTranslation mInstrumentTranslation;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    
    public SectionTranslation() {
        super();
    }
    
    public static SectionTranslation findByLanguage(String language) {
        return new Select().from(SectionTranslation.class).where("Language = ?", language).executeSingle();
    }

    public static SectionTranslation findByRemoteId(Long id) {
        return new Select().from(SectionTranslation.class).where("RemoteId = ?", id).executeSingle();
    }
    
    public Section getSection() {
        return mSection;
    }
    
    public void setSection(Section section) {
        mSection = section;
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
