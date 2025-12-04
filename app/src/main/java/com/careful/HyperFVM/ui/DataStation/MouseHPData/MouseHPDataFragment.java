package com.careful.HyperFVM.ui.DataStation.MouseHPData;

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

public class MouseHPDataFragment extends Fragment {

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initTabLayoutFragments(TabLayoutFragmentStateAdapter adapter) {
        //添加Fragment和对应的标题，按标签顺序
        adapter.addFragment(new MeiweiHuoshanFukongFragment(), getResources().getString(R.string.mouse_meiwei_huoshan_fukong));
        adapter.addFragment(new XingjiHaidiJingjiFragment(), getResources().getString(R.string.mouse_xingji_haidi_jingji));
        adapter.addFragment(new FanwaiFragment(), getResources().getString(R.string.mouse_fanwai));
        adapter.addFragment(new CrossServerFragment(), getResources().getString(R.string.mouse_cross_server));
        adapter.addFragment(new MotaFragment(), getResources().getString(R.string.mouse_mota));
        adapter.addFragment(new MotaBossFragment(), getResources().getString(R.string.mouse_mota_boss));
        adapter.addFragment(new WorldBossFragment(), getResources().getString(R.string.mouse_world_boss));
        adapter.addFragment(new CrossServerBossFragment(), getResources().getString(R.string.mouse_cross_server_boss));
        adapter.addFragment(new SpecialFragment(), getResources().getString(R.string.mouse_special));
    }
}