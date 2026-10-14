package com.careful.HyperFVM.Activities.Oobe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;

/**
 * OOBE 流程入口（对齐 HC provision DefaultActivity 的欢迎页职责，页面推进为标准栈式）。
 *
 * <p>页面链：欢迎（本 Activity 内置 Fragment）→ 软件介绍 → 权限申请 → 主题设置 → 设置完毕，
 * 中间四页各为独立 Activity（{@link OobeIntroductionActivity} 等）以标准栈式推进：前进为普通
 * startActivity（不 finish），返回交系统默认 finish——转场统一为系统默认动画，
 * 方向天然正确，且不拦截返回，跨页支持预见式返回动画（predictive back）。
 *
 * <ul>
 *   <li><b>欢迎→介绍</b>：{@link #goToNextFromButton(View)} 普通 intent 启动介绍页；</li>
 *   <li><b>返回仲裁</b>：欢迎页吞掉 back（禁退，防首启拦截回环；手势为预见预览后回弹）；</li>
 * </ul>
 * first_run 标记在完毕页点"开始"时写入；本页 singleTask：OOBE 期间经桌面再入时
 * onNewIntent 重置回欢迎页（其上页面实例被系统清除）。
 */
public class OobeActivity extends BaseActivity implements IOnFocusListener {

    private static final String WELCOME_TAG = OobeWelcomeFragment.class.getSimpleName();

    // HC StartupFragment 同款 2s 点击节流（防转场期间连点重复启动介绍页）
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 欢迎页为 glow 深色页：按设置应用主题模式与深浅色（须在 super.onCreate 前）
        DarkModeManager.applyDarkMode();
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        // 全屏沉浸（与 MainActivity 同套：edge-to-edge + MIUI 小白条适配）
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_oobe);

        // 欢迎 Fragment 常驻本页（back 回欢迎页时保留原位，HC StartupState 同责）
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(WELCOME_TAG) == null) {
            fm.beginTransaction()
                    .replace(R.id.oobe_fragment_container, new OobeWelcomeFragment(), WELCOME_TAG)
                    .commit();
        }

        // 返回仲裁：欢迎页（栈底）吞掉 back——中途退出 OOBE 会回 MainActivity 被首启拦截再次跳回形成死循环
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 无操作：吞掉返回（手势返回会先播放预见预览再回弹，符合栈底页语义）
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // singleTask 再入（OOBE 期间经桌面再进图标）：其上页面实例已被系统清除，重置回欢迎页
        mLastClickTime = 0;
        FragmentManager fm = getSupportFragmentManager();
        Fragment welcome = fm.findFragmentByTag(WELCOME_TAG);
        FragmentTransaction tx = fm.beginTransaction();
        if (welcome == null) {
            tx.replace(R.id.oobe_fragment_container, new OobeWelcomeFragment(), WELCOME_TAG);
        }
        tx.commitAllowingStateLoss();
    }

    /**
     * 欢迎 → 软件介绍：普通 intent 启动介绍页（系统默认转场）。
     * 欢迎 Fragment 全程保留原位，作为返回的目标页面。
     *
     * @param originView "下一步"按钮（仅作点击目标判空与节流依据）
     */
    public void goToNextFromButton(View originView) {
        long now = System.currentTimeMillis();
        if (originView == null || Math.abs(now - mLastClickTime) < 2000L) {
            return; // HC "click too fast" 同款节流
        }
        mLastClickTime = now;

        startActivity(new Intent(this, OobeIntroductionActivity.class));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.oobe_fragment_container);
        if (current instanceof IOnFocusListener listener) {
            listener.onWindowFocusChanged(hasFocus);
        }
    }
}
