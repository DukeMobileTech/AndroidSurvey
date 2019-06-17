package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

public class RatingViewHolder extends QuestionViewHolder {
    private final static int NUM_STARS = 5;
    private float mRating;
    private RatingBar mRatingBar;

    RatingViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
//        mRatingBar = new RatingBar(getActivity());
//        mRatingBar.setNumStars(NUM_STARS);
//        mRatingBar.setLayoutParams(new ViewGroup.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));
//        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
//            @Override
//            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
//                mRating = rating;
//                setResponse(null);
//            }
//        });
//        mRatingBar.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (mSpecialResponses != null) {
//                    mSpecialResponses.clearCheck();
//                }
//                return false;
//            }
//        });
//        questionComponent.addView(mRatingBar);
    }

    @Override
    protected String serialize() {
        return String.valueOf(mRating);
    }

    @Override
    protected void deserialize(String responseText) {
        if (!responseText.equals("")) {
            mRating = Float.parseFloat(responseText);
            mRatingBar.setRating(mRating);
        }
    }

//    @Override
//    protected void unSetResponse() {
//        mRatingBar.setRating(0);
//        setResponseTextBlank();
//    }
}
