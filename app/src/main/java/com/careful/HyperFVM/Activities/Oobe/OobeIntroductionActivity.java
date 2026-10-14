package com.careful.HyperFVM.Activities.Oobe;

import androidx.fragment.app.Fragment;

/**
 * OOBE 软件介绍页（对齐 HC {@code PermissionSettingsActivity} 的页面壳）。
 * 内部组件由 {@link OobeIntroductionFragment} 承载，"下一步"经
 * {@link OobePageActivity#goNextStep()} 栈式启动权限申请页。
 */
public class OobeIntroductionActivity extends OobePageActivity {

    @Override
    protected Fragment getFragment() {
        return new OobeIntroductionFragment();
    }

    @Override
    protected String getFragmentTag() {
        return OobeIntroductionFragment.class.getSimpleName();
    }

    @Override
    protected Class<? extends OobePageActivity> getNextPage() {
        return OobePermissionActivity.class;
    }
}
