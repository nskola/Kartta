package fi.neskola.kartta.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import fi.neskola.kartta.R;


public class MarkPointBottomSheetFragment extends BottomSheetDialogFragment {

    public static MarkPointBottomSheetFragment newInstance() {
        return new MarkPointBottomSheetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_layout, container,
                false);
        return view;

    }
}
