package org.adaptlab.chpir.android.survey.viewmodels;

import android.app.Application;
import android.os.Build;
import android.util.Log;
import android.util.LongSparseArray;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Section;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;

public class SurveyViewModel extends AndroidViewModel {
    public final String TAG = this.getClass().getName();
    private final SurveyRepository mSurveyRepository;
    private LiveData<Survey> mLiveDataSurvey;
    private HashSet<String> mQuestionsToSkipSet;
    private HashMap<String, List<String>> mQuestionsToSkipMap;
    private HashMap<String, Question> mQuestionsMap;
    private HashMap<String, Response> mResponses;
    private HashMap<String, Question> mQuestionsWithoutResponses;
    private LongSparseArray<Section> mSections;
    private LinkedHashMap<String, List<String>> mExpandableListData;
    private LongSparseArray<String> mDisplayTitles;
    private List<Question> mQuestions;
    private List<String> mExpandableListTitle;
    private List<Display> mDisplays;
    private List<Integer> mPreviousDisplays;
    private List<String> mLocations;

    private Survey mSurvey;

    private int mDisplayPosition;
    private String mDeviceLanguage;
    private String mInstrumentLanguage;

    public SurveyViewModel(@NonNull Application application, String uuid) {
        super(application);
        mSurveyRepository = new SurveyRepository(application);
        if (uuid == null) return;
        mLiveDataSurvey = mSurveyRepository.getSurveyDao().findByUUID(uuid);
        mDisplayTitles = new LongSparseArray<>();
        mPreviousDisplays = new ArrayList<>();
    }

    public void addDisplayTitle(Long displayId, String translation) {
        mDisplayTitles.put(displayId, translation);
    }

    public String getDisplayTitle(Long displayId) {
        return mDisplayTitles.get(displayId);
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
        if (mQuestionsToSkipSet == null) return new HashSet<>();
        return mQuestionsToSkipSet;
    }

    public Display lastDisplay () {
        return mDisplays.get(mDisplayPosition);
    }

    public int getDisplayPosition() {
        return mDisplayPosition;
    }

    public void setDisplayPosition(int position) {
        mDisplayPosition = position;
    }

    public void decrementDisplayPosition() {
        mDisplayPosition -= 1;
    }

    public void incrementDisplayPosition() {
        mDisplayPosition += 1;
    }

    public HashMap<String, Question> getQuestionsMap() {
        return mQuestionsMap;
    }

    public List<Question> getQuestions() {
        return mQuestions;
    }

    public void setQuestions(List<Question> questions) {
        mQuestions = questions;
        mQuestionsMap = new HashMap<>();
        for (Question question : questions) {
            mQuestionsMap.put(question.getQuestionIdentifier(), question);
        }
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

    public void setPreviousDisplays(List<Integer> previousDisplays) {
        mPreviousDisplays = previousDisplays;
    }

    public LongSparseArray<Section> getSections() {
        return mSections;
    }

    public void setSections(LongSparseArray<Section> sections) {
        mSections = sections;
    }

    public LinkedHashMap<String, List<String>> getExpandableListData() {
        return mExpandableListData;
    }

    public void setExpandableListData(LinkedHashMap<String, List<String>> listData) {
        mExpandableListData = listData;
    }

    public List<String> getExpandableListTitle() {
        return mExpandableListTitle;
    }

    public void setExpandableListTitle(List<String> listTitle) {
        mExpandableListTitle = listTitle;
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
        if (mQuestionsToSkipMap == null) return;
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

    public void persistPreviousDisplays() {
        mSurvey.setPreviousDisplays(StringUtils.join(mPreviousDisplays, COMMA));
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

    public void setSurveyLastDisplayPosition() {
        mSurvey.setLastDisplayPosition(mDisplayPosition);
    }

    public void setSurveyLastUpdatedTime() {
        mSurvey.setLastUpdated(new Date());
    }

    public void setSurveyComplete() {
        mSurvey.setComplete(true);
    }

    public HashMap<String, Response> getResponses() {
        return mResponses;
    }

    public void setResponses(HashMap<String, Response> map) {
        mResponses = map;
    }

    public void setResponse(String questionIdentifier, Response response) {
        mResponses.put(questionIdentifier, response);
    }

    public String getDeviceLanguage() {
        return mDeviceLanguage;
    }

    public void setDeviceLanguage(String language) {
        mDeviceLanguage = language;
    }

    public String getInstrumentLanguage() {
        return mInstrumentLanguage;
    }

    public void setInstrumentLanguage(String language) {
        mInstrumentLanguage = language;
    }

    public void setSurveyLanguage() {
        mSurvey.setLanguage(mDeviceLanguage);
    }

    public void setQuestionsWithoutResponses() {
        mQuestionsWithoutResponses = new HashMap<>(getQuestionsMap());
        for (String identifier : getQuestionsToSkipSet()) {
            mQuestionsWithoutResponses.remove(identifier);
        }
        for (Map.Entry<String, Response> entry : getResponses().entrySet()) {
            if (!entry.getValue().isEmptyResponse()) {
                mQuestionsWithoutResponses.remove(entry.getKey());
            }
        }
        HashMap<String, Question> map = new HashMap<>(mQuestionsWithoutResponses);
        for (Map.Entry<String, Question> entry : mQuestionsWithoutResponses.entrySet()) {
            if (entry.getValue().isDeleted() || entry.getValue().getQuestionType() == null ||
                    entry.getValue().getQuestionType().equals(Question.INSTRUCTIONS)) {
                map.remove(entry.getKey());
            }
        }
        mQuestionsWithoutResponses = map;
    }

    public HashMap<String, Question> getQuestionsWithoutResponses() {
        return mQuestionsWithoutResponses;
    }

    public List<String> getLocations() {
        return mLocations;
    }

    public void setLocations(List<String> mLocations) {
        this.mLocations = mLocations;
    }
}
