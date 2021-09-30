package fi.neskola.kartta.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import fi.neskola.kartta.R;
import fi.neskola.kartta.application.KarttaApplication;
import fi.neskola.kartta.models.IRecord;
import fi.neskola.kartta.ui.activities.MapsActivity;
import fi.neskola.kartta.ui.views.RecordListRecyclerViewAdapter;
import fi.neskola.kartta.viewmodels.RecordListViewModel;
import fi.neskola.kartta.viewmodels.ViewEvent;

public class RecordListFragment extends Fragment implements RecordListRecyclerViewAdapter.ViewHolderClickListener {

    RecordListViewModel viewModel;

    private RecyclerView recyclerView;
    private RecordListRecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<IRecord> recordArrayList = new ArrayList<>();

    private TextView textView_noRecords;

    @Override
    public void onAttach(Context context) {
        viewModel = ((MapsActivity) context).viewModelProvider.get(RecordListViewModel.class);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_main);
        textView_noRecords = view.findViewById(R.id.textView_noRecords);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerViewAdapter = new RecordListRecyclerViewAdapter(view.getContext(), recordArrayList, this);
        recyclerView.setAdapter(recyclerViewAdapter);

        viewModel.getRecordListObservable().observe( getViewLifecycleOwner(), recordList -> {
            recordArrayList.clear();
            if (recordList == null || recordList.size() < 1) {
                textView_noRecords.setVisibility(View.VISIBLE);
            } else {
                textView_noRecords.setVisibility(View.GONE);
                recordArrayList.addAll(recordList);
            }
            recyclerViewAdapter.notifyDataSetChanged();
        });

        viewModel.getViewEventObservable().observe( getViewLifecycleOwner(), eventWrapper -> {
            ViewEvent viewEvent = eventWrapper.getContentIfNotHandled();
            if (viewEvent == null)
                return;
            if (viewEvent.event == ViewEvent.Event.REQUEST_REMOVE) {
                createRemoveDialog(viewEvent.record);
            }
        });

        return view;
    }

    @Override
    public void onListItemClicked(IRecord record) {
        viewModel.onListItemClicked(record);
    }

    private void createRemoveDialog(IRecord record) {
        if (getContext() == null)
            return;
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    viewModel.onRemoveRecord(record);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(String.format("Remove %s?", record.getName()))
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

}