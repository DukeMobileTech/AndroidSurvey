package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class RearPictureViewHolder extends PictureViewHolder {
    RearPictureViewHolder(View itemView, Context context) {
        super(itemView, context);
    }
//    private static final String TAG = "RearPictureViewHolder";
//    private ResponsePhoto mPhoto;

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
//        loadOrCreateResponsePhoto();
//        if (isCameraAvailable()) {
//            mPhoto = getResponsePhoto();
//            Button mCameraButton = new Button(getActivity());
//            mCameraButton.setText(R.string.enable_camera);
//            LinearLayout.LayoutParams buttonLayout = new LinearLayout.LayoutParams(500, 120);
//            buttonLayout.gravity = Gravity.CENTER;
//            mCameraButton.setLayoutParams(buttonLayout);
//            mCameraButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    FragmentManager fm = getActivity().getSupportFragmentManager();
//                    FragmentTransaction transaction = fm.beginTransaction();
//                    mCameraFragment = CameraFragment.newCameraFragmentInstance(mPhoto, REAR_CAMERA);
//                    transaction.replace(R.id.fragmentContainer, mCameraFragment);
//                    transaction.addToBackStack(null);
//                    transaction.commit();
//                }
//            });
//            mPhotoView = new ImageView(getActivity());
//            boolean picturePresent = showPhoto();
//            questionComponent.addView(mCameraButton);
//            questionComponent.addView(mPhotoView);
//            if (picturePresent) {
//                questionComponent.addView(setDeleteButton(mPhoto, mPhotoView));
//            }
//        } else {
//            Log.i(TAG, "Camera Not Available");
//        }
    }

}
