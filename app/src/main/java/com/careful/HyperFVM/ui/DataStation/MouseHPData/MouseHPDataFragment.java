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
        adapter.addFragment(new MeiweiHuoshanFukongFragment(), getResources().getString(R.string.text_data_images_index_mouse_hp_1));
        adapter.addFragment(new XingjiHaidiJingjiFragment(), getResources().getString(R.string.text_data_images_index_mouse_hp_2));
        adapter.addFragment(new FanwaiFragment(), getResources().getString(R.string.text_data_images_index_mouse_hp_3));
        adapter.addFragment(new CrossServerFragment(), getResources().getString(R.string.text_data_images_index_mouse_hp_4));
        adapter.addFragment(new MotaFragment(), getResources().getString(R.string.text_data_images_index_mouse_hp_5));
        adapter.addFragment(new MotaBossFragment(), getResources().getString(R.string.text_data_images_index_mouse_hp_8));
        adapter.addFragment(new WorldBossFragment(), getResources().getString(R.string.text_data_images_index_mouse_hp_6));
        adapter.addFragment(new CrossServerBossFragment(), getResources().getString(R.string.text_data_images_index_mouse_hp_7));
        adapter.addFragment(new SpecialFragment(), getResources().getString(R.string.text_data_images_index_mouse_hp_10));
    }
}