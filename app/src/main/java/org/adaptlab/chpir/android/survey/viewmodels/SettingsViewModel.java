package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.repositories.SettingsRepository;

import java.util.List;

public class SettingsViewModel extends AndroidViewModel {
    private final SettingsRepository mSettingsRepository;
    private final LiveData<Settings> mSettings;
    private final LiveData<List<String>> mLanguages;
    private boolean mSetting = false;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        mSettingsRepository = new SettingsRepository(application);
        mSettings = mSettingsRepository.getSettingsDao().getInstance();
        mLanguages = mSettingsRepository.getSettingsDao().languages();
    }

    public LiveData<Settings> getSettings() {
        return mSettings;
    }

    public LiveData<List<String>> getLanguages() {
        return mLanguages;
    }

    public void update(Settings settings) {
        mSettingsRepository.update(settings);
    }

    public boolean isSetting() {
        return mSetting;
    }

    public void setSetting(boolean setting) {
        this.mSetting = setting;
    }
}
