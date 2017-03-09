package org.adaptlab.chpir.android.survey.roster;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Question;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Roster;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.roster.listeners.ScrollViewListener;
import org.adaptlab.chpir.android.survey.roster.views.OHScrollView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.adaptlab.chpir.android.survey.FormatUtils.stripHtml;

public class RosterActivity extends AppCompatActivity implements ScrollViewListener {
    public final static String EXTRA_ROSTER_ID =
            "org.adaptlab.chpir.android.survey.roster.roster_id";
    public final static String EXTRA_SURVEY_ID =
            "org.adaptlab.chpir.android.survey.roster.survey_id";
    public final static String EXTRA_INSTRUMENT_ID =
            "org.adaptlab.chpir.android.survey.roster.instrument_id";
    public final static String EXTRA_PARTICIPANT_METADATA =
            "org.adaptlab.chpir.android.survey.roster.metadata";
    public static final String EXTRA_ROSTER_UUID =
            "org.adaptlab.chpir.android.survey.roster.roster_uuid";
    public static final String EXTRA_QUESTION_ID =
            "org.adaptlab.chpir.android.survey.roster.question_id";
    public final static String EXTRA_SURVEY_IDENTIFIER =
            "org.adaptlab.chpir.android.survey.roster.survey_identifier";
    private final String TAG = "RosterActivity";
    private boolean interceptScroll = true;
    private OHScrollView headerScrollView;
    private OHScrollView contentScrollView;
    private TableLayout dataLayout;
    private TableLayout identifierLayout;
    private Integer[] columnWidths;
    private LinkedHashMap<Survey, List<Response>> mSurveyResponsesMap;
    private Roster mRoster;
    private Question surveyIdentifier;
    private List<Question> mQuestions;
    private List<Survey> mSurveys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roster);

        Long instrumentId = getIntent().getLongExtra(EXTRA_INSTRUMENT_ID, -1);
        if (instrumentId == -1) return;
        Instrument instrument = Instrument.findByRemoteId(instrumentId);

        String metadata = getIntent().getStringExtra(EXTRA_PARTICIPANT_METADATA);
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
        } else {
            String rosterUUID = getIntent().getStringExtra(EXTRA_ROSTER_UUID);
            if (rosterUUID != null) {
                mRoster = Roster.findByUUID(rosterUUID);
            } else {
                mRoster = new Roster();
                mRoster.setInstrument(instrument);
                mRoster.save();
            }
        }
        setTitle(mRoster.identifier(this));
        mQuestions = new ArrayList<>();
        mSurveys = new ArrayList<>();
        new QuestionLoaderTask().execute(instrument);

        dataLayout = (TableLayout) findViewById(R.id.content_table);
        identifierLayout = (TableLayout) findViewById(R.id.participant_id);
        headerScrollView = (OHScrollView) findViewById(R.id.header_scroll);
        contentScrollView = (OHScrollView) findViewById(R.id.content_scroll);
        headerScrollView.setScrollViewListener(this);
        contentScrollView.setScrollViewListener(this);
    }

    private void setHeaders() {
        setSurveyIdentifier();
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams
                .MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        for (int k = 0; k < mQuestions.size(); k++) {
            Question question = mQuestions.get(k);
            TextView headerView = new TextView(this);
            setColumnWidths(headerView, k);
            headerView.setText(stripHtml(question.getText()));
            if (question == surveyIdentifier) {
                setTextViewAttributes(headerView, ContextCompat.getColor(this,
                        R.color.primary_light), Typeface.BOLD);
                LinearLayout participantIDLayout = (LinearLayout) findViewById(R.id.header_1);
                if (participantIDLayout != null) {
                    participantIDLayout.addView(headerView);
                }
            } else {
                setTextViewAttributes(headerView, ContextCompat.getColor(this,
                        R.color.primary_light), Typeface.BOLD);
                setFirstRowListener(headerView, question);
                row.addView(headerView);
            }
        }
        TableLayout rosterHeaders = (TableLayout) findViewById(R.id.header_2);
        if (rosterHeaders != null) {
            rosterHeaders.addView(row);
        }
    }

    private void setSurveyIdentifier() {
        for (Question question : mQuestions) {
            if (question.identifiesSurvey()) {
                surveyIdentifier = question;
                break;
            }
        }
        if (surveyIdentifier == null) {
            surveyIdentifier = mQuestions.get(0);
        }
    }

    private void setColumnWidths(final TextView view, final int position) {
        view.post(new Runnable() {
            @Override
            public void run() {
                columnWidths[position] = view.getWidth();
            }
        });
    }

    private void setTextViewAttributes(TextView view, int color, int typeface) {
        int minimumHeight = 100;
        int minimumWidth = 100;
        int paddingBottomTop = 10;
        int paddingLeftRight = 20;
        int maxLinesPerRow = 1;
        int textSize = 15;

        view.setMinimumHeight(minimumHeight);
        view.setMinimumWidth(minimumWidth);
        view.setTextColor(ContextCompat.getColor(this, R.color.primary_text));
        view.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        view.setPadding(paddingLeftRight, paddingBottomTop, paddingLeftRight, paddingBottomTop);
        view.setTypeface(view.getTypeface(), typeface);
        view.setTextSize(textSize);
        view.setMaxLines(maxLinesPerRow);
        view.setBackgroundColor(color);
        view.setEllipsize(TextUtils.TruncateAt.END);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
    }

    private void setFirstRowListener(TextView headerView, final Question question) {
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RosterActivity.this, ResponseViewerActivity.class);
                intent.putExtra(EXTRA_ROSTER_ID, mRoster.getId());
                intent.putExtra(EXTRA_QUESTION_ID, question.getId());
                intent.putExtra(EXTRA_SURVEY_IDENTIFIER, surveyIdentifier.getId());
                startActivity(intent);
            }
        });
    }

    private void drawTableView() {
        identifierLayout.removeAllViews();
        dataLayout.removeAllViews();
        for (int j = 0; j < mSurveys.size(); j++) {
            displayResponses(mSurveys.get(j));
        }
    }

    private void displayResponses(Survey survey) {
        TableRow idRow = new TableRow(this);
        TableRow responseRow = new TableRow(this);
        List<Response> responseList = mSurveyResponsesMap.get(survey);
        boolean isSurveyIdSet = false;
        for (int k = 0; k < responseList.size(); k++) {
            TextView view = new TextView(this);
            adjustColumnWidths(view, k);
            Response response = responseList.get(k);
            if (response != null) {
                if (response.getQuestion() == surveyIdentifier) {
                    setIdView(view, idRow, survey, response.getText());
                    isSurveyIdSet = true;
                } else {
                    showCell(responseRow, view, Color.TRANSPARENT, Typeface.NORMAL,
                            response.getText());
                }
            } else {
                if (k == responseList.size() - 1 && !isSurveyIdSet) {
                    setIdView(view, idRow, survey, "");
                    isSurveyIdSet = true;
                } else {
                    showCell(responseRow, view, Color.TRANSPARENT, Typeface.NORMAL, "");
                }
            }
        }
        identifierLayout.addView(idRow);
        dataLayout.addView(responseRow);
    }

    private void showCell(TableRow row, TextView view, int color, int typeface, String text) {
        setTextViewAttributes(view, color, typeface);
        view.setText(text);
        row.addView(view);
    }

    private void setIdView(TextView view, TableRow row, final Survey survey, String text) {
        showCell(row, view, ContextCompat.getColor(this, R.color.primary_light), Typeface.BOLD, text);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RosterActivity.this, ParticipantViewerActivity.class);
                intent.putExtra(EXTRA_SURVEY_ID, survey.getId());
                startActivity(intent);
            }
        });
    }

    private void adjustColumnWidths(final TextView view, final int pos) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setWidth(columnWidths[pos]);
            }
        });
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
                startActivity(intent);
                return true;
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
    protected void onResume() {
        super.onResume();
        new RosterLoaderTask().execute(mRoster);
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
            columnWidths = new Integer[mQuestions.size()];
            setHeaders();
            if (dialog.isShowing()) dialog.dismiss();
            new RosterLoaderTask().execute(mRoster);
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
                List<Response> responses = new ArrayList<>();
                for (Question question : mQuestions) {
                    responses.add(survey.getResponseByQuestion(question));
                }
                map.put(survey, responses);
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
            mSurveyResponsesMap = rosterItems;
            mSurveys.clear();
            mSurveys.addAll(rosterItems.keySet());
            drawTableView();
            if (dialog.isShowing()) dialog.dismiss();
        }
    }

}