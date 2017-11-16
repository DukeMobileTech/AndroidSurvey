package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.AppUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "RandomizedFactors")
public class RandomizedFactor extends ReceiveModel{
    private static final String TAG = "RandomizedFactor";
    @Column(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;
    @Column(name = "Title")
    private String mTitle;
    // https://github.com/pardom/ActiveAndroid/issues/22
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;

    public RandomizedFactor() {
        super();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");

            // If a factor already exists, update it from the remote
            RandomizedFactor factor = RandomizedFactor.findByRemoteId(remoteId);
            if (factor == null) {
                factor = this;
            }

            if (AppUtil.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            factor.setTitle(jsonObject.getString("title"));
            factor.setInstrument(jsonObject.getLong("instrument_id"));
            factor.setRemoteId(remoteId);
            factor.save();
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static RandomizedFactor findByRemoteId(Long id) {
        return new Select().from(RandomizedFactor.class).where("RemoteId = ?", id).executeSingle();
    }

    public List<RandomizedOption> randomizedOptions() {
        return new Select().from(RandomizedOption.class).where("RandomizedFactor = ?", getId()).execute();
    }

    private void setTitle(String title) {
        mTitle = title;
    }

    private void setInstrument(Long instrumentId) {
        mInstrumentRemoteId = instrumentId;
    }

    private void setRemoteId(Long remoteId) {
        mRemoteId = remoteId;
    }

    public String getTitle() {
        return mTitle;
    }

    public Instrument getInstrument() {
        return Instrument.findByRemoteId(mInstrumentRemoteId);
    }
}
