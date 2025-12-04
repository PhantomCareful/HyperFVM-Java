package com.careful.HyperFVM.ui.DataStation.DecomposeAndExchangeData;

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

public class DecomposeAndExchangeDataFragment extends Fragment {

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
        adapter.addFragment(new GoldenCardFragment(), getResources().getString(R.string.golden_card));
        adapter.addFragment(new ConstellationCardFragment(), getResources().getString(R.string.constellation_card));
        adapter.addFragment(new ChickenCardFragment(), getResources().getString(R.string.chicken_card));
        adapter.addFragment(new DogCardFragment(), getResources().getString(R.string.dog_card));
        adapter.addFragment(new PigCardFragment(), getResources().getString(R.string.pig_card));
        adapter.addFragment(new CatCardFragment(), getResources().getString(R.string.cat_card));
        adapter.addFragment(new CattleCardFragment(), getResources().getString(R.string.cattle_card));
        adapter.addFragment(new TigerCardFragment(), getResources().getString(R.string.tiger_card));
        adapter.addFragment(new RabbitCardFragment(), getResources().getString(R.string.rabbit_card));
        adapter.addFragment(new DragonCardFragment(), getResources().getString(R.string.dragon_card));
        adapter.addFragment(new SnakeCardFragment(), getResources().getString(R.string.snake_card));
    }
}