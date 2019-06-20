package org.adaptlab.chpir.android.survey.utils;

import org.adaptlab.chpir.android.survey.entities.Translatable;
import org.adaptlab.chpir.android.survey.entities.Translation;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.util.List;

public class TranslationUtil {
    public static String getText(Translatable translatable, List<? extends Translation> translations, SurveyViewModel viewModel) {
        if (viewModel.getInstrumentLanguage().equals(viewModel.getDeviceLanguage()) || translations == null || translations.isEmpty())
            return translatable.getText();
        for (Translation translation : translations) {
            if (translation.getLanguage().equals(viewModel.getDeviceLanguage()))
                return translation.getText();
        }
        return translatable.getText();
    }
}
