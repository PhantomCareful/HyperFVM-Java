package com.careful.HyperFVM.ui.DataStation;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.careful.HyperFVM.MainActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataStationBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.ExecuteDailyTasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class DataStationFragment extends Fragment {
    private FragmentDataStationBinding binding;
    private DBHelper dbHelper;
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "app_preferences";
    private static final String FIRST_RUN_KEY = "first_run";
    private static final String CURRENT_FRAGMENT_INDEX_KEY = "current_fragment_index";

    private int currentFragmentIndex = 0; // 0: CardDataIndex, 1: Auxiliary, 2: Images
    private final List<Fragment> fragments = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataStationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 初始化视图和工具类
        initViews();
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

    private void initViews() {

        // 初始化Fragment列表
        fragments.add(new CardDataIndexFragment());
        fragments.add(new CardDataAuxiliaryListFragment());
        fragments.add(new DataImagesIndexFragment());

        // 恢复保存的索引：优先从savedInstanceState获取，否则默认0
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentFragmentIndex = preferences.getInt(CURRENT_FRAGMENT_INDEX_KEY, 0);
        switch (currentFragmentIndex) {
            case 0:
                setTopAppBarTitle("防御卡目录\uD83E\uDD0F");
                break;
            case 1:
                setTopAppBarTitle("增幅卡名单\uD83E\uDD0F");
                break;
            case 2:
                setTopAppBarTitle("数据图合集\uD83E\uDD0F");
                break;
        }

        // 显示对应的子Fragment（无论是否是首次加载，都根据当前索引显示）
        replaceChildFragment(fragments.get(currentFragmentIndex));

    }

    // 添加切换到下一个Fragment的方法
    public void switchToNextFragment() {
        currentFragmentIndex = (currentFragmentIndex + 1) % fragments.size();
        Log.d("currentFragmentIndex", String.valueOf(currentFragmentIndex));
        switch (currentFragmentIndex) {
            case 0:
                setTopAppBarTitle("防御卡目录\uD83E\uDD0F");
                break;
            case 1:
                setTopAppBarTitle("增幅卡名单\uD83E\uDD0F");
                break;
            case 2:
                setTopAppBarTitle("数据图合集\uD83E\uDD0F");
                break;
        }
        // 保存当前页面序号
        preferences.edit().putInt(CURRENT_FRAGMENT_INDEX_KEY, currentFragmentIndex).apply();
        replaceChildFragment(fragments.get(currentFragmentIndex));
    }

    /**
     * 替换子Fragment容器中的内容（核心方法）
     * 注意：在Fragment中管理子Fragment必须用getChildFragmentManager()
     */
    private void replaceChildFragment(Fragment childFragment) {
        // 获取子Fragment管理器（因为是在DataStationFragment内部切换子Fragment）
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        // 设置动画：参数1=新Fragment进入动画，参数2=旧Fragment退出动画
        transaction.setCustomAnimations(
                R.anim.slide_in_right,  // 你的进入动画（例如slide_in_right）
                R.anim.slide_out_left    // 你的退出动画（例如slide_out_left）
        );

        // 执行替换操作（容器ID替换为你实际的子Fragment容器ID）
        transaction.replace(R.id.fragment_container, childFragment);

        // 提交事务
        transaction.commit();
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
                .setMessage("请先阅读概览页面上的内容，以便更好地使用本应用。")
                .setPositiveButton("去阅读", (dialog, which) -> navigateToOverview())
                .setNegativeButton("稍后再说", null)
                .setCancelable(false)
                .show();
    }

    private void navigateToOverview() {
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.navigation_data_station, true)
                .setEnterAnim(R.anim.slide_in_left)
                .setExitAnim(R.anim.slide_out_right)
                .build();
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.navigation_overview, null, navOptions);

        // 关键：跳转后同步更新底部导航栏选中状态
        if (getActivity() instanceof MainActivity) {
            // 传入overview对应的导航ID，强制更新选中状态
            ((MainActivity) getActivity()).updateNavigationSelection(R.id.navigation_overview);
        }
    }

    private void setTopAppBarTitle(String title) {
        Log.d("currentFragmentIndex", title);
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
