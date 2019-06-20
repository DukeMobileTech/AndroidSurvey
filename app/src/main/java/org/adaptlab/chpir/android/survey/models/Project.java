package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "Projects")
public class Project extends ReceiveModel {
    private static final String TAG = "Project";

    @Column(name = "Name")
    private String mName;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Description")
    private String mDescription;

    public static Project findByRemoteId(Long id) {
        return new Select().from(Project.class).where("RemoteId = ?", id).executeSingle();
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            Project project = Project.findByRemoteId(remoteId);
            if (project == null) {
                project = this;
            }
            project.setRemoteId(remoteId);
            project.setName(jsonObject.getString("name"));
            project.setDescription(jsonObject.getString("description"));
            project.save();
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public String getName() {
        return mName;
    }

    private void setName(String name) {
        mName = name;
    }

    private void setRemoteId(Long id) {
        mRemoteId = id;
    }

    private void setDescription(String description) {
        mDescription = description;
    }

}