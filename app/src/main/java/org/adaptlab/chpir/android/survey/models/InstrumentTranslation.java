package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;


@Table(name = "InstrumentTranslations")
public class InstrumentTranslation extends Model {

    @Column(name = "Title")
    private String mTitle;
    @Column(name = "Language")
    private String mLanguage;
    @Column(name = "Alignment")
    private String mAlignment;
    @Column(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;
    @Column(name = "CriticalMessage")
    private String mCriticalMessage;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Active")
    private boolean mActive;

    
    public InstrumentTranslation() {
        super();
    }
    
    /*
     * Finders
     */   
    public static InstrumentTranslation findByLanguage(String language) {
        return new Select().from(InstrumentTranslation.class).where("Language = ?", language).executeSingle();
    }

    public static InstrumentTranslation findByRemoteId(Long id) {
        return new Select().from(InstrumentTranslation.class).where("RemoteId = ?", id).executeSingle();
    }
    
    /*
     * Getters/Setters
     */
    public String getTitle() {
        return mTitle;
    }
    
    public void setTitle(String title) {
        mTitle = title;
    }
    
    public String getLanguage() {
        return mLanguage;
    }
    
    public void setLanguage(String language) {
        mLanguage = language;
    }
    
    public String getAlignment() {
        return mAlignment;
    }
    
    public void setAlignment(String alignment) {
        mAlignment = alignment;
    }
    
    public Instrument getInstrument() {
        return Instrument.findByRemoteId(getInstrumentRemoteId());
    }
    
    public void setInstrumentRemoteId(Long instrumentId) {
        mInstrumentRemoteId = instrumentId;
    }

    private Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }

    public String getCriticalMessage() {
        return mCriticalMessage;
    }

    public void setCriticalMessage(String message) {
        mCriticalMessage = message;
    }

    public void setActive(boolean active) {
        mActive = active;
    }

    public boolean getActive() {
        return mActive;
    }

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }
}