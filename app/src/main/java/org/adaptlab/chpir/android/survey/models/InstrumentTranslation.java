package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;


@Table(name = "InstrumentTranslations")
public class InstrumentTranslation extends Model {

    @Column(name = "Title")
    private String mTitle;
    @Column(name = "Language", index = true)
    private String mLanguage;
    @Column(name = "Alignment")
    private String mAlignment;
    @Column(name = "InstrumentRemoteId", index = true)
    private Long mInstrumentRemoteId;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE, index = true)
    private Long mRemoteId;
    @Column(name = "Active", index = true)
    private boolean mActive;

    public InstrumentTranslation() {
        super();
    }

    public static InstrumentTranslation findByLanguage(String language) {
        return new Select().from(InstrumentTranslation.class).where("Language = ?", language).executeSingle();
    }

    public static InstrumentTranslation findByRemoteId(Long id) {
        return new Select().from(InstrumentTranslation.class).where("RemoteId = ?", id).executeSingle();
    }

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

    private Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }

    public void setInstrumentRemoteId(Long instrumentId) {
        mInstrumentRemoteId = instrumentId;
    }

    public boolean getActive() {
        return mActive;
    }

    public void setActive(boolean active) {
        mActive = active;
    }

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }
}