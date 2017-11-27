package org.adaptlab.chpir.android.survey.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.adaptlab.chpir.android.activerecordcloudsync.ReceiveModel;
import org.adaptlab.chpir.android.survey.AppUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@Table(name = "Options")
public class Option extends ReceiveModel {
    public static final String ANY_RESPONSE = "ANY RESPONSE";
    private static final String TAG = "Option";

    @Column(name = "Question")
    private Question mQuestion;
    @Column(name = "Text")
    private String mText;
    // https://github.com/pardom/ActiveAndroid/issues/22
    @Column(name = "RemoteId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private Long mRemoteId;
    @Column(name = "NextQuestion")
    private String mNextQuestion;
    @Column(name = "NumberInQuestion")
    private int mNumberInQuestion;
    @Column(name = "InstrumentVersion")
    private int mInstrumentVersion;
    @Column(name = "Deleted")
    private boolean mDeleted;
    @Column(name = "Special")
    private boolean mSpecial;
    @Column(name = "Critical")
    private boolean mCritical;
    @Column(name = "CompleteSurvey")
    private boolean mCompleteSurvey;
    @Column(name = "RemoteOptionSetId")
    private Long mRemoteOptionSetId;

    public Option() {
        super();
    }

    public static List<Option> getAll() {
        return new Select().from(Option.class).where("Deleted != ?", 1).orderBy("Id ASC").execute();
    }

    /*
     * If the language of the instrument is the same as the language setting on the
     * device (or through the admin settings), then return the default option text.
     *
     * If another language is requested, iterate through option translations to
     * find translated text.
     *
     * If the language requested is not available as a translation, return the non-translated
     * text for the option.
     */
    public String getText() {
        if (getInstrument().getLanguage().equals(getDeviceLanguage())) return mText;
        if (activeTranslation() != null) return activeTranslation().getText();
        for (OptionTranslation translation : translations()) {
            if (translation.getLanguage().equals(getDeviceLanguage())) {
                return translation.getText();
            }
        }

        // Fall back to default
        return mText;
    }

    public OptionTranslation activeTranslation() {
        if (getInstrument().activeTranslation() == null) return null;
        return new Select().from(OptionTranslation.class)
                .where("InstrumentTranslation = ? AND Option = ?",
                        getInstrument().activeTranslation().getId(), getId()).executeSingle();
    }

    private Instrument getInstrument() {
        return getQuestion().getInstrument();
    }

    // TODO: 11/27/17 FIX
    public Question getQuestion() {
//        return mQuestion;
        return new Select().from(Question.class).where("RemoteOptionSetId = ?", getRemoteOptionId()).executeSingle();
    }

    public String getDeviceLanguage() {
        return Instrument.getDeviceLanguage();
    }

    /*
     * Relationships
     */
    public List<OptionTranslation> translations() {
        return getMany(OptionTranslation.class, "Option");
    }

    public void setQuestion(Question question) {
        mQuestion = question;
    }

    public void setText(String text) {
        mText = text;
    }

    @Override
    public void createObjectFromJSON(JSONObject jsonObject) {
        try {
            Long remoteId = jsonObject.getLong("id");

            // If an option already exists, update it from the remote
            Option option = Option.findByRemoteId(remoteId);
            if (option == null) {
                option = this;
            }
            option.setRemoteId(remoteId);

            if (AppUtil.DEBUG) Log.i(TAG, "Creating object from JSON Object: " + jsonObject);
            option.setText(jsonObject.getString("text"));
//            option.setQuestion(Question.findByRemoteId(jsonObject.getLong("question_id")));
//            option.setNextQuestion(jsonObject.getString("next_question"));
//            if (!jsonObject.isNull("number_in_question")) {
//                option.setNumberInQuestion(jsonObject.getInt("number_in_question"));
//            }
//            option.setInstrumentVersion(jsonObject.getInt("instrument_version"));
//            option.setSpecial(jsonObject.getBoolean("special"));
//            if (!jsonObject.isNull("deleted_at")) {
//                option.setDeleted(true);
//            }
//            if (!jsonObject.isNull("critical")) {
//                option.setCritical(jsonObject.getBoolean("critical"));
//            }
//            option.setCompleteSurvey(jsonObject.optBoolean("complete_survey"));
            option.setRemoteOptionSetId(jsonObject.optLong("option_set_id"));
            option.save();

            // Generate translations
            JSONArray translationsArray = jsonObject.optJSONArray("option_translations");
            if (translationsArray != null) {
                for (int i = 0; i < translationsArray.length(); i++) {
                    JSONObject translationJSON = translationsArray.getJSONObject(i);
                    Long translationRemoteId = translationJSON.getLong("id");
                    OptionTranslation translation = OptionTranslation.findByRemoteId(translationRemoteId);
                    if (translation == null) {
                        translation = new OptionTranslation();
                    }
                    translation.setRemoteId(translationRemoteId);
                    translation.setLanguage(translationJSON.getString("language"));
                    translation.setOption(option);
                    translation.setText(translationJSON.getString("text"));
                    translation.setInstrumentTranslation(InstrumentTranslation.findByRemoteId(
                            translationJSON.optLong("instrument_translation_id")));
                    translation.save();
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "Error parsing object json", je);
        }
    }

    private void setRemoteOptionSetId(Long id) {
        mRemoteOptionSetId = id;
    }

    private Long getRemoteOptionId() {
        return mRemoteOptionSetId;
    }

    public static Option findByRemoteId(Long id) {
        return new Select().from(Option.class).where("RemoteId = ?", id).executeSingle();
    }

    /*
     * Find an existing translation, or return a new OptionTranslation
     * if a translation does not yet exist.
     */
    public OptionTranslation getTranslationByLanguage(String language) {
        for (OptionTranslation translation : translations()) {
            if (translation.getLanguage().equals(language)) {
                return translation;
            }
        }

        OptionTranslation translation = new OptionTranslation();
        translation.setLanguage(language);
        return translation;
    }

    // Used for skip patterns
    public Question getNextQuestion() {
        return findByQuestionIdentifier(mNextQuestion);
    }

    public Question findByQuestionIdentifier(String question) {
        return Question.findByQuestionIdentifier(question);
    }

    private void setNextQuestion(String nextQuestion) {
        mNextQuestion = nextQuestion;
    }

    public List<Skip> skips() {
        return getMany(Skip.class, "Option");
    }

    public List<Question> questionsToSkip() {
        return new Select("Questions.*").from(Question.class).innerJoin(Skip.class).on("Questions" +
                ".Id = Skips.Question AND Skips.Option =?", getId()).execute();
    }

    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(Long id) {
        mRemoteId = id;
    }

    public int getNumberInQuestion() {
        return mNumberInQuestion;
    }

    private void setNumberInQuestion(int number) {
        mNumberInQuestion = number;
    }

    public int getInstrumentVersion() {
        return mInstrumentVersion;
    }

    private void setInstrumentVersion(int version) {
        mInstrumentVersion = version;
    }

    public boolean getSpecial() {
        return mSpecial;
    }

    private void setSpecial(boolean special) {
        mSpecial = special;
    }

    public boolean getDeleted() {
        return mDeleted;
    }

    private void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public boolean getCritical() {
        return mCritical;
    }

    private void setCritical(boolean critical) {
        mCritical = critical;
    }

    public boolean getCompleteSurvey() {
        return mCompleteSurvey;
    }

    private void setCompleteSurvey(boolean completeSurvey) {
        mCompleteSurvey = completeSurvey;
    }
}