package com.careful.HyperFVM.Fragments.DataStation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.careful.HyperFVM.Activities.NecessaryThings.UsingInstructionActivity;
import com.careful.HyperFVM.MainActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataStationBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.ExecuteDailyTasks;
import com.careful.HyperFVM.utils.OtherUtils.TabLayoutFragmentStateAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DataStationFragment extends Fragment {
    private FragmentDataStationBinding binding;
    private DBHelper dbHelper;
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "app_preferences";
    private static final String FIRST_RUN_KEY = "first_run";

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;

    private TabLayoutFragmentStateAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataStationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setTopAppBarTitle(getResources().getString(R.string.label_data_station));

        dbHelper = new DBHelper(requireContext());

        viewPager2 = root.findViewById(R.id.View_Page2);
        adapter = new TabLayoutFragmentStateAdapter(this);
        initTabLayoutFragments(adapter);
        viewPager2.setAdapter(adapter);
        viewPager2.setUserInputEnabled(false);
        root.postDelayed(() -> viewPager2.setOffscreenPageLimit(2), 500);
        bindTabLayoutWithViewPager2();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkFirstRun();
        // 没有启用自动任务的话，才在这里执行每日任务
        if (!dbHelper.getSettingValue("自动任务")) {
            ExecuteDailyTasks executeDailyTasks = new ExecuteDailyTasks(requireContext());
            executeDailyTasks.executeDailyTasks();
        }
    }

    private void bindTabLayoutWithViewPager2() {
        // 1. 获取MainActivity中的TabLayout实例
        Activity activity = requireActivity();
        if (activity instanceof MainActivity) {
            tabLayout = activity.findViewById(R.id.Tab_Layout);
            if (tabLayout != null) {
                // 2. 通过TabLayoutMediator绑定
                new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> tab.setText(adapter.getPageTitle(position))).attach();
            }
        }
    }

    private void initTabLayoutFragments(TabLayoutFragmentStateAdapter adapter) {
        //添加Fragment和对应的标题，按标签顺序
        adapter.addFragment(new CardDataIndexFragment(), getResources().getString(R.string.card_data_index));
        adapter.addFragment(new CardDataAuxiliaryListFragment(), getResources().getString(R.string.card_data_auxiliary_list));
        adapter.addFragment(new DataImagesIndexFragment(), getResources().getString(R.string.data_images_index));
    }

    private void checkFirstRun() {
        if (preferences.getBoolean(FIRST_RUN_KEY, true)) {
            showWelcomeDialog();
            preferences.edit().putBoolean(FIRST_RUN_KEY, false).apply();
        }
    }

    private void showWelcomeDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("欢迎使用 HyperFVM")
                .setMessage("如果您是第一次使用，建议您先阅读使用说明，以快速了解本App。")
                .setPositiveButton("去阅读👉", (dialog, which) -> {
                    Intent intent = new Intent(requireActivity(), UsingInstructionActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("我是老手\uD83D\uDE0E", null)
                .setCancelable(false)
                .show();
    }

    private void setTopAppBarTitle(String title) {
        //设置顶栏标题
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
