package org.adaptlab.chpir.android.survey.models;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.activeandroid.Cache;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

@Table(name = "Rosters", id = BaseColumns._ID)
public class Roster extends SendModel {
    @Column(name = "UUID")
    private String mUUID;
    @Column(name = "SentToRemote")
    private boolean mSent;
    @Column(name = "Complete")
    private boolean mComplete;
    @Column(name = "Identifier", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private String mIdentifier;
    @Column(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;

    private final String TAG = "Roster";

    public Roster() {
        super();
        mSent = false;
        mComplete = false;
        mUUID = UUID.randomUUID().toString();
    }

    public static Cursor getCursor() {
        From query = new Select("Rosters.*").from(Roster.class);
        return Cache.openDatabase().rawQuery(query.toSql(), query.getArguments());
    }

    public void setInstrument(Instrument instrument) {
        mInstrumentRemoteId = instrument.getRemoteId();
    }

    public Instrument getInstrument() {
        return Instrument.findByRemoteId(mInstrumentRemoteId);
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setIdentifier(String identifier) {
        mIdentifier = identifier;
    }

    public void setComplete(boolean status) {
        mComplete = status;
    }

    public String identifier(Context context) {
        if (getIdentifier() == null || getIdentifier().trim().isEmpty())
            return context.getString(R.string.unidentified_roster) + " " + getId();
        else
            return getIdentifier();
    }

    public List<Survey> surveys() {
        return new Select().from(Survey.class).where("RosterUUID = ?", mUUID).execute();
    }

    public static Roster findByIdentifier(String identifier) {
        return new Select().from(Roster.class).where("Identifier = ?", identifier).executeSingle();
    }

    public static Roster findByUUID(String uuid) {
        return new Select().from(Roster.class).where("UUID = ?", uuid).executeSingle();
    }

    @Override
    public String getPrimaryKey() {
        return BaseColumns._ID;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("instrument_id", getInstrument().getRemoteId());
            jsonObject.put("instrument_version_number", getInstrument().getVersionNumber());
            jsonObject.put("instrument_title", getInstrument().getTitle());
            jsonObject.put("uuid", mUUID);
            jsonObject.put("identifier", mIdentifier);

            json.put("roster", jsonObject);
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "JSON exception", je);
        }
        return json;
    }

    @Override
    public boolean isSent() {
        return mSent;
    }

    @Override
    public boolean readyToSend() {
        return mComplete;
    }

    @Override
    public void setAsSent(Context context) {
        mSent = true;
        this.save();
        // TODO: 12/1/16 Show notifications
        // TODO: 12/1/16 Do not send roster surveys until Roster is complete
        // TODO: 12/1/16 Remove Roster surveys from the Surveys tab
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public boolean belongsToRoster() {
        return true;
    }

}