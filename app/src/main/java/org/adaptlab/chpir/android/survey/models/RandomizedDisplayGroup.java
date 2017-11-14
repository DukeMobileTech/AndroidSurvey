package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.AppUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "RandomizedDisplayGroup")
public class RandomizedDisplayGroup extends ReceiveModel {
    private static final String TAG = "RandomizedDisplayGroup";

    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Title")
    private String mTitle;
    @Column(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        if (AppUtil.DEBUG) Log.i(TAG, "createObjectFromJSON: " + jsonObject);
        try {
            Long remoteId = jsonObject.getLong("id");
            RandomizedDisplayGroup randomizedDisplayGroup = findByRemoteId(remoteId);
            if (randomizedDisplayGroup == null) {
                randomizedDisplayGroup = this;
            }
            randomizedDisplayGroup.setRemoteId(remoteId);
            randomizedDisplayGroup.setTitle(jsonObject.getString("title"));
            randomizedDisplayGroup.setInstrumentRemoteId(jsonObject.getLong("instrument_id"));
            randomizedDisplayGroup.save();

            JSONArray displayGroupsArray = jsonObject.optJSONArray("display_groups");
            if (displayGroupsArray != null) {
                if (AppUtil.DEBUG) Log.i(TAG, "displayGroupsArray: " + displayGroupsArray);
                for (int k = 0; k < displayGroupsArray.length(); k++) {
                    JSONObject displayGroupJSON = displayGroupsArray.getJSONObject(k);
                    Long displayGroupId = displayGroupJSON.getLong("id");
                    DisplayGroup displayGroup = DisplayGroup.findByRemoteId(displayGroupId);
                    if (displayGroup == null) {
                        displayGroup = new DisplayGroup();
                    }
                    displayGroup.setRemoteId(displayGroupId);
                    displayGroup.setTitle(displayGroupJSON.getString("title"));
                    displayGroup.setPosition(displayGroupJSON.getInt("position"));
                    displayGroup.setRandomizedDisplayGroup(findByRemoteId(displayGroupJSON.getLong("randomized_display_group_id")));
                    displayGroup.save();
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static RandomizedDisplayGroup findByRemoteId(Long id) {
        return new Select().from(RandomizedDisplayGroup.class).where("RemoteId = ?", id).executeSingle();
    }

    public List<DisplayGroup> displayGroups() {
        return new Select().from(DisplayGroup.class).where("RandomizedDisplayGroup = ?", getId()).orderBy("Position ASC").execute();
    }

    // TODO: 11/13/17 Fix this patchwork
    public Question firstQuestion() {
        return new Select().from(Question.class)
                .innerJoin(DisplayGroup.class)
                .on("Questions.DisplayGroup IS NOT null AND DisplayGroups.RandomizedDisplayGroup=" + getId())
                .orderBy("NumberInInstrument ASC")
                .executeSingle();
    }

    private void setRemoteId(Long remoteId) {
        mRemoteId = remoteId;
    }

    private void setTitle(String title) {
        mTitle = title;
    }

    private void setInstrumentRemoteId(Long id) {
        mInstrumentRemoteId = id;
    }
}
