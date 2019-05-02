package org.adaptlab.chpir.android.survey.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.adaptlab.chpir.android.survey.SurveyApp;
import org.adaptlab.chpir.android.survey.SurveyRoomDatabase;
import org.adaptlab.chpir.android.survey.daos.DisplayDao;
import org.adaptlab.chpir.android.survey.daos.InstrumentDao;
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
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SetLoopsTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "SetLoopsTask";
    private static final int LOOP_MAX = 12;
    private static final String LIST_DELIMITER = ",";
    private static final int LOWER_BOUND = 100000;
    private static final int UPPER_BOUND = 1000000;

    @Override
    protected Void doInBackground(Void... voids) {
        SurveyRoomDatabase database = SurveyRoomDatabase.getDatabase(SurveyApp.getInstance());
        Settings settings = database.settingsDao().getInstanceSync();
        List<Instrument> instruments = database.instrumentDao().projectInstrumentsSync(Long.valueOf(settings.getProjectId()));
        for (Instrument instrument : instruments) {
            List<Display> displays = database.displayDao().instrumentDisplaysSync(instrument.getRemoteId());
            List<Question> questions = database.questionDao().instrumentQuestionsSync(instrument.getRemoteId());
            for (Question question : questions) {
                if (question.getLoopQuestionCount() > 0 && question.getLoopSource() == null && question.getDisplayId() != null) {
                    List<LoopQuestion> loopQuestions = database.loopQuestionDao().allLoopQuestionsSync(question.getRemoteId());
                    if (loopQuestions.size() > 0) {
                        if (question.getQuestionType().equals(Question.INTEGER)) {
                            for (LoopQuestion loopQuestion : loopQuestions) {
                                Display display = getDisplay(question, loopQuestion, loopQuestions, database);
                                for (int k = 1; k <= LOOP_MAX; k++) {
                                    createLoopQuestion(question, loopQuestion, k, display, database);
                                }
                                displays = updateDisplays(displays, display, question, false, database);
                            }
                        } else if (question.isMultipleResponseLoop()) {
                            for (LoopQuestion loopQuestion : loopQuestions) {
                                Display display = getDisplay(question, loopQuestion, loopQuestions, database);
                                if (TextUtils.isEmpty(loopQuestion.getOptionIndices())) {
                                    for (int k = 0; k < question.getOptionCount(); k++) {
                                        createLoopQuestion(question, loopQuestion, k, display, database);
                                    }
                                    if (question.isOtherQuestionType()) {
                                        createLoopQuestion(question, loopQuestion, question.getOptionCount(), display, database);
                                    }
                                } else {
                                    // Loop only for particular options
                                    String[] indices = loopQuestion.getOptionIndices().split(LIST_DELIMITER);
                                    for (String index : indices) {
                                        int ind = Integer.parseInt(index);
                                        createLoopQuestion(question, loopQuestion, ind, display, database);
                                    }
                                }
                                displays = updateDisplays(displays, display, question, loopQuestion.isSameDisplay(), database);
                            }
                        }
                        sanitizeDisplays(displays, getDisplayQuestions(questions), database);
                    }
                }
            }
            setInstrumentLoaded(database, instrument);
        }
        return null;
    }

    private void setInstrumentLoaded(SurveyRoomDatabase db, Instrument instrument) {
        DisplayDao dao = db.displayDao();
        InstrumentDao instrumentDao = db.instrumentDao();
        List<Question> questions = db.questionDao().instrumentQuestionsSync(instrument.getRemoteId());
        HashMap<Long, List<Question>> dQuestions = getDisplayQuestions(questions);
        for (Display display : dao.instrumentDisplaysSync(instrument.getRemoteId())) {
            if (dQuestions.get(display.getRemoteId()) == null ||
                    display.getQuestionCount() != dQuestions.get(display.getRemoteId()).size()) {
                instrument.setLoaded(false);
                instrumentDao.update(instrument);
                return;
            }
        }
        instrument.setLoaded(true);
        instrumentDao.update(instrument);
    }

    private void sanitizeDisplays(List<Display> displays, HashMap<Long, List<Question>> displayQuestions, SurveyRoomDatabase db) {
        DisplayDao dao = db.displayDao();
        for (Iterator<Display> iterator = displays.iterator(); iterator.hasNext(); ) {
            Display display = iterator.next();
            if (displayQuestions.get(display.getRemoteId()) == null) {
                // Display has no questions, so delete it
                dao.delete(display);
                iterator.remove();
            }
        }
        // Ensure they are numbered consecutively
        for (int k = 0; k < displays.size(); k++) {
            Display display = displays.get(k);
            if (display.getPosition() != k + 1) {
                display.setPosition(k + 1);
                dao.update(display);
            }
        }
    }

    private List<Display> updateDisplays(List<Display> displays, Display display, Question question,
                                         boolean isSameDisplay, SurveyRoomDatabase db) {
        DisplayDao dao = db.displayDao();
        Display questionDisplay = dao.findByIdSync(question.getDisplayId());
        if (displays.contains(display)) {
            int index = displays.indexOf(display);
            if (!isSameDisplay && index != questionDisplay.getPosition()) {
                displays.remove(display);
                displays.add(questionDisplay.getPosition(), display);
            }
        } else {
            displays.add(questionDisplay.getPosition(), display);
        }
        return displays;
    }

    private void createLoopQuestion(Question question, LoopQuestion lq, int index, Display display, SurveyRoomDatabase db) {
        QuestionDao dao = db.questionDao();
        Question source = dao.findByQuestionIdentifierSync(lq.getLooped());
        if (source == null) return;
        String identifier = question.getQuestionIdentifier() + "_" + source.getQuestionIdentifier() + "_" + index;
        Question loopedQuestion = dao.findByQuestionIdentifierSync(identifier);
        if (loopedQuestion == null) {
            loopedQuestion = new Question();
            loopedQuestion.setRemoteId(getQuestionId(dao));
            loopedQuestion.setDisplayId(display.getRemoteId());
            loopedQuestion.setLoopNumber(index);
            loopedQuestion.setQuestionIdentifier(identifier);
            dao.insert(loopedQuestion);
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
            source.setLoopQuestionCount(db.loopQuestionDao().loopQuestionsSync(source.getRemoteId()).size());
            dao.update(source);
        }
        dao.update(loopedQuestion);
        setLoopedQuestionNextQuestions(question, source, loopedQuestion, index, db);
        setLoopedQuestionMultipleSkips(question, source, loopedQuestion, index, db);
        setSkipsLoopedQuestion(source, loopedQuestion, db);
    }

    private void setLoopedQuestionNextQuestions(Question question, Question source, Question loopedQuestion, int index, SurveyRoomDatabase db) {
        NextQuestionDao dao = db.nextQuestionDao();
        List<NextQuestion> nextQuestions = dao.questionNextQuestionsSync(source.getQuestionIdentifier(), source.getInstrumentRemoteId());
        String qi = loopedQuestion.getQuestionIdentifier();
        for (NextQuestion nextQuestion : nextQuestions) {
            String oi = nextQuestion.getOptionIdentifier();
            String nqi = question.getQuestionIdentifier() + "_" + nextQuestion.getNextQuestionIdentifier() + "_" + index;
            String value = nextQuestion.getValue();
            NextQuestion nq = dao.findByAttributesSync(qi, oi, nqi, value);
            if (nq == null) {
                nq = new NextQuestion();
                nq.setQuestionIdentifier(qi);
                nq.setOptionIdentifier(oi);
                nq.setNextQuestionIdentifier(nqi);
                nq.setDeleted(nextQuestion.isDeleted());
                nq.setInstrumentRemoteId(nextQuestion.getInstrumentRemoteId());
                nq.setValue(value);
                dao.insert(nq);
            }
        }
    }

    private void setLoopedQuestionMultipleSkips(Question question, Question source, Question loopedQuestion, int index, SurveyRoomDatabase db) {
        MultipleSkipDao dao = db.multipleSkipDao();
        List<MultipleSkip> multipleSkips = dao.questionMultipleSkipsSync(source.getQuestionIdentifier(), source.getInstrumentRemoteId());
        String qi = loopedQuestion.getQuestionIdentifier();
        for (MultipleSkip multipleSkip : multipleSkips) {
            String oi = multipleSkip.getOptionIdentifier();
            String val = multipleSkip.getValue();
            String sqi = question.getQuestionIdentifier() + "_" + multipleSkip.getSkipQuestionIdentifier() + "_" + index;
            createMultipleSkip(dao, sqi, multipleSkip, qi, oi, val);
        }
    }

    private void setSkipsLoopedQuestion(Question source, Question loopedQuestion, SurveyRoomDatabase db) {
        MultipleSkipDao dao = db.multipleSkipDao();
        List<MultipleSkip> skipsQuestion = dao.skipsQuestionMultipleSkipsSync(source.getQuestionIdentifier(), source.getInstrumentRemoteId());
        String sqi = loopedQuestion.getQuestionIdentifier();
        for (MultipleSkip multipleSkip : skipsQuestion) {
            String qi = multipleSkip.getQuestionIdentifier();
            String oi = multipleSkip.getOptionIdentifier();
            String val = multipleSkip.getValue();
            createMultipleSkip(dao, sqi, multipleSkip, qi, oi, val);
        }
    }

    private void createMultipleSkip(MultipleSkipDao dao, String sqi, MultipleSkip multipleSkip, String qi, String oi, String val) {
        MultipleSkip ms = dao.findByAttributesSync(sqi, oi, sqi, val);
        if (ms == null) {
            ms = new MultipleSkip();
            ms.setQuestionIdentifier(qi);
            ms.setOptionIdentifier(oi);
            ms.setValue(val);
            ms.setSkipQuestionIdentifier(sqi);
            ms.setDeleted(multipleSkip.isDeleted());
            ms.setInstrumentRemoteId(multipleSkip.getInstrumentRemoteId());
            dao.insert(ms);
        }
    }

    private Display getDisplay(Question q, LoopQuestion lq, List<LoopQuestion> lqs, SurveyRoomDatabase db) {
        DisplayDao dao = db.displayDao();
        Display parent = dao.findByIdSync(q.getDisplayId());
        if (lq.isSameDisplay()) {
            parent.setQuestionCount(parent.getQuestionCount() + getDisplayQuestionCount(q, lq, lqs));
            dao.update(parent);
            return parent;
        }
        Display display = dao.findByTitleAndInstrumentIdSync(parent.getTitle() + " p2", q.getInstrumentRemoteId());
        if (display == null) {
            display = new Display();
            display.setTitle(parent.getTitle() + " p2");
            display.setInstrumentRemoteId(q.getInstrumentRemoteId());
            display.setSectionId(parent.getSectionId());
            display.setRemoteId(getDisplayId(dao));
            display.setDeleted(parent.isDeleted());
        }
        display.setQuestionCount(getDisplayQuestionCount(q, lq, lqs));
        dao.update(display);
        return display;
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
                String[] indices = lq.getOptionIndices().split(LIST_DELIMITER);
                return indices.length * count;
            }
        }
    }

    private long getDisplayId(DisplayDao dao) {
        long remoteId = getId();
        Display display = dao.findByIdSync(remoteId);
        if (display != null) {
            getDisplayId(dao);
        }
        return remoteId;
    }

    private long getQuestionId(QuestionDao dao) {
        long remoteId = getId();
        Question question = dao.findByIdSync(remoteId);
        if (question != null) {
            getQuestionId(dao);
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
    private HashMap<Long, List<Question>> getDisplayQuestions(List<Question> questions) {
        HashMap<Long, List<Question>> dQuestions = new HashMap<>();
        for (Question question : questions) {
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
