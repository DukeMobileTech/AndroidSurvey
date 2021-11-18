package org.adaptlab.chpir.android.survey;

import android.os.Bundle;
import androidx.fragment.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.activeandroid.Model;

import org.adaptlab.chpir.android.survey.models.RawScore;
import org.adaptlab.chpir.android.survey.models.Score;
import org.adaptlab.chpir.android.survey.models.ScoreUnit;

import java.util.ArrayList;

public class ScoreUnitFragment extends ListFragment {
    public final static String EXTRA_SCORE_ID =
            "org.adaptlab.chpir.android.survey.score_id";

    private Score mScore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Long scoreId = getActivity().getIntent().getLongExtra(EXTRA_SCORE_ID, -1);
        if (scoreId == -1) return;
        mScore = Model.load(Score.class, scoreId);
        setListAdapter(new ScoreUnitAdapter((ArrayList<ScoreUnit>) mScore.getScoreScheme()
                .scoreUnits()));
        getActivity().setTitle(mScore.getSurveyIdentifier());
    }

    private class ScoreUnitAdapter extends ArrayAdapter<ScoreUnit> {

        public ScoreUnitAdapter(ArrayList<ScoreUnit> scores) {
            super(getActivity(), 0, scores);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout
                        .list_item_score_unit, null);
            }

            ScoreUnit scoreUnit = getItem(position);
            if (scoreUnit != null) {
                TextView scoreType = (TextView) convertView.findViewById(R.id.score_type_value);
                scoreType.setText(scoreUnit.getScoreType().toString());

                TextView questionIds = (TextView) convertView.findViewById(R.id
                        .question_identifiers_value);
                questionIds.setText(scoreUnit.questionIdentifiers());

                TextView minScore = (TextView) convertView.findViewById(R.id.minimum_score_value);
                minScore.setText(String.valueOf(scoreUnit.getMin()));

                TextView maxScore = (TextView) convertView.findViewById(R.id.maximum_score_value);
                maxScore.setText(String.valueOf(scoreUnit.getMax()));

                TextView scoreWeight = (TextView) convertView.findViewById(R.id.score_weight_value);
                scoreWeight.setText(String.valueOf(scoreUnit.getWeight()));
            }

            RawScore rawScore = mScore.findRawScoreByScoreUnit(scoreUnit);
            if (rawScore != null) {
                TextView unitRawScore = (TextView) convertView.findViewById(R.id.unit_score_value);
                unitRawScore.setText(String.valueOf(rawScore.getValue()));

                TextView weightedScore = (TextView) convertView.findViewById(R.id.weighted_score_value);
                weightedScore.setText(String.valueOf(rawScore.getWeightedScore()));
            }
            return convertView;
        }
    }
}
