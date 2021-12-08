package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.InstrumentActivity;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Response;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.tasks.SubmitSurveyTask;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class SubmittedSurveyPagerFragment extends Fragment {
    private static final String TAG = "SurveyViewPagerFragment";
    private SubmittedSurveyAdapter submittedSurveysAdapter;
    private List<Survey> mSurveys;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setSurveyLists();

        View view = inflater.inflate(R.layout.recycler_view, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        submittedSurveysAdapter = new SubmittedSurveyAdapter(mSurveys);
        recyclerView.setAdapter(submittedSurveysAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_instrument, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_item_submit_all).setEnabled(true).setVisible(true);
        menu.findItem(R.id.menu_item_settings).setEnabled(false).setVisible(false);
        menu.findItem(R.id.menu_item_refresh).setEnabled(false).setVisible(false);
        menu.findItem(R.id.menu_item_progress_action).setEnabled(false).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_submit_all) {
            submitAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void submitAll() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.submit_survey)
                .setMessage(R.string.submit_survey_message)
                .setPositiveButton(R.string.submit,
                        (dialog, id) -> {
                            for (Survey survey : mSurveys) {
                                prepareForSubmission(survey);
                            }
                            new SubmitSurveyTask(getActivity()).execute();
                            startActivity(new Intent(getActivity(), InstrumentActivity.class));
                            if (getActivity() != null) getActivity().finish();
                        })
                .setNegativeButton(R.string.cancel,
                        (dialog, id) -> {
                        })
                .show();
    }

    private void setSurveyLists() {
        mSurveys = new ArrayList<>();
        for (Survey survey : Survey.getAllProjectSurveys(AppUtil.getProjectId())) {
            if (survey.isSent() || survey.isQueued()) {
                mSurveys.add(survey);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setSurveyLists();
        submittedSurveysAdapter.updateSurveys(mSurveys);
    }

    private void prepareForSubmission(Survey survey) {
        if (survey.readyToSend()) {
            if (survey.getCompletedResponseCount() == 0 && survey.responses().size() > 0) {
                survey.setCompletedResponseCount(survey.responses().size());
            }
            survey.setQueued(true);
        }
    }

    public class SubmittedSurveyAdapter extends RecyclerView.Adapter<SurveyViewHolder> {
        List<Survey> mSurveys;

        SubmittedSurveyAdapter(List<Survey> surveys) {
            mSurveys = surveys;
        }

        public void add(Survey survey) {
            mSurveys.add(0, survey);
            notifyItemInserted(0);
        }

        void updateSurveys(List<Survey> newSurveys) {
            final List<Survey> oldSurveys = new ArrayList<>(this.mSurveys);
            this.mSurveys.clear();
            if (newSurveys != null) {
                this.mSurveys.addAll(newSurveys);
            }

            DiffUtil.calculateDiff(new DiffUtil.Callback() {

                @Override
                public int getOldListSize() {
                    return oldSurveys.size();
                }

                @Override
                public int getNewListSize() {
                    return mSurveys.size();
                }

                @Override
                public boolean areItemsTheSame(int oldSurveyPosition, int newSurveyPosition) {
                    return oldSurveys.get(oldSurveyPosition).equals(
                            mSurveys.get(newSurveyPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldSurveyPosition, int newSurveyPosition) {
                    Survey oldSurvey = oldSurveys.get(oldSurveyPosition);
                    Survey newSurvey = mSurveys.get(newSurveyPosition);
                    return oldSurvey.getLastUpdated().equals(newSurvey.getLastUpdated()) &&
                            oldSurvey.responses().size() == newSurvey.responses().size();
                }
            }).dispatchUpdatesTo(this);
        }

        @NonNull
        @Override
        public SurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View surveyView = getLayoutInflater().inflate(R.layout.list_item_survey,
                    parent, false);
            return new SurveyViewHolder(surveyView);
        }

        @Override
        public void onBindViewHolder(@NonNull final SurveyViewHolder viewHolder, int position) {
            viewHolder.setSurvey(mSurveys.get(position));
        }

        @Override
        public int getItemCount() {
            return mSurveys.size();
        }

    }

    public class SurveyViewHolder extends RecyclerView.ViewHolder {
        View mViewContent;
        TextView surveyTextView;
        TextView progressTextView;
        Survey mSurvey;

        SurveyViewHolder(final View itemView) {
            super(itemView);
            surveyTextView = itemView.findViewById(R.id.surveyProperties);
            progressTextView = itemView.findViewById(R.id.surveyProgress);
            mViewContent = itemView.findViewById(R.id.list_item_survey_content);
        }

        public void setSurvey(Survey survey) {
            this.mSurvey = survey;
            Instrument instrument = survey.getInstrument();
            List<Response> responses = survey.responses();

            String surveyTitle = survey.identifier(getContext(), responses) + "\n";
            String instrumentTitle = instrument.getTitle() + "\n";
            String lastUpdated = DateFormat.getDateTimeInstance().format(survey.getLastUpdated()) + "  ";

            SpannableString spannableString = new SpannableString(surveyTitle + instrumentTitle + lastUpdated);
            // survey title
            spannableString.setSpan(new RelativeSizeSpan(1.2f), 0, surveyTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(
                    R.color.primary_text)), 0, surveyTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // instrument title
            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyTitle.length(),
                    surveyTitle.length() + instrumentTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(
                    R.color.secondary_text)), surveyTitle.length(), surveyTitle.length() +
                    instrumentTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // last updated at
            int end = surveyTitle.length() + instrumentTitle.length() + lastUpdated.length();
            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyTitle.length() +
                    instrumentTitle.length(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                            .secondary_text)), surveyTitle.length() + instrumentTitle.length(),
                    end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // progress
            spannableString.setSpan(new RelativeSizeSpan(0.8f), end,
                    end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .secondary_text)), end, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            surveyTextView.setText(spannableString);

            String progress = "";
            if (survey.isSent()) {
                progress = getString(R.string.submitted) + " " + (survey.getCompletedResponseCount() - responses.size())
                        + " " + getString(R.string.of) + " " + survey.getCompletedResponseCount();
            } else if (survey.isQueued()) {
                progress = getString(R.string.submitted) + " " +
                        (survey.getCompletedResponseCount() - responses.size()) + " "
                        + getString(R.string.of) + " " + survey.getCompletedResponseCount();
            }
            SpannableString progressString = new SpannableString(progress);
            progressString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            progressTextView.setText(progressString);
        }

    }

}
