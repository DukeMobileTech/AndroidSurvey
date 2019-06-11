package org.adaptlab.chpir.android.survey.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.Survey2Activity;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Response;
import org.adaptlab.chpir.android.survey.entities.Survey;
import org.adaptlab.chpir.android.survey.relations.SurveyRelation;
import org.adaptlab.chpir.android.survey.tasks.SetInstrumentLabelTask;
import org.adaptlab.chpir.android.survey.tasks.SubmitSurveyTask;
import org.adaptlab.chpir.android.survey.utils.InstrumentListLabel;

import java.text.DateFormat;
import java.util.List;

public class ProjectSurveyAdapter extends RecyclerView.Adapter<ProjectSurveyAdapter.SurveyViewHolder> {

    private final LayoutInflater mInflater;
    private List<SurveyRelation> mSurveyRespons;
    private List<Instrument> mInstruments;
    private Context mContext;

    public ProjectSurveyAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void setSurveys(List<SurveyRelation> list) {
        mSurveyRespons = list;
        notifyDataSetChanged();
    }

    public void setInstruments(List<Instrument> instruments) {
        mInstruments = instruments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View surveyView = mInflater.inflate(R.layout.list_item_survey_background, parent, false);
        return new SurveyViewHolder(surveyView);
    }

    @Override
    public void onBindViewHolder(@NonNull final SurveyViewHolder viewHolder, int position) {
        SurveyRelation surveyRelation = mSurveyRespons.get(position);
        Instrument instrument = getSurveyInstrument(surveyRelation);
        if (surveyRelation != null && instrument != null) {
            viewHolder.setData(surveyRelation, instrument);
            setSurveyDeleteAction(viewHolder);
            setSurveyLaunchAction(viewHolder, instrument);
            setSurveySubmitAction(viewHolder);
        }
    }

    private Instrument getSurveyInstrument(SurveyRelation surveyRelation) {
        if (mInstruments == null) return null;
        for (Instrument instrument : mInstruments) {
            if (instrument.getRemoteId().equals(surveyRelation.survey.getInstrumentRemoteId())) {
                return instrument;
            }
        }
        return null;
    }

    private void setSurveyDeleteAction(@NonNull final SurveyViewHolder viewHolder) {
        viewHolder.mDeleteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.delete_survey_title)
                        .setMessage(R.string.delete_survey_message)
                        .setPositiveButton(R.string.delete, new DialogInterface
                                .OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                remove(viewHolder.getAdapterPosition());
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface
                                .OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                notifyItemChanged(viewHolder.getAdapterPosition());
                            }
                        })
                        .show();
            }
        });
    }

    private void setSurveyLaunchAction(@NonNull final SurveyViewHolder viewHolder, final Instrument instrument) {
        viewHolder.mViewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SurveyRelation surveyRelation = mSurveyRespons.get(viewHolder.getAdapterPosition());
                Survey survey = surveyRelation.survey;
                if (survey == null) return;
                if (survey.isQueued() || survey.isSent()) {
                    Toast.makeText(mContext, R.string.survey_submitted, Toast.LENGTH_LONG).show();
                } else if (!instrument.isLoaded()) {
                    Toast.makeText(mContext, R.string.instrument_not_loaded, Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(mContext, Survey2Activity.class);
                    i.putExtra(Survey2Activity.EXTRA_INSTRUMENT_ID, survey.getInstrumentRemoteId());
                    i.putExtra(Survey2Activity.EXTRA_SURVEY_UUID, survey.getUUID());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mContext.startActivity(i, ActivityOptions.makeSceneTransitionAnimation((Activity) mContext).toBundle());
                    } else {
                        mContext.startActivity(i);
                    }
                }
            }
        });
    }

    private void setSurveySubmitAction(final SurveyViewHolder viewHolder) {
        viewHolder.mSubmitAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.submit_survey)
                        .setMessage(R.string.submit_survey_message)
                        .setPositiveButton(R.string.submit,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        SurveyRelation survey = mSurveyRespons.get(viewHolder.getAdapterPosition());
                                        prepareForSubmission(survey);
                                        new SubmitSurveyTask(mContext).execute();
                                        notifyItemChanged(viewHolder.getAdapterPosition());
                                    }
                                })
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        notifyItemChanged(viewHolder.getAdapterPosition());
                                    }
                                })
                        .show();
            }
        });
    }

    public void remove(int position) {
        if (position > -1 && position < mSurveyRespons.size()) {
            SurveyRelation survey = mSurveyRespons.get(position);
            if (survey != null) {
                mSurveyRespons.remove(position);
//                survey.delete();
                notifyItemRemoved(position);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mSurveyRespons == null ? 0 : mSurveyRespons.size();
    }

    public void prepareForSubmission(SurveyRelation surveyRelation) {
        Survey survey = surveyRelation.survey;
        List<Response> responses = surveyRelation.responses;
        if (survey.getCompletedResponseCount() == 0 && responses.size() > 0) {
            survey.setCompletedResponseCount(responses.size());
        }
        survey.setQueued(true);
    }

    public class SurveyViewHolder extends RecyclerView.ViewHolder {
        public View mViewContent;
        public View mActionContainer;
        TextView mDeleteAction;
        TextView mSubmitAction;
        TextView surveyTextView;
        TextView progressTextView;
        SurveyRelation mSurvey;

        SurveyViewHolder(final View itemView) {
            super(itemView);
            surveyTextView = itemView.findViewById(R.id.surveyProperties);
            progressTextView = itemView.findViewById(R.id.surveyProgress);
            mViewContent = itemView.findViewById(R.id.list_item_survey_main_content);
            mActionContainer = itemView.findViewById(R.id.list_item_survey_action_container);
            mDeleteAction = itemView.findViewById(R.id.list_item_survey_action_delete);
            mSubmitAction = itemView.findViewById(R.id.list_item_survey_action_submit);
        }

        public void setData(SurveyRelation surveyRelation, Instrument instrument) {
            this.mSurvey = surveyRelation;
            Survey survey = surveyRelation.survey;
            List<Response> responses = surveyRelation.responses;
            String surveyTitle = survey.identifier(mContext) + "\n";
            String instrumentTitle = instrument.getTitle() + "\n";
            String lastUpdated = DateFormat.getDateTimeInstance().format(
                    survey.getLastUpdated()) + "  ";
            SpannableString spannableString = new SpannableString(surveyTitle +
                    instrumentTitle + lastUpdated);
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
                if (responses.size() > 0) {
                    mSubmitAction.setVisibility(View.VISIBLE);
                } else {
                    mSubmitAction.setVisibility(View.GONE);
                }
                progressString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color
                        .blue)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (survey.isComplete()) {
                progressString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color
                        .green)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mSubmitAction.setVisibility(View.VISIBLE);
            } else {
                progressString.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color
                        .red)), 0, progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mSubmitAction.setVisibility(View.GONE);
            }

            progressTextView.setText(progressString);
        }

    }
}
