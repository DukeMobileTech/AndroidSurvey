package org.adaptlab.chpir.android.survey.questionfragments;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.QuestionFragment;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.location.LocationManager;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoLocationFragment extends QuestionFragment {
    private static final String TAG = "GeoLocationFragment";
    private TextView mLatitude;
    private TextView mLongitude;
    private TextView mAltitude;

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
        final LocationManager locationManager = getSurveyFragment().getLocationManager();
        locationManager.startLocationUpdates();
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_location, null);
        mLatitude = (TextView) view.findViewById(R.id.latitude);
        mLongitude = (TextView) view.findViewById(R.id.longitude);
        mAltitude = (TextView) view.findViewById(R.id.altitude);
        Button locationUpdate = (Button) view.findViewById(R.id.update_location_button);
        locationUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLatitude.setText(locationManager.getLatitude());
                mLongitude.setText(locationManager.getLongitude());
                mAltitude.setText(locationManager.getAltitude());
                setResponseText();
            }
        });
        questionComponent.addView(view);
    }

    @Override
    protected void deserialize(String responseText) {
        if (!TextUtils.isEmpty(responseText)) {
            try {
                JSONObject location = new JSONObject(responseText);
                mLatitude.setText(location.getString("latitude"));
                mLongitude.setText(location.getString("longitude"));
                mAltitude.setText(location.getString("altitude"));
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing object json", e);
            }
        }
    }

    @Override
    protected String serialize() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("latitude", mLatitude.getText().toString());
            jsonObject.put("longitude", mLongitude.getText().toString());
            jsonObject.put("altitude", mAltitude.getText().toString());
        } catch (JSONException e) {
            Log.e(TAG, "JSON exception", e);
        }
        return jsonObject.toString();
    }

}
