package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.AppUtil;
import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "QuestionRandomizedFactors")
public class QuestionRandomizedFactor extends ReceiveModel {
    private static final String TAG = "QuestionRandomizedFactor";
    @Column(name = "Question")
    private Question mQuestion;
    @Column(name = "RandomizedFactor")
    private RandomizedFactor mRandomizedFactor;
    // https://github.com/pardom/ActiveAndroid/issues/22
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Position")
    private int mPosition;

    public QuestionRandomizedFactor() {
        super();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");

            // If a factor already exists, update it from the remote
            QuestionRandomizedFactor qrFactor = QuestionRandomizedFactor.findByRemoteId(remoteId);
            if (qrFactor == null) {
                qrFactor = this;
            }

            if (AppUtil.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            qrFactor.setPosition(jsonObject.getInt("position"));
            qrFactor.setQuestion(Question.findByRemoteId(jsonObject.getLong("question_id")));
            qrFactor.setRandomizedFactor(RandomizedFactor.findByRemoteId(jsonObject.getLong("randomized_factor_id")));
            qrFactor.setRemoteId(remoteId);
            qrFactor.save();
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static QuestionRandomizedFactor findByRemoteId(Long id) {
        return new Select().from(QuestionRandomizedFactor.class).where("RemoteId = ?", id).executeSingle();
    }

    public RandomizedFactor getRandomizedFactor() {
        return mRandomizedFactor;
    }

    private void setPosition(int position) {
        mPosition = position;
    }

    private void setQuestion(Question question) {
        mQuestion = question;
    }

    private void setRemoteId(Long remoteId) {
        mRemoteId = remoteId;
    }

    private void setRandomizedFactor(RandomizedFactor randomizedFactor) {
        mRandomizedFactor = randomizedFactor;
    }
}
