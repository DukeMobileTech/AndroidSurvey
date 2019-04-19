package org.adaptlab.chpir.android.survey.viewpagerfragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.adaptlab.chpir.android.survey.R;
import org.adaptlab.chpir.android.survey.adapters.InstrumentAdapter;
import org.adaptlab.chpir.android.survey.entities.Instrument;
import org.adaptlab.chpir.android.survey.viewmodels.InstrumentViewModel;

import java.util.List;

public class InstrumentViewPagerFragment extends Fragment {
    private static final String TAG = "InstrumentViewPagerFrag";

    private InstrumentAdapter mInstrumentAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        mInstrumentAdapter = new InstrumentAdapter(this.getContext());
        recyclerView.setAdapter(mInstrumentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.border));
        recyclerView.addItemDecoration(dividerItemDecoration);

        if (this.getActivity() == null) return view;
        InstrumentViewModel mInstrumentViewModel = ViewModelProviders.of(this.getActivity()).get(InstrumentViewModel.class);
        mInstrumentViewModel.getAllInstruments().observe(this, new Observer<List<Instrument>>() {
            @Override
            public void onChanged(@Nullable final List<Instrument> instruments) {
                mInstrumentAdapter.setInstruments(instruments);
            }
        });
        return view;
    }

}