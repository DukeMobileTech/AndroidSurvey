package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "DisplayTranslations")
public class DisplayTranslation extends Model {
    @Column(name = "Display")
    private Display mDisplay;
    @Column(name = "Language")
    private String mLanguage;
    @Column(name = "Text")
    private String mText;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;

    public DisplayTranslation() {
        super();
    }

    public static DisplayTranslation findByLanguage(String language) {
        return new Select().from(DisplayTranslation.class).where("Language = ?", language).executeSingle();
    }

    public static DisplayTranslation findByRemoteId(Long id) {
        return new Select().from(DisplayTranslation.class).where("RemoteId = ?", id).executeSingle();
    }

    public Display getDisplay() {
        return mDisplay;
    }

    public void setDisplay(Display display) {
        mDisplay = display;
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
