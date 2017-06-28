package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "GridLabelTranslations")
public class GridLabelTranslation extends Model {

    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "InstrumentTranslation")
    private InstrumentTranslation mInstrumentTranslation;
    @Column(name = "GridLabel")
    private GridLabel mGridLabel;
    @Column(name = "Label")
    private String mLabel;

    public static GridLabelTranslation findByRemoteId(Long remoteId) {
        return new Select().from(GridLabelTranslation.class).where("RemoteId = ?", remoteId)
                .executeSingle();
    }

    private InstrumentTranslation getInstrumentTranslation() {
        return mInstrumentTranslation;
    }

    public void setInstrumentTranslation(InstrumentTranslation translation) {
        mInstrumentTranslation = translation;
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }

    public GridLabel getGrid() {
        return mGridLabel;
    }

    public void setGridLabel(GridLabel grid) {
        mGridLabel = grid;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String text) {
        mLabel = text;
    }

    public String getLanguage() {
        return getInstrumentTranslation().getLanguage();
    }
}