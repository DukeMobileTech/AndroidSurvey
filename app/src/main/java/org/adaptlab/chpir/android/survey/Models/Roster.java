package org.adaptlab.chpir.android.survey.models;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.activeandroid.Cache;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
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
        return null;
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