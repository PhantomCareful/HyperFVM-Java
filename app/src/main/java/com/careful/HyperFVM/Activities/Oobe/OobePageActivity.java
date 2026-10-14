package com.careful.HyperFVM.Activities.Oobe;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;

/**
 * OOBE 子页面 Activity 基类（对齐 HC provision {@code BaseActivity}/{@code ProvisionDetailActivity}）。
 *
 * <p>多 Activity 架构下每页是独立窗口，以标准栈式推进：前进为普通 intent 启动下一页
 * （不 finish 自己，留在栈内作为返回目标），返回交系统默认 finish（栈内上一页实例自然浮现）。
 * 转场统一为系统默认动画，方向天然正确（前进=打开、后退=返回），且不拦截返回键，
 * 跨页支持预见式返回动画（predictive back）。
 *
 * <ul>
 *   <li>下一步 → {@link #goNextStep()}：栈式启动 {@link #getNextPage()}
 *       （HC 页面 Activity {@code onNextAminStart} 同责）</li>
 *   <li>返回 → 系统默认 back：本页退栈，无需 setResult 结果码协议</li>
 *   <li>窗口焦点转发 Fragment（HC {@code ProvisionDetailActivity#onWindowFocusChanged} 同责）</li>
 * </ul>
 */
public abstract class OobePageActivity extends BaseActivity implements IOnFocusListener {

    protected Fragment mFragment;

    // "下一步"点击节流：栈式前进不 finish 本页，防快速双击叠出两个新页实例
    private long mLastNextClickTime = 0;

    protected abstract Fragment getFragment();

    protected abstract String getFragmentTag();

    /**
     * 下一页页面类（页面链终点返回 null，如完毕页——点"开始"直接进 MainActivity，
     * 无中间页式"下一步"）。
     */
    protected Class<? extends OobePageActivity> getNextPage() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 浅色内容页：按设置应用主题模式与深浅色（须在 super.onCreate 前，与编排器同套）
        DarkModeManager.applyDarkMode();
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        // 全屏沉浸（与 MainActivity 同套：edge-to-edge，页面布局用 InsetsUtil 补系统栏）
        EdgeToEdge.enable(this);

        FragmentManager fm = getSupportFragmentManager();
        mFragment = fm.findFragmentByTag(getFragmentTag());
        if (mFragment == null) {
            mFragment = getFragment();
            fm.beginTransaction()
                    .replace(android.R.id.content, mFragment, getFragmentTag())
                    .commit();
        }

        // 不注册返回拦截：back 交系统默认处理——本页 finish，栈内上一页浮出，
        // 系统返回动画与预见式返回动画（predictive back）随即可用
    }

    /**
     * "下一步"：栈式启动下一页（不 finish 自己，本页留栈作为返回目标）。
     */
    public void goNextStep() {
        Class<? extends OobePageActivity> next = getNextPage();
        if (next == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - mLastNextClickTime < 500L) {
            return; // 防快速双击叠出两个新页实例
        }
        mLastNextClickTime = now;
        startActivity(new Intent(this, next));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mFragment instanceof IOnFocusListener listener) {
            listener.onWindowFocusChanged(hasFocus);
        }
    }
}
