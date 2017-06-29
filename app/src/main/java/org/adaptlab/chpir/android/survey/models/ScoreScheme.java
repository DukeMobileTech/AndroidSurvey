package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "ScoreSchemes")
public class ScoreScheme extends ReceiveModel {
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Title")
    private String mTitle;
    @Column(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;
    @Column(name = "Deleted")
    private boolean mDeleted;

    private static final String TAG = "ScoreScheme";
    
    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            ScoreScheme scheme = ScoreScheme.findByRemoteId(remoteId);
            if (scheme == null) {
                scheme = this;
            }
            scheme.setRemoteId(remoteId);
            scheme.setInstrumentRemoteId(jsonObject.getLong("instrument_id"));
            scheme.setTitle(jsonObject.getString("title"));
            if (jsonObject.isNull("deleted_at")) {
                scheme.setDeleted(false);
            } else {
                scheme.setDeleted(true);
            }
            scheme.save();
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static ScoreScheme findByRemoteId(Long remoteId) {
        return new Select().from(ScoreScheme.class).where("RemoteId = ?", remoteId).executeSingle();
    }

    private void setRemoteId(Long remoteId) {
        mRemoteId = remoteId;
    }

    private void setInstrumentRemoteId(long instrumentRemoteId) {
        mInstrumentRemoteId = instrumentRemoteId;
    }

    private void setTitle(String title) {
        mTitle = title;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public String getTitle() {
        return mTitle;
    }

    public List<ScoreUnit> scoreUnits() {
        return new Select().from(ScoreUnit.class)
                .where("ScoreScheme = ? AND Deleted != ?", getId(), 1)
                .orderBy("QuestionNumberInInstrument")
                .execute();
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    public Instrument getInstrument() {
        return new Select().from(Instrument.class).where("RemoteId = ?", mInstrumentRemoteId).executeSingle();
    }

}