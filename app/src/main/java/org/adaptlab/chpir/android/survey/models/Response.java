package org.adaptlab.chpir.android.survey.models;

import android.content.Context;
import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.SendModel;
import org.adaptlab.chpir.android.survey.utils.AuthUtils;
import org.adaptlab.chpir.android.survey.verhoeff.ParticipantIdValidator;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.isEmpty;

@Table(name = "Responses")
public class Response extends SendModel {
    public final static String LIST_DELIMITER = ",";
    public static final String SKIP = "SKIP";
    public static final String RF = "RF";
    public static final String NA = "NA";
    public static final String DK = "DK";
    public static final String BLANK = "";
    private static final String TAG = "Response";
    @Column(name = "Question")
    private Question mQuestion;
    @Column(name = "Text")
    private String mText;
    @Column(name = "Other_Response")
    private String mOtherResponse;
    @Column(name = "SpecialResponse")
    private String mSpecialResponse;
    @Column(name = "SentToRemote")
    private boolean mSent;
    @Column(name = "TimeStarted")
    private Date mTimeStarted;
    @Column(name = "TimeEnded")
    private Date mTimeEnded;
    @Column(name = "UUID")
    private String mUUID;
    @Column(name = "DeviceUser")
    private DeviceUser mDeviceUser;
    @Column(name = "QuestionVersion")
    private int mQuestionVersion;
    @Column(name = "SurveyUUID")
    private String mSurveyUUID;
    @Column(name = "RandomizedData")
    private String mRandomizedData;

    public Response() {
        super();
        mSent = false;
        mText = "";
        mSpecialResponse = "";
        mUUID = UUID.randomUUID().toString();
        setDeviceUser(AuthUtils.getCurrentUser());
    }

    public static List<Response> getAll() {
        return new Select().from(Response.class).orderBy("Id ASC").execute();
    }

    public boolean saveWithValidation() {
        if (isValid()) {
            setQuestionVersion(getQuestion().getQuestionVersion());
            save();
            getSurvey().setLastUpdated(new Date());
            getSurvey().save();
            return true;
        } else {
            return false;
        }
    }

    public boolean isValid() {
        if (mQuestion.getValidation() == null) return true;
        if (mQuestion.getValidation().getValidationType().equals(
                Validation.Type.REGEX.toString())) {
            return getText().matches(mQuestion.getValidation().getValidationText());
        } else if (mQuestion.getValidation().getValidationType().equals(
                Validation.Type.SUM_OF_PARTS.toString())) {
            double sum = 0.0;
            for (String text : getText().split(Response.LIST_DELIMITER)) {
                if (!isEmpty(text)) {
                    sum += Double.parseDouble(text);
                }
            }
            return sum == Double.parseDouble(mQuestion.getValidation().getValidationText());
        } else if (mQuestion.getValidation().getValidationType().equals(
                Validation.Type.RESPONSE.toString())) {
            return validateResponseType();
        } else {
            return mQuestion.getValidation().getValidationType().equals(Validation.Type.VERHOEFF
                    .toString()) && ParticipantIdValidator.validate(getText());
        }
    }

    private boolean validateResponseType() {
        Question validationQuestion = Question.findByQuestionIdentifier(
                mQuestion.getValidation().getValidationText());
        Response response = getSurvey().getResponseByQuestion(validationQuestion);
        double validationResponse = 0.0, givenResponse = 0.0;
        if (response != null && !isEmpty(response.getText())) {
            validationResponse = Double.parseDouble(response.getText());
        }
        if (!isEmpty(getText())) {
            givenResponse = Double.parseDouble(getText());
        }
        String operator = mQuestion.getValidation().getRelationalOperator();
        switch (operator) {
            case "==":
                return givenResponse == validationResponse;
            case "!=":
                return givenResponse != validationResponse;
            case ">":
                return givenResponse > validationResponse;
            case "<":
                return givenResponse < validationResponse;
            case ">=":
                return givenResponse >= validationResponse;
            case "<=":
                return givenResponse <= validationResponse;
            default:
                return true;
        }
    }

    public Question getQuestion() {
        return mQuestion;
    }

    public void setQuestion(Question question) {
        mQuestion = question;
    }

    public Survey getSurvey() {
        if (getSurveyUUID() == null) return null;
        return Survey.findByUUID(getSurveyUUID());
    }

    public String getText() {
        return mText;
    }

    private String getSurveyUUID() {
        return mSurveyUUID;
    }

    public void setSurvey(Survey survey) {
        mSurveyUUID = survey.getUUID();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("survey_uuid", (getSurvey() == null) ? getSurveyUUID() : getSurvey()
                    .getUUID());
            jsonObject.put("question_id", getQuestion().getRemoteId());
            jsonObject.put("text", getText());
            jsonObject.put("other_response", getOtherResponse());
            jsonObject.put("special_response", getSpecialResponse());
            jsonObject.put("time_started", getTimeStarted());
            jsonObject.put("time_ended", getTimeEnded());
            jsonObject.put("question_identifier", getQuestion().getQuestionIdentifier());
            jsonObject.put("uuid", getUUID());
            jsonObject.put("question_version", getQuestionVersion());
            jsonObject.put("randomized_data", getRandomizedData());
            if (getDeviceUser() != null) {
                jsonObject.put("device_user_id", getDeviceUser().getRemoteId());
            }

            json.put("response", jsonObject);
        } catch (JSONException je) {
            Log.e(TAG, "JSON exception", je);
        }
        return json;
    }

    public String getOtherResponse() {
        return mOtherResponse;
    }

    public void setOtherResponse(String otherResponse) {
        mOtherResponse = otherResponse;
    }

    public String getSpecialResponse() {
        return mSpecialResponse;
    }

    public void setSpecialResponse(String specialResponse) {
        mSpecialResponse = specialResponse;
    }

    public Date getTimeStarted() {
        return mTimeStarted;
    }

    public void setTimeStarted(Date time) {
        mTimeStarted = time;
    }

    public Date getTimeEnded() {
        return mTimeEnded;
    }

    public String getUUID() {
        return mUUID;
    }

    private int getQuestionVersion() {
        return mQuestionVersion;
    }

    public void setQuestionVersion(int version) {
        mQuestionVersion = version;
    }

    public String getRandomizedData() {
        return mRandomizedData;
    }

    public DeviceUser getDeviceUser() {
        return mDeviceUser;
    }

    public void setDeviceUser(DeviceUser deviceUser) {
        mDeviceUser = deviceUser;
    }

    public void setRandomizedData(String data) {
        mRandomizedData = data;
    }

    public void setTimeEnded(Date time) {
        mTimeEnded = time;
    }

    @Override
    public boolean isSent() {
        return mSent;
    }

    /*
     * Only send if survey is ready to send.
     */
    @Override
    public boolean readyToSend() {
        return (getSurvey() == null) || getSurvey().readyToSend();
    }

    @Override
    public void setAsSent(Context context) {
        mSent = true;
        this.save();
        if (getResponsePhoto() == null) {
            this.delete();
        }
        if (getSurvey() != null) getSurvey().deleteIfComplete();
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public boolean belongsToRoster() {
        return (getSurvey() != null) && getSurvey().belongsToRoster();
    }

    public ResponsePhoto getResponsePhoto() {
        return new Select().from(ResponsePhoto.class).where("Response = ?", getId())
                .executeSingle();
    }

    public void setResponse(String text) {
        mText = text;
    }

    public boolean hasSpecialResponse() {
        return mSpecialResponse.equals(SKIP) || mSpecialResponse.equals(RF) ||
                mSpecialResponse.equals(NA) || mSpecialResponse.equals(DK);
    }

}