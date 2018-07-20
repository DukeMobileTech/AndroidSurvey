package org.adaptlab.chpir.android.survey;

import android.support.v4.app.Fragment;
import android.util.Log;

import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.questionfragments.AddressQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.DateQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.DecimalNumberQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.DropDownQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.EmailAddressQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.FreeResponseQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.FrontPictureQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.GeoLocationFragment;
import org.adaptlab.chpir.android.survey.questionfragments.InstructionsQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.IntegerQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.LabeledSliderQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.ListOfIntegerBoxesQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.ListOfTextBoxesQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.MonthAndYearQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.PhoneNumberQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.RangeQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.RatingQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.RearPictureQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.SelectMultipleImageQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.SelectMultipleQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.SelectMultipleWriteOtherQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.SelectOneImageQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.SelectOneQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.SelectOneWriteOtherQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.SliderQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.TimeQuestionFragment;
import org.adaptlab.chpir.android.survey.questionfragments.YearQuestionFragment;

public class QuestionFragmentFactory {
    private static final String TAG = "QuestionFragmentFactory";

    public static Fragment createQuestionFragment(Question question) {
        String type;
        if (question.getQuestionType() == null) {
            type = Question.QuestionType.FREE_RESPONSE.toString();
        } else {
            type = question.getQuestionType().toString();
        }
        Fragment fragment;

        if (Question.QuestionType.SELECT_ONE.toString().equals(type)) {
            fragment = new SelectOneQuestionFragment();
        } else if (Question.QuestionType.SELECT_MULTIPLE.toString().equals(type)) {
            fragment = new SelectMultipleQuestionFragment();
        } else if (Question.QuestionType.SELECT_ONE_WRITE_OTHER.toString().equals(type)) {
            fragment = new SelectOneWriteOtherQuestionFragment();
        } else if (Question.QuestionType.SELECT_MULTIPLE_WRITE_OTHER.toString().equals(type)) {
            fragment = new SelectMultipleWriteOtherQuestionFragment();
        } else if (Question.QuestionType.FREE_RESPONSE.toString().equals(type)) {
            fragment = new FreeResponseQuestionFragment();
        } else if (Question.QuestionType.SLIDER.toString().equals(type)) {
            fragment = new SliderQuestionFragment();
        } else if (Question.QuestionType.FRONT_PICTURE.toString().equals(type)) {
            fragment = new FrontPictureQuestionFragment();
        } else if (Question.QuestionType.REAR_PICTURE.toString().equals(type)) {
            fragment = new RearPictureQuestionFragment();
        } else if (Question.QuestionType.DATE.toString().equals(type)) {
            fragment = new DateQuestionFragment();
        } else if (Question.QuestionType.RATING.toString().equals(type)) {
            fragment = new RatingQuestionFragment();
        } else if (Question.QuestionType.TIME.toString().equals(type)) {
            fragment = new TimeQuestionFragment();
        } else if (Question.QuestionType.LIST_OF_TEXT_BOXES.toString().equals(type)) {
            fragment = new ListOfTextBoxesQuestionFragment();
        } else if (Question.QuestionType.INTEGER.toString().equals(type)) {
            fragment = new IntegerQuestionFragment();
        } else if (Question.QuestionType.EMAIL_ADDRESS.toString().equals(type)) {
            fragment = new EmailAddressQuestionFragment();
        } else if (Question.QuestionType.DECIMAL_NUMBER.toString().equals(type)) {
            fragment = new DecimalNumberQuestionFragment();
        } else if (Question.QuestionType.INSTRUCTIONS.toString().equals(type)) {
            fragment = new InstructionsQuestionFragment();
        } else if (Question.QuestionType.MONTH_AND_YEAR.toString().equals(type)) {
            fragment = new MonthAndYearQuestionFragment();
        } else if (Question.QuestionType.YEAR.toString().equals(type)) {
            fragment = new YearQuestionFragment();
        } else if (Question.QuestionType.PHONE_NUMBER.toString().equals(type)) {
            fragment = new PhoneNumberQuestionFragment();
        } else if (Question.QuestionType.ADDRESS.toString().equals(type)) {
            fragment = new AddressQuestionFragment();
        } else if (Question.QuestionType.SELECT_ONE_IMAGE.toString().equals(type)) {
            fragment = new SelectOneImageQuestionFragment();
        } else if (Question.QuestionType.SELECT_MULTIPLE_IMAGE.toString().equals(type)) {
            fragment = new SelectMultipleImageQuestionFragment();
        } else if (Question.QuestionType.LIST_OF_INTEGER_BOXES.toString().equals(type)) {
            fragment = new ListOfIntegerBoxesQuestionFragment();
        } else if (Question.QuestionType.LABELED_SLIDER.toString().equals(type)) {
            fragment = new LabeledSliderQuestionFragment();
        } else if (Question.QuestionType.GEO_LOCATION.toString().equals(type)) {
            fragment = new GeoLocationFragment();
        } else if (Question.QuestionType.DROP_DOWN.toString().equals(type)) {
            fragment = new DropDownQuestionFragment();
        } else if (Question.QuestionType.RANGE.toString().equals(type)) {
            fragment = new RangeQuestionFragment();
        } else {
            // Return free response fragment if unknown question type
            // This should never happen
            Log.wtf(TAG, "Received unknown question type: " + type);
            fragment = new FreeResponseQuestionFragment();
        }

        return fragment;
    }
}