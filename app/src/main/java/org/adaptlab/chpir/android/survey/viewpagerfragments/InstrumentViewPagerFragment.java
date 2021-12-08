package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.activerecordcloudsync.ActiveRecordCloudSync;
import org.adaptlab.chpir.android.activerecordcloudsync.HttpUtil;
import org.adaptlab.chpir.android.activerecordcloudsync.NotificationUtils;
import org.adaptlab.chpir.android.survey.AdminActivity;
import org.adaptlab.chpir.android.survey.InstrumentActivity;
import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SurveyActivity;
import org.adaptlab.chpir.android.survey.SurveyFragment;
import org.adaptlab.chpir.android.survey.models.AdminSettings;
import org.adaptlab.chpir.android.survey.models.Image;
import org.adaptlab.chpir.android.survey.models.Instrument;
import org.adaptlab.chpir.android.survey.models.Project;
import org.adaptlab.chpir.android.survey.rules.InstrumentLaunchRule;
import org.adaptlab.chpir.android.survey.rules.RuleBuilder;
import org.adaptlab.chpir.android.survey.rules.RuleCallback;
import org.adaptlab.chpir.android.survey.tasks.SetInstrumentLabelTask;
import org.adaptlab.chpir.android.survey.tasks.SetScoreUnitOrderingQuestionTask;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.utils.FormatUtils;
import org.adaptlab.chpir.android.survey.utils.InstrumentListLabel;

import java.util.ArrayList;
import java.util.List;

import static org.adaptlab.chpir.android.survey.utils.AppUtil.getProjectId;

