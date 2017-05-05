package org.adaptlab.chpir.android.survey.questionfragments;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import org.adaptlab.chpir.android.survey.GridFragment;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.roster.views.OHScrollView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultipleSelectGridFragment extends GridFragment {

	private Map<String, List<CheckBox>> mCheckBoxes;
	private Question mQuestion;
	private Map<String, List<Integer>> mResponseIndices;
	private boolean interceptScroll = true;
	private OHScrollView headerScrollView;
	private OHScrollView contentScrollView;

    @Override
    public void onScrollChanged(OHScrollView scrollView, int x, int y, int oldX, int oldY) {
        if (interceptScroll) {
            interceptScroll = false;
            if (scrollView == headerScrollView) {
                contentScrollView.onOverScrolled(x, y, true, true);
            } else if (scrollView == contentScrollView) {
                headerScrollView.onOverScrolled(x, y, true, true);
            }
            interceptScroll = true;
        }
    }
    
	@Override
	protected void deserialize(String responseText) {
		if (responseText.equals("")) {
			for (List<CheckBox> checkBoxList : mCheckBoxes.values()) {
				for (CheckBox box : checkBoxList) {
					if (box.isChecked()) {
						box.setChecked(false);
					}
				}
			}
		} else {
	        String[] listOfIndices = responseText.split(LIST_DELIMITER);
	        for (String index : listOfIndices) {
	            if (!index.equals("")) {
	                Integer indexInteger = Integer.parseInt(index);
	                mCheckBoxes.get(mQuestion.getQuestionIdentifier()).get(indexInteger).setChecked(true);
	            }
	        }
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_table_question, parent, false);
//		headerScrollView = (OHScrollView) getActivity().findViewById(R.id.table_options_header_view);
//		contentScrollView = (OHScrollView) getActivity().findViewById(R.id.table_body_options_view);
//		headerScrollView.setScrollViewListener(this);
//		contentScrollView.setScrollViewListener(this);
////		TableLayout headerTable = (TableLayout) v.findViewById(R.id.table_options_header);
//		TableRow headerRow = new TableRow(getActivity());
////		headerRow.setBackground(getResources().getDrawable(R.drawable.table_border));
//		TextView questionTextHeader = new TextView(getActivity());
//		questionTextHeader.setText("Question Text");
//		questionTextHeader.setWidth(getQuestionColumnWidth());
//		questionTextHeader.setTypeface(Typeface.DEFAULT_BOLD);
//		headerRow.addView(questionTextHeader);
//		for (GridLabel label : getGrid().labels()) {
//        	TextView textView = new TextView(getActivity());
//        	textView.setText(label.getLabelText());
//        	textView.setWidth(getOptionColumnWidth());
//        	textView.setTypeface(Typeface.DEFAULT_BOLD);
//        	headerRow.addView(textView);
//        }
//        headerTable.addView(headerRow, 0);
		
//		TableLayout gridTableLayout = (TableLayout) v.findViewById(R.id.table_body_options_choice);
//		List<Question> questionList = getQuestions();
//		mResponseIndices = new HashMap<String, List<Integer>>();
//		mCheckBoxes = new HashMap<String, List<CheckBox>>();
//		for (int k = 0; k < questionList.size(); k++) {
//			final Question q = questionList.get(k);
//			TableRow questionRow = new TableRow(getActivity());
////			questionRow.setBackground(getResources().getDrawable(R.drawable.table_border));
//			TextView questionText = new TextView(getActivity());
//			questionText.setText(q.getText());
//			questionText.setWidth(getQuestionColumnWidth());
//			questionRow.addView(questionText);
//
//			List<CheckBox> checkBoxes =  new ArrayList<CheckBox>();
//			for (GridLabel label : getGrid().labels()) {
//				final int id = getGrid().labels().indexOf(label);
//				CheckBox checkbox = new CheckBox(getActivity());
//				checkbox.setSaveEnabled(false);
//				checkbox.setId(id);
//				checkbox.setWidth(getOptionColumnWidth());
//				checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//					@Override
//					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//						setResponses(q, id);
//					}
//				});
//				questionRow.addView(checkbox);
//				checkBoxes.add(checkbox);
//			}
//			mQuestion = q;
//			mCheckBoxes.put(q.getQuestionIdentifier(), checkBoxes);
//			deserialize(getSurvey().getResponseByQuestion(q).getText());
//			gridTableLayout.addView(questionRow, k);
//		}

		return v;
	}
	
	private void saveResponses(Question question, List<Integer> responseIndices) {
		String serialized = "";
		for (int i = 0; i < responseIndices.size(); i++) {
			serialized += responseIndices.get(i);
			if (i < responseIndices.size() - 1)
				serialized += LIST_DELIMITER;
		}
		Response response = getSurvey().getResponseByQuestion(question);
		response.setResponse(serialized);
		if (isAdded() && !response.getText().equals("")) {
			response.setSpecialResponse("");
			ActivityCompat.invalidateOptionsMenu(getActivity());
		}
		response.save();
	}

	@Override
	protected String serialize() { return null; }

	private void setResponses(Question question, Integer responseIndex) {
		if (mResponseIndices.containsKey(question.getQuestionIdentifier())) {
			List<Integer> responses = mResponseIndices.get(question.getQuestionIdentifier());
			if (responses.contains(responseIndex)) {
				responses.remove(responseIndex);
			} else {
				responses.add(responseIndex);
			}
			mResponseIndices.put(question.getQuestionIdentifier(), responses);
		} else {
			List<Integer> list = new ArrayList<Integer>();
			list.add(responseIndex);
			mResponseIndices.put(question.getQuestionIdentifier(), list);
		}
		saveResponses(question, mResponseIndices.get(question.getQuestionIdentifier()));
	}
	
}
