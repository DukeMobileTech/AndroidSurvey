package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import org.adaptlab.chpir.android.survey.entities.InstrumentTranslation;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.repositories.SettingsRepository;

import java.util.List;

public class SettingsViewModel extends AndroidViewModel {
    private SettingsRepository mSettingsRepository;
    private LiveData<Settings> mSettings;
    private LiveData<List<InstrumentTranslation>> allLanguages;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        mSettingsRepository = new SettingsRepository(application);
        mSettings = mSettingsRepository.getSettings();
        allLanguages = mSettingsRepository.allLanguages();
    }

    public LiveData<Settings> getSettings() {
        return mSettings;
    }

    public LiveData<List<InstrumentTranslation>> getAllLanguages() {
        return allLanguages;
    }

    public void updateSettings(Settings settings) {
        mSettingsRepository.update(settings);
    }

}
