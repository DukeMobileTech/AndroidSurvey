package org.adaptlab.chpir.android.survey.questionfragments;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.adaptlab.chpir.android.survey.CameraFragment;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.ResponsePhoto;

public class RearPictureQuestionFragment extends PictureQuestionFragment {
    private static final String TAG = "RearPictureViewHolder";
    private ResponsePhoto mPhoto;

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        loadOrCreateResponsePhoto();
        if (isCameraAvailable()) {
            mPhoto = getResponsePhoto();
            Button mCameraButton = new Button(getActivity());
            mCameraButton.setText(R.string.enable_camera);
            LinearLayout.LayoutParams buttonLayout = new LinearLayout.LayoutParams(500, 120);
            buttonLayout.gravity = Gravity.CENTER;
            mCameraButton.setLayoutParams(buttonLayout);
            mCameraButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    mCameraFragment = CameraFragment.newCameraFragmentInstance(mPhoto, REAR_CAMERA);
                    transaction.replace(R.id.fragmentContainer, mCameraFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
            mPhotoView = new ImageView(getActivity());
            boolean picturePresent = showPhoto();
            questionComponent.addView(mCameraButton);
            questionComponent.addView(mPhotoView);
            if (picturePresent) {
                questionComponent.addView(setDeleteButton(mPhoto, mPhotoView));
            }
        } else {
            Log.i(TAG, "Camera Not Available");
        }
    }

}
