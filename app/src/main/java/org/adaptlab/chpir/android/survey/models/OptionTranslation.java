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
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;

    public OptionTranslation() {
        super();
    }

    public static OptionTranslation findByRemoteId(Long id) {
        return new Select().from(OptionTranslation.class).where("RemoteId = ?", id).executeSingle();
    }

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

    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }

}
