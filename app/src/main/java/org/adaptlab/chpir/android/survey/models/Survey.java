package org.adaptlab.chpir.android.survey.models;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.Cache;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/*
* Content Providers require column _id i.e. BaseColumns._ID which is different from the primary key
* used by ActiveAndroid. As a result, the expected ActiveAndroid relationships do not work
* and therefore have to be handled using the custom primary key or another key.
 */
@Table(name = "Surveys", id = BaseColumns._ID)
public class Survey extends SendModel {
    private static final String TAG = "Survey";

    @Column(name = "UUID")
    private String mUUID;
    @Column(name = "SentToRemote")
    private boolean mSent;
    @Column(name = "Complete")
    private boolean mComplete;
    @Column(name = "Latitude")
    private String mLatitude;
    @Column(name = "Longitude")
    private String mLongitude;
    @Column(name = "LastUpdated")
    private Date mLastUpdated;
    @Column(name = "Metadata")
    private String mMetadata;
    @Column(name = "ProjectId")
    private Long mProjectId;
    @Column(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;
    @Column(name = "CriticalResponses")
    private boolean mCriticalResponses;
    @Column(name = "RosterUUID")
    private String mRosterUUID;
    @Column(name = "Language")
    private String mLanguage;
    @Column(name = "SkippedQuestions")
    private String mSkippedQuestions;
    @Column(name = "SkipMaps")
    private String mSkipMaps;
    @Column(name = "Identifier")
    private String mIdentifier;
    @Column(name = "CompletedResponseCount")
    private int mCompletedResponseCount;

    public Survey() {
        super();
        mSent = false;
        mComplete = false;
        mUUID = UUID.randomUUID().toString();
        mLastUpdated = new Date();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("instrument_id", getInstrument().getRemoteId());
            jsonObject.put("instrument_version_number", getInstrument().getVersionNumber());
            jsonObject.put("device_uuid", getAdminInstanceDeviceIdentifier());
            jsonObject.put("device_label", AppUtil.getAdminSettingsInstance().getDeviceLabel());
            jsonObject.put("uuid", mUUID);
            jsonObject.put("instrument_title", getInstrument().getTitle());
            jsonObject.put("latitude", mLatitude);
            jsonObject.put("longitude", mLongitude);
            jsonObject.put("metadata", mMetadata);
            jsonObject.put("has_critical_responses", getCriticalResponses());
            jsonObject.put("skipped_questions", getSkippedQuestions());
            if (mRosterUUID != null) {
                jsonObject.put("roster_uuid", mRosterUUID);
            }
            jsonObject.put("language", mLanguage);
            json.put("survey", jsonObject);
        } catch (JSONException je) {
            Log.e(TAG, "JSON exception", je);
        }
        return json;
    }

