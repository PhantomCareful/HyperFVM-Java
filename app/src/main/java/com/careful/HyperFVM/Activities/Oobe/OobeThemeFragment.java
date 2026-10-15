package com.careful.HyperFVM.Activities.Oobe;

import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.ACCENT_DEFAULT;
import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.CONTENT_APP_THEME;
import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.CONTENT_DIY_FONT_SCALE;
import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.CONTENT_IS_DYNAMIC_BACKGROUND;
import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE;
import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.CONTENT_THEME_MODE;
import static com.careful.HyperFVM.Activities.Necessary.SettingsActivity.FONT_SCALE_STEP;
import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Dropdown.DropdownPopupHelper;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * OOBE 主题设置页（浅色 Provision 风格骨架）。
 * 本轮只建框架：页头（返回+标题）/ 占位内容区 / 底部下一步；
 * 主题选择器与读写功能（ThemeManager.KEY_THEME_MODE 六选一 + KEY_ACCENT_COLOR
 */
public class OobeThemeFragment extends Fragment {
    private View view;

    private DBHelper dbHelper;

    private String currentTheme;
    private View themeSelectorContainer;
    private TextView themeCurrentSelection;
    private String currentThemeMode;
    private View themeModeSelectorContainer;
    private TextView themeModeCurrentSelection;

    private Slider fontScaleSlider;
    private TextView fontScaleCurrentValue;
    private float fontScale;
    /** 拖动跨档 tick 振动：tracking 门控（仅手指拖动振，程序 setValue 不振）+ 上次 tick 的档位序号 */
    private boolean sliderTracking;
    private int sliderLastTickStep;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oobe_theme, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;

        // 初始化数据库
        dbHelper = HyperFVMApplication.getDBHelper();

        // 滚动位置由系统自动保存/恢复：NestedScrollView（@id/oobe_theme_scroll）的
        // View 树状态在 recreate 时随 Fragment 自动保存并恢复，无需手动读写

        // 初始化各种装饰效果
        initDecoration();

        // 初始化主题选择器
        initThemeSelector();

        // 初始化开关状态（从数据库读取）
        initSwitchesAndSliders();

        // 设置开关监听（更新数据库）
        setupSwitchAndSliderListeners();
        
