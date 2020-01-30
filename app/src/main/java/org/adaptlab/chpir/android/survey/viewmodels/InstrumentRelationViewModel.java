package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.util.LongSparseArray;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.relations.DisplayRelation;
import org.adaptlab.chpir.android.survey.relations.InstrumentRelation;
import org.adaptlab.chpir.android.survey.relations.SectionRelation;
import org.adaptlab.chpir.android.survey.repositories.InstrumentRelationRepository;

import java.util.List;

public class InstrumentRelationViewModel extends AndroidViewModel {
    private LiveData<InstrumentRelation> mInstrumentRelation;
    private LongSparseArray<SectionRelation> mSections;
    private LongSparseArray<DisplayRelation> mDisplays;

    public InstrumentRelationViewModel(@NonNull Application application, Long instrumentId) {
        super(application);
        InstrumentRelationRepository repository = new InstrumentRelationRepository(application, instrumentId);
        mInstrumentRelation = repository.getInstrumentRelation();
    }

    public LiveData<InstrumentRelation> getInstrumentRelation() {
        return mInstrumentRelation;
    }

    public SectionRelation getSection(Long id) {
        return mSections.get(id);
    }

    public DisplayRelation getDisplay(Long id) {
        return mDisplays.get(id);
    }

    public void addSectionRelations(List<SectionRelation> sectionRelations) {
        mSections = new LongSparseArray<>();
        mDisplays = new LongSparseArray<>();
        for (SectionRelation sectionRelation : sectionRelations) {
            mSections.put(sectionRelation.section.getRemoteId(), sectionRelation);
            for (DisplayRelation displayRelation : sectionRelation.displays) {
                mDisplays.put(displayRelation.display.getRemoteId(), displayRelation);
            }
        }
    }
}
