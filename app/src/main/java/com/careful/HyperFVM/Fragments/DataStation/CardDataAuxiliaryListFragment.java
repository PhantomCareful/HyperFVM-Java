package com.careful.HyperFVM.Fragments.DataStation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.databinding.FragmentDataStationCardDataAuxiliaryListBinding;

public class CardDataAuxiliaryListFragment extends Fragment {

    private FragmentDataStationCardDataAuxiliaryListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataStationCardDataAuxiliaryListBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}