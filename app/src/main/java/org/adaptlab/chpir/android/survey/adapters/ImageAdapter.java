package org.adaptlab.chpir.android.survey.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import org.adaptlab.chpir.android.survey.relations.CollageRelation;
import org.adaptlab.chpir.android.survey.relations.DiagramRelation;
import org.adaptlab.chpir.android.survey.relations.QuestionRelation;

import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private final String TAG = "ImageAdapter";
    private final Context mContext;
    private final List<DiagramRelation> mDiagramRelations;
    private final QuestionRelation mQuestionRelation;

    public ImageAdapter(Context context, QuestionRelation questionRelation, CollageRelation collageRelation) {
        this.mContext = context;
        this.mQuestionRelation = questionRelation;
        this.mDiagramRelations = collageRelation.diagrams;
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
        ImageView imageView;

        if (convertView == null) {
            Log.i(TAG, "convertView is null");
            imageView = new ImageView(mContext);

            DiagramRelation diagramRelation = mDiagramRelations.get(position);
            String path = mContext.getFilesDir().getAbsolutePath() + "/" +
                    mQuestionRelation.question.getInstrumentRemoteId() + "/" +
                    diagramRelation.options.get(0).option.getIdentifier() + ".png";
            Log.i(TAG, "PATH: " + path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = true;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            double imageViewWidth = (width - (width * 0.1)) / mDiagramRelations.size();
            double scale = imageViewWidth / bitmap.getWidth();
            width = (int) Math.round(bitmap.getWidth() * scale);
            int height = (int) Math.round(bitmap.getHeight() * scale);

            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            imageView.setImageBitmap(bitmap);

//            gridView = parent.findViewById(R.id.gridView);

//            // get layout from mobile.xml
//            gridView = inflater.inflate(R.layout.mobile, null);
//
//            // set value into textview
//            TextView textView = (TextView) gridView
//                    .findViewById(R.id.grid_item_label);
//            textView.setText(mobileValues[position]);
//
//            // set image based on selected text
//            ImageView imageView = (ImageView) gridView
//                    .findViewById(R.id.grid_item_image);
//
//            String mobile = mobileValues[position];
//
//            if (mobile.equals("Windows")) {
//                imageView.setImageResource(R.drawable.windows_logo);
//            } else if (mobile.equals("iOS")) {
//                imageView.setImageResource(R.drawable.ios_logo);
//            } else if (mobile.equals("Blackberry")) {
//                imageView.setImageResource(R.drawable.blackberry_logo);
//            } else {
//                imageView.setImageResource(R.drawable.android_logo);
//            }

        } else {
            imageView = (ImageView) convertView;
            Log.i("ImageAdapter", "convertView is NOT null");
        }

        return imageView;
    }
}
