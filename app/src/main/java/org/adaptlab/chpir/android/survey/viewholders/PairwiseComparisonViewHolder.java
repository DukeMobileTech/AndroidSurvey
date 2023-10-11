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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.slider.Slider;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.DiagramRelation;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetOptionRelation;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.io.File;
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

        ConstraintLayout imageLayout = (ConstraintLayout) inflater.inflate(R.layout.pair_wise, null);
        for (final OptionRelation optionRelation : optionRelations) {
            OptionSetOptionRelation relation = getOptionSetOptionRelation(optionRelation);
            int index = optionRelations.indexOf(optionRelation);
            LinearLayout linearLayout;
            if (index == 0) {
                linearLayout = imageLayout.findViewById(R.id.leftLayout);
            } else {
                linearLayout = imageLayout.findViewById(R.id.rightLayout);
            }
            if (!relation.optionCollages.isEmpty()) {
                List<DiagramRelation> diagramRelations = relation.optionCollages.get(0).collages.get(0).diagrams;
                for (int k = 0; k < diagramRelations.size(); k++) {
                    DiagramRelation diagramRelation = diagramRelations.get(k);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inScaled = true;
                    String path = getPath(diagramRelation);
                    if (!path.isEmpty()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();
                        double targetHeight = displayMetrics.heightPixels * 0.35;
                        double scale = targetHeight / height;
                        width = (int) Math.round(width * scale);
                        height = (int) Math.round(height * scale);
                        ImageView imageView = new ImageView(getContext());
                        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                        imageView.setImageBitmap(bitmap);
                        linearLayout.addView(imageView);
                    }
                }
            }
        }
        questionComponent.addView(imageLayout);

        View sliderLayout = inflater.inflate(R.layout.slider, null);
        TextView textView = sliderLayout.findViewById(R.id.beforeSliderTextView);
        textView.setText(getOptionSetInstructions());

        mSlider = sliderLayout.findViewById(R.id.discreteSlider);
        mSlider.setLabelFormatter(value -> {
            if (value < 0) {
                return "A" + Math.abs((int) value);
            } else if (value > 0) {
                return "B" + Math.abs((int) value);
            } else {
                return "0";
            }
        });
        mSlider.addOnChangeListener((slider, value, fromUser) -> {
            mProgress = value;
            saveResponse();
        });
        questionComponent.addView(sliderLayout);
    }

    private String getPath(DiagramRelation diagramRelation) {
        if (diagramRelation.options.isEmpty()) return "";
        String path;
        String optionIdentifier = diagramRelation.options.get(0).option.getIdentifier();
        String translatedOptionIdentifier = "";
        SurveyViewModel mSurveyViewModel = getSurveyViewModel();
        String absolutePath = getContext().getFilesDir().getAbsolutePath();
        Long instrumentId = getQuestionRelation().question.getInstrumentRemoteId();

        if (!mSurveyViewModel.getInstrumentLanguage().equals(mSurveyViewModel.getDeviceLanguage())) {
            translatedOptionIdentifier = diagramRelation.options.get(0).option.getIdentifier() + "_" +
                    mSurveyViewModel.getDeviceLanguage().toUpperCase();
        }
        if (translatedOptionIdentifier.isEmpty()) {
            path = absolutePath + "/" + instrumentId + "/" + optionIdentifier + ".png";
        } else {
            path = absolutePath + "/" + instrumentId + "/" + translatedOptionIdentifier + ".png";
            File file = new File(path);
            if (!file.exists()) {
                path = absolutePath + "/" + instrumentId + "/" + optionIdentifier + ".png";
            }
        }
        return path;
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
