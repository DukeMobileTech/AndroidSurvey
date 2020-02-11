package org.adaptlab.chpir.android.survey.entities;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.adaptlab.chpir.android.survey.daos.BaseDao;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "Questions")
public class Question implements SurveyEntity, Translatable {
    public static final String SELECT_ONE = "SELECT_ONE";
    public static final String SELECT_MULTIPLE = "SELECT_MULTIPLE";
    public static final String SELECT_ONE_WRITE_OTHER = "SELECT_ONE_WRITE_OTHER";
    public static final String SELECT_MULTIPLE_WRITE_OTHER = "SELECT_MULTIPLE_WRITE_OTHER";
    public static final String FREE_RESPONSE = "FREE_RESPONSE";
    public static final String SLIDER = "SLIDER";
    public static final String FRONT_PICTURE = "FRONT_PICTURE";
    public static final String REAR_PICTURE = "REAR_PICTURE";
    public static final String DATE = "DATE";
    public static final String RATING = "RATING";
    public static final String TIME = "TIME";
    public static final String LIST_OF_TEXT_BOXES = "LIST_OF_TEXT_BOXES";
    public static final String INTEGER = "INTEGER";
    public static final String EMAIL_ADDRESS = "EMAIL_ADDRESS";
    public static final String DECIMAL_NUMBER = "DECIMAL_NUMBER";
    public static final String INSTRUCTIONS = "INSTRUCTIONS";
    public static final String MONTH_AND_YEAR = "MONTH_AND_YEAR";
    public static final String YEAR = "YEAR";
    public static final String PHONE_NUMBER = "PHONE_NUMBER";
    public static final String ADDRESS = "ADDRESS";
    public static final String SELECT_ONE_IMAGE = "SELECT_ONE_IMAGE";
    public static final String SELECT_MULTIPLE_IMAGE = "SELECT_MULTIPLE_IMAGE";
    public static final String LIST_OF_INTEGER_BOXES = "LIST_OF_INTEGER_BOXES";
    public static final String LABELED_SLIDER = "LABELED_SLIDER";
    public static final String GEO_LOCATION = "GEO_LOCATION";
    public static final String DROP_DOWN = "DROP_DOWN";
    public static final String RANGE = "RANGE";
    public static final String SUM_OF_PARTS = "SUM_OF_PARTS";
    public static final String SIGNATURE = "SIGNATURE";

    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "RemoteId", index = true)
    private Long mRemoteId;
    @SerializedName("text")
    @ColumnInfo(name = "Text")
    private String mText;
    @SerializedName("question_type")
    @ColumnInfo(name = "QuestionType")
    private @QuestionType
    String mQuestionType;
    @SerializedName("question_identifier")
    @ColumnInfo(name = "QuestionIdentifier", index = true)
    private String mQuestionIdentifier;
    @SerializedName("question_id")
    @ColumnInfo(name = "QuestionId", index = true)
    private Long mQuestionId;
    @SerializedName("option_count")
    @ColumnInfo(name = "OptionCount")
    private int mOptionCount;
    @SerializedName("instrument_version")
    @ColumnInfo(name = "InstrumentVersion")
    private int mInstrumentVersion;
    @SerializedName("number_in_instrument")
    @ColumnInfo(name = "NumberInInstrument", index = true)
    private int mNumberInInstrument;
    @SerializedName("position")
    @ColumnInfo(name = "Position", index = true)
    private int mPosition;
    @SerializedName("identifies_survey")
    @ColumnInfo(name = "IdentifiesSurvey")
    private boolean mIdentifiesSurvey;
    @SerializedName("image_count")
    @ColumnInfo(name = "ImageCount")
    private int mImageCount;
    @SerializedName("instruction_id")
    @ColumnInfo(name = "InstructionId")
    private Long mInstructionId;
    @SerializedName("question_version")
    @ColumnInfo(name = "QuestionVersion")
    private int mQuestionVersion;
    @SerializedName("deleted_at")
    @ColumnInfo(name = "Deleted")
    private boolean mDeleted;
    @SerializedName("instrument_id")
    @ColumnInfo(name = "InstrumentRemoteId", index = true)
    private Long mInstrumentRemoteId;
    @SerializedName("option_set_id")
    @ColumnInfo(name = "RemoteOptionSetId")
    private Long mRemoteOptionSetId;
    @SerializedName("display_id")
    @ColumnInfo(name = "DisplayId")
    private Long mDisplayId;
    @SerializedName("special_option_set_id")
    @ColumnInfo(name = "RemoteSpecialOptionSetId")
    private Long mRemoteSpecialOptionSetId;
    @SerializedName("table_identifier")
    @ColumnInfo(name = "TableIdentifier")
    private String mTableIdentifier;
    @SerializedName("validation_id")
    @ColumnInfo(name = "ValidationId")
    private Long mValidationId;
    @SerializedName("rank_responses")
    @ColumnInfo(name = "RankResponses")
    private boolean mRankResponses;
    @SerializedName("loop_question_count")
    @ColumnInfo(name = "LoopQuestionCount")
    private int mLoopQuestionCount;
    @ColumnInfo(name = "LoopSource")
    private String mLoopSource;
    @ColumnInfo(name = "LoopNumber")
    private int mLoopNumber;
    @ColumnInfo(name = "TextToReplace")
    private String mTextToReplace;
    @SerializedName("pop_up_instruction_id")
    @ColumnInfo(name = "PopUpInstructionId")
    private Long mPopUpInstructionId;
    @SerializedName("after_text_instruction_id")
    @ColumnInfo(name = "AfterTextInstructionId")
    private Long mAfterTextInstructionId;
    @SerializedName("carry_forward_identifier")
    @ColumnInfo(name = "CarryForwardIdentifier")
    private String mCarryForwardIdentifier;
    @SerializedName("carry_forward_option_set_id")
    @ColumnInfo(name = "CarryForwardOptionSetId")
    private Long mCarryForwardOptionSetId;
    @SerializedName("default_response")
    @ColumnInfo(name = "DefaultResponse")
    private String mDefaultResponse;

    @Ignore
    @SerializedName("question_translations")
    private List<QuestionTranslation> mQuestionTranslations;

    public static Question copyAttributes(Question destination, Question source) {
        destination.mText = source.mText;
        destination.mQuestionType = source.mQuestionType;
        destination.mOptionCount = source.mOptionCount;
        destination.mInstrumentVersion = source.mInstrumentVersion;
        destination.mIdentifiesSurvey = source.mIdentifiesSurvey;
        destination.mQuestionVersion = source.mQuestionVersion;
        destination.mDeleted = source.mDeleted;
        destination.mInstrumentRemoteId = source.mInstrumentRemoteId;
        destination.mRemoteOptionSetId = source.mRemoteOptionSetId;
        destination.mRemoteSpecialOptionSetId = source.mRemoteSpecialOptionSetId;
        destination.mTableIdentifier = source.mTableIdentifier;
        destination.mValidationId = source.mValidationId;
        destination.mRankResponses = source.mRankResponses;
        destination.mLoopQuestionCount = source.mLoopQuestionCount;
        destination.mQuestionId = source.mQuestionId;
        destination.mPosition = source.mPosition;
        destination.mImageCount = source.mImageCount;
        destination.mInstructionId = source.mInstructionId;
        destination.mPopUpInstructionId = source.mPopUpInstructionId;
        destination.mAfterTextInstructionId = source.mAfterTextInstructionId;
        destination.mCarryForwardIdentifier = source.mCarryForwardIdentifier;
        destination.mCarryForwardOptionSetId = source.mCarryForwardOptionSetId;
        destination.mDefaultResponse = source.mDefaultResponse;
        return destination;
    }

    @NonNull
    public Long getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(@NonNull Long remoteId) {
        this.mRemoteId = remoteId;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    @QuestionType
    public String getQuestionType() {
        return mQuestionType;
    }

    public void setQuestionType(@QuestionType String questionType) {
        this.mQuestionType = questionType;
    }

    public String getQuestionIdentifier() {
        return mQuestionIdentifier;
    }

    public void setQuestionIdentifier(String questionIdentifier) {
        this.mQuestionIdentifier = questionIdentifier;
    }

    public Long getQuestionId() {
        return mQuestionId;
    }

    public void setQuestionId(Long questionId) {
        this.mQuestionId = questionId;
    }

    public int getOptionCount() {
        return mOptionCount;
    }

    public void setOptionCount(int optionCount) {
        this.mOptionCount = optionCount;
    }

    public int getInstrumentVersion() {
        return mInstrumentVersion;
    }

    public void setInstrumentVersion(int instrumentVersion) {
        this.mInstrumentVersion = instrumentVersion;
    }

    public int getNumberInInstrument() {
        return mNumberInInstrument;
    }

    public void setNumberInInstrument(int numberInInstrument) {
        this.mNumberInInstrument = numberInInstrument;
    }

    public boolean isIdentifiesSurvey() {
        return mIdentifiesSurvey;
    }

    public void setIdentifiesSurvey(boolean identifiesSurvey) {
        this.mIdentifiesSurvey = identifiesSurvey;
    }

    public int getImageCount() {
        return mImageCount;
    }

    public void setImageCount(int imageCount) {
        this.mImageCount = imageCount;
    }

    public Long getInstructionId() {
        return mInstructionId;
    }

    public void setInstructionId(Long instructionId) {
        this.mInstructionId = instructionId;
    }

    public int getQuestionVersion() {
        return mQuestionVersion;
    }

    public void setQuestionVersion(int questionVersion) {
        this.mQuestionVersion = questionVersion;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }

    public Long getInstrumentRemoteId() {
        return mInstrumentRemoteId;
    }

    public void setInstrumentRemoteId(Long instrumentRemoteId) {
        this.mInstrumentRemoteId = instrumentRemoteId;
    }

    public Long getRemoteOptionSetId() {
        return mRemoteOptionSetId;
    }

    public void setRemoteOptionSetId(Long remoteOptionSetId) {
        this.mRemoteOptionSetId = remoteOptionSetId;
    }

    public Long getDisplayId() {
        return mDisplayId;
    }

    public void setDisplayId(Long displayId) {
        this.mDisplayId = displayId;
    }

    public Long getRemoteSpecialOptionSetId() {
        return mRemoteSpecialOptionSetId;
    }

    public void setRemoteSpecialOptionSetId(Long remoteSpecialOptionSetId) {
        this.mRemoteSpecialOptionSetId = remoteSpecialOptionSetId;
    }

    public String getTableIdentifier() {
        return mTableIdentifier;
    }

    public void setTableIdentifier(String tableIdentifier) {
        this.mTableIdentifier = tableIdentifier;
    }

    public Long getValidationId() {
        return mValidationId;
    }

    public void setValidationId(Long validationId) {
        this.mValidationId = validationId;
    }

    public boolean isRankResponses() {
        return mRankResponses;
    }

    public void setRankResponses(boolean rankResponses) {
        this.mRankResponses = rankResponses;
    }

    public int getLoopQuestionCount() {
        return mLoopQuestionCount;
    }

    public void setLoopQuestionCount(int loopQuestionCount) {
        this.mLoopQuestionCount = loopQuestionCount;
    }

    public String getLoopSource() {
        return mLoopSource;
    }

    public void setLoopSource(String loopSource) {
        this.mLoopSource = loopSource;
    }

    public int getLoopNumber() {
        return mLoopNumber;
    }

    public void setLoopNumber(int loopNumber) {
        this.mLoopNumber = loopNumber;
    }

    public String getTextToReplace() {
        return mTextToReplace;
    }

    public void setTextToReplace(String textToReplace) {
        this.mTextToReplace = textToReplace;
    }

    public List<QuestionTranslation> getQuestionTranslations() {
        return mQuestionTranslations;
    }

    public void setQuestionTranslations(List<QuestionTranslation> translations) {
        this.mQuestionTranslations = translations;
    }

    @Override
    public Type getType() {
        return new TypeToken<ArrayList<Question>>() {
        }.getType();
    }

    @Override
    public List<QuestionTranslation> getTranslations() {
        return mQuestionTranslations;
    }

    @Override
    public void save(BaseDao dao, List list) {
        dao.updateAll(list);
        dao.insertAll(list);
    }

    public boolean isOtherQuestionType() {
        return (mQuestionType.equals(SELECT_ONE_WRITE_OTHER) || mQuestionType.equals(SELECT_MULTIPLE_WRITE_OTHER));
    }

    public boolean isMultipleResponseLoop() {
        return (mQuestionType.equals(SELECT_MULTIPLE) || mQuestionType.equals(SELECT_MULTIPLE_WRITE_OTHER) ||
                mQuestionType.equals(LIST_OF_INTEGER_BOXES) || mQuestionType.equals(LIST_OF_TEXT_BOXES));
    }

    public boolean isSingleResponse() {
        return (mQuestionType.equals(SELECT_ONE) || mQuestionType.equals(SELECT_ONE_WRITE_OTHER) ||
                mQuestionType.equals(DROP_DOWN));
    }

    public boolean isMultipleResponse() {
        return mQuestionType.equals(SELECT_MULTIPLE) || mQuestionType.equals(SELECT_MULTIPLE_WRITE_OTHER);
    }

    public boolean isListResponse() {
        return mQuestionType.equals(LIST_OF_INTEGER_BOXES) || mQuestionType.equals(LIST_OF_TEXT_BOXES);
    }

    public boolean isCarryForward() {
        return !TextUtils.isEmpty(mCarryForwardIdentifier);
    }

    @NonNull
    public String toString() {
        return new ToStringBuilder(this).
                append("RemoteId", mRemoteId).
                append("NumberInInstrument", mNumberInInstrument).
                append("Text", mText).
                append("QuestionIdentifier", mQuestionIdentifier).
                append("DisplayId", mDisplayId).
                append("InstrumentRemoteId", mInstrumentRemoteId).
                append("QuestionType", mQuestionType).
                append("OptionSetId", mRemoteOptionSetId).
                append("RemoteOptionSetId", mRemoteSpecialOptionSetId).
                append("TableIdentifier", mTableIdentifier).
                toString();
    }

    public Long getPopUpInstructionId() {
        return mPopUpInstructionId;
    }

    public void setPopUpInstructionId(Long mPopUpInstruction) {
        this.mPopUpInstructionId = mPopUpInstruction;
    }

    public Long getAfterTextInstructionId() {
        return mAfterTextInstructionId;
    }

    public void setAfterTextInstructionId(Long id) {
        this.mAfterTextInstructionId = id;
    }

    public String getCarryForwardIdentifier() {
        return mCarryForwardIdentifier;
    }

    public void setCarryForwardIdentifier(String mCarryForwardIdentifier) {
        this.mCarryForwardIdentifier = mCarryForwardIdentifier;
    }

    public String getDefaultResponse() {
        return mDefaultResponse;
    }

    public void setDefaultResponse(String mDefaultResponse) {
        this.mDefaultResponse = mDefaultResponse;
    }

    public Long getCarryForwardOptionSetId() {
        return mCarryForwardOptionSetId;
    }

    public void setCarryForwardOptionSetId(Long mCarryForwardOptionSetId) {
        this.mCarryForwardOptionSetId = mCarryForwardOptionSetId;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({SELECT_ONE, SELECT_MULTIPLE, SELECT_ONE_WRITE_OTHER, SELECT_MULTIPLE_WRITE_OTHER,
            FREE_RESPONSE, SLIDER, FRONT_PICTURE, REAR_PICTURE, DATE, RATING, TIME, LIST_OF_TEXT_BOXES,
            INTEGER, EMAIL_ADDRESS, DECIMAL_NUMBER, INSTRUCTIONS, MONTH_AND_YEAR, YEAR, PHONE_NUMBER,
            ADDRESS, SELECT_ONE_IMAGE, SELECT_MULTIPLE_IMAGE, LIST_OF_INTEGER_BOXES, LABELED_SLIDER,
            GEO_LOCATION, DROP_DOWN, RANGE, SUM_OF_PARTS, SIGNATURE})
    public @interface QuestionType {
    }

}
