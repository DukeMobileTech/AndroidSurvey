package org.adaptlab.chpir.android.survey.QuestionFragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.GridFragment;
import org.adaptlab.chpir.android.survey.Models.GridLabel;
import org.adaptlab.chpir.android.survey.Models.Question;
import org.adaptlab.chpir.android.survey.Models.Response;
import org.adaptlab.chpir.android.survey.R;

import java.util.ArrayList;
import java.util.List;

public class SingleSelectGridFragment extends GridFragment {
    private static final String TAG = "SingleSelectGridFragment";
    private int mIndex;
	private List<RadioGroup> mRadioGroups;
	
	@Override
	protected void deserialize(String responseText) {
		if (responseText.equals("")) {
			for (RadioGroup group : mRadioGroups) {
				int checked = group.getCheckedRadioButtonId();
	        	if (checked > -1) {
	        		((RadioButton) group.getChildAt(checked)).setChecked(false);
	        	}
			}
		} else {
			((RadioButton) mRadioGroups.get(mIndex).getChildAt(Integer.parseInt(responseText))).setChecked(true);
		}
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_table_question, parent, false);
		TableLayout headerTableLayout = (TableLayout) v.findViewById(R.id.header_table_view);
		TableRow headerRow= new TableRow(getActivity());
        headerRow.setBackground(getResources().getDrawable(R.drawable.table_border));
		TextView questionTextHeader = new TextView(getActivity());
        questionTextHeader.setText("Question Text");
        questionTextHeader.setWidth(getQuestionColumnWidth());
        questionTextHeader.setTypeface(Typeface.DEFAULT_BOLD);
        headerRow.addView(questionTextHeader);
        
        for (GridLabel label : getGrid().labels()) {
        	TextView textView = new TextView(getActivity());
            textView.setText(label.getLabelText());
        	textView.setWidth(getOptionColumnWidth());
        	textView.setTypeface(Typeface.DEFAULT_BOLD);
        	headerRow.addView(textView);
        }
        headerTableLayout.addView(headerRow, 0);
		
		TableLayout bodyTableLayout = (TableLayout) v.findViewById(R.id.body_table_view);
        mRadioGroups = new ArrayList<RadioGroup>();
        List<Question> questionList = getQuestions();
        for (int k = 0; k < questionList.size(); k++) {
	        final Question q = questionList.get(k);
        	TableRow questionRow= new TableRow(getActivity());
            questionRow.setBackground(getResources().getDrawable(R.drawable.table_border));
            TextView questionText = new TextView(getActivity());
            questionText.setText(q.getText());
            questionText.setWidth(getQuestionColumnWidth());
            questionRow.addView(questionText);
            
            RadioGroup radioButtons = new RadioGroup(getActivity());
            radioButtons.setOrientation(RadioGroup.HORIZONTAL);           
            for (GridLabel label : getGrid().labels()) {
            	int id = getGrid().labels().indexOf(label);
            	RadioButton button = new RadioButton(getActivity());
                button.setSaveEnabled(false);
                button.setId(id);
            	button.setWidth(getOptionColumnWidth());
            	radioButtons.addView(button, id);
            }
            questionRow.addView(radioButtons);           
            bodyTableLayout.addView(questionRow, k);
            radioButtons.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					setResponseIndex(q, checkedId);
				}        	
            });
            mRadioGroups.add(radioButtons);
            mIndex = k;
            deserialize(getSurvey().getResponseByQuestion(q).getText());
        }
        return v;
    }  

	@Override
	protected String serialize() { return null; }

	private void setResponseIndex(Question q, int checkedId) {
		Response response = getSurvey().getResponseByQuestion(q);
		response.setResponse(String.valueOf(checkedId));
		if (isAdded() && !response.getText().equals("")) {
            response.setSpecialResponse("");
            ActivityCompat.invalidateOptionsMenu(getActivity());
        }
		response.save();
	}

}
