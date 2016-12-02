package org.adaptlab.chpir.android.survey.Models;

import android.content.Context;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

@Table(name = "Rosters")
public class Roster extends SendModel {
    @Column(name = "UUID")
    private String mUUID;
    @Column(name = "SentToRemote")
    private boolean mSent;
    @Column(name = "Complete")
    private boolean mComplete;
    @Column(name = "Identifier")
    private String mIdentifier;
    @Column(name = "Instrument")
    private Instrument mInstrument;

    public Roster() {
        super();
        mSent = false;
        mComplete = false;
        mUUID = UUID.randomUUID().toString();
    }

    public void setInstrument(Instrument instrument) {
        mInstrument = instrument;
    }

    public Instrument getInstrument() {
        return mInstrument;
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

}