package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;

import java.util.List;

public class PairwiseComparisonViewHolder extends QuestionViewHolder {
    private float mProgress;
    private Slider mSlider;

    PairwiseComparisonViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    protected void beforeAddViewHook(ViewGroup questionComponent) {
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        List<OptionRelation> optionRelations = getOptionRelations();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        double targetWidth = 0.25 * deviceWidth;

        ConstraintLayout imageLayout = (ConstraintLayout) inflater.inflate(R.layout.pairwise, null);
        for (final OptionRelation optionRelation : optionRelations) {
            int index = optionRelations.indexOf(optionRelation);
            ImageView imageView;
            if (index == 0) {
                imageView = imageLayout.findViewById(R.id.item_image_left);
            } else {
                imageView = imageLayout.findViewById(R.id.item_image_right);
            }

            String path = getContext().getFilesDir().getAbsolutePath() + "/" +
                    getQuestionRelation().question.getInstrumentRemoteId() + "/" +
                    optionRelation.option.getIdentifier() + ".png";
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = true;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > targetWidth) {
                double scale = targetWidth / width;
                width = (int) Math.round(width * scale);
                height = (int) Math.round(height * scale);
            }
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            imageView.setImageBitmap(bitmap);
        }
        questionComponent.addView(imageLayout);

        View sliderLayout = inflater.inflate(R.layout.slider, null);
        mSlider = sliderLayout.findViewById(R.id.discreteSlider);
        LabelFormatter formatter = value -> Math.abs((int) value) + "";
        mSlider.setLabelFormatter(formatter);
        mSlider.addOnChangeListener((slider, value, fromUser) -> {
            mProgress = value;
            saveResponse();
        });
        questionComponent.addView(sliderLayout);
    }

    @Override
    protected String serialize() {
        return String.valueOf(mProgress);
    }

    @Override
    protected void deserialize(String responseText) {
        if (responseText.equals("")) {
            mSlider.setValue((float) 0.0);
        } else {
            mSlider.setValue(Float.parseFloat(responseText));
        }
    }

    @Override
    protected void unSetResponse() {
        mSlider.setValue((float) 0.0);
    }

    @Override
    protected void showOtherText(int position) {
    }

}
