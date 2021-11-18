package org.adaptlab.chpir.android.survey.models;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.collection.LongSparseArray;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/*
 * Content Providers require column _id i.e. BaseColumns._ID which is different from the primary key
 * used by ActiveAndroid. As a result, the expected ActiveAndroid relationships do not work
 * and therefore have to be handled using the custom primary key or another key.
 */
@Table(name = "Instruments", id = BaseColumns._ID)
public class Instrument extends ReceiveModel {
    public static final String KHMER_LANGUAGE_CODE = "km";
    public static final String KHMER_FONT_LOCATION = "fonts/khmerOS.ttf";
    public static final String LEFT_ALIGNMENT = "left";
    public static final int LOOP_MAX = 12;
    public static final int BOUND = 1000000;
    private static final String TAG = "Instrument";
    @Column(name = "Title")
    private String mTitle;
    // https://github.com/pardom/ActiveAndroid/issues/22
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE, index = true)
    private Long mRemoteId;
    @Column(name = "Language")
    private String mLanguage;
    @Column(name = "Alignment")
    private String mAlignment;
    @Column(name = "VersionNumber")
    private int mVersionNumber;
    @Column(name = "QuestionCount")
    private int mQuestionCount;
    @Column(name = "ProjectId", index = true)
    private Long mProjectId;
    @Column(name = "Published", index = true)
    private boolean mPublished;
    @Column(name = "Deleted", index = true)
    private boolean mDeleted;
    @Column(name = "ShowSectionsFragment")
    private boolean mShowSectionsFragment;
    @Column(name = "DirectReviewNavigation")
    private boolean mDirectReviewNavigation;
    @Column(name = "SpecialOptions")
    private String mSpecialOptions;
    @Column(name = "Loaded")
    private boolean mLoaded;
    @Column(name = "Roster")
    private boolean mRoster;
    @Column(name = "Scorable")
    private boolean mScorable;

    private List<Display> mDisplays;

    public Instrument() {
        super();
    }

    public static List<Instrument> getAllProjectInstruments(Long projectId) {
        return new Select().from(Instrument.class)
                .where("ProjectID = ? AND Published = ? AND Deleted != ?", projectId, 1, 1)
                .orderBy("Title")
                .execute(); //sqlite saves booleans as integers
    }

    /*
     * Finders
     */
    public static List<Instrument> getAll() {
        return new Select().from(Instrument.class).where("Deleted != ?", 1).orderBy("Title")
                .execute();
    }

    public static List<String> getLanguages() {
        List<String> languages = new ArrayList<>();
        languages.add("en");
        List<InstrumentTranslation> translations = new Select().from(InstrumentTranslation.class).groupBy("Language").execute();
        for (InstrumentTranslation instrumentTranslation : translations) {
            if (!TextUtils.isEmpty(instrumentTranslation.getLanguage()) && !languages.contains(instrumentTranslation.getLanguage().trim())) {
                languages.add(instrumentTranslation.getLanguage().trim());
            }
        }
        return languages;
    }

    public static Instrument findByRemoteId(Long id) {
        return new Select().from(Instrument.class).where("RemoteId = ?", id).executeSingle();
    }

    public boolean loaded() {
        return mLoaded;
    }

    /*
     * If the language of the instrument is the same as the language setting on the
     * device (or through the admin settings), then return the default instrument title.
     *
     * If another language is requested, iterate through instrument translations to
     * find translated title.
     *
     * If the language requested is not available as a translation (or is blank), return the
     * non-translated
     * text for the title.
     */
    public String getTitle() {
        if (getLanguage().equals(AppUtil.getDeviceLanguage())) return mTitle;
        if (activeTranslation() != null && !activeTranslation().getTitle().trim().equals("")) {
            return activeTranslation().getTitle();
        }
        for (InstrumentTranslation translation : translations()) {
            if (translation.getLanguage().equals(AppUtil.getDeviceLanguage())
                    && !translation.getTitle().trim().equals("")) {
                return translation.getTitle();
            }
        }

        // Fall back to default
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String language) {
        mLanguage = language;
    }

    public InstrumentTranslation activeTranslation() {
        return new Select().from(InstrumentTranslation.class)
                .where("InstrumentRemoteId = ? AND Language = ? AND Active = ?", mRemoteId, AppUtil.getDeviceLanguage(), 1).executeSingle();
    }

    public List<InstrumentTranslation> translations() {
        return new Select().from(InstrumentTranslation.class)
                .where("InstrumentRemoteId = ?", getRemoteId())
                .execute();
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }

    private void sanitize() {
        HashMap<Long, List<Question>> mDisplayQuestions = getDisplayQuestions();
        for (Display display : displays()) {
            if (mDisplayQuestions.get(display.getRemoteId()) == null || display.getQuestionCount()
                    != mDisplayQuestions.get(display.getRemoteId()).size()) {
                setLoaded(false);
                return;
            }
        }
        setLoaded(true);
    }

    @NonNull
    private HashMap<Long, List<Question>> getDisplayQuestions() {
        HashMap<Long, List<Question>> mDisplayQuestions = new HashMap<>();
        for (Question question : questions()) {
            List<Question> displayQuestions = mDisplayQuestions.get(question.getDisplayId());
            if (displayQuestions == null) {
                displayQuestions = new ArrayList<>();
            }
            displayQuestions.add(question);
            mDisplayQuestions.put(question.getDisplayId(), displayQuestions);
        }
        return mDisplayQuestions;
    }

    public int getQuestionCount() {
        return mQuestionCount;
    }

    public void setQuestionCount(int num) {
        mQuestionCount = num;
    }

    private void setLoaded(boolean status) {
        mLoaded = status;
        save();
    }

    public List<Question> questions() {
        return new Select().from(Question.class)
                .where("InstrumentRemoteId = ? AND Deleted != ?", getRemoteId(), 1)
                .orderBy("NumberInInstrument ASC")
                .execute();
    }

    public List<Display> displays() {
        return new Select().from(Display.class)
                .where("InstrumentId = ? AND Deleted != ?", getRemoteId(), 1)
                .orderBy("Position ASC")
                .execute();
    }

    public HashMap<Display, List<DisplayInstruction>> displayInstructions() {
        HashMap<Display, List<DisplayInstruction>> map = new HashMap<>();
        for (Display display : displays()) {
            map.put(display, display.displayInstructions());
        }
        return map;
    }

    public LongSparseArray<OptionSet> optionSets() {
        LongSparseArray<OptionSet> optionSets = new LongSparseArray<>();
        for (OptionSet optionSet : OptionSet.getAll()) {
            optionSets.append(optionSet.getRemoteId(), optionSet);
        }
        return optionSets;
    }

    public LongSparseArray<Instruction> instructions() {
        LongSparseArray<Instruction> instructions = new LongSparseArray<>();
        for (Instruction instruction : Instruction.getAll()) {
            instructions.append(instruction.getRemoteId(), instruction);
        }
        return instructions;
    }

    public HashMap<String, List<NextQuestion>> nextQuestions() {
        HashMap<String, List<NextQuestion>> map = new HashMap<>();
        for (NextQuestion nextQuestion : NextQuestion.getAll(mRemoteId)) {
            List<NextQuestion> nextQuestions = map.get(nextQuestion.getQuestionIdentifier());
            if (nextQuestions == null) {
                nextQuestions = new ArrayList<>();
            }
            nextQuestions.add(nextQuestion);
            map.put(nextQuestion.getQuestionIdentifier(), nextQuestions);
        }
        return map;
    }

    public HashMap<String, List<ConditionSkip>> conditionSkips() {
        HashMap<String, List<ConditionSkip>> map = new HashMap<>();
        for (ConditionSkip conditionSkip : ConditionSkip.getAll(mRemoteId)) {
            List<ConditionSkip> conditionSkips = map.get(conditionSkip.getQuestionIdentifier());
            if (conditionSkips == null) {
                conditionSkips = new ArrayList<>();
            }
            conditionSkips.add(conditionSkip);
            map.put(conditionSkip.getQuestionIdentifier(), conditionSkips);
        }
        return map;
    }

    public HashMap<String, List<CriticalResponse>> criticalResponses() {
        HashMap<String, List<CriticalResponse>> map = new HashMap<>();
        for (CriticalResponse criticalResponse : CriticalResponse.getAll()) {
            List<CriticalResponse> criticalResponses = map.get(criticalResponse.getQuestionIdentifier());
            if (criticalResponses == null) {
                criticalResponses = new ArrayList<>();
            }
            criticalResponses.add(criticalResponse);
            map.put(criticalResponse.getQuestionIdentifier(), criticalResponses);
        }
        return map;
    }

    public HashMap<String, List<MultipleSkip>> multipleSkips() {
        HashMap<String, List<MultipleSkip>> map = new HashMap<>();
        for (MultipleSkip multipleSkip : MultipleSkip.getAll(mRemoteId)) {
            List<MultipleSkip> multipleSkips = map.get(multipleSkip.getQuestionIdentifier());
            if (multipleSkips == null) {
                multipleSkips = new ArrayList<>();
            }
            multipleSkips.add(multipleSkip);
            map.put(multipleSkip.getQuestionIdentifier(), multipleSkips);
        }
        return map;
    }

    public HashMap<String, List<FollowUpQuestion>> followUpQuestions() {
        HashMap<String, List<FollowUpQuestion>> map = new HashMap<>();
        for (FollowUpQuestion followUpQuestion : FollowUpQuestion.getAll(mRemoteId)) {
            List<FollowUpQuestion> followUpQuestions = map.get(followUpQuestion.getQuestionIdentifier());
            if (followUpQuestions == null) {
                followUpQuestions = new ArrayList<>();
            }
            followUpQuestions.add(followUpQuestion);
            map.put(followUpQuestion.getQuestionIdentifier(), followUpQuestions);
        }
        return map;
    }

    public HashMap<String, List<LoopQuestion>> loopQuestions() {
        HashMap<String, List<LoopQuestion>> hashMap = new HashMap<>();
        for (Question question : questions()) {
            if (question.getLoopQuestionCount() > 0) {
                List<LoopQuestion> loopQuestions = question.loopQuestions();
                if (loopQuestions.size() > 0) {
                    hashMap.put(question.getQuestionIdentifier(), loopQuestions);
                }
            }
        }
        return hashMap;
    }

    public HashMap<Question, List<Option>> optionsMap(List<Question> questions) {
        int capacity = (int) Math.ceil(getQuestionCount() / 0.75);
        HashMap<Question, List<Option>> map = new HashMap<>(capacity);
        for (Question question : questions) {
            if (question.hasOptions()) {
                map.put(question, question.defaultOptions());
            }
        }
        return map;
    }

    public HashMap<Long, List<Option>> specialOptionsMap() {
        HashMap<Long, List<Option>> map = new HashMap<>();
        List<OptionInOptionSet> specialOptionInOptionSet = new Select().from(OptionInOptionSet.class).where("Special = 1 AND Deleted = 0").execute();
        for (OptionInOptionSet optionInOptionSet : specialOptionInOptionSet) {
            List<Option> options = map.get(optionInOptionSet.getRemoteOptionSetId());
            if (options == null) {
                List<Option> list = new ArrayList<>();
                list.add(Option.findByRemoteId(optionInOptionSet.getRemoteOptionId()));
                map.put(optionInOptionSet.getRemoteOptionSetId(), list);
            } else {
                options.add(Option.findByRemoteId(optionInOptionSet.getRemoteOptionId()));
                map.put(optionInOptionSet.getRemoteOptionSetId(), options);
            }
        }
        return map;
    }

    public HashMap<Long, List<OptionSetTranslation>> optionSetTranslations() {
        HashMap<Long, List<OptionSetTranslation>> map = new HashMap<>();
        for (OptionSetTranslation optionSetTranslation : OptionSetTranslation.getAll()) {
            List<OptionSetTranslation> list = map.get(optionSetTranslation.getOptionSetId());
            if (list == null) list = new ArrayList<>();
            list.add(optionSetTranslation);
            map.put(optionSetTranslation.getOptionSetId(), list);
        }
        return map;
    }

    public Typeface getTypeFace(Context context) {
        if (AppUtil.getDeviceLanguage().equals(KHMER_LANGUAGE_CODE)) {
            return Typeface.createFromAsset(context.getAssets(), KHMER_FONT_LOCATION);
        } else {
            return Typeface.DEFAULT;
        }
    }

    public int getDefaultGravity() {
        if (getAlignment().equals(LEFT_ALIGNMENT)) {
            return Gravity.LEFT;
        } else {
            return Gravity.RIGHT;
        }
    }

    public String getAlignment() {
        if (getLanguage().equals(AppUtil.getDeviceLanguage())) return mAlignment;
        if (activeTranslation() != null) return activeTranslation().getAlignment();
        for (InstrumentTranslation translation : translations()) {
            if (translation.getLanguage().equals(AppUtil.getDeviceLanguage())) {
                return translation.getAlignment();
            }
        }

        // Fall back to default
        return mAlignment;
    }

    private void setAlignment(String alignment) {
        mAlignment = alignment;
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");

            // If an instrument already exists, update it from the remote
            Instrument instrument = Instrument.findByRemoteId(remoteId);
            if (instrument == null) {
                instrument = this;
            }

            if (AppUtil.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            instrument.setRemoteId(remoteId);
            instrument.setTitle(jsonObject.getString("title"));
            instrument.setLanguage(jsonObject.getString("language"));
            instrument.setAlignment(jsonObject.getString("alignment"));
            instrument.setVersionNumber(jsonObject.getInt("current_version_number"));
            instrument.setQuestionCount(jsonObject.getInt("question_count"));
            instrument.setProjectId(jsonObject.getLong("project_id"));
            instrument.setPublished(jsonObject.optBoolean("published"));
            instrument.setShowSectionsFragment(jsonObject.getBoolean("show_sections_page"));
            instrument.setDirectReviewNavigation(jsonObject.getBoolean("navigate_to_review_page"));
            instrument.setSpecialOptions(jsonObject.getString("special_options"));
            instrument.setRoster(jsonObject.optBoolean("roster"));
            instrument.setScorable(jsonObject.optBoolean("scorable"));
            if (jsonObject.isNull("deleted_at")) {
                instrument.setDeleted(false);
            } else {
                instrument.setDeleted(true);
            }
            instrument.save();

            // Generate translations
            JSONArray translationsArray = jsonObject.optJSONArray("instrument_translations");
            if (translationsArray != null) {
                for (int i = 0; i < translationsArray.length(); i++) {
                    JSONObject translationJSON = translationsArray.getJSONObject(i);
                    Long translationRemoteId = translationJSON.getLong("id");
                    InstrumentTranslation translation = InstrumentTranslation.findByRemoteId(translationRemoteId);
                    if (translation == null) {
                        translation = new InstrumentTranslation();
                    }
                    translation.setRemoteId(translationRemoteId);
                    translation.setLanguage(translationJSON.getString("language"));
                    translation.setInstrumentRemoteId(instrument.getRemoteId());
                    translation.setAlignment(translationJSON.getString("alignment"));
                    translation.setTitle(translationJSON.getString("title"));
                    translation.setActive(translationJSON.optBoolean("active"));
                    translation.save();
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public List<Survey> surveys() {
        return new Select().from(Survey.class)
                .where("InstrumentRemoteId = ?", getRemoteId())
                .execute();
    }

    @Override
    public String toString() {
        return mTitle;
    }

    public int getVersionNumber() {
        return mVersionNumber;
    }

    public void setVersionNumber(int version) {
        mVersionNumber = version;
    }

    public Long getProjectId() {
        return mProjectId;
    }

    public void setProjectId(Long id) {
        mProjectId = id;
    }

    public void setPublished(boolean published) {
        mPublished = published;
    }

    private void setShowSectionsFragment(boolean showSectionsFragment) {
        mShowSectionsFragment = showSectionsFragment;
    }

    private void setDirectReviewNavigation(boolean directReviewNavigation) {
        mDirectReviewNavigation = directReviewNavigation;
    }

    private void setSpecialOptions(String specialOptions) {
        mSpecialOptions = specialOptions.replaceAll("[^A-Za-z0-9,]", "");
    }

    public boolean isRoster() {
        return mRoster;
    }

    private void setRoster(boolean isRoster) {
        mRoster = isRoster;
    }

    public List<ScoreScheme> scoreSchemes() {
        return new Select().from(ScoreScheme.class)
                .where("InstrumentRemoteId = ? AND Deleted != ?", getRemoteId(), 1)
                .execute();
    }

    public boolean isScorable() {
        return mScorable;
    }

    private void setScorable(boolean status) {
        mScorable = status;
    }

    public void setLoops() {
        mDisplays = displays();
        List<Question> questions = questions();
        //Set display position to a double
        for (int k = 0; k < mDisplays.size(); k++) {
            Display display = mDisplays.get(k);
            display.setDisplayPosition(display.getPosition());
        }

        HashMap<String, Question> questionsMap = new HashMap<>();
        for (Question question : questions) {
            questionsMap.put(question.getQuestionIdentifier(), question);
        }
        for (Question question : questions) {
            if (question.getLoopQuestionCount() > 0 && question.getLoopSource() == null
                    && question.getDisplayId() != null) {
                List<LoopQuestion> loopQuestions = question.loopQuestionsWithDeleted();
                if (loopQuestions.size() > 0) {
                    List<Question> loopedQuestions = new ArrayList<>();
                    for (LoopQuestion lq : loopQuestions) {
                        Question q = questionsMap.get(lq.getLooped());
                        if (q != null) loopedQuestions.add(q);
                    }
                    Collections.sort(loopedQuestions, new Comparator<Question>() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public int compare(Question q1, Question q2) {
                            return Integer.compare(q1.getNumberInInstrument(), q2.getNumberInInstrument());
                        }
                    });

                    Display dis = getDisplay(question, loopQuestions.get(0), loopQuestions);
                    if (question.getQuestionType().equals(Question.QuestionType.INTEGER)) {
                        // Create display instruction
                        for (int k = 1; k <= LOOP_MAX; k++) {
                            int index = k - 1;
                            String text = question.getText() + " : " + index;
                            createDisplayInstruction(question, loopedQuestions.get(0), loopQuestions.get(0), dis, text, index);
                        }

                        for (LoopQuestion loopQuestion : loopQuestions) {
                            Display display = getDisplay(question, loopQuestion, loopQuestions);
                            for (int k = 1; k <= LOOP_MAX; k++) {
                                createLoopQuestion(question, loopQuestion, k, display);
                            }
                        }
                    } else if (question.isMultipleResponseLoop()) {
                        // Create Display Instruction
                        if (TextUtils.isEmpty(loopQuestions.get(0).getOptionIndices())) {
                            for (int k = 0; k < question.getOptionCount(); k++) {
                                String text = question.getText() + " : " + question.defaultOptions().get(k).getText(this);
                                createDisplayInstruction(question, loopedQuestions.get(0), loopQuestions.get(0), dis, text, k);
                            }
                            if (question.isOtherQuestionType()) {
                                String text = question.getText() + " : Other";
                                createDisplayInstruction(question, loopedQuestions.get(0), loopQuestions.get(0), dis, text, question.getOptionCount());
                            }
                        } else {
                            // Loop only for particular options
                            String[] indices = loopQuestions.get(0).getOptionIndices().split(Response.LIST_DELIMITER);
                            for (String index : indices) {
                                int ind = Integer.parseInt(index);
                                String text = question.getText() + " : " + question.defaultOptions().get(ind).getText(this);
                                createDisplayInstruction(question, loopedQuestions.get(0), loopQuestions.get(0), dis, text, ind);
                            }
                        }

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
                                String[] indices = loopQuestion.getOptionIndices().split(Response.LIST_DELIMITER);
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
        sanitizeDisplays();
        sanitize();
    }

    private void createDisplayInstruction(Question parent, Question looped, LoopQuestion lq, Display display, String text, int index) {
        Instruction  instruction = Instruction.findByText(text);
        if (instruction == null) instruction = createLoopInstruction(text);
        int position;
        if (lq.isSameDisplay()) {
            position = looped.getNumberInInstrument();
        } else {
            position = looped.getNumberInInstrument() + (index * parent.getLoopQuestionCount());
        }
        DisplayInstruction displayInstruction = new Select().from(DisplayInstruction.class).where(
                "RemoteDisplayId = ? AND InstructionId = ? AND Position = ?",
                display.getRemoteId(), instruction.getRemoteId(), position).executeSingle();
        if (displayInstruction == null) {
            long remoteId = new Random().nextLong();
            DisplayInstruction di = DisplayInstruction.findByRemoteId(remoteId);
            while (di != null) {
                remoteId = new Random().nextLong();
                di = DisplayInstruction.findByRemoteId(remoteId);
            }
            displayInstruction = new DisplayInstruction();
            displayInstruction.setRemoteId(remoteId);
            displayInstruction.setDisplayId(display.getRemoteId());
            displayInstruction.setInstructionId(instruction.getRemoteId());
            displayInstruction.setPosition(position);
            displayInstruction.setDeleted(false);
            displayInstruction.save();
        }
    }

    private void sanitizeDisplays() {
        HashMap<Long, List<Question>> displayQuestions = getDisplayQuestions();
        for (Iterator<Display> iterator = mDisplays.iterator(); iterator.hasNext(); ) {
            Display display = iterator.next();
            if (displayQuestions.get(display.getRemoteId()) == null) {
                // Display has no questions, so delete it
                display.delete();
                iterator.remove();
            }
            // Temp fix
            List<Question> questions = displayQuestions.get(display.getRemoteId());
            if (questions != null && display.getQuestionCount() != questions.size()) {
                boolean getOut = false;
                for (Question question : questions) {
                    if (question.getLoopQuestionCount() > 0 ) {
                        List<LoopQuestion> loopQuestions = question.loopQuestions();
                        for (LoopQuestion loopQuestion : loopQuestions) {
                            if (loopQuestion.isSameDisplay()) {
                                display.setQuestionCount(questions.size());
                                getOut = true;
                                break;
                            }
                        }
                    }
                    if (getOut) break;
                }
            }
        }
        // Ensure they are numbered consecutively
        Collections.sort(mDisplays, new Comparator<Display>() {
            @Override
            public int compare(Display o1, Display o2) {
                return Double.compare(o1.getDisplayPosition(), o2.getDisplayPosition());
            }
        });
        for (int k = 0; k < mDisplays.size(); k++) {
            Display display = mDisplays.get(k);
            if (display.getPosition() != k + 1) {
                display.setPosition(k + 1);
                display.save();
            }
        }
    }

    private Display getDisplay(Question q, LoopQuestion lq, List<LoopQuestion> lqs) {
        Display parent = q.getDisplay();
        if (lq.isSameDisplay()) {
            return parent;
        }
        Display display = Display.findByTitleAndInstrument(parent.getTitle() + " p2",
                q.getInstrumentRemoteId());
        if (display == null) {
            display = new Display();
            display.setMode(parent.getMode());
            display.setTitle(parent.getTitle() + " p2");
            display.setInstrumentId(q.getInstrumentRemoteId());
            display.setSectionId(parent.getSectionId());
            display.setRemoteId(getBoundedDisplayId());
            display.setDeleted(parent.getDeleted());
            mDisplays.add(display);
        }
        display.setDisplayPosition(parent.getDisplayPosition() + 0.1);
        display.setQuestionCount(getDisplayQuestionCount(q, lq, lqs));
        display.save();
        return display;
    }

    private int getDisplayQuestionCount(Question q, LoopQuestion lq, List<LoopQuestion> list) {
        int count = 0;
        for (LoopQuestion loopQuestion : list) {
            if (!loopQuestion.isDeleted()) {
                count++;
            }
        }
        if (q.getQuestionType().equals(Question.QuestionType.INTEGER)) {
            return LOOP_MAX * count;
        } else {
            if (TextUtils.isEmpty(lq.getOptionIndices())) {
                if (q.isOtherQuestionType()) {
                    return (q.getOptionCount() + 1) * count;
                } else {
                    return q.getOptionCount() * count;
                }
            } else {
                String[] indices = lq.getOptionIndices().split(Response.LIST_DELIMITER);
                return indices.length * count;
            }
        }
    }

    private void createLoopQuestion(Question question, LoopQuestion lq, int index, Display display) {
        Question source = lq.loopedQuestion();
        if (source == null) return;
        String identifier = question.getQuestionIdentifier() + "_" + source.getQuestionIdentifier() + "_" + index;
        Question loopedQuestion = Question.findByQuestionIdentifier(identifier);
        if (loopedQuestion == null) {
            loopedQuestion = new Question();
            loopedQuestion.setRemoteId(getBoundedRemoteId());
            loopedQuestion.setDisplay(display.getRemoteId());
            loopedQuestion.setLoopNumber(index);
            loopedQuestion.setQuestionIdentifier(identifier);
        }
        loopedQuestion.setLoopSource(source.getQuestionIdentifier());
        loopedQuestion = Question.copyAttributes(loopedQuestion, source);
        if (lq.isSameDisplay()) {
            loopedQuestion.setNumberInInstrument(source.getNumberInInstrument());
        } else {
            loopedQuestion.setNumberInInstrument(source.getNumberInInstrument() + (index * question.getLoopQuestionCount()));
        }
        loopedQuestion.setPosition(source.getNumberInInstrument() + "_" + index);
        loopedQuestion.setTextToReplace(lq.getTextToReplace());
        if (lq.isDeleted()) {
            loopedQuestion.setDeleted(true);
            source.setLoopQuestionCount(source.loopQuestions().size());
            source.save();
        }
        loopedQuestion.save();
        setLoopedQuestionSkips(question, source, loopedQuestion, index);
        setLoopedQuestionMultipleSkips(question, source, loopedQuestion, index);
        setSkipsLoopedQuestion(source, loopedQuestion);
    }

    private void setSkipsLoopedQuestion(Question source, Question loopedQuestion) {
        List<MultipleSkip> skipsQuestion = source.skipsQuestion();
        String sqi = loopedQuestion.getQuestionIdentifier();
        for (MultipleSkip multipleSkip : skipsQuestion) {
            String qi = multipleSkip.getQuestionIdentifier();
            String oi = multipleSkip.getOptionIdentifier();
            String val = multipleSkip.getValue();
            MultipleSkip ms = getMultipleSkip(sqi, oi, sqi, val);
            if (ms == null) {
                ms = new MultipleSkip();
                ms.setQuestionIdentifier(qi);
                ms.setOptionIdentifier(oi);
                ms.setValue(val);
                ms.setSkipQuestionIdentifier(sqi);
                ms.setDeleted(multipleSkip.getDeleted());
                ms.setRemoteInstrumentId(multipleSkip.getRemoteInstrumentId());
                ms.save();
            }
        }
    }

    private void setLoopedQuestionMultipleSkips(Question question, Question source,
                                                Question loopedQuestion, int index) {
        List<MultipleSkip> multipleSkips = source.multipleSkips(this);
        String qi = loopedQuestion.getQuestionIdentifier();
        for (MultipleSkip multipleSkip : multipleSkips) {
            String oi = multipleSkip.getOptionIdentifier();
            String val = multipleSkip.getValue();
            String sqi = question.getQuestionIdentifier() + "_" + multipleSkip.getSkipQuestionIdentifier() + "_" + index;
            MultipleSkip ms = getMultipleSkip(qi, oi, sqi, val);
            if (ms == null) {
                ms = new MultipleSkip();
                ms.setQuestionIdentifier(qi);
                ms.setOptionIdentifier(oi);
                ms.setValue(val);
                ms.setSkipQuestionIdentifier(sqi);
                ms.setDeleted(multipleSkip.getDeleted());
                ms.setRemoteInstrumentId(multipleSkip.getRemoteInstrumentId());
                ms.save();
            }
        }
    }

    private MultipleSkip getMultipleSkip(String qi, String oi, String sqi, String value) {
        return new Select().from(MultipleSkip.class).where("QuestionIdentifier = ? AND " +
                        "OptionIdentifier = ? AND SkipQuestionIdentifier = ? AND Value = ?",
                qi, oi, sqi, value).executeSingle();
    }

    private void setLoopedQuestionSkips(Question question, Question source, Question loopedQuestion, int index) {
        List<NextQuestion> nextQuestions = source.nextQuestions(this);
        String qi = loopedQuestion.getQuestionIdentifier();
        for (NextQuestion nextQuestion : nextQuestions) {
            String oi = nextQuestion.getOptionIdentifier();
            String nqi = question.getQuestionIdentifier() + "_" + nextQuestion.getNextQuestionIdentifier() + "_" + index;
            NextQuestion nq = getNextQuestion(qi, oi, nqi);
            if (nq == null) {
                nq = new NextQuestion();
                nq.setQuestionIdentifier(qi);
                nq.setOptionIdentifier(oi);
                nq.setNextQuestionIdentifier(nqi);
                nq.setDeleted(nextQuestion.getDeleted());
                nq.setRemoteInstrumentId(nextQuestion.getRemoteInstrumentId());
                nq.save();
            }
        }
    }

    private NextQuestion getNextQuestion(String qi, String oi, String nqi) {
        return new Select().from(NextQuestion.class).where("QuestionIdentifier = ? AND OptionIdentifier = ? " +
                "AND NextQuestionIdentifier = ?", qi, oi, nqi).executeSingle();
    }

    private long getBoundedRemoteId() {
        long remoteId;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            remoteId = ThreadLocalRandom.current().nextLong(BOUND);
        } else {
            remoteId = RandomUtils.nextLong(0, BOUND);
        }
        Question question = Question.findByRemoteId(remoteId);
        if (question != null) {
            getBoundedRemoteId();
        }
        return remoteId;
    }

    private Instruction createLoopInstruction(String text) {
        Instruction instruction = new Instruction();
        instruction.setRemoteId(getUnusedRemoteId());
        instruction.setText(text);
        instruction.setDeleted(false);
        instruction.save();
        return instruction;
    }

    private long getUnusedRemoteId() {
        long remoteId;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            remoteId = ThreadLocalRandom.current().nextLong();
        } else {
            remoteId = new Random().nextLong();
        }
        Instruction instruction = Instruction.findByRemoteId(remoteId);
        if (instruction != null) {
            return getUnusedRemoteId();
        }
        return remoteId;
    }

    private long getBoundedDisplayId() {
        long remoteId;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            remoteId = ThreadLocalRandom.current().nextLong(1000, 10000);
        } else {
            remoteId = RandomUtils.nextLong(1000, 10000);
        }
        Display display = Display.findByRemoteId(remoteId);
        if (display != null) {
            getBoundedDisplayId();
        }
        return remoteId;
    }

}