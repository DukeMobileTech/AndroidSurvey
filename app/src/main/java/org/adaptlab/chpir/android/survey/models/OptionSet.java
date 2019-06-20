package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "OptionSets")
public class OptionSet extends ReceiveModel {
    private static final String TAG = "OptionSet";

    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Deleted")
    private boolean mDeleted;
    @Column(name = "Special")
    private boolean mSpecial;
    @Column(name = "Title")
    private String mTitle;
    @Column(name = "Instructions")
    private String mInstructions;

    public static OptionSet findByRemoteId(Long id) {
        return new Select().from(OptionSet.class).where("RemoteId = ?", id).executeSingle();
    }

    public static List<OptionSet> getAll() {
        return new Select().from(OptionSet.class).where("Deleted != ?", 1)
                .orderBy("Id ASC")
                .execute();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            if (BuildConfig.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            Long remoteId = jsonObject.getLong("id");
            OptionSet optionSet = OptionSet.findByRemoteId(remoteId);
            if (optionSet == null) {
                optionSet = this;
            }
            optionSet.setRemoteId(remoteId);
            optionSet.setTitle(jsonObject.optString("title"));
            optionSet.setDeleted(jsonObject.optBoolean("deleted_at", false));
            optionSet.setSpecial(jsonObject.optBoolean("special", false));
            optionSet.setInstructions(jsonObject.optString("instructions"));
            optionSet.save();

            JSONArray translationsArray = jsonObject.optJSONArray("option_set_translations");
            if (translationsArray != null) {
                new Delete().from(OptionSetTranslation.class).where("OptionSetId = ?", optionSet.getRemoteId()).execute();
                for (int i = 0; i < translationsArray.length(); i++) {
                    JSONObject translationJSON = translationsArray.getJSONObject(i);
                    Long translationRemoteId = translationJSON.getLong("id");
                    OptionSetTranslation translation = OptionSetTranslation.findByRemoteId(translationRemoteId);
                    if (translation == null) {
                        translation = new OptionSetTranslation();
                    }
                    translation.setRemoteId(translationRemoteId);
                    translation.setOptionSetId(translationJSON.getLong("option_set_id"));
                    translation.setOptionTranslationId(translationJSON.getLong("option_translation_id"));
                    translation.setOptionId(translationJSON.getLong("option_id"));
                    translation.setLanguage(translationJSON.getString("language"));
                    translation.save();
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    private void setRemoteId(Long id) {
        mRemoteId = id;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    private void setSpecial(boolean special) {
        mSpecial = special;
    }

    public String getInstructions() {
        return mInstructions;
    }

    private void setInstructions(String instructions) {
        mInstructions = instructions;
    }

    public String getTitle() {
        return mTitle;
    }

    private void setTitle(String title) {
        mTitle = title;
    }
}
