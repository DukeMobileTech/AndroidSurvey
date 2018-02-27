package org.adaptlab.chpir.android.survey.models;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.AppUtil;
import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.FormatUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Table(name = "Questions")
public class Question extends ReceiveModel {

    public static final String FOLLOW_UP_TRIGGER_STRING = "\\[followup\\]";
    private static final String RANDOMIZATION_TRIGGER = "\\[RANDOMIZED_FACTOR\\]";
    private static final String TAG = "QuestionModel";

    @Column(name = "Text")
    private String mText;
    @Column(name = "QuestionType")
    private QuestionType mQuestionType;
    @Column(name = "QuestionIdentifier")
    private String mQuestionIdentifier;
    @Column(name = "FollowingUpQuestion")
    private Question mFollowingUpQuestion;
    @Column(name = "FollowUpPosition")
    private int mFollowUpPosition;
    @Column(name = "RegExValidation")
    private String mRegExValidation;
    @Column(name = "RegExValidationMessage")
    private String mRegExValidationMessage;
    @Column(name = "OptionCount")
    private int mOptionCount;
    @Column(name = "InstrumentVersion")
    private int mInstrumentVersion;
    @Column(name = "NumberInInstrument")
    private int mNumberInInstrument;
    @Column(name = "IdentifiesSurvey")
    private boolean mIdentifiesSurvey;
    // https://github.com/pardom/ActiveAndroid/issues/22
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "ImageCount")
    private int mImageCount;
    @Column(name = "Instructions")
    private String mInstructions;
    @Column(name = "QuestionVersion")
    private int mQuestionVersion;
    @Column(name = "Grid")
    private Grid mGrid;
    @Column(name = "Deleted")
    private boolean mDeleted;
    @Column(name = "Section")
    private Section mSection;
    @Column(name = "InstrumentRemoteId")
    private Long mInstrumentRemoteId;
    @Column(name = "Critical")
    private boolean mCritical;
    @Column(name = "NumberInGrid")
    private int mNumberInGrid;
    @Column(name = "RemoteOptionSetId")
    private Long mRemoteOptionSetId;
    @Column(name = "DisplayId")
    private Long mDisplayId;
    @Column(name = "RemoteSpecialOptionSetId")
    private Long mRemoteSpecialOptionSetId;

    public Question() {
        super();
    }

    /*
     * Finders
     */
    public static List<Question> getAll() {
        return new Select().from(Question.class).where("Deleted != ?", 1).orderBy("Id ASC")
                .execute();
    }

    public static Question findByNumberInInstrument(Integer questionNumber, Long
            instrumentRemoteId) {
        return new Select().from(Question.class).where("NumberInInstrument = ? AND " +
                "InstrumentRemoteId = ? AND Deleted != ?", questionNumber, instrumentRemoteId, 1)
                .executeSingle();
    }

    public String getRegExValidationMessage() {
        if (getInstrument().getLanguage().equals(Instrument.getDeviceLanguage()))
            return mRegExValidationMessage;
        if (activeTranslation() != null) return activeTranslation().getRegExValidationMessage();
        for (QuestionTranslation translation : translations()) {
            if (translation.getLanguage().equals(Instrument.getDeviceLanguage())) {
                return translation.getRegExValidationMessage();
            }
        }

        // Fall back to default
        return mRegExValidationMessage;
    }

    public Instrument getInstrument() {
        return Instrument.findByRemoteId(getInstrumentRemoteId());
    }

    public List<QuestionTranslation> translations() {
        return getMany(QuestionTranslation.class, "Question");
    }

    public Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }

    public void setInstrumentRemoteId(Long instrumentId) {
        mInstrumentRemoteId = instrumentId;
    }

    private void setRegExValidationMessage(String message) {
        if (message.equals("null") || message.equals(""))
            mRegExValidationMessage = null;
        else
            mRegExValidationMessage = message;
    }

    public boolean hasSkipPattern() {
        for (Option option : defaultOptions()) {
            if (option.getNextQuestion() != null &&
                    !option.getNextQuestion().getQuestionIdentifier().equals("")) {
                return true;
            }
        }
        return false;
    }

    public boolean isSkipQuestionType() {
        return (getQuestionType() == QuestionType.SELECT_ONE || getQuestionType() == QuestionType.SELECT_ONE_WRITE_OTHER);
    }

    public boolean isMultipleSkipQuestion(Instrument instrument) {
        List<MultipleSkip> multipleSkips = new Select().from(MultipleSkip.class)
                .where("QuestionIdentifier = ? AND RemoteInstrumentId = ?",
                        getQuestionIdentifier(), instrument.getRemoteId())
                .execute();
        return (multipleSkips.size() > 0);
    }

    public boolean hasSkips(Instrument instrument) {
        List<NextQuestion> skipOptions = new Select().from(NextQuestion.class)
                .where("QuestionIdentifier = ? AND RemoteInstrumentId = ?",
                        getQuestionIdentifier(), instrument.getRemoteId())
                .execute();
        return (skipOptions.size() > 0);
    }

    public boolean hasCompleteSurveyOption() {
        Option completeSurveyOption = new Select().from(Option.class)
                .where("Question = ? AND Deleted != ? AND Special = ? AND CompleteSurvey = ?",
                        getId(), 1, 0, 1)
                .executeSingle();
        return completeSurveyOption != null;
    }

    public List<Option> defaultOptions() {
//        return new Select().from(Option.class)
//                .where("Question = ? AND Deleted != ? AND Special = ?", getId(), 1, 0)
//                .orderBy("NumberInQuestion ASC")
//                .execute();
        return new Select().from(Option.class)
                .where("RemoteOptionSetId = ? AND Deleted != ? AND Special = ?", getRemoteOptionId(), 1, 0)
                .orderBy("NumberInQuestion ASC")
                .execute();
    }

    public String getQuestionIdentifier() {
        return mQuestionIdentifier;
    }

    public void setQuestionIdentifier(String questionIdentifier) {
        mQuestionIdentifier = questionIdentifier;
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
    public String getFollowingUpText(HashMap<Question, Response> responses, Context context) {
        String questionText = getText();
        for (FollowUpQuestion followingUpQuestion : followingUpQuestions()) {
            Response followUpResponse = responses.get(followingUpQuestion.getFollowingUpOnQuestion());

            if (followUpResponse == null || followUpResponse.getText().equals("") || followUpResponse.hasSpecialResponse()) {
                continue;
            }

            if (followUpWithOptionText(followingUpQuestion)) {
                String text = followingUpQuestion.getFollowingUpOnQuestion().getOptionTextByResponse(followUpResponse, context);
                questionText = questionText.replaceFirst(FOLLOW_UP_TRIGGER_STRING, text);
            } else {
                questionText = questionText.replaceFirst(FOLLOW_UP_TRIGGER_STRING, followUpResponse.getText());
            }
        }
        return questionText;
    }

    private List<FollowUpQuestion> followingUpQuestions() {
        return new Select().from(FollowUpQuestion.class)
                .where("QuestionIdentifier = ? AND RemoteInstrumentId = ? ", mQuestionIdentifier, mInstrumentRemoteId)
                .orderBy("Position")
                .execute();
    }

    public List<FollowUpQuestion> toFollowUpOnQuestions() {
        return new Select().from(FollowUpQuestion.class)
                .where("FollowingUpQuestionIdentifier = ? AND RemoteInstrumentId = ? ", mQuestionIdentifier, mInstrumentRemoteId)
                .orderBy("Position")
                .execute();
    }

    public boolean isToFollowUpOnQuestion() {
        return (toFollowUpOnQuestions().size() > 0);
    }

    /*
    * position in QuestionRandomizedFactor starts from 1, hence k + 1
    * save response at the end as a fallback for INSTRUCTIONS questions type which has no questionComponent - saving to db happens here
     */
    public String getRandomizedText(Response response) {
        JSONObject randomizedData;
        String text = getText();
        if (response == null) return text;
        if (!TextUtils.isEmpty(response.getRandomizedData())) {
            try {
                randomizedData = new JSONObject(response.getRandomizedData());
                for (int k = 0; k < questionRandomizedFactors().size(); k++) {
                    text = text.replaceFirst(RANDOMIZATION_TRIGGER, randomizedData.getString(String.valueOf(k + 1)));
                }
            } catch (JSONException e) {
                if (BuildConfig.DEBUG) Log.e(TAG, "JSON exception", e);
            }
        } else {
            Random random = new Random();
            randomizedData = new JSONObject();
            for (int k = 0; k < questionRandomizedFactors().size(); k++) {
                List<RandomizedOption> randomizedOptions = questionRandomizedFactors().get(k).getRandomizedFactor().randomizedOptions();
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

//    public Question getFollowingUpOnQuestion() {
//        return mFollowingUpQuestion;
//    }

    /*
     * Question types which must have their responses (represented as indices)
     * mapped to the original option text.
     */
    public boolean followUpWithOptionText(FollowUpQuestion followingUpQuestion) {
        return followingUpQuestion.getFollowingUpOnQuestion().getQuestionType().equals(QuestionType.SELECT_MULTIPLE) || followingUpQuestion.getFollowingUpOnQuestion().getQuestionType().equals(QuestionType.SELECT_ONE) || followingUpQuestion.getFollowingUpOnQuestion().getQuestionType().equals(QuestionType.SELECT_ONE_WRITE_OTHER) || followingUpQuestion.getFollowingUpOnQuestion().getQuestionType().equals(QuestionType.SELECT_MULTIPLE_WRITE_OTHER);
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
        if (getInstrument().getLanguage().equals(Instrument.getDeviceLanguage())) return mText;
        if (activeTranslation() != null) return activeTranslation().getText();
        for (QuestionTranslation translation : translations()) {
            if (translation.getLanguage().equals(Instrument.getDeviceLanguage())) {
                return translation.getText();
            }
        }

        // Fall back to default
        return mText;
    }

    public QuestionTranslation activeTranslation() {
        if (getInstrument().activeTranslation() == null) return null;
        return new Select().from(QuestionTranslation.class)
                .where("InstrumentTranslation = ? AND Question = ?",
                        getInstrument().activeTranslation().getId(), getId()).executeSingle();
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
                return FormatUtils.unformatMultipleResponses(defaultOptions(), text, context);
            } else if (Integer.parseInt(text) == defaultOptions().size()) {
                return (response.getOtherResponse() == null) ? "Other" : response.getOtherResponse();
            } else {
                return defaultOptions().get(Integer.parseInt(text)).getText();
            }
        } catch (NumberFormatException nfe) {
            Log.e(TAG, text + " is not an option number");
            return text;
        } catch (IndexOutOfBoundsException iob) {
            Log.e(TAG, text + " is an out of range option number");
            return text;
        }
    }

    public QuestionType getQuestionType() {
        return mQuestionType;
    }

    /*
     * Return true if this response can be an array of multiple options.
     */
    public boolean hasMultipleResponses() {
        return getQuestionType().equals(QuestionType.SELECT_MULTIPLE) ||
                getQuestionType().equals(QuestionType.SELECT_MULTIPLE_WRITE_OTHER);
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

    /*
     * Getters/Setters
     */
    public void setText(String text) {
        mText = text;
    }

    public void setFollowingUpQuestion(Question question) {
        mFollowingUpQuestion = question;
    }

    /*
     * Check that all of the options are loaded and that the instrument version
     * numbers of the question components match the expected instrument version
     * number.
     */
    public boolean loaded() {
        return true;
//        return getOptionCount() == optionCount() && getImageCount() == imageCount();
    }

    private int getImageCount() {
        return mImageCount;
    }

    public int imageCount() {
        return new Select().from(Image.class).where("Question = ?", getId()).count();
    }

    private int getOptionCount() {
        return mOptionCount;
    }

    public int optionCount() {
        return new Select().from(Option.class).where("Question = ? AND Deleted != ?", getId(), 1).count();
    }

    public void setOptionCount(int num) {
        mOptionCount = num;
    }

    public void setImageCount(int count) {
        mImageCount = count;
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

            if (AppUtil.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            question.setText(jsonObject.getString("text"));
            question.setQuestionType(jsonObject.getString("question_type"));
            question.setQuestionIdentifier(jsonObject.getString("question_identifier"));
            question.setInstrumentRemoteId(jsonObject.optLong("instrument_id"));
            question.setRegExValidation(jsonObject.optString("reg_ex_validation", null));
            question.setRegExValidationMessage(jsonObject.optString("reg_ex_validation_message", null));
            question.setOptionCount(jsonObject.optInt("option_count"));
            question.setImageCount(jsonObject.optInt("image_count"));
            question.setInstrumentVersion(jsonObject.getInt("instrument_version"));
            question.setIdentifiesSurvey(jsonObject.optBoolean("identifies_survey", false));
            if (!jsonObject.isNull("number_in_instrument")) {
                question.setNumberInInstrument(jsonObject.getInt("number_in_instrument"));
            }
//            if (!jsonObject.isNull("follow_up_position")) {
//                question.setFollowUpPosition(jsonObject.getInt("follow_up_position"));
//            }
            question.setInstructions(jsonObject.getString("instructions"));
            question.setQuestionVersion(jsonObject.getInt("question_version"));
            question.setDisplay(jsonObject.optLong("display_id"));
//            question.setFollowingUpQuestion(Question.findByQuestionIdentifier(jsonObject.getString("following_up_question_identifier")));
//            if (!jsonObject.isNull("grid_id")) {
//                question.setGrid(Grid.findByRemoteId(jsonObject.getLong("grid_id")));
//            }
//            if (!jsonObject.isNull("number_in_grid")) {
//                question.setNumberInGrid(jsonObject.getInt("number_in_grid"));
//            }
//            if (!jsonObject.isNull("section_id")) {
//                question.setSection(Section.findByRemoteId(jsonObject.getLong("section_id")));
//            }
            question.setRemoteId(remoteId);
            if (jsonObject.isNull("deleted_at")) {
                question.setDeleted(false);
            } else {
                question.setDeleted(true);
            }
            if (!jsonObject.isNull("critical")) {
                question.setCritical(jsonObject.getBoolean("critical"));
            }
            question.setRemoteOptionSetId(jsonObject.optLong("option_set_id"));
            question.setRemoteSpecialOptionSetId(jsonObject.optLong("special_option_set_id"));
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
                    translation.setQuestion(question);
                    translation.setText(translationJSON.getString("text"));
                    translation.setRegExValidationMessage(translationJSON.optString("reg_ex_validation_message"));
                    translation.setInstrumentTranslation(InstrumentTranslation.findByRemoteId(translationJSON.optLong("instrument_translation_id")));
                    if (!translationJSON.isNull("instructions")) {
                        translation.setInstructions(translationJSON.getString("instructions"));
                    }
                    translation.save();
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    private void setRemoteSpecialOptionSetId(Long id) {
        mRemoteSpecialOptionSetId = id;
    }

    private void setRemoteOptionSetId(Long id) {
        mRemoteOptionSetId = id;
    }

    public Long getRemoteOptionId() {
        return mRemoteOptionSetId;
    }

    public Long getRemoteSpecialOptionSetId() {
        return mRemoteSpecialOptionSetId;
    }

    public static Question findByRemoteId(Long id) {
        return new Select().from(Question.class).where("RemoteId = ?", id).executeSingle();
    }

    private void setIdentifiesSurvey(boolean identifiesSurvey) {
        mIdentifiesSurvey = identifiesSurvey;
    }

    public static Question findByQuestionIdentifier(String identifier) {
        if (identifier == null) return null;
        return new Select().from(Question.class).where("QuestionIdentifier = ?", identifier).executeSingle();
    }

    private void setNumberInGrid(int num) {
        mNumberInGrid = num;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    /*
     * Find an existing translation, or return a new QuestionTranslation
     * if a translation does not yet exist.
     */
    public QuestionTranslation getTranslationByLanguage(String language) {
        for (QuestionTranslation translation : translations()) {
            if (translation.getLanguage().equals(language)) {
                return translation;
            }
        }

        QuestionTranslation translation = new QuestionTranslation();
        translation.setLanguage(language);
        return translation;
    }

    public static boolean validQuestionType(String questionType) {
        for (QuestionType type : QuestionType.values()) {
            if (type.name().equals(questionType)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFollowUpQuestion() {
        return (followingUpQuestions().size() > 0);
    }

    public List<Question> questionsToSkip() {
        List<Question> toBeSkipped = new ArrayList<Question>();
        for (Option option : options()) {
            for (Question question : option.questionsToSkip()) {
                toBeSkipped.add(question);
            }
        }
        return toBeSkipped;
    }

    public List<Option> options() {
//        return new Select().from(Option.class)
//                .where("Question = ? AND Deleted != ?", getId(), 1)
//                .orderBy("NumberInQuestion ASC")
//                .execute();
        return new Select().from(Option.class)
                .where("RemoteOptionSetId = ? AND Deleted != ?", getRemoteOptionId(), 1)
                .orderBy("NumberInQuestion ASC")
                .execute();
    }

    public Option specialOptionByText(String optionText) {
        return new Select().from(Option.class)
                .where("Question = ? AND Deleted != ? AND Special = ? AND Text = ?", getId(), 1,
                        1, optionText)
                .executeSingle();
    }

    public Option anyResponseOption() {
        return new Select().from(Option.class)
                .where("Question = ? AND Deleted != ? AND Special = ? AND Text = ?",
                        getId(), 1, 1, Option.ANY_RESPONSE)
                .executeSingle();
    }

    /*
     * Relationships
     */
    public boolean hasOptions() {
        return !defaultOptions().isEmpty();
    }

    public boolean hasSpecialOptions() {
        return (mRemoteSpecialOptionSetId > 0);
    }

    public boolean hasSpecialSkips(Instrument instrument) {
        if (!hasSpecialOptions()) return false;
        List<NextQuestion> specialSkipOptions = new Select("NextQuestions.*")
                .from(NextQuestion.class)
                .innerJoin(Option.class)
                .on("Options.Identifier=NextQuestions.OptionIdentifier")
                .where("QuestionIdentifier = ? AND RemoteInstrumentId = ?",
                        getQuestionIdentifier(), instrument.getRemoteId())
                .execute();
        return (specialSkipOptions.size() > 0);
    }

    public List<Option> specialOptions() {
//        return new Select().from(Option.class)
//                .where("Question = ? AND Deleted != ? AND Special = ?", getId(), 1, 1)
//                .orderBy("NumberInQuestion ASC")
//                .execute();
        return new Select().from(Option.class)
                .where("RemoteOptionSetId = ? AND Deleted != ?", mRemoteSpecialOptionSetId, 1)
                .orderBy("NumberInQuestion ASC")
                .execute();
    }

    public List<Image> images() {
        return new Select().from(Image.class).where("Question = ?", getId()).execute();
    }

    public List<Option> criticalOptions() {
        return new Select().from(Option.class)
                .where("Question = ? AND Deleted != ? AND Critical = ?", getId(), 1, 1)
                .orderBy("NumberInQuestion ASC")
                .execute();
    }

    private List<QuestionRandomizedFactor> questionRandomizedFactors() {
        return new Select().from(QuestionRandomizedFactor.class).where("Question = ?", getId()).orderBy("Position ASC").execute();
    }

    public String getRegExValidation() {
        if (TextUtils.isEmpty(mRegExValidation) || mRegExValidation.equals("null")) {
            return null;
        } else {
            return mRegExValidation;
        }
    }

    public void setRegExValidation(String validation) {
        mRegExValidation = validation;
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }

    public int getInstrumentVersion() {
        return mInstrumentVersion;
    }

    public void setInstrumentVersion(int version) {
        mInstrumentVersion = version;
    }

    public int getNumberInInstrument() {
        return mNumberInInstrument;
    }

    private void setNumberInInstrument(int number) {
        mNumberInInstrument = number;
    }

    public int getFollowUpPosition() {
        return mFollowUpPosition;
    }

    private void setFollowUpPosition(int position) {
        mFollowUpPosition = position;
    }

    public boolean identifiesSurvey() {
        return mIdentifiesSurvey;
    }

    public String getInstructions() {
        if (getInstrument().getLanguage().equals(Instrument.getDeviceLanguage())) return mInstructions;
        if (activeTranslation() != null) return activeTranslation().getInstructions();
        for (QuestionTranslation translation : translations()) {
            if (translation.getLanguage().equals(Instrument.getDeviceLanguage())) {
                return translation.getInstructions();
            }
        }
        // Fall back to default
        return mInstructions;
    }

    private void setInstructions(String instructions) {
        mInstructions = instructions;
    }

    public int getQuestionVersion() {
        return mQuestionVersion;
    }

    private void setQuestionVersion(int version) {
        mQuestionVersion = version;
    }

    public int getNumberInGrid() {
        return mNumberInGrid;
    }

    public boolean firstInGrid() {
        return belongsToGrid() && getNumberInGrid() == 1;
    }

    public boolean isFirstQuestionInSection() {
        if (getSection() == null || getSection().questions().size() == 0) return false;
        return (getSection().questions().get(0) == this);
    }

    public Section getSection() {
        return mSection;
    }

    public void setSection(Section section) {
        mSection = section;
    }

    public boolean belongsToGrid() {
        return getGrid() != null;
    }

    public Grid getGrid() {
        return mGrid;
    }

    private void setGrid(Grid grid) {
        mGrid = grid;
    }

    public boolean isLastQuestion() {
        return (this == getInstrument().questions().get(getInstrument().getQuestionCount() - 1));
    }

    public boolean getCritical() {
        return mCritical;
    }

    private void setCritical(boolean critical) {
        mCritical = critical;
    }

    public boolean hasRandomizedFactors() {
        return questionRandomizedFactors().size() > 0;
    }

    private void setDisplay(long display) {
        mDisplayId = display;
    }

    public Display getDisplay() {
        if (mDisplayId == null) return null;
        return Display.findByRemoteId(mDisplayId);
    }

    public enum QuestionType {
        SELECT_ONE, SELECT_MULTIPLE, SELECT_ONE_WRITE_OTHER, SELECT_MULTIPLE_WRITE_OTHER, FREE_RESPONSE, SLIDER, FRONT_PICTURE, REAR_PICTURE, DATE, RATING, TIME, LIST_OF_TEXT_BOXES, INTEGER, EMAIL_ADDRESS, DECIMAL_NUMBER, INSTRUCTIONS, MONTH_AND_YEAR, YEAR, PHONE_NUMBER, ADDRESS, SELECT_ONE_IMAGE, SELECT_MULTIPLE_IMAGE, LIST_OF_INTEGER_BOXES, LABELED_SLIDER, GEO_LOCATION, DROP_DOWN
    }

    public static boolean selectOneVariant(QuestionType type) {
        return (type == QuestionType.SELECT_ONE || type == QuestionType.SELECT_ONE_WRITE_OTHER || type == QuestionType.SELECT_ONE_IMAGE);
    }

    public boolean isOtherQuestionType() {
        return (mQuestionType == QuestionType.SELECT_ONE_WRITE_OTHER);
    }

    public boolean isTextEntryQuestionType() {
        QuestionType type = getQuestionType();
        return (type == QuestionType.FREE_RESPONSE || type == QuestionType.LIST_OF_INTEGER_BOXES || type == QuestionType.LIST_OF_TEXT_BOXES || type == QuestionType.INTEGER || type == QuestionType.EMAIL_ADDRESS || type == QuestionType.DECIMAL_NUMBER || type == QuestionType.INSTRUCTIONS || type == QuestionType.PHONE_NUMBER || type == QuestionType.ADDRESS);
    }

}