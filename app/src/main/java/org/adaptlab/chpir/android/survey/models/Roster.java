package org.adaptlab.chpir.android.survey.models;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.core.app.NotificationCompat;

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
    private final String TAG = "Roster";
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

    public static Roster findByIdentifier(String identifier) {
        return new Select().from(Roster.class).where("Identifier = ?", identifier).executeSingle();
    }

    public static Roster findByUUID(String uuid) {
        return new Select().from(Roster.class).where("UUID = ?", uuid).executeSingle();
    }

    public Instrument getInstrument() {
        return Instrument.findByRemoteId(mInstrumentRemoteId);
    }

    public void setInstrument(Instrument instrument) {
        mInstrumentRemoteId = instrument.getRemoteId();
    }

    public String getIdentifier() {
        return mIdentifier;
    }

    public void setIdentifier(String identifier) {
        mIdentifier = identifier;
    }

    public String getUUID() {
        return mUUID;
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

    public List<RosterLog> rosterLogs() {
        return new Select().from(RosterLog.class).where("RosterUUID = ?", mUUID).execute();
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
        EventLog eventLog = new EventLog(EventLog.EventType.SENT_ROSTER, context);
        eventLog.setInstrumentRemoteId(getInstrument().getRemoteId());
        eventLog.setSurveyIdentifier(identifier(context));
        eventLog.save();

        Resources r = context.getResources();

        Notification notification = new NotificationCompat.Builder(context)
                .setTicker(r.getString(R.string.app_name))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(r.getString(R.string.app_name))
                .setContentText(eventLog.getLogMessage(context))
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(eventLog.getLogMessage(context), 1, notification);
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