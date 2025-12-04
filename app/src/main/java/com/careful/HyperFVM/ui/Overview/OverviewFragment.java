package com.careful.HyperFVM.ui.Overview;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentOverviewBinding;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Objects;

public class OverviewFragment extends Fragment {

    private FragmentOverviewBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOverviewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setTopAppBarTitle(getResources().getString(R.string.label_overview));

        return root;
    }

    private void setTopAppBarTitle(String title) {
        //设置标题
        Activity activity = getActivity();
        if (activity != null) {
            MaterialToolbar toolbar = activity.findViewById(R.id.Top_AppBar);
            toolbar.setTitle(title);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}