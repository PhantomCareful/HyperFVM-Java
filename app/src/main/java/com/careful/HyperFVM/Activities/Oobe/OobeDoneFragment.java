package com.careful.HyperFVM.Activities.Oobe;

import static com.careful.HyperFVM.utils.ForDesign.Blur.MiuiLogoBlurUtil.BLEND_ALPHA_CLAMP;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.MainActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.MiuiLogoBlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Glow.GlowController;
import com.careful.HyperFVM.utils.ForDesign.Glow.RenderViewLayout;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;

import fan.animation.Folme;
import fan.animation.FolmeEase;
import fan.animation.base.AnimConfig;
import fan.animation.controller.AnimState;
import fan.animation.property.ViewProperty;

/**
 * OOBE 设置完毕页（复刻 HC provision CongratulationFragment 精简版）。
 *
 * <ul>
 *   <li>背景：glow 流光（onViewCreated 即 warmUp+start 提前渲染：Skia 首次编译挪到窗口首帧前、
 *       流光随转场起跑，消除“焦点才启动”造成的进入首帧纯黑；SDK&lt;33 根背景降级 ?attr/colorSurface 纯色）</li>
 *   <li>流光：logo/文字三层（HC Congratulation 原值）、"开始"按钮两层、状态文字两层（mode 19+3）</li>
 *   <li>入场：logo 区 translationY 100→0 + alpha quartOut 1500ms；按钮 alpha delay 1000ms、
 *       2s 后才 enable（HC 防误触）；alpha 终值一律 BLEND_ALPHA_CLAMP 防 OS1.0 blend 跳过</li>
 *   <li>点"开始"：启动 MainActivity（系统默认切换动画，不自定义转场），
 *       并写 first_run=false（流程走完才落标记，中途杀进程重进仍从欢迎页开始）</li>
 * </ul>
 */
public class OobeDoneFragment extends Fragment implements IOnFocusListener {

    // 与宿主同一 SharedPreferences（首启标记在点"开始"时写入）
    private static final String PREFS_NAME = "app_preferences";
    private static final String FIRST_RUN_KEY = "first_run";

    private RenderViewLayout mRenderViewLayout;
    private GlowController mGlowController;
    private View mLogoImageWrapper;
    private View mDoneNext;

    // 入场编排只在首次获得窗口焦点时播一次
    private boolean mEntered = false;
    // "开始"防重复点击
    private boolean mStarting = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oobe_done, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLogoImageWrapper = view.findViewById(R.id.logo_image_wrapper);
        mDoneNext = view.findViewById(R.id.done_next);
        View btnBg = view.findViewById(R.id.btn_bg);
        View logoImage = view.findViewById(R.id.logo_image);
        View textLogo = view.findViewById(R.id.text_logo);
        View stateText = view.findViewById(R.id.done_state_text);
        View contentView = view.findViewById(R.id.content_view);

        // 动态获取导航栏高度（小白条/三键导航）
        InsetsUtil.setNavigationBarHeight(requireContext(), view, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mDoneNext.getLayoutParams();
            params.bottomMargin = height;
            params.topMargin = height;
            params.leftMargin = height;
            params.rightMargin = height;
            mDoneNext.setLayoutParams(params);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // glow 流光：与欢迎页同套降采样容器
            mRenderViewLayout = view.findViewById(R.id.render_view_layout);
            View glowView = new View(requireContext());
            mRenderViewLayout.attachView(glowView, 0.2f, Color.BLACK);
            mGlowController = new GlowController(glowView);
            // 与欢迎页同套预热后立即起跑：Skia 首次编译挪到窗口首帧前；
            // 完毕页无开场光圈（恒亮流光），提前 start 让流光随转场渐显，
            // 消除“等窗口焦点才 start”造成的进入首帧纯黑
            mGlowController.warmUp();
            mGlowController.start(false);
        } else {
            // 降级：RuntimeShader 需 Android 13，根背景改用主题 colorSurface 纯色
            // （解析值跟随当前深浅色，与页内 colorOnSurface 文字形成正确对比）
            TypedValue typedValue = new TypedValue();
            if (requireContext().getTheme().resolveAttribute(
                    com.google.android.material.R.attr.colorSurface, typedValue, true)
                    && typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT
                    && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                view.setBackgroundColor(typedValue.data);
            }
        }

