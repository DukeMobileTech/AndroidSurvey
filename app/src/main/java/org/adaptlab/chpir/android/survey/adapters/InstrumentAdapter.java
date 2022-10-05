package org.adaptlab.chpir.android.survey.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SurveyActivity;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.tasks.SetInstrumentLabelTask;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.adaptlab.chpir.android.survey.utils.InstrumentListLabel;

import java.util.List;

public class InstrumentAdapter extends RecyclerView.Adapter<InstrumentAdapter.InstrumentViewHolder> {
    private final LayoutInflater mInflater;
    private final Context mContext;
    private List<Instrument> mInstruments;

    public InstrumentAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void setInstruments(List<Instrument> instruments) {
        mInstruments = instruments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InstrumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View instrumentView = mInflater.inflate(R.layout.list_item_instrument, parent, false);
        return new InstrumentViewHolder(instrumentView, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull InstrumentViewHolder viewHolder, int position) {
        viewHolder.setInstrument(mInstruments.get(position));
    }

    @Override
    public int getItemCount() {
        return mInstruments == null ? 0 : mInstruments.size();
    }

    class InstrumentViewHolder extends RecyclerView.ViewHolder {
        TextView instrumentPropertiesTextView;
        Instrument mInstrument;
        Context mContext;

        InstrumentViewHolder(View itemView, Context context) {
            super(itemView);
            mContext = context;
            instrumentPropertiesTextView = itemView.findViewById(R.id.instrumentProperties);
            setOnClickListener(itemView);
        }

        private void setOnClickListener(View itemView) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mInstrument == null || !mInstrument.isLoaded()) {
                        Toast.makeText(mContext, R.string.instrument_not_loaded, Toast.LENGTH_LONG).show();
                    } else {
                        Intent i = new Intent(mContext, SurveyActivity.class);
                        i.putExtra(SurveyActivity.EXTRA_INSTRUMENT_ID, mInstrument.getRemoteId());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mContext.startActivity(i, ActivityOptions.makeSceneTransitionAnimation((Activity) mContext).toBundle());
                        } else {
                            mContext.startActivity(i);
                        }
                    }
                }
            });
        }

        void setInstrument(Instrument instrument) {
            this.mInstrument = instrument;
            int numQuestions = instrument.getQuestionCount();
            String title = instrument.getTitle() + "\n";
            String questionCount = numQuestions + " " + FormatUtils.pluralize(numQuestions,
                    mContext.getString(R.string.question), mContext.getString(R.string.questions)) + "  ";
            String version = mContext.getString(R.string.version) + ": " + instrument.getVersionNumber();
            SpannableString spannableText = new SpannableString(title + questionCount + version);
            // Title styling
            spannableText.setSpan(new RelativeSizeSpan(1.2f), 0, title.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(
                    R.color.primary_text)), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Question count styling
            spannableText.setSpan(new RelativeSizeSpan(0.8f), title.length(), title.length() +
                    questionCount.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(
                    R.color.secondary_text)), title.length(),
                    title.length() + questionCount.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
                    title.length(), title.length() + questionCount.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Version styling
            int end = title.length() + questionCount.length() + version.length();
            spannableText.setSpan(new RelativeSizeSpan(0.8f), title.length() +
                            questionCount.length(), end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color
                    .secondary_text)), title.length() + questionCount.length(), end, 0);
            spannableText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                    title.length() + questionCount.length(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            instrumentPropertiesTextView.setText(spannableText);

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
            setInstrumentLabelTask.execute(new InstrumentListLabel(instrument, instrumentPropertiesTextView));
        }
    }
}
