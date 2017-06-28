package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "GridLabels")
public class GridLabel extends ReceiveModel {
    private static final String TAG = "GridLabel";
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Grid")
    private Grid mGrid;
    @Column(name = "Label")
    private String mLabel;
    @Column(name = "Deleted")
    private boolean mDeleted;
    @Column(name = "Position")
    private int mPosition;

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            GridLabel gridLabel = GridLabel.findByRemoteId(remoteId);
            if (gridLabel == null) {
                gridLabel = this;
            }
            gridLabel.setRemoteId(remoteId);
            gridLabel.setLabel(jsonObject.getString("label"));
            gridLabel.setGrid(Grid.findByRemoteId(jsonObject.getLong("grid_id")));
            gridLabel.setPosition(jsonObject.optInt("position"));
            if (!jsonObject.isNull("deleted_at")) {
                gridLabel.setDeleted(true);
            }
            gridLabel.save();

            JSONArray translationsArray = jsonObject.optJSONArray("grid_label_translations");
            if (translationsArray != null) {
                for (int i = 0; i < translationsArray.length(); i++) {
                    JSONObject translationJSON = translationsArray.getJSONObject(i);
                    Long translationRemoteId = translationJSON.getLong("id");
                    GridLabelTranslation translation = GridLabelTranslation.findByRemoteId(translationRemoteId);
                    if (translation == null) {
                        translation = new GridLabelTranslation();
                    }
                    translation.setRemoteId(translationRemoteId);
                    translation.setGridLabel(gridLabel);
                    translation.setLabel(translationJSON.getString("label"));
                    translation.setInstrumentTranslation(InstrumentTranslation.findByRemoteId(
                            translationJSON.optLong("instrument_translation_id")));
                    translation.save();
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static GridLabel findByRemoteId(Long remoteId) {
        return new Select().from(GridLabel.class).where("RemoteId = ?", remoteId).executeSingle();
    }

    private void setLabel(String label) {
        mLabel = label;
    }

    public int getPosition() {
        return mPosition;
    }

    private void setPosition(int position) {
        mPosition = position;
    }

    public boolean getDeleted() {
        return mDeleted;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    private Grid getGrid() {
        return mGrid;
    }

    private void setGrid(Grid grid) {
        mGrid = grid;
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(Long remoteId) {
        mRemoteId = remoteId;
    }

    public String getLabelText() {
        if (getInstrument().getLanguage().equals(Instrument.getDeviceLanguage())) return mLabel;
        if (activeTranslation() != null) {
            return activeTranslation().getLabel();
        }
        for (GridLabelTranslation translation : translations()) {
            if (translation.getLanguage().equals(Instrument.getDeviceLanguage())) {
                return translation.getLabel();
            }
        }
        return mLabel;
    }

    private GridLabelTranslation activeTranslation() {
        if (getInstrument().activeTranslation() == null) return null;
        return new Select().from(GridLabelTranslation.class)
                .where("InstrumentTranslation = ? AND GridLabel = ?",
                        getInstrument().activeTranslation().getId(), getId()).executeSingle();
    }

    private Instrument getInstrument() {
        return getGrid().getInstrument();
    }

    private List<GridLabelTranslation> translations() {
        return getMany(GridLabelTranslation.class, "GridLabel");
    }
}
