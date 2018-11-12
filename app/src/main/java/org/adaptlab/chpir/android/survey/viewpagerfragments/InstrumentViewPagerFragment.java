package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SurveyActivity;
import org.adaptlab.chpir.android.survey.SurveyFragment;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.rules.InstrumentLaunchRule;
import org.adaptlab.chpir.android.survey.rules.RuleBuilder;
import org.adaptlab.chpir.android.survey.rules.RuleCallback;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class InstrumentViewPagerFragment extends Fragment {
    private InstrumentAdapter mInstrumentAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        mInstrumentAdapter = new InstrumentAdapter();
        recyclerView.setAdapter(mInstrumentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView
                .getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
        recyclerView.addItemDecoration(dividerItemDecoration);

        return view;
    }

    public void refreshRecyclerView() {
        mInstrumentAdapter.updateInstruments(Instrument.getAllProjectInstruments(
                AppUtil.getProjectId()));
    }

    private class InstrumentAdapter extends RecyclerView.Adapter<InstrumentViewHolder> {
        private List<Instrument> mInstruments;

        InstrumentAdapter() {
            mInstruments = Instrument.getAllProjectInstruments(AppUtil.getProjectId());
        }

        void updateInstruments(List<Instrument> newInstruments) {
            final List<Instrument> oldInstruments = new ArrayList<>(this.mInstruments);
            this.mInstruments.clear();
            if (newInstruments != null) {
                this.mInstruments.addAll(newInstruments);
            }

            DiffUtil.calculateDiff(new DiffUtil.Callback() {

                @Override
                public int getOldListSize() {
                    return oldInstruments.size();
                }

                @Override
                public int getNewListSize() {
                    return mInstruments.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return oldInstruments.get(oldItemPosition).equals(mInstruments.get
                            (newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Instrument oldInstrument = oldInstruments.get(oldItemPosition);
                    Instrument newInstrument = mInstruments.get(newItemPosition);
                    return oldInstrument.getVersionNumber() == newInstrument.getVersionNumber() &&
                            oldInstrument.questions().size() == newInstrument.questions().size() &&
                            oldInstrument.loaded() == newInstrument.loaded();
                }
            }).dispatchUpdatesTo(this);
        }

        @NonNull
        @Override
        public InstrumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View instrumentView = inflater.inflate(R.layout.list_item_instrument, parent,
                    false);
            return new InstrumentViewHolder(instrumentView);
        }

        @Override
        public void onBindViewHolder(@NonNull InstrumentViewHolder viewHolder, int position) {
            viewHolder.setInstrument(mInstruments.get(position));
        }

        @Override
        public int getItemCount() {
            return mInstruments.size();
        }

    }

    private class InstrumentViewHolder extends RecyclerView.ViewHolder {
        TextView instrumentPropertiesTextView;
        Instrument mInstrument;

        InstrumentViewHolder(View itemView) {
            super(itemView);
            instrumentPropertiesTextView = itemView.findViewById(R.id.instrumentProperties);
            setOnClickListener(itemView);
        }

        private void setOnClickListener(View itemView) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mInstrument != null && mInstrument.loaded() && mInstrument.questions()
                            .size() > 0) {
                        new RuleBuilder(getActivity())
                                .addRule(new InstrumentLaunchRule(mInstrument,
                                        getActivity().getString(R.string
                                                .rule_failure_instrument_launch)))
                                .showToastOnFailure(true)
                                .setCallbacks(new RuleCallback() {
                                    public void onRulesPass() {
                                        Intent i = new Intent(getActivity(),
                                                SurveyActivity.class);
                                        i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID,
                                                mInstrument.getRemoteId());
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            startActivity(i, ActivityOptions
                                                    .makeSceneTransitionAnimation(getActivity())
                                                    .toBundle());
                                        } else {
                                            startActivity(i);
                                        }
                                    }

                                    public void onRulesFail() {
                                    }
                                }).checkRules();
                    }
                }
            });
        }

        void setInstrument(Instrument instrument) {
            this.mInstrument = instrument;
            int numQuestions = instrument.getQuestionCount();
            String title = instrument.getTitle() + "\n";
            String questionCount = numQuestions + " " + FormatUtils.pluralize(numQuestions,
                    getString(R.string.question), getString(R.string.questions)) + "  ";
            String version = getString(R.string.version) + ": " + instrument.getVersionNumber();
            SpannableString spannableText = new SpannableString(title + questionCount +
                    version);
            // Title styling
            spannableText.setSpan(new RelativeSizeSpan(1.2f), 0, title.length(), Spannable
                    .SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .primary_text)), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Question count styling
            spannableText.setSpan(new RelativeSizeSpan(0.8f), title.length(), title.length() +
                    questionCount.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                            .secondary_text)), title.length(), title.length() + questionCount
                            .length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
                    title.length(), title.length() + questionCount.length(), Spannable
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
            // Version styling
            spannableText.setSpan(new RelativeSizeSpan(0.8f), title.length() + questionCount
                            .length(), title.length() + questionCount.length() + version
                            .length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .secondary_text)), title.length() + questionCount.length(), title.length() +
                    questionCount.length() + version.length(), 0);
            spannableText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                    title.length() + questionCount.length(), title.length() + questionCount
                            .length() + version.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            instrumentPropertiesTextView.setText(spannableText);
            new SetInstrumentLabelTask().execute(new InstrumentListLabel(instrument,
                    instrumentPropertiesTextView));
        }
    }

    private class SetInstrumentLabelTask extends AsyncTask<InstrumentListLabel, Void,
            InstrumentListLabel> {

        @Override
        protected InstrumentListLabel doInBackground(InstrumentListLabel... params) {
            InstrumentListLabel instrumentListLabel = params[0];
            Instrument instrument = instrumentListLabel.getInstrument();
            instrumentListLabel.setLoaded(instrument.loaded());
            return instrumentListLabel;
        }

        @Override
        protected void onPostExecute(InstrumentListLabel instrumentListLabel) {
            if (isAdded()) {
                if (instrumentListLabel.isLoaded()) {
                    instrumentListLabel.getTextView().setTextColor(Color.BLACK);
                } else {
                    instrumentListLabel.getTextView().setTextColor(Color.RED);
                }
            }
        }
    }

    private class InstrumentListLabel {
        private Instrument mInstrument;
        private TextView mTextView;
        private Boolean mLoaded;

        InstrumentListLabel(Instrument instrument, TextView textView) {
            this.mInstrument = instrument;
            this.mTextView = textView;
        }

        public Instrument getInstrument() {
            return mInstrument;
        }

        public TextView getTextView() {
            return mTextView;
        }

        public void setLoaded(boolean loaded) {
            mLoaded = loaded;
        }

        public Boolean isLoaded() {
            return mLoaded;
        }
    }

}