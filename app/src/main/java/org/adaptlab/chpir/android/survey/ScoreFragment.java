package org.adaptlab.chpir.android.survey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.models.Score;

import java.util.ArrayList;

public class ScoreFragment extends ListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.scores);
        setListAdapter(new ScoreAdapter((ArrayList<Score>) Score.getAll()));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Score score = (Score) getListAdapter().getItem(position);
        Intent intent = new Intent(getActivity(), ScoreUnitActivity.class);
        intent.putExtra(ScoreUnitFragment.EXTRA_SCORE_ID, score.getId());
        startActivity(intent);
    }

    private class ScoreAdapter extends ArrayAdapter<Score> {

        public ScoreAdapter(ArrayList<Score> scores) {
            super(getActivity(), 0, scores);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_score, null);
            }

            Score score = getItem(position);
            if (score != null) {
                TextView surveyIdentifier = (TextView) convertView.findViewById(R.id.survey_identifier);
                surveyIdentifier.setText(score.getSurveyIdentifier());

                TextView schemeTitle = (TextView) convertView.findViewById(R.id.scheme_name_text);
                schemeTitle.setText(String.valueOf(score.getScoreScheme().getTitle()));

                TextView totalRawScore = (TextView) convertView.findViewById(R.id.total_raw_score_value);
                totalRawScore.setText(String.valueOf(score.getRawScoreSum()));

                TextView totalWeightedScore = (TextView) convertView.findViewById(R.id.total_weighted_score_value);
                totalWeightedScore.setText(String.valueOf(score.getWeightedScoreSum()));
            }
            return convertView;
        }
    }
}