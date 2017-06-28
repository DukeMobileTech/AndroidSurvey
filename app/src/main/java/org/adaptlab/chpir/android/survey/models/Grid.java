package org.adaptlab.chpir.android.survey.models;

import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.json.JSONArray;
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

            JSONArray translationsArray = jsonObject.optJSONArray("grid_translations");
            if (translationsArray != null) {
                for (int i = 0; i < translationsArray.length(); i++) {
                    JSONObject translationJSON = translationsArray.getJSONObject(i);
                    Long translationRemoteId = translationJSON.getLong("id");
                    GridTranslation translation = GridTranslation.findByRemoteId
                            (translationRemoteId);
                    if (translation == null) {
                        translation = new GridTranslation();
                    }
                    translation.setRemoteId(translationRemoteId);
                    translation.setGrid(grid);
                    translation.setName(translationJSON.getString("name"));
                    translation.setInstrumentTranslation(InstrumentTranslation.findByRemoteId(
                            translationJSON.optLong("instrument_translation_id")));
                    if (!translationJSON.isNull("instructions")) {
                        translation.setInstructions(translationJSON.getString("instructions"));
                    }
                    translation.save();
                }
            }
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

    public String getText() {
        if (getInstrument().getLanguage().equals(Instrument.getDeviceLanguage())) {
            if (TextUtils.isEmpty(mInstructions) || mInstructions.equals("null")) {
                return mName;
            } else {
                return mName + "<p> </p>" + mInstructions;
            }
        } else {
            if (activeTranslation() != null) {
                if (TextUtils.isEmpty(activeTranslation().getInstructions()) ||
                        activeTranslation().getInstructions().equals("null")) {
                    return activeTranslation().getName();
                }
                return activeTranslation().getName() + "<p> </p>" + activeTranslation()
                        .getInstructions();
            } else {
                for (GridTranslation translation : translations()) {
                    if (translation.getLanguage().equals(Instrument.getDeviceLanguage())) {
                        return translation.getName() + "<p> </p>" + translation.getInstructions();
                    }
                }
            }
        }
        //fallback
        return mName + "<p> </p>" + mInstructions;
    }

    public Instrument getInstrument() {
        return Instrument.findByRemoteId(mInstrumentRemoteId);
    }

    private GridTranslation activeTranslation() {
        if (getInstrument().activeTranslation() == null) return null;
        return new Select().from(GridTranslation.class)
                .where("InstrumentTranslation = ? AND Grid = ?",
                        getInstrument().activeTranslation().getId(), getId()).executeSingle();
    }

    private List<GridTranslation> translations() {
        return getMany(GridTranslation.class, "Grid");
    }

}