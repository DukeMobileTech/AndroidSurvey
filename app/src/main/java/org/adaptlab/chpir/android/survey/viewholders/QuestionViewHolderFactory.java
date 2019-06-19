package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;

import org.adaptlab.chpir.android.survey.entities.Question;
import org.adaptlab.chpir.android.survey.utils.ConstantUtils;

public class QuestionViewHolderFactory {
    private static final int ADDRESS = 0;
    private static final int DATE = 1;
    private static final int DECIMAL = 2;
    private static final int DROP_DOWN = 3;
    private static final int EMAIL = 4;
    private static final int FREE_RESPONSE = 5;
    private static final int FRONT_PICTURE = 6;
    private static final int GEO_LOCATION = 7;
    private static final int INSTRUCTIONS = 8;
    private static final int INTEGER = 9;
    private static final int LABELED_SLIDER = 10;
    private static final int INTEGER_BOX = 11;
    private static final int TEXT_BOX = 12;
    private static final int MONTH_AND_YEAR = 13;
    private static final int MULTIPLE = 14;
    private static final int PHONE_NUMBER = 15;
    private static final int RANGE = 16;
    private static final int RATING = 17;
    private static final int REAR_PICTURE = 18;
    private static final int MULTIPLE_IMAGE = 19;
    private static final int MULTIPLE_WRITE_OTHER = 20;
    private static final int ONE_IMAGE = 21;
    private static final int ONE = 22;
    private static final int ONE_WRITE_OTHER = 23;
    private static final int SIGNATURE = 24;
    private static final int SLIDER = 25;
    private static final int SUM = 26;
    private static final int TIME = 27;
    private static final int YEAR = 28;
    private static final int SELECT_ONE_TABLE = 29;
    private static final int SELECT_MULTIPLE_TABLE = 30;
    public static final int TABLE_HEADER = 31;

    public static QuestionViewHolder createViewHolder(View view, Context context, int viewType, QuestionViewHolder.OnResponseSelectedListener listener) {
        switch (viewType) {
            case ADDRESS:
                return new AddressViewHolder(view, context, listener);
            case DATE:
                return new DateViewHolder(view, context, listener);
            case DECIMAL:
                return new DecimalViewHolder(view, context, listener);
            case DROP_DOWN:
                return new DropDownViewHolder(view, context, listener);
            case EMAIL:
                return new EmailViewHolder(view, context, listener);
            case FREE_RESPONSE:
                return new FreeResponseViewHolder(view, context, listener);
            case FRONT_PICTURE:
                return new FrontPictureViewHolder(view, context, listener);
            case GEO_LOCATION:
                return new GeoLocationViewHolder(view, context, listener);
            case INSTRUCTIONS:
                return new InstructionsViewHolder(view, context, listener);
            case INTEGER:
                return new IntegerViewHolder(view, context, listener);
            case LABELED_SLIDER:
                return new LabeledSliderViewHolder(view, context, listener);
            case INTEGER_BOX:
                return new IntegerBoxesViewHolder(view, context, listener);
            case TEXT_BOX:
                return new TextBoxesViewHolder(view, context, listener);
            case MONTH_AND_YEAR:
                return new MonthAndYearViewHolder(view, context, listener);
            case PHONE_NUMBER:
                return new PhoneNumberViewHolder(view, context, listener);
            case RANGE:
                return new RangeViewHolder(view, context, listener);
            case RATING:
                return new RatingViewHolder(view, context, listener);
            case REAR_PICTURE:
                return new RearPictureViewHolder(view, context, listener);
            case MULTIPLE:
                return new SelectMultipleViewHolder(view, context, listener);
            case MULTIPLE_IMAGE:
                return new SelectMultipleImageViewHolder(view, context, listener);
            case MULTIPLE_WRITE_OTHER:
                return new SelectMultipleWriteOtherViewHolder(view, context, listener);
            case ONE:
                return new SelectOneViewHolder(view, context, listener);
            case ONE_IMAGE:
                return new SelectOneImageViewHolder(view, context, listener);
            case ONE_WRITE_OTHER:
                return new SelectOneWriteOtherViewHolder(view, context, listener);
            case SIGNATURE:
                return new SignatureViewHolder(view, context, listener);
            case SLIDER:
                return new SliderViewHolder(view, context, listener);
            case SUM:
                return new SumOfPartsViewHolder(view, context, listener);
            case TIME:
                return new TimeViewHolder(view, context, listener);
            case YEAR:
                return new YearViewHolder(view, context, listener);
            case SELECT_ONE_TABLE:
                return new SelectOneTableViewHolder(view, context, listener);
            case SELECT_MULTIPLE_TABLE:
                return new SelectMultipleTableViewHolder(view, context, listener);
            case TABLE_HEADER:
                return new TableHeaderViewHolder(view, context);
            default:
                return new FreeResponseViewHolder(view, context, listener);
        }
    }

    public static int getQuestionViewType(String type) {
        switch (type) {
            case Question.ADDRESS:
                return ADDRESS;
            case Question.DATE:
                return DATE;
            case Question.DECIMAL_NUMBER:
                return DECIMAL;
            case Question.DROP_DOWN:
                return DROP_DOWN;
            case Question.EMAIL_ADDRESS:
                return EMAIL;
            case Question.FREE_RESPONSE:
                return FREE_RESPONSE;
            case Question.FRONT_PICTURE:
                return FRONT_PICTURE;
            case Question.GEO_LOCATION:
                return GEO_LOCATION;
            case Question.INSTRUCTIONS:
                return INSTRUCTIONS;
            case Question.INTEGER:
                return INTEGER;
            case Question.LABELED_SLIDER:
                return LABELED_SLIDER;
            case Question.LIST_OF_INTEGER_BOXES:
                return INTEGER_BOX;
            case Question.LIST_OF_TEXT_BOXES:
                return TEXT_BOX;
            case Question.MONTH_AND_YEAR:
                return MONTH_AND_YEAR;
            case Question.PHONE_NUMBER:
                return PHONE_NUMBER;
            case Question.RANGE:
                return RANGE;
            case Question.RATING:
                return RATING;
            case Question.REAR_PICTURE:
                return REAR_PICTURE;
            case Question.SELECT_MULTIPLE:
                return MULTIPLE;
            case Question.SELECT_MULTIPLE_IMAGE:
                return MULTIPLE_IMAGE;
            case Question.SELECT_MULTIPLE_WRITE_OTHER:
                return MULTIPLE_WRITE_OTHER;
            case Question.SELECT_ONE:
                return ONE;
            case Question.SELECT_ONE_IMAGE:
                return ONE_IMAGE;
            case Question.SELECT_ONE_WRITE_OTHER:
                return ONE_WRITE_OTHER;
            case Question.SIGNATURE:
                return SIGNATURE;
            case Question.SLIDER:
                return SLIDER;
            case Question.SUM_OF_PARTS:
                return SUM;
            case Question.TIME:
                return TIME;
            case Question.YEAR:
                return YEAR;
            case ConstantUtils.SELECT_ONE_TABLE:
                return SELECT_ONE_TABLE;
            case ConstantUtils.SELECT_MULTIPLE_TABLE:
                return SELECT_MULTIPLE_TABLE;
            case ConstantUtils.TABLE_HEADER:
                return TABLE_HEADER;
            default:
                return FREE_RESPONSE;
        }
    }

}
