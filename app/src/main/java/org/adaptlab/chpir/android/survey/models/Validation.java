package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.AppUtil.getDeviceLanguage;

@Table(name = "Validations")
public class Validation extends ReceiveModel {
    private static final String TAG = "Option";

    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Title")
    private String mTitle;
    @Column(name = "ValidationText")
    private String mValidationText;
    @Column(name = "ValidationMessage")
    private String mValidationMessage;
    @Column(name = "ValidationType")
    private String mValidationType;
    @Column(name = "ResponseIdentifier")
    private String mResponseIdentifier;
    @Column(name = "RelationalOperator")
    private String mRelationalOperator;
    @Column(name = "Deleted")
    private boolean mDeleted;

    public Validation() {
        super();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            Validation validation = Validation.findByRemoteId(remoteId);
            if (validation == null) {
                validation = this;
            }
            validation.setRemoteId(remoteId);
            validation.setTitle(jsonObject.optString("title"));
            validation.setValidationText(jsonObject.optString("validation_text"));
            validation.setValidationMessage(jsonObject.optString("validation_message"));
            validation.setValidationType(jsonObject.optString("validation_type"));
            validation.setResponseIdentifier(jsonObject.optString("response_identifier"));
            validation.setRelationalOperator(jsonObject.optString("relational_operator"));
            validation.setDeleted(jsonObject.optBoolean("deleted_at"));
            validation.save();

            JSONArray translationsArray = jsonObject.optJSONArray("validation_translations");
            if (translationsArray != null) {
                for (int i = 0; i < translationsArray.length(); i++) {
                    JSONObject translationJSON = translationsArray.getJSONObject(i);
                    Long translationRemoteId = translationJSON.getLong("id");
                    ValidationTranslation translation = ValidationTranslation.findByRemoteId(
                            translationRemoteId);
                    if (translation == null) {
                        translation = new ValidationTranslation();
                    }
                    translation.setRemoteId(translationRemoteId);
                    translation.setLanguage(translationJSON.getString("language"));
                    translation.setText(translationJSON.getString("text"));
                    translation.setValidation(Validation.findByRemoteId(translationJSON.getLong(
                            "validation_id")));
                    translation.save();
                }
            }
        } catch (JSONException je) {
            if (AppUtil.DEBUG) Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static Validation findByRemoteId(Long id) {
        return new Select().from(Validation.class).where("RemoteId = ?", id).executeSingle();
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    private void setRemoteId(Long id) {
        mRemoteId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    private void setTitle(String title) {
        mTitle = title;
    }

    public String getValidationText() {
        return mValidationText;
    }

    private List<ValidationTranslation> translations() {
        return getMany(ValidationTranslation.class, "Validation");
    }

    private void setValidationText(String validationText) {
        mValidationText = validationText;
    }

    public String getValidationMessage(Instrument instrument) {
        if (instrument.getLanguage().equals(getDeviceLanguage())) return mValidationMessage;
        for (ValidationTranslation translation : translations()) {
            if (translation.getLanguage().equals(getDeviceLanguage())) {
                return translation.getText();
            }
        }
        return mValidationMessage;
    }

    private void setValidationMessage(String validationMessage) {
        mValidationMessage = validationMessage;
    }

    public String getValidationType() {
        return mValidationType;
    }

    private void setValidationType(String validationType) {
        mValidationType = validationType;
    }

    public String getResponseIdentifier() {
        return mResponseIdentifier;
    }

    private void setResponseIdentifier(String responseIdentifier) {
        mResponseIdentifier = responseIdentifier;
    }

    public String getRelationalOperator() {
        return mRelationalOperator;
    }

    private void setRelationalOperator(String relationalOperator) {
        mRelationalOperator = relationalOperator;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public enum Type {
        REGEX, VERHOEFF, SUM_OF_PARTS, RESPONSE
    }
}
