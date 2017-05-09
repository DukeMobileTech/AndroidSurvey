package org.adaptlab.chpir.android.survey.models;

import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "Grids")
public class Grid extends ReceiveModel {
    private static final String TAG = "Grid";
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Name")
    private String mName;
    @Column(name = "QuestionType")
    private String mQuestionType;
    @Column(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;
    @Column(name = "Deleted")
    private boolean mDeleted;
    @Column(name = "Instructions")
    private String mInstructions;

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            Grid grid = Grid.findByRemoteId(remoteId);
            if (grid == null) {
                grid = this;
            }
            grid.setRemoteId(remoteId);
            grid.setQuestionType(jsonObject.getString("question_type"));
            grid.setName(jsonObject.getString("name"));
            grid.setInstrumentRemoteId(jsonObject.getLong("instrument_id"));
            grid.setInstructions(jsonObject.getString("instructions"));
            if (!jsonObject.isNull("deleted_at")) {
                grid.setDeleted(true);
            }
            grid.save();
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static Grid findByRemoteId(Long remoteId) {
        return new Select().from(Grid.class).where("RemoteId = ?", remoteId).executeSingle();
    }

    private void setQuestionType(String questionType) {
        mQuestionType = questionType;
    }

    private void setName(String name) {
        mName = name;
    }

    private void setInstrumentRemoteId(Long instrumentId) {
        mInstrumentRemoteId = instrumentId;
    }

    private void setInstructions(String instructions) {
        mInstructions = instructions;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    private void setRemoteId(Long remoteId) {
        mRemoteId = remoteId;
    }

    public List<Question> questions() {
        return new Select()
                .from(Question.class)
                .where("Grid = ? AND Deleted != ?", getId(), 1)
                .orderBy("NumberInGrid ASC")
                .execute();
    }

    public List<GridLabel> labels() {
        return new Select()
                .from(GridLabel.class)
                .where("Grid = ? AND Deleted != ?", getId(), 1)
                .orderBy("Position ASC")
                .execute();
    }

    // TODO: 5/3/17 Add support for translations
    public String getText() {
        if (TextUtils.isEmpty(mInstructions) || mInstructions.equals("null")) {
            return mName;
        } else {
            return mName + "<p> </p>" + mInstructions;
        }
    }
}