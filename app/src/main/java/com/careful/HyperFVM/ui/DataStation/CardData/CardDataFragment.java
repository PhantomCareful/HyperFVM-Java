package com.careful.HyperFVM.ui.DataStation.CardData;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.OtherUtils.TabLayoutFragmentStateAdapter;
import com.careful.HyperFVM.databinding.FragmentDataStationSecondBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CardDataFragment extends Fragment {

    private FragmentDataStationSecondBinding binding;

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;

    private TabLayoutFragmentStateAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataStationSecondBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        tabLayout = root.findViewById(R.id.Tab_Layout);
        viewPager2 = root.findViewById(R.id.View_Page2);

        adapter = new TabLayoutFragmentStateAdapter(this);
        initTabLayoutFragments(adapter);

        viewPager2.setAdapter(adapter);
        //禁用左右滑动切换页面
        //viewPager2.setUserInputEnabled(false);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) ->
                tab.setText(adapter.getPageTitle(position))
        ).attach();

        // 获取CardDataIndexFragment（CardDataIndexFragment）并设置回调
        CardDataIndexFragment cardDataIndexFragment = (CardDataIndexFragment) adapter.getFragment(0);
        cardDataIndexFragment.setOnImageClickCallback(targetPosition -> {
            // 切换到目标Fragment
            viewPager2.setCurrentItem(targetPosition, false); // 第二个参数true表示平滑滚动
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataBothWayAndThreeShotFragment bothWayAndThreeShotFragment = (CardDataBothWayAndThreeShotFragment) adapter.getFragment(5);
        bothWayAndThreeShotFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataXPultFragment xPultFragment = (CardDataXPultFragment) adapter.getFragment(6);
        xPultFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataStarAndPvpFragment starAndPvpFragment = (CardDataStarAndPvpFragment) adapter.getFragment(7);
        starAndPvpFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataAuxiliaryFragment auxiliaryFragment = (CardDataAuxiliaryFragment) adapter.getFragment(8);
        auxiliaryFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataEnergyFlowerFragment energyFlowerFragment = (CardDataEnergyFlowerFragment) adapter.getFragment(9);
        energyFlowerFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataJellyPuddingAndTrayFragment jellyPuddingAndTrayFragment = (CardDataJellyPuddingAndTrayFragment) adapter.getFragment(10);
        jellyPuddingAndTrayFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataAirForceIFragment airForceIFragment = (CardDataAirForceIFragment) adapter.getFragment(11);
        airForceIFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataEightDirectionsFragment eightDirectionsFragment = (CardDataEightDirectionsFragment) adapter.getFragment(12);
        eightDirectionsFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataFollowFragment followFragment = (CardDataFollowFragment) adapter.getFragment(13);
        followFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataSundryFragment sundryFragment = (CardDataSundryFragment) adapter.getFragment(14);
        sundryFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataStraightShotFragment straightShotFragment = (CardDataStraightShotFragment) adapter.getFragment(15);
        straightShotFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataAshBombFragment ashBombFragment = (CardDataAshBombFragment) adapter.getFragment(16);
        ashBombFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataAshlessBombFragment ashlessBombFragment = (CardDataAshlessBombFragment) adapter.getFragment(17);
        ashlessBombFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataCoolDownAndUpgradeAndDriveFogAndClearObstacleAndSpecialEffectFragment coolDownAndUpgradeAndDriveFogAndClearObstacleAndSpecialEffectFragment = (CardDataCoolDownAndUpgradeAndDriveFogAndClearObstacleAndSpecialEffectFragment) adapter.getFragment(18);
        coolDownAndUpgradeAndDriveFogAndClearObstacleAndSpecialEffectFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataBreadCommonAndBreadMiddleAndGuardProtectorFragment breadCommonAndBreadMiddleAndGuardProtectorFragment = (CardDataBreadCommonAndBreadMiddleAndGuardProtectorFragment) adapter.getFragment(19);
        breadCommonAndBreadMiddleAndGuardProtectorFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        // 设置回调，点击FAB跳转至第0页
        CardDataCardFusionFragment cardFusionFragment = (CardDataCardFusionFragment) adapter.getFragment(20);
        cardFusionFragment.setOnBackToIndexCallback(() -> {
            // 切换ViewPager2到第0页（CardDataIndexFragment）
            viewPager2.setCurrentItem(0, false); // 第二个参数false：无平滑滚动（可改为true）
        });

        return root;
    }

    private void initTabLayoutFragments(TabLayoutFragmentStateAdapter adapter) {
        //添加Fragment和对应的标题，按标签顺序
        adapter.addFragment(new CardDataIndexFragment(), getResources().getString(R.string.card_data_index));
        adapter.addFragment(new CardDataAuxiliaryListFragment(), getResources().getString(R.string.card_data_auxiliary_list));
        adapter.addFragment(new CardGoldenComposeFragment(), getResources().getString(R.string.card_golden_compose));
        adapter.addFragment(new CardDataNewCardDeliveryFragment(), getResources().getString(R.string.new_card_delivery));
        adapter.addFragment(new CardDataNameFragment(), getResources().getString(R.string.card_name));
        adapter.addFragment(new CardDataBothWayAndThreeShotFragment(), getResources().getString(R.string.both_way_and_three_shot));
        adapter.addFragment(new CardDataXPultFragment(), getResources().getString(R.string.x_pult));
        adapter.addFragment(new CardDataStarAndPvpFragment(), getResources().getString(R.string.star_and_pvp));
        adapter.addFragment(new CardDataAuxiliaryFragment(), getResources().getString(R.string.auxiliary));
        adapter.addFragment(new CardDataEnergyFlowerFragment(), getResources().getString(R.string.energy_flower));
        adapter.addFragment(new CardDataJellyPuddingAndTrayFragment(), getResources().getString(R.string.jelly_pudding_and_tray));
        adapter.addFragment(new CardDataAirForceIFragment(), getResources().getString(R.string.air_force_i));
        adapter.addFragment(new CardDataEightDirectionsFragment(), getResources().getString(R.string.eight_directions));
        adapter.addFragment(new CardDataFollowFragment(), getResources().getString(R.string.follow));
        adapter.addFragment(new CardDataSundryFragment(), getResources().getString(R.string.sundry));
        adapter.addFragment(new CardDataStraightShotFragment(), getResources().getString(R.string.straight_shot));
        adapter.addFragment(new CardDataAshBombFragment(), getResources().getString(R.string.ash_bomb));
        adapter.addFragment(new CardDataAshlessBombFragment(), getResources().getString(R.string.ashless_bomb));
        adapter.addFragment(new CardDataCoolDownAndUpgradeAndDriveFogAndClearObstacleAndSpecialEffectFragment(), getResources().getString(R.string.cool_down_and_upgrade_and_drive_fog_and_clear_obstacle_and_special_effect));
        adapter.addFragment(new CardDataBreadCommonAndBreadMiddleAndGuardProtectorFragment(), getResources().getString(R.string.bread_common_and_bread_middle_and_guard_protector));
        adapter.addFragment(new CardDataCardFusionFragment(), getResources().getString(R.string.card_fusion));
    }
}