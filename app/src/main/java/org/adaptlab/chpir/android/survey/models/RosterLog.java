package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "RosterLogs")
public class RosterLog extends Model {
    @Column(name = "RosterUUID")
    private String mRosterUUID;
    @Column(name = "SurveyUUID")
    private String mSurveyUUID;
    @Column(name = "Complete")
    private boolean mComplete;
    @Column(name = "Identifier")
    private String mIdentifier;

    public RosterLog() {
        super();
    }

    public static RosterLog findByRosterAndSurvey(String rosterUUID, String surveyUUID) {
        return new Select().from(RosterLog.class).where("RosterUUID = ? AND SurveyUUID = ?",
                rosterUUID, surveyUUID).executeSingle();
    }

    public void setRosterUUID(String uuid) {
        mRosterUUID = uuid;
    }

    public void setSurveyUUID(String uuid) {
        mSurveyUUID = uuid;
    }

    public void setComplete(boolean status) {
        mComplete = status;
    }

    public void setIdentifier(String identifier) {
        mIdentifier = identifier;
    }

}
