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

@Table(name = "Options")
public class Option extends ReceiveModel {
    private static final String TAG = "Option";

    @Column(name = "Text")
    private String mText;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Deleted")
    private boolean mDeleted;
    @Column(name = "Identifier")
    private String mIdentifier;

    public Option() {
        super();
    }

    public static List<Option> getAll() {
        return new Select().from(Option.class).where("Deleted != ?", 1).orderBy("Id ASC").execute();
    }

    public static Option findByRemoteId(Long id) {
        return new Select().from(Option.class).where("RemoteId = ?", id).executeSingle();
    }

    /*
     * If the language of the instrument is the same as the language setting on the
     * device (or through the admin settings), then return the default option text.
     *
     * If another language is requested, iterate through option translations to
     * find translated text.
     *
     * If the language requested is not available as a translation, return the non-translated
     * text for the option.
     */
    public String getText(Instrument instrument) {
        if (instrument.getLanguage().equals(getDeviceLanguage())) return mText;
        for (OptionTranslation translation : translations()) {
            if (translation.getLanguage().equals(getDeviceLanguage())) {
                return translation.getText();
            }
        }
        // Fall back to default
        return mText;
    }

    public String getNonTranslatedText() {
        return mText;
    }

    public String getDeviceLanguage() {
        return AppUtil.getDeviceLanguage();
    }

    private List<OptionTranslation> translations() {
        return getMany(OptionTranslation.class, "Option");
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");

            // If an option already exists, update it from the remote
            Option option = Option.findByRemoteId(remoteId);
            if (option == null) {
                option = this;
            }
            option.setRemoteId(remoteId);

            if (AppUtil.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            option.setText(jsonObject.getString("text"));
            if (jsonObject.isNull("deleted_at")) {
                option.setDeleted(false);
            } else {
                option.setDeleted(true);
            }
            option.setIdentifier(jsonObject.optString("identifier"));
            option.save();

            // Generate translations
            JSONArray translationsArray = jsonObject.optJSONArray("option_translations");
            if (translationsArray != null) {
                for (int i = 0; i < translationsArray.length(); i++) {
                    JSONObject translationJSON = translationsArray.getJSONObject(i);
                    Long translationRemoteId = translationJSON.getLong("id");
                    OptionTranslation translation = OptionTranslation.findByRemoteId(translationRemoteId);
                    if (translation == null) {
                        translation = new OptionTranslation();
                    }
                    translation.setRemoteId(translationRemoteId);
                    translation.setLanguage(translationJSON.getString("language"));
                    translation.setOption(option);
                    translation.setText(translationJSON.getString("text"));
                    translation.save();
                }
            }
        } catch (JSONException je) {
            if (AppUtil.DEBUG) Log.e(TAG, "Error parsing object json", je);
        }
    }

    public void setText(String text) {
        mText = text;
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    private void setIdentifier(String id) {
        mIdentifier = id;
    }

    public boolean isExclusive(Question question) {
        for (OptionInOptionSet optionInOptionSet : question.exclusiveOptions()) {
            if (optionInOptionSet.getRemoteOptionId().equals(getRemoteId())) {
                return optionInOptionSet.isExclusive();
            }
        }
        return false;
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }

    public boolean getDeleted() {
        return mDeleted;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

}