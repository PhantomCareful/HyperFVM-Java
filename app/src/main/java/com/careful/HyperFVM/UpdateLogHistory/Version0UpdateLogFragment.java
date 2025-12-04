package com.careful.HyperFVM.UpdateLogHistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.databinding.FragmentVersion0UpdateLogBinding;

public class Version0UpdateLogFragment extends Fragment {

    private FragmentVersion0UpdateLogBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //初始化binding
        binding = FragmentVersion0UpdateLogBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
}