package org.adaptlab.chpir.android.survey.models;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Table(name = "Questions")
public class Question extends ReceiveModel {
    public static final String COMPLETE_SURVEY = "COMPLETE_SURVEY";
    private static final String TAG = "Question";
    private static final String FOLLOW_UP_TRIGGER_STRING = "\\[followup\\]";
    private static final String RANDOMIZATION_TRIGGER = "\\[RANDOMIZED_FACTOR\\]";
    @Column(name = "Text")
    private String mText;
    @Column(name = "QuestionType")
    private QuestionType mQuestionType;
    @Column(name = "QuestionIdentifier", index = true)
    private String mQuestionIdentifier;
    @Column(name = "OptionCount")
    private int mOptionCount;
    @Column(name = "InstrumentVersion")
    private int mInstrumentVersion;
    @Column(name = "NumberInInstrument", index = true)
    private int mNumberInInstrument;
    @Column(name = "IdentifiesSurvey")
    private boolean mIdentifiesSurvey;
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE, index = true)
    private Long mRemoteId;
    @Column(name = "ImageCount")
    private int mImageCount;
    @Column(name = "InstructionId")
    private Long mInstructionId;
    @Column(name = "QuestionVersion")
    private int mQuestionVersion;
    @Column(name = "Grid")
    private Grid mGrid;
    @Column(name = "Deleted", index = true)
    private boolean mDeleted;
    @Column(name = "Section")
    private Section mSection;
    @Column(name = "InstrumentRemoteId", index = true)
    private Long mInstrumentRemoteId;
    @Column(name = "NumberInGrid")
    private int mNumberInGrid;
    @Column(name = "RemoteOptionSetId")
    private Long mRemoteOptionSetId;
    @Column(name = "DisplayId")
    private Long mDisplayId;
    @Column(name = "RemoteSpecialOptionSetId")
    private Long mRemoteSpecialOptionSetId;
    @Column(name = "TableIdentifier")
    private String mTableIdentifier;
    @Column(name = "ValidationId")
    private Long mValidationId;
    @Column(name = "RankResponses")
    private boolean mRankResponses;
    @Column(name = "LoopQuestionCount")
    private int mLoopQuestionCount;
    @Column(name = "LoopSource")
    private String mLoopSource;
    @Column(name = "LoopNumber")
    private int mLoopNumber;
    @Column(name = "QuestionId")
    private Long mQuestionId;
    @Column(name = "TextToReplace")
    private String mTextToReplace;
    @Column(name = "Position", index = true)
    private String mPosition; // Used for display purposes

    public Question() {
        super();
    }

    public static List<Question> getAll() {
        return new Select().from(Question.class).where("Deleted != ?", 1)
                .orderBy("Id ASC")
                .execute();
    }

    public static Question findByQuestionIdentifier(String identifier) {
        if (identifier == null) return null;
        return new Select().from(Question.class).where("QuestionIdentifier = ?", identifier)
                .executeSingle();
    }
    
    public static Question findByQuestionIdentifier(String identifier, Long instrumentId) {
        if (identifier == null) return null;
        return new Select().from(Question.class).where("QuestionIdentifier = ? AND InstrumentRemoteId = ?",
                identifier, instrumentId).executeSingle();
    }

    public static Question findByQuestionIdentifier(String identifier, Long instrumentId, boolean isDeleted) {
        if (identifier == null) return null;
        return new Select().from(Question.class).where("QuestionIdentifier = ? AND InstrumentRemoteId = ? AND Deleted = ?",
                identifier, instrumentId, isDeleted ? 1 : 0).executeSingle();
    }

    static void copyAttributes(Question destination, Question source) {
        destination.mText = source.mText;
        destination.mQuestionType = source.mQuestionType;
        destination.mOptionCount = source.mOptionCount;
        destination.mInstrumentVersion = source.mInstrumentVersion;
        destination.mIdentifiesSurvey = source.mIdentifiesSurvey;
        destination.mQuestionVersion = source.mQuestionVersion;
        destination.mDeleted = source.mDeleted;
        destination.mSection = source.mSection;
        destination.mInstrumentRemoteId = source.mInstrumentRemoteId;
        destination.mRemoteOptionSetId = source.mRemoteOptionSetId;
        destination.mRemoteSpecialOptionSetId = source.mRemoteSpecialOptionSetId;
        destination.mTableIdentifier = source.mTableIdentifier;
        destination.mValidationId = source.mValidationId;
        destination.mRankResponses = source.mRankResponses;
        destination.mLoopQuestionCount = source.mLoopQuestionCount;
        destination.mQuestionId = source.mQuestionId;
        destination.mInstructionId = source.mInstructionId;
    }

    public static Question findByRemoteId(Long id) {
        return new Select().from(Question.class).where("RemoteId = ?", id).executeSingle();
    }

    static boolean validQuestionType(String questionType) {
        for (QuestionType type : QuestionType.values()) {
            if (type.name().equals(questionType)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSingleResponse() {
        return (getQuestionType() == QuestionType.SELECT_ONE ||
                getQuestionType() == QuestionType.SELECT_ONE_WRITE_OTHER ||
                getQuestionType() == QuestionType.DROP_DOWN);
    }

    public QuestionType getQuestionType() {
        return mQuestionType;
    }

    public void setQuestionType(String questionType) {
        if (validQuestionType(questionType)) {
            mQuestionType = QuestionType.valueOf(questionType);
        } else {
            // This should never happen
            // We should prevent syncing data unless app is up to date
            Log.wtf(TAG, "Received invalid question type: " + questionType);
        }
    }

    public boolean isMultipleSkipQuestion(Instrument instrument) {
        return multipleSkips(instrument).size() > 0;
    }

    List<MultipleSkip> multipleSkips(Instrument instrument) {
        return new Select().from(MultipleSkip.class)
                .where("QuestionIdentifier = ? AND RemoteInstrumentId = ? AND Deleted = ?",
                        getQuestionIdentifier(), instrument.getRemoteId(), false)
                .execute();
    }

    List<MultipleSkip> skipsQuestion() {
        return new Select().from(MultipleSkip.class).where(
                "SkipQuestionIdentifier = ? AND RemoteInstrumentId = ? AND Deleted = ?",
                getQuestionIdentifier(), mInstrumentRemoteId, false).execute();
    }

    public String getQuestionIdentifier() {
        return mQuestionIdentifier;
    }

    public void setQuestionIdentifier(String questionIdentifier) {
        mQuestionIdentifier = questionIdentifier;
    }

    public Instrument getInstrument() {
        return Instrument.findByRemoteId(getInstrumentRemoteId());
    }

    public Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }

    public void setInstrumentRemoteId(Long instrumentId) {
        mInstrumentRemoteId = instrumentId;
    }

    public boolean hasRegularOptionSkips(Instrument instrument) {
        return (nextQuestions(instrument).size() > 0);
    }

    List<NextQuestion> nextQuestions(Instrument instrument) {
        return new Select().from(NextQuestion.class)
                .where("QuestionIdentifier = ? AND RemoteInstrumentId = ? AND Deleted = ?",
                        getQuestionIdentifier(), instrument.getRemoteId(), 0)
                .execute();
    }

    public boolean hasExclusiveOption() {
        return exclusiveOptions().size() > 0;
    }

    List<OptionInOptionSet> exclusiveOptions() {
        return new Select().from(OptionInOptionSet.class)
                .where("RemoteOptionSetId = ? AND Deleted = ? AND IsExclusive = ?",
                        getRemoteOptionSetId(), false, true)
                .execute();
    }

    public Long getRemoteOptionSetId() {
        return mRemoteOptionSetId;
    }

    private void setRemoteOptionSetId(Long id) {
        mRemoteOptionSetId = id;
    }

    /*
     * Return the processed string for a following up question.
     *
     * Replace the follow up trigger string token with the appropriate
     * response.  If this is a question with options, then map the option
     * number to the option text.  If not, then return the text response.
     *
     * If the question that is being followed up on was skipped by the user,
     * then return nothing.  This question will be skipped in that case.
     */
    public String getFollowingUpText(HashMap<String, Response> responses, Context context) {
        String questionText = getText();
        for (FollowUpQuestion followingUpQuestion : followingUpQuestions()) {
            Response followUpResponse = responses.get(followingUpQuestion.getFollowingUpQuestionIdentifier());

            if (followUpResponse == null || followUpResponse.getText().equals("") ||
                    followUpResponse.hasSpecialResponse()) {
                continue;
            }

            if (followUpWithOptionText(followingUpQuestion)) {
                String text = followingUpQuestion.getFollowingUpOnQuestion()
                        .getOptionTextByResponse(followUpResponse, context);
                questionText = questionText.replaceFirst(FOLLOW_UP_TRIGGER_STRING, text);
            } else {
                questionText = questionText.replaceFirst(FOLLOW_UP_TRIGGER_STRING,
                        followUpResponse.getText());
            }
        }
        return questionText;
    }

    /*
     * position in QuestionRandomizedFactor starts from 1, hence k + 1
     * save response at the end as a fallback for INSTRUCTIONS questions type which has no
     * questionComponent - saving to db happens here
     */
    public String getRandomizedText(Response response) {
        JSONObject randomizedData;
        String text = getText();
        if (response == null) return text;
        if (!TextUtils.isEmpty(response.getRandomizedData())) {
            try {
                randomizedData = new JSONObject(response.getRandomizedData());
                for (int k = 0; k < questionRandomizedFactors().size(); k++) {
                    text = text.replaceFirst(RANDOMIZATION_TRIGGER, randomizedData.getString
                            (String.valueOf(k + 1)));
                }
            } catch (JSONException e) {
                if (BuildConfig.DEBUG) Log.e(TAG, "JSON exception", e);
            }
        } else {
            Random random = new Random();
            randomizedData = new JSONObject();
            for (int k = 0; k < questionRandomizedFactors().size(); k++) {
                List<RandomizedOption> randomizedOptions = questionRandomizedFactors().get(k)
                        .getRandomizedFactor().randomizedOptions();
                int index = random.nextInt(randomizedOptions.size());
                String optionText = randomizedOptions.get(index).getText();
                text = text.replaceFirst(RANDOMIZATION_TRIGGER, optionText);
                try {
                    randomizedData.put(String.valueOf(k + 1), optionText);
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "JSON exception", e);
                }
            }
            response.setRandomizedData(randomizedData.toString());
            response.save(); // fallback when INSTRUCTIONS question type
        }
        return text;
    }

    /*
     * If the language of the instrument is the same as the language setting on the
     * device (or through the admin settings), then return the question text.
     *
     * If another language is requested, iterate through question translations to
     * find translated text.
     *
     * If the language requested is not available as a translation, return the non-translated
     * text for the question.
     */
    public String getText() {
        String iLanguage = getInstrument().getLanguage();
        String dLanguage = AppUtil.getDeviceLanguage();
        if (iLanguage.equals(dLanguage)) return mText;
        List<QuestionTranslation> translations = translations();
        for (QuestionTranslation translation : translations) {
            if (translation.getLanguage().equals(dLanguage)) {
                return translation.getText();
            }
        }
        if (dLanguage.contains("-")) {
            dLanguage = dLanguage.split("-")[0];
            for (QuestionTranslation translation : translations) {
                if (translation.getLanguage().equals(dLanguage)) {
                    return translation.getText();
                }
            }
        }
        // Fall back to default
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    private List<QuestionRandomizedFactor> questionRandomizedFactors() {
        return new Select().from(QuestionRandomizedFactor.class).where("Question = ?", getId())
                .orderBy("Position ASC").execute();
    }

    public List<QuestionTranslation> translations() {
        if (mQuestionId == null)
            return new ArrayList<>(); // TODO: 11/7/18 Placeholder that needs fixing
        return new Select().from(QuestionTranslation.class).where("QuestionId = ?", mQuestionId).execute();
    }

    /*
     * Question types which must have their responses (represented as indices)
     * mapped to the original option text.
     */
    private boolean followUpWithOptionText(FollowUpQuestion followingUpQuestion) {
        return followingUpQuestion.getFollowingUpOnQuestion().getQuestionType().equals(
                QuestionType.SELECT_MULTIPLE) || followingUpQuestion.getFollowingUpOnQuestion()
                .getQuestionType().equals(QuestionType.SELECT_ONE) || followingUpQuestion
                .getFollowingUpOnQuestion().getQuestionType().equals(QuestionType
                        .SELECT_ONE_WRITE_OTHER) || followingUpQuestion.getFollowingUpOnQuestion
                ().getQuestionType().equals(QuestionType.SELECT_MULTIPLE_WRITE_OTHER);
    }

    /*
     * Map a response represented as an index to its corresponding
     * option text.  If this is an "other" response, return the
     * text specified in the other response.
     */
    private String getOptionTextByResponse(Response response, Context context) {
        String text = response.getText();
        try {
            if (hasMultipleResponses()) {
                return FormatUtils.unformatMultipleResponses(defaultOptions(), text, context,
                        getInstrument());
            } else if (Integer.parseInt(text) == defaultOptions().size()) {
                return (response.getOtherResponse() == null) ? "Other" : response
                        .getOtherResponse();
            } else {
                return defaultOptions().get(Integer.parseInt(text)).getText(getInstrument());
            }
        } catch (NumberFormatException nfe) {
            Log.e(TAG, text + " is not an option number");
            return text;
        } catch (IndexOutOfBoundsException iob) {
            Log.e(TAG, text + " is an out of range option number");
            return text;
        }
    }

    /*
     * Return true if this response can be an array of multiple options.
     */
    public boolean hasMultipleResponses() {
        return getQuestionType().equals(QuestionType.SELECT_MULTIPLE) ||
                getQuestionType().equals(QuestionType.SELECT_MULTIPLE_WRITE_OTHER);
    }

    public boolean hasListResponses() {
        return getQuestionType().equals(QuestionType.LIST_OF_INTEGER_BOXES) ||
                getQuestionType().equals(QuestionType.LIST_OF_TEXT_BOXES);
    }

    /*
     * Check that all of the options are loaded and that the instrument version
     * numbers of the question components match the expected instrument version
     * number.
     */
    boolean loaded() {
        return true;
//        return getOptionCount() == optionCount() && getImageCount() == imageCount();
    }

    private void setImageCount(int count) {
        mImageCount = count;
    }

    private void setOptionCount(int num) {
        mOptionCount = num;
    }

    public int getOptionCount() {
        return mOptionCount;
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");
            // If a question already exists, update it from the remote
            Question question = Question.findByRemoteId(remoteId);
            if (question == null) {
                question = this;
            }
            question.setRemoteId(remoteId);
            if (AppUtil.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            question.setText(jsonObject.getString("text"));
            question.setQuestionType(jsonObject.getString("question_type"));
            question.setQuestionIdentifier(jsonObject.getString("question_identifier"));
            question.setInstrumentRemoteId(jsonObject.optLong("instrument_id"));
            question.setOptionCount(jsonObject.optInt("option_count"));
            question.setImageCount(jsonObject.optInt("image_count"));
            question.setInstrumentVersion(jsonObject.getInt("instrument_version"));
            question.setIdentifiesSurvey(jsonObject.optBoolean("identifies_survey", false));
            if (!jsonObject.isNull("number_in_instrument")) {
                question.setNumberInInstrument(jsonObject.getInt("number_in_instrument"));
                question.setPosition(jsonObject.optString("number_in_instrument"));
            }
            question.setInstruction(jsonObject.optLong("instruction_id"));
            question.setQuestionVersion(jsonObject.getInt("question_version"));
            question.setDisplay(jsonObject.optLong("display_id"));
            if (jsonObject.isNull("deleted_at")) {
                question.setDeleted(false);
            } else {
                question.setDeleted(true);
            }
            question.setRemoteOptionSetId(jsonObject.optLong("option_set_id"));
            question.setRemoteSpecialOptionSetId(jsonObject.optLong("special_option_set_id"));
            question.setTableIdentifier(jsonObject.getString("table_identifier"));
            if (!jsonObject.isNull("validation_id")) {
                question.setValidationId(jsonObject.getLong("validation_id"));
            }
            question.setRankResponses(jsonObject.optBoolean("rank_responses"));
            question.setLoopQuestionCount(jsonObject.optInt("loop_question_count", 0));
            question.setQuestionId(jsonObject.optLong("question_id"));
            question.save();

            // Generate translations
            JSONArray translationsArray = jsonObject.optJSONArray("question_translations");
            if (translationsArray != null) {
                for (int i = 0; i < translationsArray.length(); i++) {
                    JSONObject translationJSON = translationsArray.getJSONObject(i);
                    Long translationRemoteId = translationJSON.getLong("id");
                    QuestionTranslation translation = QuestionTranslation.findByRemoteId(translationRemoteId);
                    if (translation == null) {
                        translation = new QuestionTranslation();
                    }
                    translation.setRemoteId(translationRemoteId);
                    translation.setLanguage(translationJSON.getString("language"));
                    translation.setText(translationJSON.getString("text"));
                    translation.setQuestionId(jsonObject.optLong("question_id"));
                    translation.save();
                }
            }
            if (question.getLoopQuestionCount() > 0) {
                JSONArray loopsArray = jsonObject.optJSONArray("loop_questions");
                if (loopsArray != null) {
                    for (int i = 0; i < loopsArray.length(); i++) {
                        JSONObject loopJSON = loopsArray.getJSONObject(i);
                        Long loopRemoteId = loopJSON.getLong("id");
                        LoopQuestion loopQuestion = LoopQuestion.findByRemoteId(loopRemoteId);
                        if (loopQuestion == null) {
                            loopQuestion = new LoopQuestion();
                        }
                        loopQuestion.setRemoteId(loopRemoteId);
                        loopQuestion.setQuestion(question);
                        loopQuestion.setParent(loopJSON.optString("parent"));
                        loopQuestion.setLooped(loopJSON.optString("looped"));
                        String indices = loopJSON.getString("option_indices");
                        if (indices.equals("null")) {
                            indices = null;
                        }
                        loopQuestion.setOptionIndices(indices);
                        if (loopJSON.isNull("deleted_at")) {
                            loopQuestion.setDeleted(false);
                        } else {
                            loopQuestion.setDeleted(true);
                        }
                        loopQuestion.setSameDisplay(loopJSON.optBoolean("same_display", false));
                        loopQuestion.setTextToReplace(loopJSON.optString("replacement_text", null));
                        loopQuestion.save();
                    }
                }
            }
            JSONArray criticalResponsesArray = jsonObject.optJSONArray("critical_responses");
            if (criticalResponsesArray != null) {
                for (int i = 0; i < criticalResponsesArray.length(); i++) {
                    JSONObject criticalResponseJSON = criticalResponsesArray.getJSONObject(i);
                    Long criticalResponseRemoteId = criticalResponseJSON.getLong("id");
                    CriticalResponse criticalResponse = CriticalResponse.findByRemoteId(criticalResponseRemoteId);
                    if (criticalResponse == null) {
                        criticalResponse = new CriticalResponse();
                    }
                    criticalResponse.setRemoteId(criticalResponseRemoteId);
                    criticalResponse.setQuestionIdentifier(criticalResponseJSON.getString("question_identifier"));
                    criticalResponse.setOptionIdentifier(criticalResponseJSON.getString("option_identifier"));
                    criticalResponse.setInstructionId(criticalResponseJSON.getLong("instruction_id"));
                    if (jsonObject.isNull("deleted_at")) {
                        criticalResponse.setDeleted(false);
                    } else {
                        criticalResponse.setDeleted(true);
                    }
                    criticalResponse.save();
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    private void setInstrumentVersion(int version) {
        mInstrumentVersion = version;
    }

    private void setIdentifiesSurvey(boolean identifiesSurvey) {
        mIdentifiesSurvey = identifiesSurvey;
    }

    void setInstruction(Long id) {
        mInstructionId = id;
    }

    public Long getInstructionId() {
        return mInstructionId;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    private void setValidationId(Long validationId) {
        mValidationId = validationId;
    }

    private void setRankResponses(boolean rankResponses) {
        mRankResponses = rankResponses;
    }

    private void setQuestionId(long id) {
        mQuestionId = id;
    }

    public int getLoopQuestionCount() {
        return mLoopQuestionCount;
    }

    public void setLoopQuestionCount(int count) {
        mLoopQuestionCount = count;
    }

    public boolean isFollowUpQuestion() {
        return (followingUpQuestions().size() > 0);
    }

    private List<FollowUpQuestion> followingUpQuestions() {
        return new Select().from(FollowUpQuestion.class)
                .where("QuestionIdentifier = ? AND RemoteInstrumentId = ? ",
                        mQuestionIdentifier, mInstrumentRemoteId)
                .orderBy("Position")
                .execute();
    }

    public List<Option> options() {
        return new Select("Options.*").distinct().from(Option.class)
                .innerJoin(OptionInOptionSet.class)
                .on("OptionInOptionSets.RemoteOptionSetId = ?", getRemoteOptionSetId())
                .where("Options.Deleted != 1 AND OptionInOptionSets.RemoteOptionId = Options" +
                        ".RemoteId")
                .orderBy("OptionInOptionSets.NumberInQuestion ASC")
                .execute();
    }

    boolean hasOptions() {
        return mRemoteOptionSetId > 0;
    }

    public List<Option> defaultOptions() {
        return new Select("Options.*").distinct().from(Option.class)
                .innerJoin(OptionInOptionSet.class)
                .on("OptionInOptionSets.RemoteOptionSetId = ?", getRemoteOptionSetId())
                .where("Options.Deleted != 1 AND OptionInOptionSets.Special = 0 AND " +
                        "OptionInOptionSets.RemoteOptionId = Options.RemoteId AND OptionInOptionSets.Deleted != 1")
                .orderBy("OptionInOptionSets.NumberInQuestion ASC")
                .execute();
    }

    public boolean hasSpecialOptionSkips(Instrument instrument) {
        return hasSpecialOptions() && specialOptionSkips(instrument).size() > 0;
    }

    public boolean hasSpecialOptions() {
        return mRemoteSpecialOptionSetId > 0;
    }

    private List<NextQuestion> specialOptionSkips(Instrument instrument) {
        return new Select("NextQuestions.*")
                .from(NextQuestion.class)
                .innerJoin(Option.class)
                .on("Options.Identifier=NextQuestions.OptionIdentifier")
                .where("NextQuestions.QuestionIdentifier = ? AND " +
                                "NextQuestions.RemoteInstrumentId = ? AND NextQuestions.Deleted = ?",
                        getQuestionIdentifier(), instrument.getRemoteId(), 0)
                .execute();
    }

    Option specialOptionByText(String optionText) {
        for (Option option : specialOptions()) {
            if (option.getIdentifier().equals(optionText)) {
                return option;
            }
        }
        return null;
    }

    public List<Option> specialOptions() {
        return new Select("Options.*").distinct().from(Option.class)
                .innerJoin(OptionInOptionSet.class)
                .on("OptionInOptionSets.RemoteOptionSetId = ?", getRemoteSpecialOptionSetId())
                .where("Options.Deleted != 1 AND OptionInOptionSets.Special = 1 AND " +
                        "OptionInOptionSets.RemoteOptionId = Options.RemoteId")
                .orderBy("OptionInOptionSets.NumberInQuestion ASC")
                .execute();
    }

    public Long getRemoteSpecialOptionSetId() {
        return mRemoteSpecialOptionSetId;
    }

    private void setRemoteSpecialOptionSetId(Long id) {
        mRemoteSpecialOptionSetId = id;
    }

    public List<Image> images() {
        return new Select().from(Image.class).where("Question = ?", getId()).execute();
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }

    public int getNumberInInstrument() {
        return mNumberInInstrument;
    }

    void setNumberInInstrument(int number) {
        mNumberInInstrument = number;
    }

    public boolean identifiesSurvey() {
        return mIdentifiesSurvey;
    }

    public int getQuestionVersion() {
        return mQuestionVersion;
    }

    private void setQuestionVersion(int version) {
        mQuestionVersion = version;
    }

    public Grid getGrid() {
        return mGrid;
    }

    public Section getSection() {
        return mSection;
    }

    public void setSection(Section section) {
        mSection = section;
    }

    public boolean hasRandomizedFactors() {
        return questionRandomizedFactors().size() > 0;
    }

    public Display getDisplay() {
        if (mDisplayId == null) return null;
        return Display.findByRemoteId(mDisplayId);
    }

    public void setDisplay(long display) {
        mDisplayId = display;
    }

    public String getTableIdentifier() {
        return mTableIdentifier;
    }

    private void setTableIdentifier(String tableIdentifier) {
        mTableIdentifier = tableIdentifier;
    }

    public Validation getValidation() {
        if (mValidationId == null) return null;
        return new Select().from(Validation.class).where("RemoteId = ?",
                mValidationId).executeSingle();
    }

    public boolean isOtherQuestionType() {
        return (mQuestionType == QuestionType.SELECT_ONE_WRITE_OTHER ||
                mQuestionType == QuestionType.SELECT_MULTIPLE_WRITE_OTHER);
    }

    public boolean isDropDownQuestionType() {
        return (mQuestionType == QuestionType.DROP_DOWN);
    }

    public boolean isTextEntryQuestionType() {
        QuestionType type = getQuestionType();
        return (type == QuestionType.FREE_RESPONSE || type == QuestionType.LIST_OF_INTEGER_BOXES
                || type == QuestionType.LIST_OF_TEXT_BOXES || type == QuestionType.INTEGER
                || type == QuestionType.EMAIL_ADDRESS || type == QuestionType.DECIMAL_NUMBER
                || type == QuestionType.INSTRUCTIONS || type == QuestionType.PHONE_NUMBER
                || type == QuestionType.ADDRESS || type == QuestionType.RANGE
                || type == QuestionType.SUM_OF_PARTS || type == QuestionType.SELECT_ONE_WRITE_OTHER
                || type == QuestionType.SELECT_MULTIPLE_WRITE_OTHER);
    }

    public boolean isMultipleResponseLoop() {
        QuestionType type = getQuestionType();
        return (type == QuestionType.SELECT_MULTIPLE || type == QuestionType.SELECT_MULTIPLE_WRITE_OTHER ||
                type == QuestionType.LIST_OF_INTEGER_BOXES || type == QuestionType.LIST_OF_TEXT_BOXES);
    }

    public boolean isSingleSelect() {
        QuestionType type = getQuestionType();
        return (type == QuestionType.SELECT_ONE || type == QuestionType.SELECT_ONE_WRITE_OTHER);
    }

    public boolean rankResponses() {
        return mRankResponses;
    }

    public String getLoopSource() {
        return mLoopSource;
    }

    void setLoopSource(String source) {
        mLoopSource = source;
    }

    public int getLoopNumber() {
        return mLoopNumber;
    }

    void setLoopNumber(int number) {
        mLoopNumber = number;
    }

    public String getTextToReplace() {
        return mTextToReplace;
    }

    public void setTextToReplace(String text) {
        mTextToReplace = text;
    }

    public Long getDisplayId() {
        return mDisplayId;
    }

    public List<LoopQuestion> loopQuestions() {
        return new Select().from(LoopQuestion.class).where("Question = ? AND Deleted = 0",
                getId()).execute();
    }

    public List<LoopQuestion> loopQuestionsWithDeleted() {
        return new Select().from(LoopQuestion.class).where("Question = ?",
                getId()).execute();
    }

    public String getPosition() {
        return mPosition;
    }

    public void setPosition(String mPosition) {
        this.mPosition = mPosition;
    }

    public enum QuestionType {
        SELECT_ONE, SELECT_MULTIPLE, SELECT_ONE_WRITE_OTHER, SELECT_MULTIPLE_WRITE_OTHER,
        FREE_RESPONSE, SLIDER, FRONT_PICTURE, REAR_PICTURE, DATE, RATING, TIME, LIST_OF_TEXT_BOXES,
        INTEGER, EMAIL_ADDRESS, DECIMAL_NUMBER, INSTRUCTIONS, MONTH_AND_YEAR, YEAR, PHONE_NUMBER,
        ADDRESS, SELECT_ONE_IMAGE, SELECT_MULTIPLE_IMAGE, LIST_OF_INTEGER_BOXES, LABELED_SLIDER,
        GEO_LOCATION, DROP_DOWN, RANGE, SUM_OF_PARTS, SIGNATURE
    }

    public String toString() {
        return "identifier: " + getQuestionIdentifier() +
                " instrumentId: " + getInstrumentRemoteId() +
                " displayId: " + getDisplayId() +
                " displayInstrumentId: " + getDisplay().getInstrument().getRemoteId() +
                " deleted: " + isDeleted()
                ;
    }
}