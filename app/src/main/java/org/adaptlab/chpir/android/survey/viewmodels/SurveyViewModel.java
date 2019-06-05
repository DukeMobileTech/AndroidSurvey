package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;

public class SurveyViewModel extends AndroidViewModel {
    public final String TAG = this.getClass().getName();
    private LiveData<Survey> mLiveDataSurvey;
    private SurveyRepository mSurveyRepository;
    private HashSet<String> mQuestionsToSkipSet;
    private HashMap<String, List<String>> mQuestionsToSkipMap;
    private Survey mSurvey;
    private int mDisplayPosition = 0;
    private List<Question> mQuestions;
    private List<Display> mDisplays;
    private List<Integer> mPreviousDisplays;

    public SurveyViewModel(@NonNull Application application, String uuid) {
        super(application);
        mSurveyRepository = new SurveyRepository(application);
        if (uuid == null) return;
        mLiveDataSurvey = mSurveyRepository.getSurveyDao().findByUUID(uuid);
    }

    public LiveData<Survey> getLiveDataSurvey() {
        return mLiveDataSurvey;
    }

    public Survey getSurvey() {
        return mSurvey;
    }

    public void setSurvey(Survey survey) {
        mSurvey = survey;
    }

    public HashSet<String> getQuestionsToSkipSet() {
        return mQuestionsToSkipSet;
    }

    public void setDisplayPosition(int position) {
        mDisplayPosition = position;
    }

    public int getDisplayPosition() {
        return mDisplayPosition;
    }

    public void decrementDisplayPosition() {
        mDisplayPosition -=1;
    }

    public void incrementDisplayPosition() {
        mDisplayPosition +=1;
    }

    public List<Question> getQuestions() {
        return mQuestions;
    }

    public void setQuestions(List<Question> mQuestions) {
        this.mQuestions = mQuestions;
    }

    public List<Display> getDisplays() {
        return mDisplays;
    }

    public void setDisplays(List<Display> mDisplays) {
        this.mDisplays = mDisplays;
    }

    public List<Integer> getPreviousDisplays() {
        return mPreviousDisplays;
    }

    public void setPreviousDisplays(List<Integer> mPreviousDisplays) {
        this.mPreviousDisplays = mPreviousDisplays;
    }

    public void setSkipData() {
        mQuestionsToSkipSet = new HashSet<>();
        mQuestionsToSkipMap = new HashMap<>();
        if (mSurvey.getSkippedQuestions() != null) {
            mQuestionsToSkipSet = new HashSet<>(Arrays.asList(mSurvey.getSkippedQuestions().split(COMMA)));
        }
        if (mSurvey.getSkipMaps() != null) {
            try {
                JSONObject jsonObject = new JSONObject(mSurvey.getSkipMaps());
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String skipString = jsonObject.getString(key);
                    String[] skipArray = skipString.split(COMMA);
                    mQuestionsToSkipMap.put(key, Arrays.asList(skipArray));
                }
                updateQuestionsToSkipSet();
            } catch (JSONException e) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Exception: ", e);
            }
        }
    }

    public void persistSkipMaps() {
        try {
            JSONObject jsonObject = new JSONObject();
            for (HashMap.Entry<String, List<String>> pair : mQuestionsToSkipMap.entrySet()) {
                if (pair.getValue() != null && pair.getValue().size() != 0) {
                    StringBuilder serialized = new StringBuilder();
                    int count = 0;
                    for (String question : pair.getValue()) {
                        serialized.append(question);
                        if (count < pair.getValue().size() - 1)
                            serialized.append(COMMA);
                        count += 1;
                    }
                    jsonObject.put(pair.getKey(), serialized.toString());
                }
            }
            mSurvey.setSkipMaps(jsonObject.toString());
        } catch (JSONException je) {
            if (BuildConfig.DEBUG) Log.e(TAG, "JSON exception", je);
        }
    }

    public void persistSkippedQuestions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSurvey.setSkippedQuestions(String.join(COMMA, mQuestionsToSkipSet));
        } else {
            StringBuilder serialized = new StringBuilder();
            int count = 0;
            for (String identifier : mQuestionsToSkipSet) {
                serialized.append(identifier);
                if (count < mQuestionsToSkipSet.size() - 1)
                    serialized.append(COMMA);
                count += 1;
            }
            mSurvey.setSkippedQuestions(serialized.toString());
        }
    }

    public void update() {
        mSurveyRepository.update(mSurvey);
    }

    public void updateQuestionsToSkipSet() {
        mQuestionsToSkipSet = new HashSet<>();
        for (HashMap.Entry<String, List<String>> curPair : mQuestionsToSkipMap.entrySet()) {
            mQuestionsToSkipSet.addAll(curPair.getValue());
        }
    }

    public void updateQuestionsToSkipMap(String questionIdentifier, List<String> questionsToSkip) {
        if (questionsToSkip.size() == 0) {
            mQuestionsToSkipMap.remove(questionIdentifier);
        } else {
            mQuestionsToSkipMap.put(questionIdentifier, questionsToSkip);
        }
    }

}
