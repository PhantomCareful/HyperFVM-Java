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
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

// 导入MainActivity用于跳转
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataStationBinding;
import com.careful.HyperFVM.ui.DataStation.CardData.CardDataFragment;
import com.careful.HyperFVM.ui.DataStation.DecomposeAndExchangeData.DecomposeAndExchangeDataFragment;
import com.careful.HyperFVM.ui.DataStation.MouseHPData.MouseHPDataFragment;
import com.careful.HyperFVM.ui.DataStation.OtherData.OtherDataFragment;
import com.careful.HyperFVM.ui.DataStation.WeaponAndGemData.WeaponAndGemDataFragment;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.ExecuteDailyTasks;
import com.careful.HyperFVM.utils.OtherUtils.TabLayoutFragmentStateAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DataStationFragment extends Fragment {
    private FragmentDataStationBinding binding;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private DBHelper dbHelper;
    private static final String PREFS_NAME = "app_preferences";
    private static final String FIRST_RUN_KEY = "first_run";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataStationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
        setTopAppBarTitle(getString(R.string.label_data_station));
        tabLayout = binding.TabLayout;
        viewPager2 = binding.ViewPage2;
        TabLayoutFragmentStateAdapter adapter = new TabLayoutFragmentStateAdapter(this);
        initTabLayoutFragments(adapter);
        viewPager2.setAdapter(adapter);
        viewPager2.setUserInputEnabled(false);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) ->
                tab.setText(adapter.getPageTitle(position))
        ).attach();
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

    private void initTabLayoutFragments(TabLayoutFragmentStateAdapter adapter) {
        adapter.addFragment(new CardDataFragment(), getResources().getString(R.string.label_card_data));
        adapter.addFragment(new WeaponAndGemDataFragment(), getResources().getString(R.string.label_weapon_gem_data));
        adapter.addFragment(new DecomposeAndExchangeDataFragment(), getResources().getString(R.string.label_decompose_exchange_data));
        adapter.addFragment(new MouseHPDataFragment(), getResources().getString(R.string.label_mouse_hp_data));
        adapter.addFragment(new OtherDataFragment(), getResources().getString(R.string.label_other_data));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
