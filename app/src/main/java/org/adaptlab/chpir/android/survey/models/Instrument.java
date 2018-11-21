package org.adaptlab.chpir.android.survey.models;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.BaseColumns;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    private static final String TAG = "Instrument";
    public static final int LOOP_MAX = 12;
    @Column(name = "Title")
    private String mTitle;
    // https://github.com/pardom/ActiveAndroid/issues/22
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "Language")
    private String mLanguage;
    @Column(name = "Alignment")
    private String mAlignment;
    @Column(name = "VersionNumber")
    private int mVersionNumber;
    @Column(name = "QuestionCount")
    private int mQuestionCount;
    @Column(name = "ProjectId")
    private Long mProjectId;
    @Column(name = "Published")
    private boolean mPublished;
    @Column(name = "Deleted")
    private boolean mDeleted;
    @Column(name = "ShowSectionsFragment")
    private boolean mShowSectionsFragment;
    @Column(name = "DirectReviewNavigation")
    private boolean mDirectReviewNavigation;
    @Column(name = "SpecialOptions")
    private String mSpecialOptions;
    @Column(name = "CriticalMessage")
    private String mCriticalMessage;
    @Column(name = "Loaded")
    private boolean mLoaded;
    @Column(name = "Roster")
    private boolean mRoster;
    @Column(name = "Scorable")
    private boolean mScorable;

    public Instrument() {
        super();
    }

    public static List<Instrument> getAllProjectInstruments(Long projectId) {
        return new Select().from(Instrument.class)
                .where("ProjectID = ? AND Published = ? AND Deleted != ?", projectId, 1, 1)
                .orderBy("Title")
                .execute(); //sqlite saves booleans as integers
    }

    public static Cursor getProjectInstrumentsCursor(Long projectId) {
        From instrumentsQuery = new Select("Instruments.*")
                .from(Instrument.class)
                .where("ProjectID = ? AND Published = ? AND Deleted = ?", projectId, true, false)
                .orderBy("Title");
        return Cache.openDatabase().rawQuery(instrumentsQuery.toSql(), instrumentsQuery.getArguments());
    }

    public static List<Instrument> loadedInstruments() {
        List<Instrument> instrumentList = new ArrayList<Instrument>();
        for (Instrument instrument : Instrument.getAll()) {
            if (instrument.loaded()) instrumentList.add(instrument);
        }
        return instrumentList;
    }

    /*
     * Finders
     */
    public static List<Instrument> getAll() {
        return new Select().from(Instrument.class).where("Deleted != ?", 1).orderBy("Title")
                .execute();
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

    public InstrumentTranslation activeTranslation() {
        return new Select().from(InstrumentTranslation.class)
                .where("InstrumentRemoteId = ? AND Language = ? AND Active = ?", mRemoteId, AppUtil.getDeviceLanguage(), 1).executeSingle();
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getLanguage() {
        return mLanguage;
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

    public void setLanguage(String language) {
        mLanguage = language;
    }

    // TODO: 5/1/18 Fix
    public void sanitize() {
        if (questionCount() < getQuestionCount()) {
            setLoaded(false);
        } else {
            setLoaded(true);
        }
        for (Question question : questions()) {
            if (!question.loaded()) {
                setLoaded(false);
                break;
            }
        }
    }

    public int questionCount() {
        return new Select().from(Question.class)
                .where("InstrumentRemoteId = ? AND Deleted != ?", getRemoteId(), 1).count();
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

    /*
     * Relationships
     */
    public List<Question> questions() {
        return new Select().from(Question.class)
                .where("InstrumentRemoteId = ? AND Deleted != ?", getRemoteId(), 1)
                .orderBy("NumberInInstrument ASC")
                .execute();
    }

    public HashMap<Display, List<Question>> displayQuestions() {
        HashMap<Display, List<Question>> map = new HashMap<>();
        for (Display display : displays()) {
            map.put(display, display.questions());
        }
        return map;
    }

    public HashMap<Display, List<DisplayInstruction>> displayInstructions() {
        HashMap<Display, List<DisplayInstruction>> map = new HashMap<>();
        for (Display display : displays()) {
            map.put(display, display.displayInstructions());
        }
        return map;
    }

    public String getCriticalMessage() {
        if (getLanguage().equals(AppUtil.getDeviceLanguage())) return mCriticalMessage;
        if (activeTranslation() != null && !activeTranslation().getCriticalMessage().trim().equals("")) {
            return activeTranslation().getCriticalMessage();
        }
        for (InstrumentTranslation translation : translations()) {
            if (translation.getLanguage().equals(AppUtil.getDeviceLanguage())
                    && !translation.getCriticalMessage().trim().equals("")) {
                return translation.getCriticalMessage();
            }
        }
        return mCriticalMessage;
    }

    private void setCriticalMessage(String message) {
        mCriticalMessage = message;
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

    /*
     * Getters/Setters
     */

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
            instrument.setCriticalMessage(jsonObject.getString("critical_message"));
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
                    translation.setCriticalMessage(translationJSON.getString("critical_message"));
                    translation.setActive(translationJSON.optBoolean("active"));
                    translation.save();
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    public static Instrument findByRemoteId(Long id) {
        return new Select().from(Instrument.class).where("RemoteId = ?", id).executeSingle();
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public InstrumentTranslation getTranslationByLanguage(String language) {
        for (InstrumentTranslation translation : translations()) {
            if (translation.getLanguage().equals(language)) {
                return translation;
            }
        }
        InstrumentTranslation translation = new InstrumentTranslation();
        translation.setLanguage(language);
        return translation;
    }

    public List<Survey> surveys() {
        return new Select().from(Survey.class)
                .where("InstrumentRemoteId = ?", getRemoteId())
                .execute();
    }

    public List<Question> criticalQuestions() {
        return new Select().from(Question.class)
                .where("InstrumentRemoteId = ? AND Deleted != ? AND Critical = ?", getRemoteId(),
                        1, 1)
                .orderBy("NumberInInstrument ASC")
                .execute();
    }

    public HashMap<Question, List<Option>> optionsMap() {
        int capacity = (int) Math.ceil(getQuestionCount() / 0.75);
        HashMap<Question, List<Option>> map = new HashMap<>(capacity);
        for (Question question : questions()) {
            if (question.hasOptions()) {
                map.put(question, question.defaultOptions());
            }
        }
        return map;
    }

    public List<Display> displays() {
        return new Select().from(Display.class)
                .where("InstrumentId = ? AND Deleted != ?", getRemoteId(), 1)
                .orderBy("Position ASC")
                .execute();
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

    public boolean getPublished() {
        return mPublished;
    }

    public void setPublished(boolean published) {
        mPublished = published;
    }

    public boolean getShowSectionsFragment() {
        return mShowSectionsFragment;
    }

    private void setShowSectionsFragment(boolean showSectionsFragment) {
        mShowSectionsFragment = showSectionsFragment;
    }

    public boolean getDirectReviewNavigation() {
        return mDirectReviewNavigation;
    }

    private void setDirectReviewNavigation(boolean directReviewNavigation) {
        mDirectReviewNavigation = directReviewNavigation;
    }

    public List<String> getSpecialOptionStrings() {
        if (TextUtils.isEmpty(getSpecialOptions())) {
            return AppUtil.getAdminSettingsInstance().getSpecialOptions();
        } else {
            return Arrays.asList(getSpecialOptions().split(","));
        }
    }

    public String getSpecialOptions() {
        return mSpecialOptions;
    }

    private void setSpecialOptions(String specialOptions) {
        mSpecialOptions = specialOptions.replaceAll("[^A-Za-z0-9,]", "");
    }

    public List<Section> sections() {
        return new Select()
                .from(Section.class)
                .where("InstrumentRemoteId = ? AND Deleted != ?", getRemoteId(), 1)
                .orderBy("FirstQuestionNumber IS NULL, FirstQuestionNumber")
                .execute();
    }

    private void setRoster(boolean isRoster) {
        mRoster = isRoster;
    }

    public boolean isRoster() {
        return mRoster;
    }

    public List<ScoreScheme> scoreSchemes() {
        return new Select().from(ScoreScheme.class)
                .where("InstrumentRemoteId = ? AND Deleted != ?", getRemoteId(), 1)
                .execute();
    }

    private void setScorable(boolean status) {
        mScorable = status;
    }

    public boolean isScorable() {
        return mScorable;
    }

    public void setLoops() {
        List<Display> mDisplays = displays();
        for (Question question : questions()) {
            if (question.getLoopQuestionCount() > 0 && question.getLoopSource() == null
                    && question.getDisplay() != null) {
                List<LoopQuestion> loopQuestions = question.loopQuestions();
                if (loopQuestions.size() > 0) {
                    if (question.getQuestionType().equals(Question.QuestionType.INTEGER)) {
                        for (LoopQuestion loopQuestion : loopQuestions) {
                            Display display = getDisplay(question, loopQuestion);
                            for (int k = 1; k <= LOOP_MAX; k++) {
                                String instruction = question.getText() + " : " + k;
                                createLoopQuestion(question, loopQuestion, instruction, k, display);
                            }
                            updateDisplays(mDisplays, display, question, false);
                        }
                    } else if (question.isMultipleResponseLoop()) {
                        for (LoopQuestion loopQuestion : loopQuestions) {
                            Display display = getDisplay(question, loopQuestion);
                            if (TextUtils.isEmpty(loopQuestion.getOptionIndices())) {
                                for (int k = 0; k < question.defaultOptions().size(); k++) {
                                    String instruction = question.getText() + " : " +
                                            question.defaultOptions().get(k).getText(this);
                                    createLoopQuestion(question, loopQuestion, instruction, k, display);
                                }
                                if (question.isOtherQuestionType()) {
                                    String instruction = question.getText() + " : Other";
                                    createLoopQuestion(question, loopQuestion, instruction,
                                                question.defaultOptions().size(), display);
                                }
                            } else {
                                // Loop only for particular options
                                String[] indices = loopQuestion.getOptionIndices().split(Response.LIST_DELIMITER);
                                for (String index : indices) {
                                    int ind = Integer.parseInt(index);
                                    String instruction = question.getText() + " : " +
                                            question.defaultOptions().get(ind).getText(this);
                                    createLoopQuestion(question, loopQuestion, instruction, ind, display);
                                }
                            }
                            updateDisplays(mDisplays, display, question, loopQuestion.isSameDisplay());
                        }
                    }
                    for (int k = 0; k < mDisplays.size(); k++) {
                        Display display = mDisplays.get(k);
                        if (display.getPosition() != k + 1) {
                            display.setPosition(k + 1);
                            display.save();
                        }
                    }
                }
            }
        }
    }

    private void updateDisplays(List<Display> mDisplays, Display display, Question question, boolean sameDisplay) {
        if (mDisplays.contains(display)) {
            int index = mDisplays.indexOf(display);
            if (!sameDisplay && index != question.getDisplay().getPosition()) {
                mDisplays.remove(display);
                mDisplays.add(question.getDisplay().getPosition(), display);
            }
        } else {
            mDisplays.add(question.getDisplay().getPosition(), display);
        }
    }

    private Display getDisplay(Question question, LoopQuestion loopQuestion) {
        Display parent = question.getDisplay();
        if (loopQuestion.isSameDisplay()) return parent;
        Display display = Display.findByTitleAndInstrument(parent.getTitle() + " p2",
                question.getInstrument().getRemoteId());
        if (display == null) {
            display = new Display();
            display.setMode(parent.getMode());
            display.setTitle(parent.getTitle() + " p2");
            display.setInstrumentId(question.getInstrument().getRemoteId());
            display.setSectionId(parent.getSectionId());
            display.setRemoteId(getBoundedDisplayId());
            display.setDeleted(parent.getDeleted());
            display.save();
        }
        return display;
    }

    private void createLoopQuestion(Question question, LoopQuestion lq, String instruction,
                                        int index, Display display) {
        Question source = lq.loopedQuestion();
        if (source == null) return;
        String identifier = question.getQuestionIdentifier() + "_" + source.getQuestionIdentifier() + "_" + index;
        Question loopedQuestion = Question.findByQuestionIdentifier(identifier);
        if (loopedQuestion == null) {
            loopedQuestion = new Question();
            loopedQuestion.setRemoteId(getBoundedRemoteId());
            loopedQuestion.setDisplay(display.getRemoteId());
            // Only set loop number & loop source for duplicated loop questions
            loopedQuestion.setLoopNumber(index);
            loopedQuestion.setLoopSource(question.getQuestionIdentifier());
            loopedQuestion.setQuestionIdentifier(identifier);
            loopedQuestion.setInstruction(createLoopInstruction(instruction).getRemoteId());
        }
        loopedQuestion = Question.copyAttributes(loopedQuestion, source);
        if (lq.isSameDisplay()) {
            loopedQuestion.setNumberInInstrument(source.getNumberInInstrument());
        } else {
            loopedQuestion.setNumberInInstrument(source.getNumberInInstrument() + (index * question.getLoopQuestionCount()));
        }
        loopedQuestion.save();
        setLoopedQuestionSkips(question, source, loopedQuestion, index);
        setLoopedQuestionMultiSkips(question, source, loopedQuestion, index);
    }

    // TODO: 11/8/18 Duplicate value for integer based skips
    private void setLoopedQuestionMultiSkips(Question question, Question source,
                                             Question loopedQuestion, int index) {
        List<MultipleSkip> multipleSkips = source.multipleSkips(this);
        String qi = loopedQuestion.getQuestionIdentifier();
        for (MultipleSkip multipleSkip : multipleSkips) {
            String oi = multipleSkip.getOptionIdentifier();
            String sqi = question.getQuestionIdentifier() + "_" + multipleSkip.getSkipQuestionIdentifier() + "_" + index;
            MultipleSkip ms = getMultipleSkip(qi, oi, sqi);
            if (ms == null) {
                ms = new MultipleSkip();
                ms.setQuestionIdentifier(qi);
                ms.setOptionIdentifier(oi);
                ms.setSkipQuestionIdentifier(sqi);
                ms.setDeleted(multipleSkip.getDeleted());
                ms.setRemoteInstrumentId(multipleSkip.getRemoteInstrumentId());
                ms.save();
            }
        }
    }

    private MultipleSkip getMultipleSkip(String qi, String oi, String sqi) {
        return new Select().from(MultipleSkip.class).where("QuestionIdentifier = ? AND OptionIdentifier = ? " +
                "AND SkipQuestionIdentifier = ?", qi, oi, sqi).executeSingle();
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
            remoteId = ThreadLocalRandom.current().nextLong(100000);
        } else {
            remoteId = RandomUtils.nextLong(0, 100000);
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
            getUnusedRemoteId();
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