package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.Instrument2Activity;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SurveyActivity;
import org.adaptlab.chpir.android.survey.SurveyFragment;
import org.adaptlab.chpir.android.survey.models.Survey;
import org.adaptlab.chpir.android.survey.utils.AppUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class SurveyViewPagerFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private SurveyAdapter mSurveyAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_fragment, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mSurveyAdapter = new SurveyAdapter();
        mRecyclerView.setAdapter(mSurveyAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        setSurveyLeftSwipe();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSurveyAdapter.updateSurveys(Survey.getAllProjectSurveys(AppUtil.getProjectId()));
    }

    private void setSurveyLeftSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper
                .SimpleCallback(0, ItemTouchHelper.LEFT) {
            // No support for drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                SurveyAdapter surveyAdapter = (SurveyAdapter) mRecyclerView.getAdapter();
                surveyAdapter.remove(viewHolder.getAdapterPosition());
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private class SurveyAdapter extends RecyclerView.Adapter<SurveyViewHolder> {
        private List<Survey> mSurveys;

        SurveyAdapter() {
            mSurveys = Survey.getAllProjectSurveys(AppUtil.getProjectId());
        }

        public void add(Survey survey) {
            mSurveys.add(0, survey);
            notifyItemInserted(0);
        }

        public void remove(int position) {
            Survey survey = mSurveys.get(position);
            if (survey != null) {
                mSurveys.remove(position);
                survey.delete();
                notifyItemRemoved(position);
            }
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
                    return oldSurveys.get(oldSurveyPosition).equals(mSurveys.get
                            (newSurveyPosition));
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
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View surveyView = inflater.inflate(R.layout.list_item_survey, parent, false);
            return new SurveyViewHolder(surveyView);
        }

        @Override
        public void onBindViewHolder(@NonNull SurveyViewHolder viewHolder, int position) {
            viewHolder.setSurvey(mSurveys.get(position));
        }

        @Override
        public int getItemCount() {
            return mSurveys.size();
        }

    }

    private class SurveyViewHolder extends RecyclerView.ViewHolder {
        TextView surveyTextView;
        Survey mSurvey;

        SurveyViewHolder(final View itemView) {
            super(itemView);
            surveyTextView = itemView.findViewById(R.id.surveyProperties);
            setOnClickListener(itemView);
        }

        private void setOnClickListener(View itemView) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSurvey != null && mSurvey.getInstrument().loaded()) {
                        Intent i = new Intent(getActivity(), SurveyActivity.class);
                        i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID,
                                mSurvey.getInstrument().getRemoteId());
                        i.putExtra(SurveyFragment.EXTRA_SURVEY_ID, mSurvey.getId());
                        i.putExtra(SurveyFragment.EXTRA_QUESTION_NUMBER,
                                mSurvey.getLastQuestion().getNumberInInstrument() - 1);
                        i.putExtra(SurveyFragment.EXTRA_AUTHORIZE_SURVEY,
                                ((Instrument2Activity) getActivity()).isAuthorizeSurvey());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startActivity(i, ActivityOptions.makeSceneTransitionAnimation
                                    (getActivity()).toBundle());
                        } else {
                            startActivity(i);
                        }
                    }
                }
            });
        }

        public void setSurvey(Survey survey) {
            this.mSurvey = survey;
            String surveyTitle = survey.identifier(AppUtil.getContext()) + "\n";
            String instrumentTitle = survey.getInstrument().getTitle() + "\n";
            String lastUpdated = DateFormat.getDateTimeInstance().format(survey
                    .getLastUpdated())
                    + "  ";
            String progress = survey.responses().size() + " " + getString(R.string.of) + " " +
                    survey.getInstrument().questions().size();

            SpannableString spannableString = new SpannableString(surveyTitle +
                    instrumentTitle +
                    lastUpdated + progress);
            // survey title
            spannableString.setSpan(new RelativeSizeSpan(1.2f), 0, surveyTitle.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .primary_text)), 0, surveyTitle.length(), Spannable
                    .SPAN_EXCLUSIVE_EXCLUSIVE);
            // instrument title
            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyTitle.length(),
                    surveyTitle
                            .length() + instrumentTitle.length(), Spannable
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .secondary_text)), surveyTitle.length(), surveyTitle.length() +
                    instrumentTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // last updated at
            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyTitle.length() +
                    instrumentTitle.length(), surveyTitle.length() + instrumentTitle.length() +
                    lastUpdated.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                            .secondary_text)), surveyTitle.length() + instrumentTitle.length(),
                    surveyTitle.length() + instrumentTitle.length() + lastUpdated.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // progress
            spannableString.setSpan(new RelativeSizeSpan(0.8f), surveyTitle.length() +
                            instrumentTitle.length() + lastUpdated.length(), surveyTitle
                            .length() +
                            instrumentTitle.length() + lastUpdated.length() + progress.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .secondary_text)), surveyTitle.length() + instrumentTitle.length() +
                    lastUpdated.length(), surveyTitle.length() + instrumentTitle.length() +
                    lastUpdated.length() + progress.length(), Spannable
                    .SPAN_EXCLUSIVE_EXCLUSIVE);

            if (survey.readyToSend()) {
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                        .green)), surveyTitle.length() + instrumentTitle.length() + lastUpdated
                        .length(), surveyTitle.length() + instrumentTitle.length() + lastUpdated
                        .length() + progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                        .red)), surveyTitle.length() + instrumentTitle.length() + lastUpdated
                        .length(), surveyTitle.length() + instrumentTitle.length() + lastUpdated
                        .length() + progress.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            surveyTextView.setText(spannableString);
        }

    }

}
