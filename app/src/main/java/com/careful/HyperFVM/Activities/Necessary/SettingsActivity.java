package com.careful.HyperFVM.Activities.Necessary;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Dropdown.DropdownPopupHelper;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForSafety.BiometricAuthHelper;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import java.util.function.Consumer;

import eightbitlab.com.blurview.BlurView;

public class SettingsActivity extends BaseActivity {
    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_settings_scroll_y";
    private static final int TOP_BAR_FADE_RANGE_DP = 50;// 顶部模糊遮罩层完整显现的滚动区间（dp）

    private DBHelper dbHelper;
    private BlurUtil blurUtil;

    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（大标题/悬浮标题/模糊层三组件全联动）

    // 强调色键（与 ThemeManager.KEY_ACCENT_COLOR 保持一致）
    private static final String CONTENT_APP_THEME = "主题-强调色";
    // 主题模式键（“Miuix/Monet（跟随系统/浅色/深色）”六选一），与 ThemeManager 保持一致
    private static final String CONTENT_THEME_MODE = "主题-主题模式";
    // 强调色的“默认”值：对应动态取色（壁纸取色），与 ThemeManager 保持一致
    private static final String ACCENT_DEFAULT = "默认";
    private String currentTheme;
    private View themeSelectorContainer;
    private TextView themeCurrentSelection;
    private String currentThemeMode;
    private View themeModeSelectorContainer;
    private TextView themeModeCurrentSelection;

    public static final String CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE = "跟随系统字体大小";
    public static final String CONTENT_DIY_FONT_SCALE = "自定义字体大小";
    private Slider fontScaleSlider;
    private float fontScale;

    public static final String CONTENT_IS_DYNAMIC_BACKGROUND = "动态背景";

    public static final String CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX = "提示语显示-防御卡全能数据库";
    public static final String CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST = "提示语显示-增幅卡名单";
    public static final String CONTENT_TOAST_IS_VISIBLE_REFRESH_DASHBOARD = "提示语显示-仪表盘刷新完成";

    public static final String CONTENT_IS_BIOMETRIC_AUTH = "安全-生物认证";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        // 小白条沉浸
        EdgeToEdge.enable(this);
        if(NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_settings);

        // 初始化数据库
        dbHelper = HyperFVMApplication.getDBHelper();

        // 初始化各种装饰效果
        initDecoration();

        // 初始化主题选择器
        initThemeSelector();

        // 初始化所有开关状态（从数据库读取）
        initSwitchesAndSliders();

        // 设置开关监听（更新数据库）
        setupSwitchAndSliderListeners();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void checkPermissionStates() {
        // 1 获取权限状态显示的TextView（根据实际布局ID调整）
        TextView notificationStateTv = findViewById(R.id.permission_current_state_notification);
        TextView installStateTv = findViewById(R.id.permission_current_state_install);

        // 2.1 跳转授予通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //点击授权
            findViewById(R.id.permission_notification_container).setOnClickListener(v -> {
                // 调用已注册的权限请求器
                Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            });
        } else {
            findViewById(R.id.permission_notification_container).setOnClickListener(null);
        }

