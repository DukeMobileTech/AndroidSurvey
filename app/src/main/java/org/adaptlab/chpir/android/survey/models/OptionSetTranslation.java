package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "OptionSetTranslations")
public class OptionSetTranslation extends Model {
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "OptionSetId")
    private Long mOptionSetId;
    @Column(name = "OptionTranslationId")
    private Long mOptionTranslationId;
    @Column(name = "OptionId")
    private Long mOptionId;
    @Column(name = "Language")
    private String mLanguage;

    public OptionSetTranslation() {
        super();
    }

    public static OptionSetTranslation findByRemoteId(Long id) {
        return new Select().from(OptionSetTranslation.class).where("RemoteId = ?", id).executeSingle();
    }

    public static List<OptionSetTranslation> getAll() {
        return new Select().from(OptionSetTranslation.class).execute();
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(Long remoteId) {
        this.mRemoteId = remoteId;
    }

    public Long getOptionSetId() {
        return mOptionSetId;
    }

    public void setOptionSetId(Long optionSetId) {
        this.mOptionSetId = optionSetId;
    }

    public Long getOptionTranslationId() {
        return mOptionTranslationId;
    }

    public void setOptionTranslationId(Long optionTranslationId) {
        this.mOptionTranslationId = optionTranslationId;
    }

    public Long getOptionId() {
        return mOptionId;
    }

    public void setOptionId(Long optionId) {
        this.mOptionId = optionId;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String language) {
        this.mLanguage = language;
    }
}