public class InstrumentViewPagerFragment extends Fragment {
    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        InstrumentAdapter mInstrumentAdapter = new InstrumentAdapter();
        recyclerView.setAdapter(mInstrumentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView
                .getContext(), DividerItemDecoration.VERTICAL);
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
        MenuItem mActionProgressItem = menu.findItem(R.id.menu_item_progress_action);
        menu.findItem(R.id.menu_item_submit_all).setEnabled(false).setVisible(false);
        if (getResources().getBoolean(R.bool.default_hide_admin_button)) {
            menu.findItem(R.id.menu_item_settings).setEnabled(false).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                Intent i = new Intent(getActivity(), AdminActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                } else {
                    startActivity(i);
                }
                return true;
            case R.id.menu_item_refresh:
                downloadInstruments();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void downloadInstruments() {
        showProgressDialog();
        RefreshInstrumentsTask asyncTask = new RefreshInstrumentsTask();
        asyncTask.setListener(() -> {
            displayProjectName();
            new SetScoreUnitOrderingQuestionTask().execute();
            RefreshImagesTask refreshImagesTask = new RefreshImagesTask();
            refreshImagesTask.setListener(() -> {
                List<Instrument> instruments = Instrument.getAllProjectInstruments(
                        getProjectId());
                for (int k = 0; k < instruments.size(); k++) {
                    InstrumentSanitizerTask sanitizerTask = new InstrumentSanitizerTask();
                    sanitizerTask.setListener(last -> {
                        if (last) {
                            AppUtil.getAdminSettingsInstance().setLastSyncTime(
                                    ActiveRecordCloudSync.getLastSyncTime());
                            finishProgressDialog();
                        }
                    });
                    sanitizerTask.execute(instruments.get(k), (k == instruments.size() - 1));
                }
                if (instruments.size() == 0) {
                    finishProgressDialog();
                }
            });
            refreshImagesTask.execute();
        });
        asyncTask.execute();
    }

    private void finishProgressDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (getActivity() != null && !getActivity().isDestroyed()) dismissProgressDialog();
        } else {
            dismissProgressDialog();
        }
    }

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setTitle(getResources().getString(
                R.string.instrument_loading_progress_header));
        mProgressDialog.setMessage(getResources().getString(
                R.string.background_process_progress_message));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && getActivity() != null && !getActivity().isFinishing() && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        refreshInstrumentsView();
    }

    private void refreshInstrumentsView() {
        startActivity(new Intent(getContext(), InstrumentActivity.class));
        if (getActivity() != null) getActivity().finish();

    }

    private void displayProjectName() {
        if (getActivity() == null) return;

        Project project = Project.findByRemoteId(getProjectId());
        TextView textView = getActivity().findViewById(R.id.projectName);
        if (project != null && textView != null) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(project.getName());
        }
    }

    private static class RefreshInstrumentsTask extends AsyncTask<Void, Void, Integer> {
        private RefreshInstrumentsTask.AsyncTaskListener mListener;

        public interface AsyncTaskListener {
            void onAsyncTaskFinished();
        }

        void setListener(RefreshInstrumentsTask.AsyncTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (NotificationUtils.checkForNetworkErrors(AppUtil.getContext())) {
                List<Instrument> instruments = Instrument.getAllProjectInstruments(getProjectId());
                for (int k = 0; k < instruments.size(); k++) {
                    if (!instruments.get(k).loaded()) {
                        AdminSettings.getInstance().resetLastSyncTime();
                        break;
                    }
                }
                ActiveRecordCloudSync.syncReceiveTables(AppUtil.getContext());
                return 0;
            } else {
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            mListener.onAsyncTaskFinished();
        }
    }

    private static class RefreshImagesTask extends AsyncTask<Void, Void, Void> {
        private final static String TAG = "ImageDownloader";

        private RefreshImagesTask.AsyncTaskListener mListener;

        public interface AsyncTaskListener {
            void onAsyncTaskFinished();
        }

        void setListener(RefreshImagesTask.AsyncTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            if (NotificationUtils.checkForNetworkErrors(AppUtil.getContext())) {
                ActiveRecordCloudSync.setAccessToken(AppUtil.getAdminSettingsInstance().getApiKey());
                ActiveRecordCloudSync.setVersionCode(AppUtil.getVersionCode(AppUtil.getContext()));
                ActiveRecordCloudSync.downloadNotification(AppUtil.getContext(),
                        android.R.drawable.stat_sys_download, R.string.sync_notification_text);
                for (Image image : Image.getAll()) {
                    HttpUtil.getFile(image);
                }
                ActiveRecordCloudSync.downloadNotification(AppUtil.getContext(),
                        android.R.drawable.stat_sys_download_done, R.string.sync_notification_complete_text);

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            mListener.onAsyncTaskFinished();
        }
    }

    private static class InstrumentSanitizerTask extends AsyncTask<Object, Void, Boolean> {
        private InstrumentSanitizerTask.AsyncTaskListener mListener;

        public interface AsyncTaskListener {
            void onAsyncTaskFinished(Boolean last);
        }

        void setListener(InstrumentSanitizerTask.AsyncTaskListener listener) {
            this.mListener = listener;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            ((Instrument) params[0]).setLoops();
            return ((Boolean) params[1]);
        }

        @Override
        protected void onPostExecute(Boolean last) {
            super.onPostExecute(last);
            mListener.onAsyncTaskFinished(last);
        }
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
                    if (mInstrument == null || !mInstrument.loaded() || mInstrument.questions().size() == 0) {
                        Toast.makeText(getActivity(), R.string.instrument_not_loaded, Toast.LENGTH_LONG).show();
                    } else {
                        new RuleBuilder(getActivity())
                                .addRule(new InstrumentLaunchRule(mInstrument,
                                        getActivity().getString(R.string.rule_failure_instrument_launch)))
                                .showToastOnFailure(true)
                                .setCallbacks(new RuleCallback() {
                                    public void onRulesPass() {
                                        Intent i = new Intent(getActivity(), SurveyActivity.class);
                                        i.putExtra(SurveyFragment.EXTRA_INSTRUMENT_ID, mInstrument.getRemoteId());
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
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
            SpannableString spannableText = new SpannableString(title + questionCount + version);
            // Title styling
            spannableText.setSpan(new RelativeSizeSpan(1.2f), 0, title.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(getResources().getColor(
                    R.color.primary_text)), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Question count styling
            spannableText.setSpan(new RelativeSizeSpan(0.8f), title.length(), title.length() +
                    questionCount.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(getResources().getColor(
                    R.color.secondary_text)), title.length(),
                    title.length() + questionCount.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
                    title.length(), title.length() + questionCount.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Version styling
            int end = title.length() + questionCount.length() +
                    version.length();
            spannableText.setSpan(new RelativeSizeSpan(0.8f), title.length() +
                            questionCount.length(), end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color
                    .secondary_text)), title.length() + questionCount.length(), end, 0);
            spannableText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                    title.length() + questionCount.length(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            instrumentPropertiesTextView.setText(spannableText);
            new SetInstrumentLabelTask(InstrumentViewPagerFragment.this).execute(
                    new InstrumentListLabel(instrument, instrumentPropertiesTextView));
        }
    }

}