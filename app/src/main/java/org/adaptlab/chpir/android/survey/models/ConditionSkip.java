package org.adaptlab.chpir.android.survey.models;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

@Table(name = "ConditionSkips")
public class ConditionSkip extends ReceiveModel {
    private static final String TAG = "ConditionSkips";

    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "RemoteQuestionId")
    private Long mRemoteQuestionId;
    @Column(name = "QuestionIdentifier")
    private String mQuestionIdentifier;
    @Column(name = "RemoteInstrumentId")
    private Long mRemoteInstrumentId;
    @Column(name = "ConditionQuestionIdentifier")
    private String mConditionQuestionIdentifier;
    @Column(name = "ConditionOptionIdentifier")
    private String mConditionOptionIdentifier;
    @Column(name = "OptionIdentifier")
    private String mOptionIdentifier;
    @Column(name = "Condition")
    private String mCondition;
    @Column(name = "NextQuestionIdentifier")
    private String mNextQuestionIdentifier;
    @Column(name = "Deleted")
    private boolean mDeleted;

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            ConditionSkip conditionSkip = ConditionSkip.findByRemoteId(remoteId);
            if (conditionSkip == null) {
                conditionSkip = this;
            }
            conditionSkip.setRemoteId(remoteId);
            conditionSkip.setQuestionIdentifier(jsonObject.getString("question_identifier"));
            conditionSkip.setRemoteQuestionId(jsonObject.getLong("question_id"));
            conditionSkip.setRemoteInstrumentId(jsonObject.getLong("instrument_id"));
            conditionSkip.setConditionQuestionIdentifier(jsonObject.optString("condition_question_identifier"));
            conditionSkip.setConditionOptionIdentifier(jsonObject.optString("condition_option_identifier"));
            conditionSkip.setOptionIdentifier(jsonObject.getString("option_identifier"));
            conditionSkip.setCondition(jsonObject.optString("condition"));
            conditionSkip.setNextQuestionIdentifier(jsonObject.getString("next_question_identifier"));
            if (jsonObject.isNull("deleted_at")) {
                conditionSkip.setDeleted(false);
            } else {
                conditionSkip.setDeleted(true);
            }
            conditionSkip.save();
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static List<ConditionSkip> getAll(Long instrumentId) {
        return new Select().from(ConditionSkip.class).where(
                "RemoteInstrumentId = ? AND Deleted != ?", instrumentId, 1).execute();
    }

    public String getQuestionIdentifier() {
        return mQuestionIdentifier;
    }

    public String getOptionIdentifier() {
        return mOptionIdentifier;
    }

    public boolean getDeleted() {
        return mDeleted;
    }

    public static ConditionSkip findByRemoteId(Long id) {
        return new Select().from(ConditionSkip.class).where("RemoteId = ?", id).executeSingle();
    }

    private void setRemoteId(Long id) {
        mRemoteId = id;
    }

    private void setQuestionIdentifier(String id) {
        mQuestionIdentifier = id;
    }

    private void setOptionIdentifier(String id) {
        mOptionIdentifier = id;
    }

    private void setNextQuestionIdentifier(String id) {
        mNextQuestionIdentifier = id;
    }

    private void setRemoteQuestionId(Long id) {
        mRemoteQuestionId = id;
    }

    private void setRemoteInstrumentId(Long id) {
        mRemoteInstrumentId = id;
    }

    private void setConditionQuestionIdentifier(String cqi) {
        mConditionQuestionIdentifier = cqi;
    }

    private void setConditionOptionIdentifier(String coi) {
        mConditionOptionIdentifier = coi;
    }

    private void setCondition(String condition) {
        mCondition = condition;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    private Question getConditionQuestion() {
        return new Select().from(Question.class)
                .where("QuestionIdentifier = ? AND InstrumentRemoteId = ?",
                        mConditionQuestionIdentifier, mRemoteInstrumentId)
                .executeSingle();
    }

    private Option getConditionOption() {
        return new Select().from(Option.class)
                .where("Identifier = ?", mConditionOptionIdentifier)
                .executeSingle();
    }

    private Question getQuestion() {
        return new Select().from(Question.class).where("QuestionIdentifier = ?",
                mQuestionIdentifier)
                .executeSingle();
    }

    private int getConditionOptionIndex() {
        return getConditionQuestion().defaultOptions().indexOf(getConditionOption());
    }

    private Option getOption() {
        return new Select().from(Option.class)
                .where("Identifier = ?", mOptionIdentifier)
                .executeSingle();
    }

    public String regularSkipTo(Response response) {
        Response conditionResponse = response.getSurvey().getResponseByQuestion(getConditionQuestion());
        if (TextUtils.isEmpty(conditionResponse.getText())) return null;
        if (mCondition.equals(Condition.ONLY.toString())) {
            if (getConditionQuestion().hasMultipleResponses()) {
                if (getResponseStrings(conditionResponse).length == 1 && getConditionOptionIndex()
                        == Integer.parseInt(conditionResponse.getText())) {
                        return mNextQuestionIdentifier;
                }
            }
        } else if (mCondition.equals(Condition.ONLY_AND.toString())) {
            if (getConditionQuestion().hasMultipleResponses()) {
                if (getResponseStrings(conditionResponse).length == 1 &&
                        getConditionOptionIndex() == Integer.parseInt(conditionResponse.getText())
                        && getQuestion().defaultOptions().indexOf(getOption())
                        == Integer.parseInt(response.getText())) {
                    return mNextQuestionIdentifier;
                }
            }
        }
        return null;
    }

    public String specialSkipTo(Response response) {
        Response conditionResponse = getConditionResponse(response);
        if (TextUtils.isEmpty(conditionResponse.getText())) return null;
        if (mCondition.equals(Condition.ONLY.toString())) {
            if (getConditionQuestion().hasMultipleResponses()) {
                if ((getResponseStrings(conditionResponse).length == 1) &&
                getConditionOptionIndex() == Integer.parseInt(conditionResponse.getText())) {
                    return mNextQuestionIdentifier;
                }
            }
        } else if (mCondition.equals(Condition.AND.toString())) {
            if (getConditionQuestion().hasMultipleResponses()) {
                String coIndex = String.valueOf(getConditionOptionIndex());
                if (Arrays.asList(getResponseStrings(conditionResponse)).contains(coIndex) &&
                        getOption() == getQuestion().specialOptionByText(
                                response.getSpecialResponse())) {
                    return mNextQuestionIdentifier;
                }
            }
        } else if (mCondition.equals(Condition.ONLY_AND.toString())) {
            if (getConditionQuestion().hasMultipleResponses()) {
                if (getResponseStrings(conditionResponse).length == 1 && getConditionOptionIndex()
                        == Integer.parseInt(conditionResponse.getText())) {
                        return mNextQuestionIdentifier;
                }
            }
        }
        return null;
    }

    @NonNull
    private String[] getResponseStrings(Response response) {
        return response.getText().split(Response.LIST_DELIMITER);
    }

    private Response getConditionResponse(Response response) {
       return response.getSurvey().getResponseByQuestion(getConditionQuestion());
    }

    public enum Condition {
        ONLY, ONLY_AND, AND
    }
}
