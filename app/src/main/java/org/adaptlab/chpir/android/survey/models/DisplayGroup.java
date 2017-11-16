package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "DisplayGroups")
public class DisplayGroup extends Model {
    private static final String TAG = "DisplayGroup";
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Title")
    private String mTitle;
    @Column(name = "Position")
    private int mPosition;
    @Column(name = "RandomizedDisplayGroup")
    private RandomizedDisplayGroup mRandomizedDisplayGroup;

    public static DisplayGroup findByRemoteId(long id) {
        return new Select().from(DisplayGroup.class).where("RemoteId = ?", id).executeSingle();
    }

    public void setRemoteId(Long remoteId) {
        mRemoteId = remoteId;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setPosition(Integer position) {
        mPosition = position;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setRandomizedDisplayGroup(RandomizedDisplayGroup group) {
        mRandomizedDisplayGroup = group;
    }

    public List<Question> questions() {
        return new Select().from(Question.class).where("DisplayGroup = ?", getId()).orderBy("NumberInInstrument ASC").execute();
    }

    public String getTitle() {
        return mTitle;
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

}
