package org.adaptlab.chpir.android.survey.viewholders;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.card.MaterialCardView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.OptionDiagramAdapter;
import org.adaptlab.chpir.android.survey.relations.DiagramRelation;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetOptionRelation;
import org.adaptlab.chpir.android.survey.relations.OptionSetRelation;

import java.util.ArrayList;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.FormatUtils.removeNonNumericCharacters;

public class SelectOneImageViewHolder extends QuestionViewHolder {
    private int mResponseIndex = -1;
    private ArrayList<MaterialCardView> mCardViews;

    SelectOneImageViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        questionComponent.removeAllViews();
        mCardViews = new ArrayList<>();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int rightMargin = (int) (displayMetrics.widthPixels * 0.1);

        OptionSetRelation optionSetRelation = getQuestionRelation().optionSets.get(0);
        List<OptionRelation> optionRelations = getOptionRelations();
        if (optionSetRelation.optionSet.isAlignImageVertical()) {
            for (final OptionRelation optionRelation : optionRelations) {
                OptionSetOptionRelation relation = getOptionSetOptionRelation(optionRelation);
                GridView gridView = (GridView) inflater.inflate(R.layout.list_item_option_grid_view, null);
                List<DiagramRelation> diagrams = relation.optionCollages.get(0).collages.get(0).diagrams;
                gridView.setNumColumns(diagrams.size());
                gridView.setAdapter(new OptionDiagramAdapter(getContext(), getQuestionRelation(),
                        diagrams, getSurveyViewModel()));

                View view = inflater.inflate(R.layout.list_item_collage, null);
                final MaterialCardView cardView = view.findViewById(R.id.materialCardView);
                cardView.setId(optionRelations.indexOf(optionRelation));
                LinearLayout linearLayout = cardView.findViewById(R.id.gridViewLayout);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, rightMargin, 0);
                linearLayout.addView(gridView, layoutParams);

                cardView.setOnClickListener(v -> {
                    int index = v.getId();
                    cardView.setSelected(!cardView.isSelected());
                    cardView.setChecked(!cardView.isChecked());
                    setResponseIndex(index, cardView.isChecked());
                });
                mCardViews.add(cardView);
                questionComponent.addView(view);
            }
        } else {
            LinearLayout view = (LinearLayout) inflater.inflate(R.layout.card_images, null);
            view.setWeightSum(optionRelations.size());
            for (int k = 0; k < optionRelations.size(); k++) {
                OptionRelation relation = optionRelations.get(k);
                View layout = inflater.inflate(R.layout.list_item_card, null);
                MaterialCardView cardView = layout.findViewById(R.id.material_card_view);
                cardView.setId(k);
                ImageView imageView = cardView.findViewById(R.id.item_image);
                String path = getContext().getFilesDir().getAbsolutePath() + "/" +
                        getQuestionRelation().question.getInstrumentRemoteId() + "/" +
                        relation.option.getIdentifier() + ".png";
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = true;
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                imageView.setImageBitmap(bitmap);

                cardView.setOnClickListener(v -> {
                    int index = v.getId();
                    cardView.setSelected(!cardView.isSelected());
                    cardView.setChecked(!cardView.isChecked());
                    setResponseIndex(index, cardView.isChecked());
                });
                mCardViews.add(cardView);
                view.addView(layout);

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
                if (params != null) {
                    params.weight = 1;
                    layout.setLayoutParams(params);
                }
            }
            questionComponent.addView(view);
        }
    }


    private void setResponseIndex(int index, boolean status) {
        MaterialCardView cardView;
        if (mResponseIndex != -1) {
            cardView = mCardViews.get(mResponseIndex);
            cardView.setChecked(false);
            cardView.setSelected(false);
        }
        mResponseIndex = index;
        cardView = mCardViews.get(mResponseIndex);
        cardView.setChecked(status);
        cardView.setSelected(status);

        saveResponse();
    }

    @Override
    protected String serialize() {
        if (mResponseIndex == -1) {
            return "";
        }
        return String.valueOf(mResponseIndex);
    }

    @Override
    protected void deserialize(String responseText) {
        if (TextUtils.isEmpty(responseText.trim())) {
            mResponseIndex = -1;
        } else {
            int index = Integer.parseInt(removeNonNumericCharacters(responseText));
            MaterialCardView cardView = mCardViews.get(index);
            cardView.setChecked(true);
            cardView.setSelected(true);
            mResponseIndex = index;
        }
    }

    @Override
    protected void unSetResponse() {
    }

    @Override
    protected void showOtherText(int position) {
    }

}
