package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "ValidationTranslations")
public class ValidationTranslation extends Model {
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Validation")
    private Validation mValidation;
    @Column(name = "Language")
    private String mLanguage;
    @Column(name = "Text")
    private String mText;

    public ValidationTranslation() {
        super();
    }

    public static ValidationTranslation findByLanguage(String language) {
        return new Select().from(ValidationTranslation.class).where("Language = ?",
                language).executeSingle();
    }

    public static ValidationTranslation findByRemoteId(Long id) {
        return new Select().from(ValidationTranslation.class).where("RemoteId = ?",
                id).executeSingle();
    }

    public Validation getValidation() {
        return mValidation;
    }

    public void setValidation(Validation validation) {
        mValidation = validation;
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
