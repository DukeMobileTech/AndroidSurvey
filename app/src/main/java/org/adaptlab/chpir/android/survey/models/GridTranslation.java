package org.adaptlab.chpir.android.survey.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "GridTranslations")
public class GridTranslation extends Model {

    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "InstrumentTranslation")
    private InstrumentTranslation mInstrumentTranslation;
    @Column(name = "Grid")
    private Grid mGrid;
    @Column(name = "Name")
    private String mName;
    @Column(name = "Instructions")
    private String mInstructions;

    public static GridTranslation findByRemoteId(Long remoteId) {
        return new Select().from(GridTranslation.class).where("RemoteId = ?", remoteId)
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

    public String getInstructions() {
        return mInstructions;
    }

    public void setInstructions(String instructions) {
        mInstructions = instructions;
    }

    public Grid getGrid() {
        return mGrid;
    }

    public void setGrid(Grid grid) {
        mGrid = grid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String text) {
        mName = text;
    }

    public String getLanguage() {
        return getInstrumentTranslation().getLanguage();
    }

}