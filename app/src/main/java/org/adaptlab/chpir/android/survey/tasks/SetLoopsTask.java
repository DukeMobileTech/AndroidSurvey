package org.adaptlab.chpir.android.survey.tasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;

import androidx.annotation.NonNull;

import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.DisplayDao;
import org.adaptlab.chpir.android.survey.daos.InstrumentDao;
import org.adaptlab.chpir.android.survey.daos.LoopQuestionDao;
import org.adaptlab.chpir.android.survey.daos.MultipleSkipDao;
import org.adaptlab.chpir.android.survey.daos.NextQuestionDao;
import org.adaptlab.chpir.android.survey.daos.QuestionDao;
import org.adaptlab.chpir.android.survey.entities.Display;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.LoopQuestion;
import org.adaptlab.chpir.android.survey.entities.MultipleSkip;
import org.adaptlab.chpir.android.survey.entities.NextQuestion;
import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.relations.InstrumentRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionTranslationRelation;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.LOOP_MAX;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.LOWER_BOUND;
import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.UPPER_BOUND;

public class SetLoopsTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "SetLoopsTask";
    private DisplayDao mDisplayDao;
    private InstrumentDao mInstrumentDao;
    private QuestionDao mQuestionDao;
    private LoopQuestionDao mLoopQuestionDao;
    private NextQuestionDao mNextQuestionDao;
    private MultipleSkipDao mMultipleSkipDao;
    private List<Display> mDisplays;
    private LongSparseArray<Display> mDisplayLongSparseArray;

    @Override
    protected Void doInBackground(Void... voids) {
        SurveyRoomDatabase database = SurveyRoomDatabase.getDatabase(SurveyApp.getInstance());
        Settings settings = database.settingsDao().getInstanceSync();
        if (settings == null || TextUtils.isEmpty(settings.getProjectId())) return null;
        mDisplayDao = database.displayDao();
        mInstrumentDao = database.instrumentDao();
        mQuestionDao = database.questionDao();
        mLoopQuestionDao = database.loopQuestionDao();
        mNextQuestionDao = database.nextQuestionDao();
        mMultipleSkipDao = database.multipleSkipDao();

        List<Instrument> instruments = mInstrumentDao.projectInstrumentsSync(Long.valueOf(settings.getProjectId()));
        for (Instrument instrument : instruments) {
            mDisplays = mDisplayDao.instrumentDisplaysSync(instrument.getRemoteId());
            mDisplayLongSparseArray = new LongSparseArray<>();
            for (Display display : mDisplays) {
                mDisplayLongSparseArray.put(display.getRemoteId(), display);
            }
            List<Question> questions = mQuestionDao.instrumentQuestionsSync(instrument.getRemoteId());
            for (Question question : questions) {
                if (question.getLoopQuestionCount() > 0 && question.getLoopSource() == null) {
                    List<LoopQuestion> loopQuestions = mLoopQuestionDao.allLoopQuestionsSync(question.getRemoteId());
                    if (loopQuestions.size() > 0) {
                        if (question.getQuestionType().equals(Question.INTEGER)) {
                            for (LoopQuestion loopQuestion : loopQuestions) {
                                Display display = getDisplay(question, loopQuestion, loopQuestions);
                                for (int k = 1; k <= LOOP_MAX; k++) {
                                    createLoopQuestion(question, loopQuestion, k, display);
                                }
                            }
                        } else if (question.isMultipleResponseLoop()) {
                            for (LoopQuestion loopQuestion : loopQuestions) {
                                Display display = getDisplay(question, loopQuestion, loopQuestions);
                                if (TextUtils.isEmpty(loopQuestion.getOptionIndices())) {
                                    for (int k = 0; k < question.getOptionCount(); k++) {
                                        createLoopQuestion(question, loopQuestion, k, display);
                                    }
                                    if (question.isOtherQuestionType()) {
                                        createLoopQuestion(question, loopQuestion, question.getOptionCount(), display);
                                    }
                                } else {
                                    // Loop only for particular options
                                    String[] indices = loopQuestion.getOptionIndices().split(COMMA);
                                    for (String index : indices) {
                                        int ind = Integer.parseInt(index);
                                        createLoopQuestion(question, loopQuestion, ind, display);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            orderDisplays();
            setInstrumentLoaded(instrument);
        }
        return null;
    }

    private Display getDisplay(Question q, LoopQuestion lq, List<LoopQuestion> lqs) {
        Display display;
        Display parent = mDisplayLongSparseArray.get(q.getDisplayId());
        if (lq.isSameDisplay()) {
            parent.setQuestionCount(parent.getQuestionCount() + getDisplayQuestionCount(q, lq, lqs));
            display = parent;
        } else {
            display = mDisplayDao.findByTitleAndInstrumentIdSync(parent.getTitle() + " p2", q.getInstrumentRemoteId());
            if (display == null) {
                display = new Display();
                display.setTitle(parent.getTitle() + " p2");
                display.setInstrumentRemoteId(parent.getInstrumentRemoteId());
                display.setSectionId(parent.getSectionId());
                display.setRemoteId(getDisplayId());
                display.setDeleted(parent.isDeleted());
                mDisplayDao.insert(display);
                mDisplayLongSparseArray.put(display.getRemoteId(), display);
                int parentIndex = mDisplays.indexOf(parent);
                mDisplays.add(parentIndex + 1, display);
            }
            int questionCount = getDisplayQuestionCount(q, lq, lqs);
            if (display.getQuestionCount() != questionCount) {
                display.setQuestionCount(questionCount);
                mDisplayDao.update(display);
            }
        }
        return display;
    }

    private void setInstrumentLoaded(Instrument instrument) {
        InstrumentRelation instrumentRelation = mInstrumentDao.findInstrumentRelationByIdSync(instrument.getRemoteId());
        List<Question> questions = new ArrayList<>();
        for (QuestionTranslationRelation questionTranslationRelation : instrumentRelation.questions) {
            questions.add(questionTranslationRelation.question);
        }
        LongSparseArray<List<Question>> displayQuestions = getDisplayQuestions(questions);
        for (Display display : instrumentRelation.displays) {
            if (displayQuestions.get(display.getRemoteId()) != null) {
                if (display.getQuestionCount() != displayQuestions.get(display.getRemoteId()).size()) {
                    instrument.setLoaded(false);
                    mInstrumentDao.update(instrument);
                    return;
                }
            }
        }
        AppUtil.setLastSyncTime(AppUtil.getCurrentSyncTime());
        AppUtil.resetCurrentSyncTime();
        instrument.setLoaded(true);
        mInstrumentDao.update(instrument);
    }

    /**
     * Number displays consecutively
     */
    private void orderDisplays() {
        for (int k = 0; k < mDisplays.size(); k++) {
            Display display = mDisplays.get(k);
            if (display.getPosition() != k + 1) {
                display.setPosition(k + 1);
            }
        }
        mDisplayDao.updateAll(mDisplays);
    }

    private void createLoopQuestion(Question question, LoopQuestion lq, int index, Display display) {
        Question source = mQuestionDao.findByQuestionIdentifierSync(lq.getLooped());
        if (source == null) return;
        String identifier = question.getQuestionIdentifier() + "_" + source.getQuestionIdentifier() + "_" + index;
        Question loopedQuestion = mQuestionDao.findByQuestionIdentifierSync(identifier);
        if (loopedQuestion == null) {
            loopedQuestion = new Question();
            loopedQuestion.setRemoteId(getQuestionId());
            loopedQuestion.setDisplayId(display.getRemoteId());
            loopedQuestion.setLoopNumber(index);
            loopedQuestion.setQuestionIdentifier(identifier);
            mQuestionDao.insert(loopedQuestion);
        }
        loopedQuestion.setLoopSource(source.getQuestionIdentifier());
        loopedQuestion = Question.copyAttributes(loopedQuestion, source);
        if (lq.isSameDisplay()) {
            loopedQuestion.setNumberInInstrument(source.getNumberInInstrument());
        } else {
            loopedQuestion.setNumberInInstrument(source.getNumberInInstrument() + (index * question.getLoopQuestionCount()));
        }
        loopedQuestion.setTextToReplace(lq.getTextToReplace());
        if (lq.isDeleted()) {
            loopedQuestion.setDeleted(true);
            source.setLoopQuestionCount(mLoopQuestionDao.loopQuestionsSync(source.getRemoteId()).size());
            mQuestionDao.update(source);
        }
        mQuestionDao.update(loopedQuestion);

        setLoopedQuestionNextQuestions(question, source, loopedQuestion, index);
        setLoopedQuestionMultipleSkips(question, source, loopedQuestion, index);
        setSkipsLoopedQuestion(source, loopedQuestion);
    }

    private void setLoopedQuestionNextQuestions(Question question, Question source, Question loopedQuestion, int index) {
        List<NextQuestion> nextQuestions = mNextQuestionDao.questionNextQuestionsSync(source.getQuestionIdentifier(), source.getInstrumentRemoteId());
        String qi = loopedQuestion.getQuestionIdentifier();
        for (NextQuestion nextQuestion : nextQuestions) {
            String oi = nextQuestion.getOptionIdentifier();
            String nqi = question.getQuestionIdentifier() + "_" + nextQuestion.getNextQuestionIdentifier() + "_" + index;
            String value = nextQuestion.getValue();
            NextQuestion nq = mNextQuestionDao.findByAttributesSync(qi, oi, nqi, value);
            if (nq == null) {
                nq = new NextQuestion();
                nq.setQuestionIdentifier(qi);
                nq.setOptionIdentifier(oi);
                nq.setNextQuestionIdentifier(nqi);
                nq.setDeleted(nextQuestion.isDeleted());
                nq.setInstrumentRemoteId(nextQuestion.getInstrumentRemoteId());
                nq.setValue(value);
                mNextQuestionDao.insert(nq);
            }
        }
    }

    private void setLoopedQuestionMultipleSkips(Question question, Question source, Question loopedQuestion, int index) {
        List<MultipleSkip> multipleSkips = mMultipleSkipDao.questionMultipleSkipsSync(source.getQuestionIdentifier(), source.getInstrumentRemoteId());
        String qi = loopedQuestion.getQuestionIdentifier();
        for (MultipleSkip multipleSkip : multipleSkips) {
            String oi = multipleSkip.getOptionIdentifier();
            String val = multipleSkip.getValue();
            String sqi = question.getQuestionIdentifier() + "_" + multipleSkip.getSkipQuestionIdentifier() + "_" + index;
            createMultipleSkip(sqi, multipleSkip, qi, oi, val);
        }
    }

    private void setSkipsLoopedQuestion(Question source, Question loopedQuestion) {
        List<MultipleSkip> skipsQuestion = mMultipleSkipDao.skipsQuestionMultipleSkipsSync(source.getQuestionIdentifier(), source.getInstrumentRemoteId());
        String sqi = loopedQuestion.getQuestionIdentifier();
        for (MultipleSkip multipleSkip : skipsQuestion) {
            String qi = multipleSkip.getQuestionIdentifier();
            String oi = multipleSkip.getOptionIdentifier();
            String val = multipleSkip.getValue();
            createMultipleSkip(sqi, multipleSkip, qi, oi, val);
        }
    }

    private void createMultipleSkip(String sqi, MultipleSkip multipleSkip, String qi, String oi, String val) {
        MultipleSkip ms = mMultipleSkipDao.findByAttributesSync(sqi, oi, sqi, val);
        if (ms == null) {
            ms = new MultipleSkip();
            ms.setQuestionIdentifier(qi);
            ms.setOptionIdentifier(oi);
            ms.setValue(val);
            ms.setSkipQuestionIdentifier(sqi);
            ms.setDeleted(multipleSkip.isDeleted());
            ms.setInstrumentRemoteId(multipleSkip.getInstrumentRemoteId());
            mMultipleSkipDao.insert(ms);
        }
    }

    private int getDisplayQuestionCount(Question q, LoopQuestion lq, List<LoopQuestion> list) {
        int count = 0;
        for (LoopQuestion loopQuestion : list) {
            if (!loopQuestion.isDeleted()) {
                count++;
            }
        }
        if (q.getQuestionType().equals(Question.INTEGER)) {
            return LOOP_MAX * count;
        } else {
            if (TextUtils.isEmpty(lq.getOptionIndices())) {
                if (q.isOtherQuestionType()) {
                    return (q.getOptionCount() + 1) * count;
                } else {
                    return q.getOptionCount() * count;
                }
            } else {
                String[] indices = lq.getOptionIndices().split(COMMA);
                return indices.length * count;
            }
        }
    }

    private long getDisplayId() {
        long remoteId = getId();
        Display display = mDisplayDao.findByIdSync(remoteId);
        while (display != null) {
            remoteId = getId();
            display = mDisplayDao.findByIdSync(remoteId);
        }
        return remoteId;
    }

    private long getQuestionId() {
        long remoteId = getId();
        Question question = mQuestionDao.findByIdSync(remoteId);
        if (question != null) {
            return getQuestionId();
        }
        return remoteId;
    }

    private long getId() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return ThreadLocalRandom.current().nextLong(LOWER_BOUND, UPPER_BOUND);
        } else {
            return RandomUtils.nextLong(LOWER_BOUND, UPPER_BOUND);
        }
    }

    @NonNull
    private LongSparseArray<List<Question>> getDisplayQuestions(List<Question> questions) {
        LongSparseArray<List<Question>> dQuestions = new LongSparseArray<>();
        for (Question question : questions) {
            if (question.isDeleted()) continue;
            List<Question> displayQuestions = dQuestions.get(question.getDisplayId());
            if (displayQuestions == null) {
                displayQuestions = new ArrayList<>();
            }
            displayQuestions.add(question);
            dQuestions.put(question.getDisplayId(), displayQuestions);
        }
        return dQuestions;
    }

    @Override
    protected void onPostExecute(Void params) {
        AppUtil.resetRemoteDownloadCount();
    }
}