    private String getAdminInstanceDeviceIdentifier() {
        return AppUtil.getAdminSettingsInstance().getDeviceIdentifier();
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public String getPrimaryKey() {
        return BaseColumns._ID;
    }

    /*
     * The identifier to display to the user to identify a Survey.
     * Return Unidentified Survey string if no response for identifier questions.
     */
    public String identifier(Context context) {
        if (isSent() && !TextUtils.isEmpty(getSubmittedIdentifier())) return getSubmittedIdentifier();
        String surveyLabel = null;
        StringBuilder identifier = new StringBuilder();

        if (!TextUtils.isEmpty(getMetadata())) {
            surveyLabel = getMetadataLabel();
        }
        if (!TextUtils.isEmpty(surveyLabel)) {
            return surveyLabel;
        }

        for (Response response : responses()) {
            if (response.getQuestion().identifiesSurvey()) {
                identifier.append(response.getText()).append(" ");
            }
        }

        if (identifier.toString().trim().isEmpty())
            return context.getString(R.string.unidentified_survey) + " " + getId();
        else
            return identifier.toString();
    }

    public String getIdentifier() {
        for (Response response : responses()) {
            if (response.getQuestion().identifiesSurvey()) {
                return response.getText();
            }
        }
        return "";
    }

    /*
     * Finders
     */
    public Response getResponseByQuestion(Question question) {
        return new Select().from(Response.class).where(
                "Question = ? AND SurveyUUID = ?",
                question.getId(), getUUID()).executeSingle();
    }

    public static List<Survey> getAllProjectSurveys(Long projectId) {
        return new Select("Surveys.*")
                .from(Survey.class)
                .innerJoin(Instrument.class)
                .on("Surveys.InstrumentRemoteId=Instruments.RemoteId AND Instruments.Published=" + 1)
                .where("Surveys.ProjectId = ? AND RosterUUID IS null", projectId)
                .orderBy("Surveys.LastUpdated DESC")
                .execute();
    }

    public static List<Survey> getAll() {
        return new Select().from(Survey.class).execute();
    }

    public static Cursor getProjectSurveysCursor(Long projectId) {
        From query = new Select("Surveys.*")
                .from(Survey.class)
                .where("ProjectId = ? AND RosterUUID IS null", projectId)
                .orderBy("LastUpdated DESC");
        return Cache.openDatabase().rawQuery(query.toSql(), query.getArguments());
    }

    public static List<Survey> getCompleted() {
        return new Select().from(Survey.class).where("Complete = ? AND ProjectID = ?", 1,
                Long.valueOf(AppUtil.getAdminSettingsInstance().getProjectId())).execute();
    }

    static List<Survey> getIncomplete() {
        return new Select().from(Survey.class).where("Complete = ? AND ProjectID = ?", 0,
                Long.valueOf(AppUtil.getAdminSettingsInstance().getProjectId())).execute();
    }

    public static Survey findByUUID(String uuid) {
        return new Select().from(Survey.class).where("UUID = ?", uuid).executeSingle();
    }

    /*
     * Relationships
     */
    public List<Response> responses() {
        return new Select().from(Response.class)
                .where("SurveyUUID = ?", getUUID())
                .orderBy("TimeEnded")
                .execute();
    }

    public List<Response> emptyResponses() {
        List<Response> responses = new Select()
                .from(Response.class)
                .where("SurveyUUID = ? AND (Text IS null OR Text = '') " +
                        "AND (SpecialResponse IS null OR SpecialResponse = '') " +
                        "AND (Other_Response IS null OR Other_Response = '')", getUUID())
                .execute();
        Iterator<Response> responseIterator = responses.iterator();
        while (responseIterator.hasNext()) {
            Response response = responseIterator.next();
            if (response.getQuestion().getQuestionType() == Question.QuestionType.INSTRUCTIONS ||
                    response.getResponsePhoto() != null) {
                responseIterator.remove();
            }
        }
        return responses;
    }

    public HashMap<Question, Response> responsesMap() {
        int capacity = (int) Math.ceil(responseCount()/0.75);
        HashMap<Question, Response> map = new HashMap<>(capacity);
        for (Response response : responses()) {
            map.put(response.getQuestion(), response);
        }
        return map;
    }

    private int responseCount() {
        return new Select().from(Response.class).where("SurveyUUID = ?", getUUID()).count();
    }

    public Instrument getInstrument() {
        return Instrument.findByRemoteId(getInstrumentRemoteId());
    }

    public void setInstrumentRemoteId(Long instrumentId) {
        mInstrumentRemoteId = instrumentId;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUuid(String id) {
        mUUID = id;
    }

    public void setAsComplete(boolean status) {
        mComplete = status;
    }

    @Override
    public boolean isSent() {
        return mSent;
    }

    @Override
    public void setAsSent(Context context) {
        mSent = true;
        this.save();

        EventLog eventLog = new EventLog(EventLog.EventType.SENT_SURVEY, context);
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
        if (notificationManager != null) {
            notificationManager.notify(eventLog.getLogMessage(context), 1, notification);
        }
    }

    @Override
    public boolean readyToSend() {
        return mComplete;
    }

    public void setLatitude(String latitude) {
        mLatitude = latitude;
    }

    public void setLongitude(String longitude) {
        mLongitude = longitude;
    }

    public Date getLastUpdated() {
        return mLastUpdated;
    }

    public void setLastUpdated(Date lastUpdate) {
        mLastUpdated = lastUpdate;
    }

    public Question getLastQuestion() {
        List<Response> responses = responses();
        if (responses.size() == 0) {
            return getInstrument().questions().get(0);
        } else {
            return responses.get(responses.size() - 1).getQuestion();
        }
    }

    public void destroy() {
        new Delete().from(Response.class).where("SurveyUUID = ?", mUUID).execute();
        this.delete();
    }

    public void setMetadata(String metadata) {
        mMetadata = metadata;
    }

    public String getMetadata() {
        return mMetadata;
    }

    public void setProjectId(Long id) {
        mProjectId = id;
    }

    public void setCriticalResponses(boolean status) {
        mCriticalResponses = status;
    }

    private String getMetadataLabel() {
        try {
            JSONObject metadata = new JSONObject(getMetadata());
            if (metadata.has("survey_label")) {
                return metadata.getString("survey_label");
            }
        } catch (JSONException er) {
            Log.e(TAG, er.getMessage());
        }

        return "";
    }

    private Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }

    private boolean getCriticalResponses() {
        return mCriticalResponses;
    }

    public void setRosterUUID(String uuid) {
        mRosterUUID = uuid;
    }

    public Roster getRoster() {
        return new Select().from(Roster.class).where("UUID = ?", mRosterUUID).executeSingle();
    }

    @Override
    public boolean belongsToRoster() {
        return mRosterUUID != null;
    }

    public void setLanguage(String language) {
        mLanguage = language;
    }

    public String getSkippedQuestions() {
        return mSkippedQuestions;
    }

    public void setSkippedQuestions(String questionIdentifiers) {
        mSkippedQuestions = questionIdentifiers;
    }

    public String getSkipMaps() {
        return mSkipMaps;
    }

    public void setSkipMaps(String skipMaps) {
        mSkipMaps = skipMaps;
    }

    public String getSubmittedIdentifier() {
        return mIdentifier;
    }

    public void setSubmittedIdentifier(String identifier) {
        mIdentifier = identifier;
        save();
    }

    public int getCompletedResponseCount() {
        return mCompletedResponseCount;
    }

    public void setCompletedResponseCount(int count) {
        mCompletedResponseCount = count;
        save();
    }

}