        // 2.2 跳转授予安装权限
        //点击授权
        findViewById(R.id.permission_install_container).setOnClickListener(v -> {
            // 跳转到安装未知应用权限设置页面
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);

            // 需要指定包名
            intent.setData(android.net.Uri.parse("package:" + this.getPackageName()));

            // 检查是否有可以处理此Intent的应用
            if (intent.resolveActivity(this.getPackageManager()) != null) {
                this.startActivity(intent);
            } else {
                // 如果无法跳转到精确设置页面，跳转到应用详情页
                Intent appDetailsIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                appDetailsIntent.setData(android.net.Uri.parse("package:" + this.getPackageName()));
                this.startActivity(appDetailsIntent);
            }
        });

        // 2.3 跳转系统“默认打开”页面，手动勾选受支持的链接（域名无法自动验证，勾选后链接直接跳转进App）
        findViewById(R.id.permission_links_container).setOnClickListener(v -> {
            // 跳转到本应用的“默认打开”设置页
            Intent intent = new Intent(android.provider.Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS)
                    .setData(android.net.Uri.parse("package:" + this.getPackageName()));

            // 检查是否有可以处理此Intent的应用
            if (intent.resolveActivity(this.getPackageManager()) != null) {
                this.startActivity(intent);
            } else {
                // 如果无法跳转到精确设置页面，跳转到应用详情页
                Intent appDetailsIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                appDetailsIntent.setData(android.net.Uri.parse("package:" + this.getPackageName()));
                this.startActivity(appDetailsIntent);
            }
        });

        // 更新UI
        notificationStateTv.setText(hasNotificationPermission() ? "已授予✅" : "点我去授权👉");
        installStateTv.setText(hasInstallPermission() ? "已授予✅" : "点我去授权👉");
    }

    /**
     * 检查是否拥有通知权限
     */
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12及以下默认拥有通知权限
            return true;
        }
    }

    /**
     * 检查是否拥有安装权限
     */
    private boolean hasInstallPermission() {
        return getPackageManager().canRequestPackageInstalls();
    }

    private void initThemeSelector() {
        themeCurrentSelection = findViewById(R.id.theme_current_selection);
        themeSelectorContainer = findViewById(R.id.theme_selector_container);
        themeModeCurrentSelection = findViewById(R.id.theme_mode_current_selection);
        themeModeSelectorContainer = findViewById(R.id.theme_mode_selector_container);

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
        ArrayAdapter<String> adapter = DropdownPopupHelper.createAdapter(this, selectedIndex, entries,
                R.layout.item_dropdown_text_selection, null);
        DropdownPopupHelper.showAnchored(this, anchor, adapter, entries.length, selectedIndex,
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
     * 应用深浅色模式，并延迟一拍重建设置页自身（确保下拉菜单已 dismiss、开关状态已写完；
     * 若深浅色变化已由系统触发重建，此处守卫跳过，避免二次重建）
     */
    private void applyThemeChangeAndRefresh() {
        HyperFVMApplication.bumpUiConfigVersion();
        DarkModeManager.applyDarkMode(); // 主题模式含（深色）/（浅色）后缀，需同步夜间模式
        getWindow().getDecorView().post(() -> {
            if (!isFinishing() && !isDestroyed()) {
                recreate();
            }
        });
    }

    /**
     * 从数据库读取状态并初始化开关和滑条
     */
    @SuppressLint("SetTextI18n")
    private void initSwitchesAndSliders() {
        MaterialSwitch materialSwitch;
        // 跟随系统字体大小开关
        boolean isFollowSystemFontScale = dbHelper.getSettingBooleanValue(CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE);
        materialSwitch = findViewById(R.id.Switch_isFixedFontScale);
        materialSwitch.setChecked(isFollowSystemFontScale);
        // 自定义字体大小滑条
        fontScaleSlider = findViewById(R.id.Slider_FontScale);
        fontScaleSlider.setEnabled(!isFollowSystemFontScale);
        fontScale = dbHelper.getSettingFloatValue(CONTENT_DIY_FONT_SCALE);
        fontScaleSlider.setValue(fontScale);
        // 动态背景开关
        boolean isDynamicBackground = HyperFVMApplication.isContentDynamicBackgroundEnabled();
        materialSwitch = findViewById(R.id.Switch_isDynamicBackground);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            materialSwitch.setEnabled(true);
            materialSwitch.setChecked(isDynamicBackground);
        } else {
            materialSwitch.setEnabled(false);
            materialSwitch.setChecked(false);
            findViewById(R.id.Switch_isDynamicBackground_Container).setEnabled(false);
        }
        // Toast显示设置开关
        boolean toastIsVisibleCardDataIndex = dbHelper.getSettingBooleanValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX);
        boolean toastIsVisibleCardDataAuxiliaryList = dbHelper.getSettingBooleanValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST);
        boolean toastIsVisibleRefreshDashboard = dbHelper.getSettingBooleanValue(CONTENT_TOAST_IS_VISIBLE_REFRESH_DASHBOARD);
        materialSwitch = findViewById(R.id.Switch_isVisible_CardDataIndex);
        materialSwitch.setChecked(toastIsVisibleCardDataIndex);
        materialSwitch = findViewById(R.id.Switch_isVisible_CardDataAuxiliaryList);
        materialSwitch.setChecked(toastIsVisibleCardDataAuxiliaryList);
        materialSwitch = findViewById(R.id.Switch_isVisible_RefreshDashboard);
        materialSwitch.setChecked(toastIsVisibleRefreshDashboard);
        // 生物认证开关
        materialSwitch = findViewById(R.id.Switch_BiometricAuth);
        if (HyperFVMApplication.isContentBiometricAuthEnabled(this)) {
            materialSwitch.setChecked(true);
        } else {
            // 设备不支持生物认证
            materialSwitch.setChecked(false);
            materialSwitch.setEnabled(false);
            findViewById(R.id.Switch_BiometricAuth_Container).setEnabled(false);
            TextView BiometricAuthDescription = findViewById(R.id.TextView_BiometricAuth_Description);
            BiometricAuthDescription.setText(getResources().getString(R.string.description_settings_biometric_auth_description_not_support) + "\n" +
                    getResources().getString(R.string.description_settings_biometric_auth));
        }
    }

    /**
     * 设置开关和滑条状态变化监听，同步更新数据库
     */
    private void setupSwitchAndSliderListeners() {
        // 跟随系统字体大小开关
        MaterialSwitch finalMaterialSwitch2 = findViewById(R.id.Switch_isFixedFontScale);
        findViewById(R.id.Switch_isFixedFontScale_Container).setOnClickListener(v -> {
            finalMaterialSwitch2.setChecked(!finalMaterialSwitch2.isChecked());
            dbHelper.updateSettingValue(CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE, Boolean.toString(finalMaterialSwitch2.isChecked()));
            fontScaleSlider.setEnabled(!finalMaterialSwitch2.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            applyThemeChangeAndRefresh();
        });
        // 自定义字体大小滑条
        fontScaleSlider.addOnChangeListener((slider, v, b) -> {
            fontScale = v;
            dbHelper.updateSettingValue(CONTENT_DIY_FONT_SCALE, String.valueOf(v));
        });
        fontScaleSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                // 啥也不做
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                // 松手时才刷新（拖动过程中仅写库，避免连续重建）
                applyThemeChangeAndRefresh();
            }
        });
        // 动态背景开关
        MaterialSwitch finalMaterialSwitch3 = findViewById(R.id.Switch_isDynamicBackground);
        findViewById(R.id.Switch_isDynamicBackground_Container).setOnClickListener(v -> {
            finalMaterialSwitch3.setChecked(!finalMaterialSwitch3.isChecked());
            dbHelper.updateSettingValue(CONTENT_IS_DYNAMIC_BACKGROUND, Boolean.toString(finalMaterialSwitch3.isChecked()));
            // 同步更新 Application 中的缓存值，保证新打开的页面无需重启也能读取到最新设置
            HyperFVMApplication.setContentIsDynamicBackground(finalMaterialSwitch3.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            applyThemeChangeAndRefresh();
        });
        // Toast显示设置开关
        MaterialSwitch finalMaterialSwitch4 = findViewById(R.id.Switch_isVisible_CardDataIndex);
        findViewById(R.id.Switch_isVisible_CardDataIndex_Container).setOnClickListener(v -> {
            finalMaterialSwitch4.setChecked(!finalMaterialSwitch4.isChecked());
            dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX, Boolean.toString(finalMaterialSwitch4.isChecked()));
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        });
        MaterialSwitch finalMaterialSwitch5 = findViewById(R.id.Switch_isVisible_CardDataAuxiliaryList);
        findViewById(R.id.Switch_isVisible_CardDataAuxiliaryList_Container).setOnClickListener(v -> {
            finalMaterialSwitch5.setChecked(!finalMaterialSwitch5.isChecked());
            dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST, Boolean.toString(finalMaterialSwitch5.isChecked()));
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        });
        MaterialSwitch finalMaterialSwitch6 = findViewById(R.id.Switch_isVisible_RefreshDashboard);
        findViewById(R.id.Switch_isVisible_RefreshDashboard_Container).setOnClickListener(v -> {
            finalMaterialSwitch6.setChecked(!finalMaterialSwitch6.isChecked());
            dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_REFRESH_DASHBOARD, Boolean.toString(finalMaterialSwitch6.isChecked()));
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        });
        // 生物认证开关
        MaterialSwitch finalMaterialSwitch7 = findViewById(R.id.Switch_BiometricAuth);
        findViewById(R.id.Switch_BiometricAuth_Container).setOnClickListener(v -> {
            boolean isChecked = finalMaterialSwitch7.isChecked();
            BiometricAuthHelper.simpleBiometricAuth(this, () -> {
                // 验证成功
                dbHelper.updateSettingValue(CONTENT_IS_BIOMETRIC_AUTH, Boolean.toString(!isChecked));
                finalMaterialSwitch7.setChecked(!isChecked);
                // 同步更新 Application 中的缓存值，保证新打开的页面无需重启也能读取到最新设置
                HyperFVMApplication.setContentIsBiometricAuth(finalMaterialSwitch7.isChecked());
            });
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
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
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(this, 50);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonBack.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout settings_container = findViewById(R.id.settings_container);
        InsetsUtil.setMarginHorizontal(this, settings_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) settings_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            settings_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBack.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());

        // 接入顶部栏滚动联动：滚动时模糊层与悬浮标题联动显现（topBarBottom 滚出后顶栏显现）
        nestedScrollUtil = NestedScrollUtil.attach(
                findViewById(R.id.scrollView),
                findViewById(R.id.topBarBottom),
                topBar,
                blurViewTopBar,
                TOP_BAR_FADE_RANGE_DP);

        // 添加模糊材质
        setupBlurEffect();

        // 添加按压动画
        findViewById(R.id.tips_theme).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.tips_dynamic_background).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.tips_toast).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));
    }

    /**
     * 在onResume阶段：检查通知权限并实时更新
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onResume() {
        super.onResume();
        // 检查权限授予状态
        checkPermissionStates();
    }

    /**
     * 保存顶部栏滚动联动的滚动位置，界面重建（深浅色切换等）后恢复
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.saveScrollY(outState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
        }
    }

    @Override
    protected void onDestroy() {
        if (blurUtil != null) {
            blurUtil.release();
            blurUtil = null;
        }

        View rootView = findViewById(android.R.id.content);
        InsetsUtil.removeListener(rootView);
        setContentView(new FrameLayout(this));

        super.onDestroy();
    }
}