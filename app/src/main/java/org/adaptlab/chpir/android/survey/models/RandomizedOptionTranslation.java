package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "RandomizedOptionTranslations")
public class RandomizedOptionTranslation extends Model {
    @Column(name = "RandomizedOption")
    private RandomizedOption mRandomizedOption;
    @Column(name = "Language")
    private String mLanguage;
    @Column(name = "Text")
    private String mText;
    @Column(name = "InstrumentTranslation")
    private InstrumentTranslation mInstrumentTranslation;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;

    RandomizedOptionTranslation() {
        super();
    }

    public static RandomizedOptionTranslation findByRemoteId(Long translationRemoteId) {
        return new Select().from(RandomizedOptionTranslation.class).where("RemoteId = ?", translationRemoteId).executeSingle();
    }

    public void setRemoteId(Long remoteId) {
        mRemoteId = remoteId;
    }

    void setRandomizedOption(RandomizedOption randomizedOption) {
        mRandomizedOption = randomizedOption;
    }

    public void setInstrumentTranslation(InstrumentTranslation instrumentTranslation) {
        mInstrumentTranslation = instrumentTranslation;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String language) {
        mLanguage = language;
    }
}
