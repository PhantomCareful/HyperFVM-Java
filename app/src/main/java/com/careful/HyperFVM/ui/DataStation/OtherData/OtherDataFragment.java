package com.careful.HyperFVM.ui.DataStation.OtherData;

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

public class OtherDataFragment extends Fragment {

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
        adapter.addFragment(new BadgeFragment(), getResources().getString(R.string.text_data_images_index_others_1));
        adapter.addFragment(new VIPFragment(), getResources().getString(R.string.text_data_images_index_others_2));
        adapter.addFragment(new LuckyFragment(), getResources().getString(R.string.text_data_images_index_others_3));
        adapter.addFragment(new MillionaireFragment(), getResources().getString(R.string.text_data_images_index_others_4));
        adapter.addFragment(new BetFragment(), getResources().getString(R.string.text_data_images_index_others_5));
        adapter.addFragment(new PetFragment(), getResources().getString(R.string.text_data_images_index_others_6));
        adapter.addFragment(new CryStoneFragment(), getResources().getString(R.string.text_data_images_index_card_17));
        adapter.addFragment(new CookeryFragment(), getResources().getString(R.string.text_data_images_index_card_18));
        adapter.addFragment(new WorldBossLevelFragment(), getResources().getString(R.string.text_data_images_index_others_7));
    }
}