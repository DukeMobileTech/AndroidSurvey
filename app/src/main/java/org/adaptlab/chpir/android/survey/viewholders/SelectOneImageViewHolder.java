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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.card.MaterialCardView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.relations.OptionRelation;
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
        int deviceWidth = displayMetrics.widthPixels;
        double targetWidth = deviceWidth - (0.25 * deviceWidth);

        OptionSetRelation optionSetRelation = getQuestionRelation().optionSets.get(0);
        List<OptionRelation> optionRelations = getOptionRelations();
        if (optionSetRelation.optionSet.isAlignImageVertical()) {
            for (final OptionRelation optionRelation : optionRelations) {
                View view = inflater.inflate(R.layout.list_item_image, null);
                final MaterialCardView cardView = view.findViewById(R.id.material_card_view);
                cardView.setId(optionRelations.indexOf(optionRelation));
                ImageView imageView = cardView.findViewById(R.id.item_image);
                String path = getContext().getFilesDir().getAbsolutePath() + "/" +
                        getQuestionRelation().question.getInstrumentRemoteId() + "/" +
                        optionRelation.option.getIdentifier() + ".png";
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = true;
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if (width < targetWidth) {
                    double scale = targetWidth / width;
                    width = (int) Math.round(width * scale);
                    height = (int) Math.round(height * scale);
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                imageView.setImageBitmap(bitmap);
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