        // 三组流光配色照抄 HC CongratulationFragment#setupBlurBackground：
        // logo 与文字三层（同首屏）、"开始"按钮两层、状态文字两层（基础层 19 + cross 3）；
        // 系统模糊不支持时 applyBlendEffect 内部静默跳过
        MiuiLogoBlurUtil.applyBlendEffect(requireContext(), contentView,
                new int[]{-867546550, -11579569, -15011328}, new int[]{19, 100, 106},
                logoImage, textLogo);
        MiuiLogoBlurUtil.applyBlendEffect(requireContext(), contentView,
                new int[]{-12763843, -15021056}, new int[]{100, 106},
                btnBg);
        MiuiLogoBlurUtil.applyBlendEffect(requireContext(), contentView,
                new int[]{-869915098, -1724697805}, new int[]{19, 3},
                stateText);

        // "开始"热区含按钮底与整块容器（与 HC mNext/btn_bg 双目标同责）
        mDoneNext.setOnClickListener(v -> startHome());
        btnBg.setOnClickListener(v -> startHome());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus || mEntered || !isAdded() || getView() == null) {
            return;
        }
        mEntered = true;

        // glow 已在 onViewCreated 提前启动（转场期即渲染）；此处只播入场编排
        startAnim();
    }

    @Override
    public void onStart() {
        super.onStart();
        // 恢复场景：重建流光（无光圈）
        // SDK 33 以下不存在 glow 流光（根背景降级为 colorSurface 纯色），控制器必为 null；版本判断同时满足 lint 的 API 级别检查
        if (mEntered && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && mGlowController != null) {
            mGlowController.start(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && mGlowController != null) {
            mGlowController.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && mGlowController != null) {
            mGlowController.stop();
            mGlowController = null;
        }
    }

    /**
     * 入场编排（对齐 HC CongratulationFragment#startAnim/startBtnAnim）：
     * logo 区上滑淡入 quartOut 1500ms（状态文字并行淡入），按钮 delay 1000ms、2s 后 enable。
     */
    private void startAnim() {
        // logo 区：translationY 100→0 + alpha 0→1（HC startLogoAnim 的 AnimState 全状态端点）
        AnimState from = new AnimState("start")
                .add(ViewProperty.ALPHA, 0.0d)
                .add(ViewProperty.TRANSLATION_Y, 100.0d);
        AnimState to = new AnimState("end")
                .add(ViewProperty.ALPHA, BLEND_ALPHA_CLAMP)
                .add(ViewProperty.TRANSLATION_Y, 0.0d);
        Folme.use(mLogoImageWrapper).state().setTo(from).to(to,
                new AnimConfig().setEase(FolmeEase.quartOut(1500L)));

        // 状态文字自身预设了 alpha=0（wrapper 的 alpha 不透传给子项），并行同参数淡入
        View stateText = getView() != null ? getView().findViewById(R.id.done_state_text) : null;
        if (stateText != null) {
            Folme.use(stateText).state()
                    .setTo(ViewProperty.ALPHA, 0.0f)
                    .to(ViewProperty.ALPHA, BLEND_ALPHA_CLAMP,
                            new AnimConfig().setEase(FolmeEase.quartOut(1500L)));
        }

        // 按钮：alpha 0→1 sinOut 450ms delay 1000ms；2s 后才 enable 防误触（HC 原参）
        Folme.use(mDoneNext).state()
                .setTo(ViewProperty.ALPHA, 0.0f)
                .to(ViewProperty.ALPHA, BLEND_ALPHA_CLAMP,
                        new AnimConfig().setEase(FolmeEase.sinOut(450L)).setDelay(1000L));
        mDoneNext.postDelayed(() -> {
            if (mDoneNext != null) {
                mDoneNext.setEnabled(true);
            }
        }, 2000L);
    }

    /**
     * "开始"：写首启标记 → 启动 MainActivity（不加自定义转场，交给系统默认切换动画）。
     */
    private void startHome() {
        if (mStarting) {
            return;
        }
        mStarting = true;

        // 流程走完才落首启标记：中途杀进程/退出重进仍从欢迎页开始（HC provisioned 语义）
        requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(FIRST_RUN_KEY, false).apply();

        // CLEAR_TASK 清掉本页之上的残留实例（OOBE 期间按 HOME 再进图标不会叠栈）
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        requireActivity().startActivity(intent);
        requireActivity().finish();
    }
}
