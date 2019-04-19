package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "MultipleSkips")
public class MultipleSkip extends ReceiveModel {
    private static final String TAG = "MultipleSkip";

    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "QuestionIdentifier")
    private String mQuestionIdentifier;
    @Column(name = "OptionIdentifier")
    private String mOptionIdentifier;
    @Column(name = "SkipQuestionIdentifier")
    private String mSkipQuestionIdentifier;
    @Column(name = "RemoteQuestionId")
    private Long mRemoteQuestionId;
    @Column(name = "RemoteInstrumentId")
    private Long mRemoteInstrumentId;
    @Column(name = "Deleted")
    private boolean mDeleted;
    @Column(name = "Value")
    private String mValue;

    public MultipleSkip() {
        super();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            MultipleSkip skipQuestion = MultipleSkip.findByRemoteId(remoteId);
            if (skipQuestion == null) {
                skipQuestion = this;
            }
            if (BuildConfig.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            skipQuestion.setRemoteId(remoteId);
            skipQuestion.setQuestionIdentifier(jsonObject.getString("question_identifier"));
            skipQuestion.setOptionIdentifier(jsonObject.getString("option_identifier"));
            skipQuestion.setSkipQuestionIdentifier(jsonObject.getString("skip_question_identifier"));
            skipQuestion.setRemoteQuestionId(jsonObject.getLong("question_id"));
            skipQuestion.setRemoteInstrumentId(jsonObject.getLong("instrument_id"));
            if (jsonObject.isNull("deleted_at")) {
                skipQuestion.setDeleted(false);
            } else {
                skipQuestion.setDeleted(true);
            }
            skipQuestion.setValue(jsonObject.optString("value", null));
            skipQuestion.save();
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static MultipleSkip findByRemoteId(Long id) {
        return new Select().from(MultipleSkip.class).where("RemoteId = ?", id).executeSingle();
    }

    public static List<MultipleSkip> getAll(Long instrumentId) {
        return new Select().from(MultipleSkip.class).where(
                "RemoteInstrumentId = ? AND Deleted != ?", instrumentId, 1).execute();
    }

    private void setRemoteId(Long id) {
        mRemoteId = id;
    }

    void setQuestionIdentifier(String id) {
        mQuestionIdentifier = id;
    }

    void setOptionIdentifier(String id) {
        mOptionIdentifier = id;
    }

    public String getOptionIdentifier() {
        return mOptionIdentifier;
    }

    void setSkipQuestionIdentifier(String id) {
        mSkipQuestionIdentifier = id;
    }

    public String getSkipQuestionIdentifier() {
        return mSkipQuestionIdentifier;
    }

    private void setRemoteQuestionId(Long id) {
        mRemoteQuestionId = id;
    }

    void setRemoteInstrumentId(Long id) {
        mRemoteInstrumentId = id;
    }

    Long getRemoteInstrumentId() {
        return mRemoteInstrumentId;
    }

    void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    boolean getDeleted() {
        return mDeleted;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public String getQuestionIdentifier() {
        return mQuestionIdentifier;
    }
}
