package com.careful.HyperFVM.Activities.Oobe;

import androidx.fragment.app.Fragment;

/**
 * OOBE 设置完毕页（对齐 HC {@code CongratulationActivity} 的页面壳）。
 * glow/入场/"开始"逻辑由 {@link OobeDoneFragment} 承载；
 * 返回交系统默认 back（finish 本页，栈内主题设置页实例浮现）。
 */
public class OobeDoneActivity extends OobePageActivity {

    @Override
    protected Fragment getFragment() {
        return new OobeDoneFragment();
    }

    @Override
    protected String getFragmentTag() {
        return OobeDoneFragment.class.getSimpleName();
    }
}
