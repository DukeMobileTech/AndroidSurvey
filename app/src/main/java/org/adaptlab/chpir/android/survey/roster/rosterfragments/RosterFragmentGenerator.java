package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import org.adaptlab.chpir.android.survey.models.Question;

public class RosterFragmentGenerator {

    public static RosterFragment createQuestionFragment(Question.QuestionType type) {
        switch (type) {
            case ADDRESS:
                return new AddressFragment();
            case DECIMAL_NUMBER:
                return new DecimalNumberFragment();
            case EMAIL_ADDRESS:
                return new EmailAddressFragment();
            case FREE_RESPONSE:
                return new FreeResponseFragment();
            case INTEGER:
                return new IntegerFragment();
            case DATE:
                return new DateFragment();
            case LIST_OF_INTEGER_BOXES:
                return new ListOfIntegerBoxesFragment();
            case LIST_OF_TEXT_BOXES:
                return new ListOfTextBoxesFragment();
            case SELECT_ONE:
                return new SelectOneFragment();
            case SELECT_MULTIPLE:
                return new SelectMultipleFragment();
            case SELECT_ONE_WRITE_OTHER:
                return new SelectOneWriteOtherFragment();
            case SELECT_MULTIPLE_WRITE_OTHER:
                return new SelectMultipleWriteOtherFragment();
            default:
                return new FreeResponseFragment();
        }
    }
}