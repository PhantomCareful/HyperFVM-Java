package com.careful.HyperFVM.Activities.Oobe;

import androidx.fragment.app.Fragment;

/**
 * OOBE 主题设置页（对齐 HC {@code BasicSettingsActivity} 的页面壳）。
 * 内部组件由 {@link OobeThemeFragment} 承载，"下一步"经
 * {@link OobePageActivity#goNextStep()} 栈式启动设置完毕页。
 */
public class OobeThemeActivity extends OobePageActivity {

    @Override
    protected Fragment getFragment() {
        return new OobeThemeFragment();
    }

    @Override
    protected String getFragmentTag() {
        return OobeThemeFragment.class.getSimpleName();
    }

    @Override
    protected Class<? extends OobePageActivity> getNextPage() {
        return OobeDoneActivity.class;
    }
}
