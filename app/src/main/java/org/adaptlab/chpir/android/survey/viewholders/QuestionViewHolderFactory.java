package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;

import org.adaptlab.chpir.android.survey.entities.Question;

public class QuestionViewHolderFactory {
    private static final int ADDRESS = 0, DATE = 1, DECIMAL = 2, DROP_DOWN = 3, EMAIL = 4, FREE_RESPONSE = 5,
            FRONT_PICTURE = 6, GEO_LOCATION = 7, INSTRUCTIONS = 8, INTEGER = 9, LABELED_SLIDER = 10,
            INTEGER_BOX = 11, TEXT_BOX = 12, MONTH_AND_YEAR = 13, MULTIPLE = 14, PHONE_NUMBER = 15,
            RANGE = 16, RATING = 17, REAR_PICTURE = 18, MULTIPLE_IMAGE = 19, MULTIPLE_WRITE_OTHER = 20,
            ONE_IMAGE = 21, ONE = 22, ONE_WRITE_OTHER = 23, SIGNATURE = 24, SLIDER = 25, SUM = 26,
            TIME = 27, YEAR = 28;

    public static QuestionViewHolder createViewHolder(View view, Context context, int viewType) {
        switch (viewType) {
            case ADDRESS:
                return new AddressViewHolder(view, context);
            case DATE:
                return new DateViewHolder(view, context);
            case DECIMAL:
                return new DecimalViewHolder(view, context);
            case DROP_DOWN:
                return new DropDownViewHolder(view, context);
            case EMAIL:
                return new EmailViewHolder(view, context);
            case FREE_RESPONSE:
                return new FreeResponseViewHolder(view, context);
            case FRONT_PICTURE:
                return new FrontPictureViewHolder(view, context);
            case GEO_LOCATION:
                return new GeoLocationViewHolder(view, context);
            case INSTRUCTIONS:
                return new InstructionsViewHolder(view, context);
            case INTEGER:
                return new IntegerViewHolder(view, context);
            case LABELED_SLIDER:
                return new LabeledSliderViewHolder(view, context);
            case INTEGER_BOX:
                return new IntegerBoxesViewHolder(view, context);
            case TEXT_BOX:
                return new TextBoxesViewHolder(view, context);
            case MONTH_AND_YEAR:
                return new MonthAndYearViewHolder(view, context);
            case PHONE_NUMBER:
                return new PhoneNumberViewHolder(view, context);
            case RANGE:
                return new RangeViewHolder(view, context);
            case RATING:
                return new RatingViewHolder(view, context);
            case REAR_PICTURE:
                return new RearPictureViewHolder(view, context);
            case MULTIPLE:
                return new SelectMultipleViewHolder(view, context);
            case MULTIPLE_IMAGE:
                return new SelectMultipleImageViewHolder(view, context);
            case MULTIPLE_WRITE_OTHER:
                return new SelectMultipleWriteOtherViewHolder(view, context);
            case ONE:
                return new SelectOneViewHolder(view, context);
            case ONE_IMAGE:
                return new SelectOneImageViewHolder(view, context);
            case ONE_WRITE_OTHER:
                return new SelectOneWriteOtherViewHolder(view, context);
            case SIGNATURE:
                return new SignatureViewHolder(view, context);
            case SLIDER:
                return new SliderViewHolder(view, context);
            case SUM:
                return new SumOfPartsViewHolder(view, context);
            case TIME:
                return new TimeViewHolder(view, context);
            case YEAR:
                return new YearViewHolder(view, context);
            default:
                return new FreeResponseViewHolder(view, context);
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
            default:
                return FREE_RESPONSE;
        }
    }

}
