package org.adaptlab.chpir.android.survey.QuestionFragments;

import org.adaptlab.chpir.android.survey.CameraFragment;
import org.adaptlab.chpir.android.survey.R;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class RearPictureQuestionFragment extends PictureQuestionFragment {
	private static final String TAG = "RearPictureQuestionFragment";
	private Button mCameraButton;

	@Override
	protected void createQuestionComponent(ViewGroup questionComponent) {
		if (isCameraAvailable()) {
			mCameraButton = new Button(getActivity());
			mCameraButton.setText(R.string.enable_camera);
			LinearLayout.LayoutParams buttonLayout = new LinearLayout.LayoutParams(500, 120);
			buttonLayout.gravity = Gravity.CENTER;
			mCameraButton.setLayoutParams(buttonLayout);
			mCameraButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					FragmentManager fm = getActivity().getSupportFragmentManager();     
					FragmentTransaction transaction = fm.beginTransaction();
					mCameraFragment = CameraFragment.newCameraFragmentInstance(getResponsePhoto(), REAR_CAMERA);
					transaction.replace(R.id.fragmentContainer, mCameraFragment);
					transaction.addToBackStack(null); 
					transaction.commit();
				}
			});
			mPhotoView = new ImageView(getActivity());
			showPhoto();
			questionComponent.addView(mCameraButton);
			questionComponent.addView(mPhotoView);
		} else {
			Log.i(TAG, "Camera Not Available");
		}
	}

}
