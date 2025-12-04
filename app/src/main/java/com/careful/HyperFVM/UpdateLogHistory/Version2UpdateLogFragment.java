package com.careful.HyperFVM.UpdateLogHistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.databinding.FragmentVersion2UpdateLogBinding;

public class Version2UpdateLogFragment extends Fragment {

    private FragmentVersion2UpdateLogBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //初始化binding
        binding = FragmentVersion2UpdateLogBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
}