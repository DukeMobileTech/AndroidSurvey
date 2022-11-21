package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.ChoiceDiagramAdapter;
import org.adaptlab.chpir.android.survey.relations.CollageRelation;
import org.adaptlab.chpir.android.survey.relations.OptionCollageRelation;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetOptionRelation;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.adaptlab.chpir.android.survey.utils.TranslationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.COMMA;
import static org.adaptlab.chpir.android.survey.utils.FormatUtils.styleTextWithHtmlWhitelist;

public class ChoiceTaskViewHolder extends QuestionViewHolder {
    private ArrayList<Integer> mResponseIndices;
    private ArrayList<MaterialCardView> mCardViews;
    private TextView mAfterChoiceText;

    ChoiceTaskViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    protected void beforeAddViewHook(ViewGroup questionComponent) {
        mAfterChoiceText = new TextView(getContext());
        mAfterChoiceText.setText(getOptionSetInstructions());
        mAfterChoiceText.setTextSize(22);
        mAfterChoiceText.setTextColor(ContextCompat.getColor(getContext(), R.color.blue));
        questionComponent.addView(mAfterChoiceText);
        setAfterChoiceTextVisibility();
    }

    private void setAfterChoiceTextVisibility() {
        if (mAfterChoiceText == null) return;

        if (mResponseIndices.size() == 0) {
            mAfterChoiceText.setVisibility(View.GONE);
        } else {
            mAfterChoiceText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        mResponseIndices = new ArrayList<>();
        mCardViews = new ArrayList<>();
        ArrayList<ArrayList<LinearLayout>> layouts = new ArrayList<>();
        ArrayList<ArrayList<Integer>> heights = new ArrayList<>();
        List<OptionRelation> optionRelations = getOptionRelations();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout imageLayout = (LinearLayout) inflater.inflate(R.layout.choice_task, null);
        for (final OptionRelation optionRelation : optionRelations) {
            ArrayList<Integer> collageHeights = new ArrayList<>();
            ArrayList<LinearLayout> collageLayouts = new ArrayList<>();
            OptionSetOptionRelation relation = getOptionSetOptionRelation(optionRelation);
            int index = optionRelations.indexOf(optionRelation);
            LinearLayout linearLayout;
            TextView textView;
            MaterialCardView cardView;
            if (index == 0) {
                cardView = imageLayout.findViewById(R.id.leftCardView);
                linearLayout = imageLayout.findViewById(R.id.leftLayout);
                textView = imageLayout.findViewById(R.id.leftTitle);
            } else if (index == 1) {
                cardView = imageLayout.findViewById(R.id.middleCardView);
                linearLayout = imageLayout.findViewById(R.id.middleLayout);
                textView = imageLayout.findViewById(R.id.middleTitle);
            } else {
                cardView = imageLayout.findViewById(R.id.rightCardView);
                linearLayout = imageLayout.findViewById(R.id.rightLayout);
                textView = imageLayout.findViewById(R.id.rightTitle);
            }
            cardView.setId(index);
            cardView.setOnClickListener(v -> {
                int clickedIndex = v.getId();
                cardView.setSelected(!cardView.isSelected());
                cardView.setChecked(!cardView.isChecked());
                setResponseIndex(clickedIndex, cardView.isChecked());
            });
            mCardViews.add(cardView);
            String text = TranslationUtil.getText(optionRelation.option, optionRelation.translations, getSurveyViewModel());
            textView.setText(styleTextWithHtmlWhitelist(text));
            for (OptionCollageRelation optionCollageRelation : relation.optionCollages) {
                for (CollageRelation collageRelation : optionCollageRelation.collages) {
                    GridView gridView = (GridView) inflater.inflate(R.layout.list_item_option_grid_view, null);
                    gridView.setNumColumns(collageRelation.diagrams.size());
                    ChoiceDiagramAdapter adapter = new ChoiceDiagramAdapter(getContext(), getQuestionRelation(),
                            collageRelation.diagrams, getSurveyViewModel(), optionRelations.size());
                    gridView.setAdapter(adapter);
                    LinearLayout gridViewLayout = new LinearLayout(getContext());
                    gridViewLayout.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.choice_option_border));
                    gridViewLayout.setPadding(5, 0, 5, 0);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 5, 0, 5);
                    gridViewLayout.addView(gridView, layoutParams);
                    linearLayout.addView(gridViewLayout);
                    collageHeights.add(adapter.getMaxHeight());
                    collageLayouts.add(gridViewLayout);
                }
            }
            heights.add(collageHeights);
            layouts.add(collageLayouts);
        }
        // Set layout heights to the maximum height of the layouts in the row
        for (int j = 0; j < layouts.size(); j++) {
            ArrayList<Integer> rowHeights = new ArrayList<>();
            for (int k = 0; k < heights.size(); k++) {
                rowHeights.add(heights.get(k).get(j));
            }
            int max = Collections.max(rowHeights);
            if (BuildConfig.DEBUG) Log.i(TAG, "Row => " + j + " MAX => " + max);
            for (int k = 0; k < heights.size(); k++) {
                ViewGroup.LayoutParams layoutParams = layouts.get(k).get(j).getLayoutParams();
                layoutParams.height = max;
            }
        }
        questionComponent.addView(imageLayout);
        beforeAddViewHook(questionComponent);
    }

    private void setResponseIndex(int index, boolean isChecked) {
        if (mResponseIndices.contains(index)) {
            mResponseIndices.remove((Integer) index);
        } else {
            mResponseIndices.add(index);
        }
        MaterialCardView cardView = mCardViews.get(index);
        if (!isChecked) {
            MaterialCardView materialCardView = new MaterialCardView(getContext());
            ColorStateList colorStateList = materialCardView.getCardForegroundColor();
            cardView.setCardForegroundColor(colorStateList);
            if (mResponseIndices.size() == 1) {
                setFirstSelection();
            }
        } else {
            if (mResponseIndices.size() == 1) {
                setFirstSelection();
            } else if (mResponseIndices.size() == 2) {
                cardView.setCardForegroundColor(getContext().getColorStateList(R.color.second));
            } else {
                mResponseIndices.clear();
                unCheckCards();
            }
        }
        setAfterChoiceTextVisibility();
        saveResponse();
    }

    private void setFirstSelection() {
        MaterialCardView cardView = mCardViews.get(mResponseIndices.get(0));
        cardView.setCardForegroundColor(getContext().getColorStateList(R.color.first));
    }

    @Override
    protected String serialize() {
        return FormatUtils.arrayListToString(mResponseIndices);
    }

    private void unCheckCards() {
        MaterialCardView materialCardView = new MaterialCardView(getContext());
        ColorStateList colorStateList = materialCardView.getCardForegroundColor();
        for (MaterialCardView cardView : mCardViews) {
            if (cardView.isChecked()) {
                cardView.setChecked(false);
                cardView.setSelected(false);
                cardView.setCardForegroundColor(colorStateList);
            }
        }
    }

    @Override
    protected void deserialize(String responseText) {
        mResponseIndices = new ArrayList<>();
        if (responseText.trim().isEmpty()) {
            unCheckCards();
        } else {
            String[] listOfIndices = responseText.split(COMMA);
            for (String index : listOfIndices) {
                if (!index.isEmpty()) {
                    int indexInteger = Integer.parseInt(index);
                    MaterialCardView cardView = mCardViews.get(indexInteger);
                    cardView.setChecked(true);
                    cardView.setSelected(true);
                    mResponseIndices.add(indexInteger);
                }
            }
            if (!mResponseIndices.isEmpty()) {
                setFirstSelection();
            }
            if (mResponseIndices.size() == 2) {
                MaterialCardView cardView = mCardViews.get(mResponseIndices.get(1));
                cardView.setCardForegroundColor(getContext().getColorStateList(R.color.second));
            }
        }
        setAfterChoiceTextVisibility();
    }

    @Override
    protected void unSetResponse() {
        mResponseIndices = new ArrayList<>();
        unCheckCards();
        setAfterChoiceTextVisibility();
    }

    @Override
    protected void showOtherText(int position) {
    }

}
