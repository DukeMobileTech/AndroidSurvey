package org.adaptlab.chpir.android.survey.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import static org.adaptlab.chpir.android.survey.utils.ConstantUtils.BLANK;

public class GeoLocationViewHolder extends QuestionViewHolder {
    private TextView mLatitude;
    private TextView mLongitude;
    private TextView mAltitude;

    GeoLocationViewHolder(View itemView, Context context, OnResponseSelectedListener listener) {
        super(itemView, context, listener);
    }

    @Override
    protected void unSetResponse() {
        mLatitude.setText(BLANK);
        mLongitude.setText(BLANK);
        mAltitude.setText(BLANK);
    }

    @Override
    protected void showOtherText(int position) {
    }

    @Override
    protected void createQuestionComponent(ViewGroup questionComponent) {
//        final LocationManager locationManager = getSurveyFragment().getLocationManager();
//        locationManager.startLocationUpdates();
//        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context
//                .LAYOUT_INFLATER_SERVICE);
//        View view = inflater.inflate(R.layout.fragment_location, null);
//        mLatitude = view.findViewById(R.id.latitude);
//        mLongitude = view.findViewById(R.id.longitude);
//        mAltitude = view.findViewById(R.id.altitude);
//        Button locationUpdate = view.findViewById(R.id.update_location_button);
//        locationUpdate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mLatitude.setText(locationManager.getLatitude());
//                mLongitude.setText(locationManager.getLongitude());
//                mAltitude.setText(locationManager.getAltitude());
//                setResponse(null);
//            }
//        });
//        questionComponent.addView(view);
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