        // 下一步 → 设置完毕页（栈式启动，本页留栈供返回）
        view.findViewById(R.id.oobe_btn_next).setOnClickListener(v ->
                ((OobePageActivity) requireActivity()).goNextStep());
    }

    private void initThemeSelector() {
        themeCurrentSelection = view.findViewById(R.id.theme_current_selection);
        themeSelectorContainer = view.findViewById(R.id.theme_selector_container);
        themeModeCurrentSelection = view.findViewById(R.id.theme_mode_current_selection);
        themeModeSelectorContainer = view.findViewById(R.id.theme_mode_selector_container);

        // 从数据库获取当前强调色（键缺失时落为默认值）
        currentTheme = dbHelper.getSettingStringValue(CONTENT_APP_THEME);
        if (currentTheme.isEmpty()) {
            currentTheme = ACCENT_DEFAULT;
        }
        themeCurrentSelection.setText(currentTheme);
        // 从数据库获取主题模式（键缺失时落为默认值）
        currentThemeMode = dbHelper.getSettingStringValue(CONTENT_THEME_MODE);
        if (currentThemeMode.isEmpty()) {
            currentThemeMode = ThemeManager.THEME_MODE_DEFAULT;
        }
        themeModeCurrentSelection.setText(currentThemeMode);

        // 主题模式行始终可点击
        themeModeSelectorContainer.setOnClickListener(v -> showThemeModeDropdown());
        // 强调色行仅在主题模式为 Monet 时可点击
        setThemeRowClickable(currentThemeMode.startsWith("Monet"));
    }

    private void showThemeDropdown() {
        showRowDropdown(themeSelectorContainer, R.array.theme_entries, currentTheme, CONTENT_APP_THEME,
                themeCurrentSelection, selectedEntries -> {
                    currentTheme = selectedEntries;
                    applyThemeChangeAndRefresh();
                });
    }

    private void showThemeModeDropdown() {
        showRowDropdown(themeModeSelectorContainer, R.array.theme_mode_entries, currentThemeMode, CONTENT_THEME_MODE,
                themeModeCurrentSelection, selectedEntries -> {
                    currentThemeMode = selectedEntries;
                    // 强调色行仅在 Monet 模式下可更改
                    setThemeRowClickable(selectedEntries.startsWith("Monet"));
                    applyThemeChangeAndRefresh();
                });
    }

    /**
     * 强调色行是否可点击（仅主题模式为 Monet 时可更改）
     */
    private void setThemeRowClickable(boolean clickable) {
        themeSelectorContainer.setOnClickListener(clickable ? v -> showThemeDropdown() : null);
    }

    /**
     * 以设置行为锚点，就地弹出简洁的下拉选项菜单（替代原先的中央列表弹窗）。
     * 行渲染（选中文字 colorPrimary 高亮+√）、宽度恒为“最长条目+对勾”、四分支展开、
     * 右缘固定对齐容器、打开时居中定位选中项、点击行外自动关闭——
     * 统一复用 DropdownPopupHelper 公用实现，本方法只负责取条目、算选中下标与选中回调
     */
    private void showRowDropdown(View anchor, int arrayId, String currentContent, String dbHelperUpdateContent,
                                 TextView currentSelection, Consumer<String> onSelected) {
        String[] entries = getResources().getStringArray(arrayId);
        // 当前选中下标（按内容匹配，未匹配到默认第一项）
        int selectedIndex = 0;
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].equals(currentContent)) {
                selectedIndex = i;
                break;
            }
        }
        ArrayAdapter<String> adapter = DropdownPopupHelper.createAdapter(requireActivity(), selectedIndex, entries,
                R.layout.item_dropdown_text_selection, null);
        DropdownPopupHelper.showAnchored(requireActivity(), anchor, adapter, entries.length, selectedIndex,
                position -> {
                    String selectedEntries = entries[position];
                    dbHelper.updateSettingValue(dbHelperUpdateContent, selectedEntries);
                    currentSelection.setText(selectedEntries);
                    // 使用回调，将selectedEntries传回调用方（同步成员变量）
                    onSelected.accept(selectedEntries);
                });
    }

    /**
     * 主题相关设置变更后立即生效：版本号+1（返回栈页面回前台时惰性重建），
     * 应用深浅色模式，并延迟一拍重建设置页自身（确保下拉菜单已 dismiss、开关状态已写完）。
     * 深浅色反转时 AppCompat 会先行自动重建本页，届时本 Fragment 已 detach——
     * 以 isAdded 前置拦截跳过手动重建，避免在已销毁的 Activity 上二次 recreate 闪退。
     */
    private void applyThemeChangeAndRefresh() {
        HyperFVMApplication.bumpUiConfigVersion();
        DarkModeManager.applyDarkMode(); // 主题模式含（深色）/（浅色）后缀，需同步夜间模式
        requireActivity().getWindow().getDecorView().post(() -> {
            if (!isAdded()) {
                return; // 深浅色反转：系统自动重建已接管本页，不可再手动 recreate
            }
            Activity activity = requireActivity();
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                activity.recreate();
            }
        });
    }

    /** 字体缩放显示值：档位值保留两位小数（显示值=生效值，字号随显示同步跳变） */
    private String formatFontScale(float value) {
        return String.format(Locale.CHINA, "%.2f", value);
    }

    /** 量化到可见档：滑条保持无级丝滑拖动，显示/写库/生效统一取档位值 */
    private float quantizeFontScale(float value) {
        // 与 SettingsActivity 同款修复：除法形式避免乘 0.01f 的浮点下漂（91*0.01f≠0.91f），
        // 保证 "0.91" 档写库为规范 0.91f，不被半像素取整边界吞掉。
        return Math.round(value / FONT_SCALE_STEP) / (1f / FONT_SCALE_STEP);
    }

    /**
     * 从数据库读取状态并初始化开关和滑条
     */
    @SuppressLint("SetTextI18n")
    private void initSwitchesAndSliders() {
        MaterialSwitch materialSwitch;
        // 跟随系统字体大小开关
        boolean isFollowSystemFontScale = dbHelper.getSettingBooleanValue(CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE);
        materialSwitch = view.findViewById(R.id.Switch_isFixedFontScale);
        materialSwitch.setChecked(isFollowSystemFontScale);
        // 自定义字体大小滑条
        fontScaleSlider = view.findViewById(R.id.Slider_FontScale);
        fontScaleSlider.setEnabled(!isFollowSystemFontScale);
        fontScale = quantizeFontScale(dbHelper.getSettingFloatValue(CONTENT_DIY_FONT_SCALE));
        fontScaleSlider.setValue(fontScale);
        // 当前值文本：changeListener 在 setup 阶段才注册，此处 setValue 不会回调，需显式初始化；
        // 取 getValue() 而非 fontScale：库值越界时 setValue 会钳到 [valueFrom, valueTo]，以滑条实际值为准
        fontScaleCurrentValue = view.findViewById(R.id.font_scale_current_value);
        fontScaleCurrentValue.setText(formatFontScale(fontScaleSlider.getValue()));
        // 动态背景开关
        boolean isDynamicBackground = HyperFVMApplication.isContentDynamicBackgroundEnabled();
        materialSwitch = view.findViewById(R.id.Switch_isDynamicBackground);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            materialSwitch.setEnabled(true);
            materialSwitch.setChecked(isDynamicBackground);
        } else {
            materialSwitch.setEnabled(false);
            materialSwitch.setChecked(false);
            view.findViewById(R.id.Switch_isDynamicBackground_Container).setEnabled(false);
        }
    }

    /**
     * 设置开关和滑条状态变化监听，同步更新数据库
     */
    private void setupSwitchAndSliderListeners() {
        // 跟随系统字体大小开关
        MaterialSwitch finalMaterialSwitch2 = view.findViewById(R.id.Switch_isFixedFontScale);
        view.findViewById(R.id.Switch_isFixedFontScale_Container).setOnClickListener(v -> {
            finalMaterialSwitch2.setChecked(!finalMaterialSwitch2.isChecked());
            dbHelper.updateSettingValue(CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE, Boolean.toString(finalMaterialSwitch2.isChecked()));
            fontScaleSlider.setEnabled(!finalMaterialSwitch2.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            applyThemeChangeAndRefresh();
        });
        // 自定义字体大小滑条
        fontScaleSlider.addOnChangeListener((slider, v, b) -> {
            fontScale = quantizeFontScale(v);
            fontScaleCurrentValue.setText(formatFontScale(fontScale));
            dbHelper.updateSettingValue(CONTENT_DIY_FONT_SCALE, String.valueOf(fontScale));
            // 拖动中每跨过一个量化档位 tick 一次振动（复刻 Miuix 滑条手感）；选型说明见 SettingsActivity 同款
            int tickStep = Math.round(fontScale / FONT_SCALE_STEP);
            if (sliderTracking && tickStep != sliderLastTickStep) {
                sliderLastTickStep = tickStep;
                slider.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            }
        });
        fontScaleSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                // 落点同步档位序号：按住后未跨档不振、跨档才 tick；tracking 同时门控程序性 setValue 不振
                sliderTracking = true;
                sliderLastTickStep = Math.round(quantizeFontScale(slider.getValue()) / FONT_SCALE_STEP);
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                sliderTracking = false;
                // 松手时才刷新（拖动过程中仅写库，避免连续重建）
                applyThemeChangeAndRefresh();
            }
        });
        // 动态背景开关
        MaterialSwitch finalMaterialSwitch3 = view.findViewById(R.id.Switch_isDynamicBackground);
        view.findViewById(R.id.Switch_isDynamicBackground_Container).setOnClickListener(v -> {
            finalMaterialSwitch3.setChecked(!finalMaterialSwitch3.isChecked());
            dbHelper.updateSettingValue(CONTENT_IS_DYNAMIC_BACKGROUND, Boolean.toString(finalMaterialSwitch3.isChecked()));
            // 同步更新 Application 中的缓存值，保证新打开的页面无需重启也能读取到最新设置
            HyperFVMApplication.setContentIsDynamicBackground(finalMaterialSwitch3.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            applyThemeChangeAndRefresh();
        });
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initDecoration() {
        // 适配状态栏高度
        ImageButton floatButtonBack = view.findViewById(R.id.oobe_back);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(requireContext(), view, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(requireContext(), 5);
            floatButtonBack.setLayoutParams(params);
        });
        // 动态获取导航栏高度（小白条/三键导航）
        CardView oobe_btn_next_container = view.findViewById(R.id.oobe_btn_next_container);
        InsetsUtil.setNavigationBarHeight(requireContext(), view, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) oobe_btn_next_container.getLayoutParams();
            params.bottomMargin = height;
            params.leftMargin = height;
            params.rightMargin = height;
            oobe_btn_next_container.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout oobe_theme_container = view.findViewById(R.id.oobe_theme_container);
        InsetsUtil.setMarginHorizontal(requireContext(), oobe_theme_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) oobe_theme_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            oobe_theme_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);
        });

        // 顺便设置返回按钮的功能
        floatButtonBack.setOnClickListener(v -> requireActivity().finish());

        // 添加按压动画
        view.findViewById(R.id.tips_dynamic_background).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        InsetsUtil.removeListener(getView());
    }
}
