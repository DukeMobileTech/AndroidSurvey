package org.adaptlab.chpir.android.survey.roster.rosterfragments;

import org.adaptlab.chpir.android.survey.models.Question;

public class RosterFragmentGenerator {

    public static RosterFragment createQuestionFragment(Question.QuestionType type) {
        switch (type) {
            case FREE_RESPONSE:
                return new FreeResponseFragment();
            case INTEGER:
                return new IntegerFragment();
            case DATE:
                return new DateFragment();
            case SELECT_ONE:
                return new SelectOneFragment();
            case SELECT_MULTIPLE:
                return new SelectMultipleFragment();
            case SELECT_ONE_WRITE_OTHER:
                return new SelectOneWriteOtherFragment();
            default:
                return new FreeResponseFragment();
        }
    }
}