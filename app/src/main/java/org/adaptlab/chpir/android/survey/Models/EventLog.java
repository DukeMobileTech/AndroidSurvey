package org.adaptlab.chpir.android.survey.Models;

import android.content.Context;
import android.content.res.Resources;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.survey.R;

import java.util.Date;
import java.util.List;

public class EventLog extends Model {
    private final static String TAG = "EVENT";
    private Context mContext;
    
    public static enum EventType {
        SENT_SURVEY
    }
    
    @Column(name = "EventType")
    private EventType mEventType;
    @Column(name = "SurveyIdentifier")
    private String mSurveyIdentifier;
    @Column(name = "Timestamp")
    private Date mTimestamp;
    @Column(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;
    
    public EventLog(EventType eventType, Context context) {
        super();
        mEventType = eventType;
        mTimestamp = new Date();
        mContext = context;
    }
    
    public static List<EventLog> getAll() {
        return new Select().from(EventLog.class).orderBy("Id ASC").execute();
    }
    
    public void setInstrumentRemoteId(Long instrumentId) {
        mInstrumentRemoteId = instrumentId;
    }

    private Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }
    
    public void setSurveyIdentifier(String identifier) {
        mSurveyIdentifier = identifier;
    }
    
    public Instrument getInstrument() {
        return Instrument.findByRemoteId(getInstrumentRemoteId());
    }
    
    public String getSurveyIdentifier() {
        return mSurveyIdentifier;
    }
    
    public Date getTimestamp() {
        return mTimestamp;
    }
    
    public String getLogMessage(Context context) {
        Resources r = context.getResources();
        
        if (mEventType == EventType.SENT_SURVEY) {
            return r.getString(R.string.event_log_sent_survey, getInstrument().getTitle(), mSurveyIdentifier);
        }
        
        return "";
    }
}