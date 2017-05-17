package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.AppUtil;
import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "RandomizedOptions")
public class RandomizedOption extends ReceiveModel {
    private static final String TAG = "RandomizedOption";

    @Column(name = "RandomizedFactor")
    private RandomizedFactor mRandomizedFactor;
    @Column(name = "Text")
    private String mText;
    // https://github.com/pardom/ActiveAndroid/issues/22
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;

    public RandomizedOption() {
        super();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");

            // If an option already exists, update it from the remote
            RandomizedOption option = RandomizedOption.findByRemoteId(remoteId);
            if (option == null) {
                option = this;
            }

            if (AppUtil.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            option.setText(jsonObject.getString("text"));
            option.setRandomizedFactor(RandomizedFactor.findByRemoteId(jsonObject.getLong("randomized_factor_id")));
            option.setRemoteId(remoteId);
            option.save();
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static RandomizedOption findByRemoteId(Long id) {
        return new Select().from(RandomizedOption.class).where("RemoteId = ?", id).executeSingle();
    }

    public RandomizedFactor getRandomizedFactor() {
        return mRandomizedFactor;
    }

    private void setRandomizedFactor(RandomizedFactor randomizedFactor) {
        mRandomizedFactor = randomizedFactor;
    }

    private void setText(String text) {
        mText = text;
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    private void setRemoteId(Long id) {
        mRemoteId = id;
    }

    public String getText() {
        return mText;
    }
}
