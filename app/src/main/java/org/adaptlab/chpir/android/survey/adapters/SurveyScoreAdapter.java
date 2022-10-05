package org.adaptlab.chpir.android.survey.adapters;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.relations.ProjectSurveyRelation;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;

import java.text.DateFormat;
import java.util.List;

public class SurveyScoreAdapter extends RecyclerView.Adapter<SurveyScoreAdapter.SurveyViewHolder> {
    private final String TAG = SurveyScoreAdapter.class.getName();
    private final LayoutInflater mInflater;
    private final Context mContext;
    private final SurveyRepository mSurveyRepository;
    private List<ProjectSurveyRelation> mProjectSurveyRelations;

    public SurveyScoreAdapter(Context context, SurveyRepository repository) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mSurveyRepository = repository;
    }

    public void setSurveys(List<ProjectSurveyRelation> list) {
        mProjectSurveyRelations = list;
        notifyDataSetChanged();
    }

    public List<ProjectSurveyRelation> getSurveyRelations() {
        return mProjectSurveyRelations;
    }

    @NonNull
    @Override
    public SurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View surveyView = mInflater.inflate(R.layout.list_item_survey, parent, false);
        return new SurveyViewHolder(surveyView);
    }

    @Override
    public void onBindViewHolder(@NonNull final SurveyViewHolder viewHolder, int position) {
        ProjectSurveyRelation surveyRelation = mProjectSurveyRelations.get(position);
        if (surveyRelation == null) return;
        viewHolder.setData(surveyRelation);
    }

    @Override
    public int getItemCount() {
        return mProjectSurveyRelations == null ? 0 : mProjectSurveyRelations.size();
    }

    public void prepareForSubmission(ProjectSurveyRelation surveyRelation) {
        Survey survey = surveyRelation.survey;
        List<Response> responses = surveyRelation.responses;
        if (survey.getCompletedResponseCount() == 0 && responses.size() > 0) {
            survey.setCompletedResponseCount(responses.size());
        }
        survey.setQueued(true);
        mSurveyRepository.update(survey);
    }

    class SurveyViewHolder extends RecyclerView.ViewHolder {
        View mViewContent;
        TextView surveyTextView;
        TextView progressTextView;
        ProjectSurveyRelation mProjectSurveyRelation;

        SurveyViewHolder(final View itemView) {
            super(itemView);
            surveyTextView = itemView.findViewById(R.id.surveyProperties);
            progressTextView = itemView.findViewById(R.id.surveyProgress);
            mViewContent = itemView.findViewById(R.id.list_item_survey_content);
        }

        void setData(ProjectSurveyRelation surveyRelation) {
            this.mProjectSurveyRelation = surveyRelation;
            Survey survey = surveyRelation.survey;
            Instrument instrument = surveyRelation.instruments.get(0);
            List<Response> responses = surveyRelation.responses;
            String surveyTitle = survey.identifier(mContext, surveyRelation.responses) + "\n";
            String instrumentTitle = instrument.getTitle() + "\n";
            String lastUpdated = DateFormat.getDateTimeInstance().format(survey.getLastUpdated()) + "  ";

            SpannableString spannableString = new SpannableString(surveyTitle + instrumentTitle + lastUpdated);
            // survey title
            spannableString.setSpan(new RelativeSizeSpan(1.2f), 0, surveyTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(
                    R.color.primary_text)), 0, surveyTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // instrument title
            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyTitle.length(),
                    surveyTitle.length() + instrumentTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(
                    R.color.secondary_text)), surveyTitle.length(), surveyTitle.length() +
                    instrumentTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // last updated at
            int end = surveyTitle.length() + instrumentTitle.length() + lastUpdated.length();
            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyTitle.length() +
                    instrumentTitle.length(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color
                            .secondary_text)), surveyTitle.length() + instrumentTitle.length(),
                    end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // progress
            spannableString.setSpan(new RelativeSizeSpan(0.8f), end,
                    end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color
                    .secondary_text)), end, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            surveyTextView.setText(spannableString);

            String progress = "";
            if (survey.isSent()) {
                progress = mContext.getString(R.string.submitted) + " " + (survey.getCompletedResponseCount() - responses.size())
                        + " " + mContext.getString(R.string.of) + " " + survey.getCompletedResponseCount();
            } else if (survey.isQueued()) {
                progress = mContext.getString(R.string.submitted) + " " +
                        (survey.getCompletedResponseCount() - responses.size()) + " "
                        + mContext.getString(R.string.of) + " " + survey.getCompletedResponseCount();
            }
            SpannableString progressString = new SpannableString(progress);
            progressString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.blue)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            progressTextView.setText(progressString);
        }

    }
}
