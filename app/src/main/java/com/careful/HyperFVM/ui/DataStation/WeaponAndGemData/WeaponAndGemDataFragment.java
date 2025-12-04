package com.careful.HyperFVM.ui.DataStation.WeaponAndGemData;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataStationSecondBinding;
import com.careful.HyperFVM.utils.OtherUtils.TabLayoutFragmentStateAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class WeaponAndGemDataFragment extends Fragment {

    private FragmentDataStationSecondBinding binding;

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDataStationSecondBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        tabLayout = root.findViewById(R.id.Tab_Layout);
        viewPager2 = root.findViewById(R.id.View_Page2);

        TabLayoutFragmentStateAdapter adapter = new TabLayoutFragmentStateAdapter(this);
        initTabLayoutFragments(adapter);

        viewPager2.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) ->
                tab.setText(adapter.getPageTitle(position))
        ).attach();

        return root;
    }

    private void initTabLayoutFragments(TabLayoutFragmentStateAdapter adapter) {
        //添加Fragment和对应的标题，按标签顺序
        adapter.addFragment(new WeaponAndGemGoldenComposeFragment(), getResources().getString(R.string.weapon_and_gem_golden_compose));
        adapter.addFragment(new WeaponMainFragment(), getResources().getString(R.string.weapon_main));
        adapter.addFragment(new WeaponSubFragment(), getResources().getString(R.string.weapon_sub));
        adapter.addFragment(new WeaponSuperFragment(), getResources().getString(R.string.weapon_super));
        adapter.addFragment(new WeaponAppearanceFragment(), getResources().getString(R.string.weapon_appearance));
        adapter.addFragment(new GemDecomposeFragment(), getResources().getString(R.string.gem_decompose));
    }
}