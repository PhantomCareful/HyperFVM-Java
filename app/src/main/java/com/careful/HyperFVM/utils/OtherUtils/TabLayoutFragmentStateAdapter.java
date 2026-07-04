package com.careful.HyperFVM.utils.OtherUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class TabLayoutFragmentStateAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> titleList = new ArrayList<>();

    //针对Activity建立顶栏分类
    public TabLayoutFragmentStateAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    //添加Fragment和对应的标题
    public void addFragment(Fragment fragment, String title) {
        fragmentList.add(fragment);
        titleList.add(title);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    @Nullable
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }
}
