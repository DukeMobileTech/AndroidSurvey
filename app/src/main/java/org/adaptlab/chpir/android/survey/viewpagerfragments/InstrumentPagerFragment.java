package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.SettingsActivity;
import org.adaptlab.chpir.android.survey.adapters.InstrumentAdapter;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.entities.Settings;
import org.adaptlab.chpir.android.survey.utils.AppUtil;
import org.adaptlab.chpir.android.survey.viewmodelfactories.ProjectInstrumentViewModelFactory;
import org.adaptlab.chpir.android.survey.viewmodels.ProjectInstrumentViewModel;
import org.adaptlab.chpir.android.survey.viewmodels.SettingsViewModel;

import java.util.List;

public class InstrumentPagerFragment extends Fragment {
    private static final String TAG = "InstrumentViewPagerFrag";

    private InstrumentAdapter mInstrumentAdapter;
    private MenuItem mActionProgressItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void setSettingsViewModel() {
        SettingsViewModel settingsViewModel = ViewModelProviders.of(getActivity()).get(SettingsViewModel.class);
        settingsViewModel.getSettings().observe(this, new Observer<Settings>() {
            @Override
            public void onChanged(@Nullable Settings settings) {
                if (settings != null && settings.getProjectId() != null) {
                    setInstrumentsViewModel(Long.valueOf(settings.getProjectId()));
                    hideProgressBar(); // hidden when last sync time is saved after a refresh
                }
            }
        });
    }

    private void setInstrumentsViewModel(long projectId) {
        ProjectInstrumentViewModelFactory factory = new ProjectInstrumentViewModelFactory(getActivity().getApplication(), projectId);
        ProjectInstrumentViewModel viewModel = ViewModelProviders.of(getActivity(), factory).get(ProjectInstrumentViewModel.class);
        viewModel.getInstruments().observe(this, new Observer<List<Instrument>>() {
            @Override
            public void onChanged(@Nullable final List<Instrument> instruments) {
                mInstrumentAdapter.setInstruments(instruments);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view_instrument, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        mInstrumentAdapter = new InstrumentAdapter(this.getContext());
        recyclerView.setAdapter(mInstrumentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
        recyclerView.addItemDecoration(dividerItemDecoration);

        setSettingsViewModel();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_instrument, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mActionProgressItem = menu.findItem(R.id.menu_item_progress_action);
        menu.findItem(R.id.menu_item_submit_all).setEnabled(false).setVisible(false);
        if (getResources().getBoolean(R.bool.default_hide_admin_button)) {
            menu.findItem(R.id.menu_item_settings).setEnabled(false).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                Intent i = new Intent(getActivity(), SettingsActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                } else {
                    startActivity(i);
                }
                return true;
            case R.id.menu_item_refresh:
                showProgressBar();
                AppUtil.downloadData(getActivity().getApplication());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showProgressBar() {
        mActionProgressItem.setVisible(true);
    }

    private void hideProgressBar() {
        if (mActionProgressItem != null)
            mActionProgressItem.setVisible(false);
    }

}