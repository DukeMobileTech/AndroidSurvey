package org.adaptlab.chpir.android.survey.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SurveyActivity;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.relations.ProjectSurveyRelation;
import org.adaptlab.chpir.android.survey.repositories.SurveyRepository;
import org.adaptlab.chpir.android.survey.tasks.SetInstrumentLabelTask;
import org.adaptlab.chpir.android.survey.tasks.SubmitSurveyTask;
import org.adaptlab.chpir.android.survey.utils.InstrumentListLabel;

import java.text.DateFormat;
import java.util.List;

public class SurveyAdapter extends RecyclerView.Adapter<SurveyAdapter.SurveyViewHolder> {
    private final String TAG = SurveyAdapter.class.getName();
    private final LayoutInflater mInflater;
    private List<ProjectSurveyRelation> mProjectSurveyRelations;
    private Context mContext;
    private SurveyRepository mSurveyRepository;

    public SurveyAdapter(Context context, SurveyRepository repository) {
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
        setSurveyLaunchAction(viewHolder);
    }

    private void setSurveyLaunchAction(@NonNull final SurveyViewHolder viewHolder) {
        viewHolder.mViewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProjectSurveyRelation surveyRelation = mProjectSurveyRelations.get(viewHolder.getAdapterPosition());
                Survey survey = surveyRelation.survey;
                Instrument instrument = surveyRelation.instruments.get(0);
                if (survey.isQueued() || survey.isSent()) {
                    Toast.makeText(mContext, R.string.survey_submitted, Toast.LENGTH_LONG).show();
                } else if (!instrument.isLoaded()) {
                    Toast.makeText(mContext, R.string.instrument_not_loaded, Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(mContext, SurveyActivity.class);
                    i.putExtra(SurveyActivity.EXTRA_INSTRUMENT_ID, survey.getInstrumentRemoteId());
                    i.putExtra(SurveyActivity.EXTRA_SURVEY_UUID, survey.getUUID());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mContext.startActivity(i, ActivityOptions.makeSceneTransitionAnimation((Activity) mContext).toBundle());
                    } else {
                        mContext.startActivity(i);
                    }
                }
            }
        });
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

    public void deleteItem(final int position) {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.delete_survey_title)
                .setMessage(R.string.delete_survey_message)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ProjectSurveyRelation projectSurveyRelation = mProjectSurveyRelations.get(position);
                        mSurveyRepository.delete(projectSurveyRelation.survey);
                        mProjectSurveyRelations.remove(position);
                        notifyItemRemoved(position);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        notifyItemChanged(position);
                    }
                })
                .show();
    }

    public void uploadItem(final int position) {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.submit_survey)
                .setMessage(R.string.submit_survey_message)
                .setPositiveButton(R.string.submit,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ProjectSurveyRelation survey = mProjectSurveyRelations.get(position);
                                prepareForSubmission(survey);
                                new SubmitSurveyTask().execute();
                                notifyItemChanged(position);
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                notifyItemChanged(position);
                            }
                        })
                .show();
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

            if (!survey.isQueued()) {
                SetInstrumentLabelTask setInstrumentLabelTask = new SetInstrumentLabelTask();
                setInstrumentLabelTask.setListener(new SetInstrumentLabelTask.AsyncTaskListener() {
                    @Override
                    public void onAsyncTaskFinished(InstrumentListLabel instrumentListLabel) {
                        if (!instrumentListLabel.isLoaded()) {
                            CharSequence text = instrumentListLabel.getTextView().getText();
                            Spannable spannable = new SpannableString(text);
                            spannable.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(
                                    R.color.red)), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            instrumentListLabel.getTextView().setText(spannable);
                        }
                    }
                });
                setInstrumentLabelTask.execute(new InstrumentListLabel(instrument, surveyTextView));
            }

            String progress;
            if (survey.isSent()) {
                progress = mContext.getString(R.string.submitted) + " " + (survey.getCompletedResponseCount() - responses.size())
                        + " " + mContext.getString(R.string.of) + " " + survey.getCompletedResponseCount();
            } else if (survey.isQueued()) {
                progress = mContext.getString(R.string.submitted) + " " +
                        (survey.getCompletedResponseCount() - responses.size()) + " "
                        + mContext.getString(R.string.of) + " " + survey.getCompletedResponseCount();
            } else {
                progress = mContext.getString(R.string.progress) + " " + responses.size() + " "
                        + mContext.getString(R.string.of) + " " + instrument.getQuestionCount();
            }
            SpannableString progressString = new SpannableString(progress);
            if (survey.isSent() || survey.isQueued()) {
                progressString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.blue)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (survey.isComplete()) {
                progressString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.green)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                progressString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.red)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            progressTextView.setText(progressString);
        }

    }
}
