package com.careful.HyperFVM.ui.DataStation;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataStationBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.ExecuteDailyTasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DataStationFragment extends Fragment {
    private FragmentDataStationBinding binding;
    private MaterialButtonToggleGroup toggleGroup;
    private FragmentManager childFragmentManager;
    private DBHelper dbHelper;
    private static final String PREFS_NAME = "app_preferences";
    private static final String FIRST_RUN_KEY = "first_run";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataStationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 初始化视图和工具类
        initViews(root, savedInstanceState);
        dbHelper = new DBHelper(requireContext());

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

    private void initViews(View root, Bundle savedInstanceState) {
        setTopAppBarTitle(getString(R.string.label_data_station));

        // 初始化 ToggleGroup
        MaterialButtonToggleGroup toggleGroup = root.findViewById(R.id.ToggleButtonGroup);
        // 默认选中第一个按钮，并显示对应的 Fragment
        if (savedInstanceState == null) { // 避免重建时重复加载
            toggleGroup.check(R.id.ToggleButton_CardDataIndex);
            replaceChildFragment(new CardDataIndexFragment());
        }

        // 监听ToggleGroup的选中事件
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) { // 只处理“选中”状态（过滤取消选中的回调）
                Fragment targetFragment;
                // 根据选中的按钮ID匹配子Fragment
                if (checkedId == R.id.ToggleButton_CardDataIndex) {
                    targetFragment = new CardDataIndexFragment();
                } else if (checkedId == R.id.ToggleButton_CardDataAuxiliaryList) {
                    targetFragment = new CardDataAuxiliaryListFragment();
                } else if (checkedId == R.id.ToggleButton_DataImagesIndex) {
                    targetFragment = new DataImagesIndexFragment();
                } else {
                    targetFragment = null;
                }
                // 切换子Fragment
                if (targetFragment != null) {
                    replaceChildFragment(targetFragment);
                }
            }
        });
    }

    /**
     * 替换子Fragment容器中的内容（核心方法）
     * 注意：在Fragment中管理子Fragment必须用getChildFragmentManager()
     */
    private void replaceChildFragment(Fragment childFragment) {
        // 获取父Fragment的子Fragment管理器（必须用getChildFragmentManager，而非getSupportFragmentManager）
        childFragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = childFragmentManager.beginTransaction();
        // 替换容器中的子Fragment（容器ID为fragment_container）
        transaction.replace(R.id.fragment_container, childFragment);
        // 可选：添加到回退栈，支持返回键返回上一个子Fragment
        // transaction.addToBackStack(null);
        transaction.commit();
    }

    private void checkFirstRun() {
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (preferences.getBoolean(FIRST_RUN_KEY, true)) {
            showWelcomeDialog();
            preferences.edit().putBoolean(FIRST_RUN_KEY, false).apply();
        }
    }

    private void showWelcomeDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("欢迎使用 HyperFVM")
                .setMessage("请先阅读概览页面上的内容，以便更好地使用本应用。")
                .setPositiveButton("去阅读", (dialog, which) -> navigateToOverview())
                .setNegativeButton("稍后再说", null)
                .setCancelable(false)
                .show();
    }

    private void navigateToOverview() {
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.navigation_data_station, true)
                .setEnterAnim(android.R.anim.fade_in)
                .setExitAnim(android.R.anim.fade_out)
                .build();
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.navigation_overview, null, navOptions);
    }

    private void setTopAppBarTitle(String title) {
        Activity activity = getActivity();
        if (activity != null) {
            MaterialToolbar toolbar = activity.findViewById(R.id.Top_AppBar);
            toolbar.setTitle(title);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 移除监听器，避免内存泄漏（Fragment视图销毁时清理）
        if (toggleGroup != null) {
            toggleGroup.removeOnButtonCheckedListener(null);
            toggleGroup = null;
        }
        childFragmentManager = null;
        binding = null;
    }
}
