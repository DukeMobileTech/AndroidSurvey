package org.adaptlab.chpir.android.survey.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.adaptlab.chpir.android.survey.relations.DiagramRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;
import org.adaptlab.chpir.android.survey.viewmodels.SurveyViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OptionDiagramAdapter extends BaseAdapter {
    private final String TAG = "OptionDiagramAdapter";
    private final Context mContext;
    private final List<DiagramRelation> mDiagramRelations;
    private final QuestionRelation mQuestionRelation;
    private final SurveyViewModel mSurveyViewModel;
    private final OnItemClickListener onClickListener;
    private List<Bitmap> mBitmaps;
    private List<Integer> mWidths;
    private List<Integer> mHeights;
    private int mHeight;

    public OptionDiagramAdapter(Context c, QuestionRelation qr, List<DiagramRelation> dr,
                                SurveyViewModel svm, OnItemClickListener listener) {
        this.onClickListener = listener;
        this.mContext = c;
        this.mQuestionRelation = qr;
        this.mDiagramRelations = dr;
        this.mSurveyViewModel = svm;
        this.mDiagramRelations.sort((dr1, dr2) -> dr1.diagram.getPosition().compareTo(dr2.diagram.getPosition()));
        setBitmaps();
    }

    private String getPath(DiagramRelation diagramRelation) {
        String path;
        String optionIdentifier = diagramRelation.options.get(0).option.getIdentifier();
        String translatedOptionIdentifier = "";
        if (!mSurveyViewModel.getInstrumentLanguage().equals(mSurveyViewModel.getDeviceLanguage())) {
            translatedOptionIdentifier = diagramRelation.options.get(0).option.getIdentifier() + "_" +
                    mSurveyViewModel.getDeviceLanguage().toUpperCase();
            if (mSurveyViewModel.getDeviceLanguage().contains("-")) {
                translatedOptionIdentifier = diagramRelation.options.get(0).option.getIdentifier() + "_" +
                        mSurveyViewModel.getDeviceLanguage().split("-")[0].toUpperCase();
            }
        }
        if (translatedOptionIdentifier.isEmpty()) {
            path = mContext.getFilesDir().getAbsolutePath() + "/" +
                    mQuestionRelation.question.getInstrumentRemoteId() + "/" +
                    optionIdentifier + ".png";
        } else {
            path = mContext.getFilesDir().getAbsolutePath() + "/" +
                    mQuestionRelation.question.getInstrumentRemoteId() + "/" +
                    translatedOptionIdentifier + ".png";
            File file = new File(path);
            if (!file.exists()) {
                path = mContext.getFilesDir().getAbsolutePath() + "/" +
                        mQuestionRelation.question.getInstrumentRemoteId() + "/" +
                        optionIdentifier + ".png";
            }
        }
//        if (BuildConfig.DEBUG) Log.i(TAG, "PATH: " + path);
        return path;
    }

    private void setBitmaps() {
        mWidths = new ArrayList<>();
        mHeights = new ArrayList<>();
        mBitmaps = new ArrayList<>();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int margin = (int) (displayMetrics.widthPixels * 0.1);
        int availableWidth = displayMetrics.widthPixels - 32 - margin;
        for (int k = 0; k < mDiagramRelations.size(); k++) {
            DiagramRelation diagramRelation = mDiagramRelations.get(k);
            Bitmap bitmap = BitmapFactory.decodeFile(getPath(diagramRelation), options);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
//            // Scale based on height
//            if (targetHeight > height) {
//                double scale = targetHeight / height;
//                width = (int) Math.round(width * scale);
//                height = (int) Math.round(height * scale);
//            }
            mBitmaps.add(bitmap);
            mHeights.add(height);
            mWidths.add(width);
        }

        double total = 0.0;
        for (int imgWidth : mWidths) {
            total += imgWidth;
        }
        double remainder = availableWidth - total;
        for (int i = 0; i < mWidths.size(); i++) {
            int width = mWidths.get(i);
            double portion = (width / total) * remainder;
            double newWidth = width + portion;
            double scale = newWidth / width;
            int width1 = (int) Math.round(width * scale);
            int height1 = (int) Math.round(mHeights.get(i) * scale);
            mWidths.set(i, width1);
            mHeights.set(i, height1);
        }

        if (mHeights.isEmpty()) {
            mHeight = 0;
        } else {
            mHeight = Collections.max(mHeights);
        }
    }

    @Override
    public int getCount() {
        return mDiagramRelations.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout linearLayout;
        if (convertView == null) {
            Bitmap bitmap = Bitmap.createScaledBitmap(mBitmaps.get(position), mWidths.get(position),
                    mHeights.get(position), true);
            ImageView imageView = new ImageView(mContext);
            imageView.setImageBitmap(bitmap);
            linearLayout = new LinearLayout(mContext);
            linearLayout.setMinimumHeight(mHeight);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            params.gravity = Gravity.CENTER;
//            linearLayout.setLayoutParams(params);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.addView(imageView);
        } else {
            linearLayout = (LinearLayout) convertView;
        }

        linearLayout.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onItemClicked();
            }
        });

        return linearLayout;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
