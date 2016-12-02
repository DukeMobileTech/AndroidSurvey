package org.adaptlab.chpir.android.survey.Roster;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.BuildConfig;
import org.adaptlab.chpir.android.survey.Roster.Listeners.ScrollViewListener;
import org.adaptlab.chpir.android.survey.Models.Instrument;
import org.adaptlab.chpir.android.survey.Models.Question;
import org.adaptlab.chpir.android.survey.Models.Response;
import org.adaptlab.chpir.android.survey.Models.Roster;
import org.adaptlab.chpir.android.survey.Models.Survey;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.Roster.Views.OHScrollView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class RosterActivity extends Activity implements ScrollViewListener {
    public final static String EXTRA_ROSTER_ID = "org.adaptlab.chpir.android.survey.roster_id";
    public final static String EXTRA_SURVEY_ID = "org.adaptlab.chpir.android.survey.survey_id";
    public final static String EXTRA_QUESTION_HEADER =
            "org.adaptlab.chpir.android.survey.question_header";
    public final static String EXTRA_INSTRUMENT_ID =
            "org.adaptlab.chpir.android.survey.instrument_id";
    public final static String EXTRA_PARTICIPANT_METADATA =
            "org.adaptlab.chpir.android.survey.metadata";

    final private String TAG = "RosterActivity";
    final private int HEADER_TEXT_SIZE = 15;
    final private int NON_HEADER_TEXT_SIZE = 15;
    final private int NEW_SURVEY_REQUEST_CODE = 100;
    final private int OLD_SURVEY_REQUEST_CODE = 200;
    private int DEFAULT_WIDTH;
    private boolean interceptScroll = true;
    private OHScrollView headerScrollView;
    private OHScrollView contentScrollView;
    private TableLayout dataLayout;
    private TableLayout identifierLayout;
    private List<Integer> colWidthList;
    private LinkedHashMap<Survey, List<Response>> mSurveys;
    private int numParticipants;
    private int numQuestions;
    private Roster mRoster;
    private Question surveyIdentifier;
    private List<Question> mQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roster);

        String metadata = getIntent().getStringExtra(EXTRA_PARTICIPANT_METADATA);
        Long instrumentId = getIntent().getLongExtra(EXTRA_INSTRUMENT_ID, -1);
        if (instrumentId == -1) return;

        Instrument instrument = Instrument.findByRemoteId(instrumentId);
        if (!TextUtils.isEmpty(metadata)) {
            try {
                JSONObject json = new JSONObject(metadata);
                String identifier = json.getString("Center ID");
                if (identifier != null) {
                    mRoster = Roster.findByIdentifier(identifier);
                }
                if (mRoster == null) {
                    mRoster = new Roster();
                    mRoster.setIdentifier(identifier);
                    mRoster.setInstrument(instrument);
                    mRoster.save();
                }
            } catch (JSONException e) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Error parsing object json", e);
            }
        }
        
        new QuestionLoaderTask().execute(instrument);
        new RosterLoaderTask().execute(mRoster);

        dataLayout = (TableLayout) findViewById(R.id.content_table);
        identifierLayout = (TableLayout) findViewById(R.id.participant_id);
        headerScrollView = (OHScrollView) findViewById(R.id.header_scroll);
        contentScrollView = (OHScrollView) findViewById(R.id.content_scroll);
        headerScrollView.setScrollViewListener(this);
        contentScrollView.setScrollViewListener(this);
    }

    private String stripHtml(String withHtml) {
        return Html.fromHtml(withHtml).toString().trim();
    }

    private void setHeaders() {
        setSurveyIdentifier();
        TextView identifierHeader = new TextView(this);
        setLinearLayoutHeaderTextViewAttrs(identifierHeader, DEFAULT_WIDTH);
        identifierHeader.setText(stripHtml(surveyIdentifier.getText()));
        LinearLayout participantIDLayout = (LinearLayout) findViewById(R.id.header_1);
        if (participantIDLayout != null) {
            participantIDLayout.addView(identifierHeader);
        }
        colWidthList = new ArrayList<>();
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams
                .MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        for (Question question : mQuestions) {
            if (question != surveyIdentifier) {
                int colWidth = stripHtml(question.getText()).length();
                colWidthList.add(colWidth);
                TextView headerView = new TextView(this);
                headerView.setText(stripHtml(question.getText()));
                setTableRowLayoutHeaderTextViewAttrs(headerView, colWidth);
                setFirstRowListener(headerView, stripHtml(question.getText()));
                row.addView(headerView);
            }
        }
        TableLayout rosterHeaders = (TableLayout) findViewById(R.id.header_2);
        if (rosterHeaders != null) {
            rosterHeaders.addView(row);
        }
    }

    private void setSurveyIdentifier() {
        for (Question question: mQuestions) {
            if (question.identifiesSurvey()) {
                surveyIdentifier = question;
                break;
            }
        }
        if (surveyIdentifier == null) {
            surveyIdentifier = mQuestions.get(0);
        }
        DEFAULT_WIDTH = stripHtml(surveyIdentifier.getText()).length();
    }

    private void drawTableView() {
        drawFirstColumnViews();
        drawRowViews();
    }

    private void setLinearLayoutHeaderTextViewAttrs(TextView view, int colWidth) {
        setTextViewAttributes(view, ContextCompat.getColor(this, R.color.frozenColumnBackground),
                Color.WHITE, HEADER_TEXT_SIZE, colWidth, Typeface.BOLD);
    }

    private void setTableRowLayoutHeaderTextViewAttrs(TextView view, int colWidth) {
        setTextViewAttributes(view, ContextCompat.getColor(this, R.color.frozenColumnBackground),
                Color.WHITE, HEADER_TEXT_SIZE, colWidth, Typeface.BOLD);
    }

    private void setFirstRowListener(TextView headerView, final String header) {
        // TODO: 12/1/16 Implement
//        headerView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(RosterActivity.this, ResponseViewerActivity.class);
//                intent.putExtra(EXTRA_CENTER_ID, mCenter.getIdentifier());
//                intent.putExtra(EXTRA_QUESTION_HEADER, header);
//                startActivity(intent);
//            }
//        });
    }

    private void drawFirstColumnViews() {
        for (int j = 0; j < numParticipants; j++) {
            addTextViewToLinearLayout(DEFAULT_WIDTH);
        }
    }

    private void drawRowViews() {
        for (int j = 0; j < numParticipants; j++) {
            addTableRowToTableLayout();
        }
    }

    private void setTextViewAttributes(TextView view, int backgroundColor, int textColor,
                                       int textSize, int colWidth, int typeface) {
        int minimumHeight = 50;
        int margin = 1;
        int padding = 5;
        int maxLinesPerRow = 5;

        view.setMinimumHeight(minimumHeight);
        view.setEms(colWidth);
        view.setTextColor(textColor);
        view.setGravity(Gravity.CENTER_HORIZONTAL);
        view.setPadding(padding, padding, padding, padding);
        view.setTypeface(view.getTypeface(), typeface);
        view.setTextSize(textSize);
        view.setMaxLines(maxLinesPerRow);
        view.setBackgroundColor(backgroundColor);
        view.setEllipsize(TextUtils.TruncateAt.END);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, margin, margin, margin);
        view.setLayoutParams(params);
    }

    private void addTextViewToLinearLayout(int colWidth) {
        TableRow row = new TableRow(this);
        TextView idView = new TextView(this);
        setTextViewAttributes(idView, ContextCompat.getColor(this, R.color.frozenColumnBackground),
                Color.WHITE, NON_HEADER_TEXT_SIZE, colWidth, Typeface.NORMAL);
        row.addView(idView);
        identifierLayout.addView(row);
    }

    private void addTableRowToTableLayout() {
        TableRow row = new TableRow(this);
        for (int k = 1; k < numQuestions; k++) {
            TextView view = new TextView(this);
            setTextViewAttributes(view, Color.WHITE, Color.BLACK,
                    NON_HEADER_TEXT_SIZE, colWidthList.get(k - 1), Typeface.NORMAL);
            row.addView(view);
        }
        dataLayout.addView(row);
    }

    private void displayData() {
        int k = 0;
        for (Survey survey : mSurveys.keySet()) {
            setFirstColumnView(survey, k);
            setRowView(survey, k);
            k++;
        }
    }

    private void setFirstColumnView(final Survey survey, int index) {
        TableRow row = (TableRow) identifierLayout.getChildAt(index);
        TextView textView = (TextView) row.getVirtualChildAt(0);
        List<Response> responses = mSurveys.get(survey);
        for (Response response : responses) {
            if (response.getQuestion() == surveyIdentifier) {
                textView.setText(response.getText());
                break;
            }
        }
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RosterActivity.this, ParticipantViewerActivity.class);
                intent.putExtra(EXTRA_SURVEY_ID, survey.getId());
                startActivityForResult(intent, OLD_SURVEY_REQUEST_CODE);
            }
        });
    }

    private void setRowView(Survey survey, int index) {
        TableRow row = (TableRow) dataLayout.getChildAt(index);
        List<Response> responses = mSurveys.get(survey);
        List<Integer> lines = new ArrayList<>();
        List<TextView> views = new ArrayList<>();
        TableRow idRow = (TableRow) identifierLayout.getChildAt(index);
        TextView idView = (TextView) idRow.getVirtualChildAt(0);
        lines.add(idView.getLineCount());
        views.add(idView);
        for (int k = 1; k < responses.size(); k++) { // TODO: 12/2/16 Fix
            TextView textView = (TextView) row.getVirtualChildAt(k - 1);
            textView.setText(responses.get(k).getText());
            lines.add(textView.getLineCount());
            views.add(textView);
        }
        int max = Collections.max(lines);
        for (TextView view : views) {
            view.setLines(max);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.roster_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_participant:
                Intent intent = new Intent(this, ParticipantEditorActivity.class);
                intent.putExtra(EXTRA_ROSTER_ID, mRoster.getId());
                startActivityForResult(intent, NEW_SURVEY_REQUEST_CODE);
                return true; // TODO: 12/2/16 Implement
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Long surveyId = data.getLongExtra(EXTRA_SURVEY_ID, -1);
            Survey survey = Survey.load(Survey.class, surveyId);
            if (survey == null) return;
            if (requestCode == NEW_SURVEY_REQUEST_CODE) {
                mSurveys.put(survey, survey.responses());
                addNewView(survey, numParticipants);
                numParticipants++;
            } else if (requestCode == OLD_SURVEY_REQUEST_CODE) {
                int k = 0;
                for (Survey oldSurvey : mSurveys.keySet()) {
                    if (oldSurvey.getIdentifier().equals(survey.getIdentifier())) {
                        updateParticipantRow(k, survey);
                        break;
                    }
                    k++;
                }
            }
        }
    }

    private void addNewView(Survey survey, int index) {
        addTextViewToLinearLayout(DEFAULT_WIDTH);
        addTableRowToTableLayout();
        setFirstColumnView(survey, index);
        setRowView(survey, index);
    }

    private void updateParticipantRow(int index, Survey survey) {
        TableRow row = (TableRow) dataLayout.getChildAt(index);
        List<Response> responses = survey.responses();
        mSurveys.put(survey, responses);
        for (int k = 1; k < responses.size(); k++) { //// TODO: 12/2/16 Fix
            TextView textView = (TextView) row.getVirtualChildAt(k - 1);
            textView.setText(responses.get(k).getText());
        }
        TableRow idRow = (TableRow) identifierLayout.getChildAt(index);
        TextView textIdView = (TextView) idRow.getVirtualChildAt(0);
        textIdView.setText(responses.get(0).getText());
    }

    private class QuestionLoaderTask extends AsyncTask<Instrument, Void, List<Question>> {
        private ProgressDialog dialog = new ProgressDialog(RosterActivity.this);

        @Override
        protected List<Question> doInBackground(Instrument... params) {
            Instrument instrument = params[0];
            return instrument.questions();
        }


        @Override
        protected void onPreExecute() {
            dialog.setTitle(R.string.instrument_loading_progress_header);
            dialog.setMessage(getString(R.string.background_process_progress_message));
            dialog.show();
        }

        @Override
        protected void onPostExecute(List<Question> questions) {
            mQuestions = questions;
            numQuestions = questions.size();
            setHeaders();
            drawTableView();
            if (dialog.isShowing()) dialog.dismiss();
        }

    }

    private class RosterLoaderTask extends AsyncTask<Roster, Void,
            LinkedHashMap<Survey, List<Response>>> {
        private ProgressDialog dialog = new ProgressDialog(RosterActivity.this);

        @Override
        protected LinkedHashMap<Survey, List<Response>> doInBackground(Roster... params) {
            Roster roster = params[0];
            LinkedHashMap<Survey, List<Response>> map = new LinkedHashMap<>();
            for (Survey survey : roster.surveys()) {
                map.put(survey, survey.responses());
            }
            return map;
        }

        @Override
        protected void onPreExecute() {
            dialog.setTitle(R.string.instrument_loading_progress_header);
            dialog.setMessage(getString(R.string.background_process_progress_message));
            dialog.show();
        }

        @Override
        protected void onPostExecute(LinkedHashMap<Survey, List<Response>> rosterItems) {
            mSurveys = rosterItems;
            numParticipants = rosterItems.keySet().size();
            displayData();
            if (dialog.isShowing()) dialog.dismiss();
        }
    }

}