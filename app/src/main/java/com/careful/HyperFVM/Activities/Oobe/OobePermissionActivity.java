package com.careful.HyperFVM.Activities.Oobe;

import androidx.fragment.app.Fragment;

/**
 * OOBE 权限申请页（对齐 HC {@code TermsAndStatementActivity} 的页面壳）。
 * 内部组件由 {@link OobePermissionFragment} 承载，"下一步"经
 * {@link OobePageActivity#goNextStep()} 栈式启动主题设置页。
 */
public class OobePermissionActivity extends OobePageActivity {

    @Override
    protected Fragment getFragment() {
        return new OobePermissionFragment();
    }

    @Override
    protected String getFragmentTag() {
        return OobePermissionFragment.class.getSimpleName();
    }

    @Override
    protected Class<? extends OobePageActivity> getNextPage() {
        return OobeThemeActivity.class;
    }
}
