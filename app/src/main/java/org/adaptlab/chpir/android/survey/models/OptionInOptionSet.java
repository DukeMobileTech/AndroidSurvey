package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "OptionInOptionSets")
public class OptionInOptionSet extends ReceiveModel {
    private static final String TAG = "OptionInOptionSet";

    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "NumberInQuestion")
    private int mNumberInQuestion;
    @Column(name = "RemoteOptionSetId")
    private Long mRemoteOptionSetId;
    @Column(name = "RemoteOptionId")
    private Long mRemoteOptionId;
    @Column(name = "Deleted")
    private boolean mDeleted;
    @Column(name = "Special")
    private boolean mSpecial;
    @Column(name = "IsExclusive")
    private boolean mIsExclusive;

    public static OptionInOptionSet findByRemoteId(Long id) {
        return new Select().from(OptionInOptionSet.class).where("RemoteId = ?", id).executeSingle();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            if (BuildConfig.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            Long remoteId = jsonObject.getLong("id");
            OptionInOptionSet option = OptionInOptionSet.findByRemoteId(remoteId);
            if (option == null) {
                option = this;
            }
            option.setRemoteId(remoteId);
            option.setRemoteOptionSetId(jsonObject.optLong("option_set_id"));
            option.setRemoteOptionId(jsonObject.optLong("option_id"));
            option.setNumberInQuestion(jsonObject.optInt("number_in_question"));
            if (jsonObject.isNull("deleted_at")) {
                option.setDeleted(false);
            } else {
                option.setDeleted(true);
            }
            option.setSpecial(jsonObject.optBoolean("special", false));
            option.setIsExclusive(jsonObject.optBoolean("is_exclusive", false));
            option.save();
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error parsing object json", je);
        }
    }

    public Long getRemoteOptionSetId() {
        return mRemoteOptionSetId;
    }

    private void setRemoteOptionSetId(Long id) {
        mRemoteOptionSetId = id;
    }

    public Long getRemoteOptionId() {
        return mRemoteOptionId;
    }

    private void setRemoteOptionId(Long id) {
        mRemoteOptionId = id;
    }

    private void setRemoteId(Long id) {
        mRemoteId = id;
    }

    private void setNumberInQuestion(int number) {
        mNumberInQuestion = number;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    private void setSpecial(boolean special) {
        mSpecial = special;
    }

    private void setIsExclusive(boolean exclusive) {
        mIsExclusive = exclusive;
    }

    public boolean isExclusive() {
        return mIsExclusive;
    }
}
