package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.json.JSONException;
import org.json.JSONObject;

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

    public Grid getGrid() {
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
        return mLabel;
    }
}
