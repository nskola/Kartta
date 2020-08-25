package fi.neskola.kartta.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import javax.inject.Inject;

import fi.neskola.kartta.R;
import fi.neskola.kartta.application.KarttaApplication;
import fi.neskola.kartta.models.IRecord;
import fi.neskola.kartta.ui.views.RecordListRecyclerViewAdapter;
import fi.neskola.kartta.viewmodels.KarttaViewModel;

public class RecordListFragment extends Fragment {

    RecyclerView recyclerView;
    RecordListRecyclerViewAdapter recyclerViewAdapter;
    ArrayList<IRecord> recordArrayList = new ArrayList<>();

    @Inject
    KarttaViewModel karttaViewModel;

    @Override
    public void onAttach(Context context) {
        //Dagger inject
        ((KarttaApplication) context.getApplicationContext()).getComponent().inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerViewAdapter = new RecordListRecyclerViewAdapter(view.getContext(), recordArrayList);
        recyclerView.setAdapter(recyclerViewAdapter);
        karttaViewModel.getRecordListObservable().observeForever( (recordList) -> {
            recordArrayList.clear();
            recordArrayList.addAll(recordList);
            recyclerViewAdapter.notifyDataSetChanged();
        });
        return view;
    }
}