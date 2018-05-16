package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

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
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static OptionSet findByRemoteId(Long id) {
        return new Select().from(OptionSet.class).where("RemoteId = ?", id).executeSingle();
    }

    private void setRemoteId(Long id) {
        mRemoteId = id;
    }

    private void setTitle(String title) {
        mTitle = title;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    private void setSpecial(boolean special) {
        mSpecial = special;
    }

    private void setInstructions(String instructions) {
        mInstructions = instructions;
    }

    public String getInstructions() {
        return mInstructions;
    }
}
