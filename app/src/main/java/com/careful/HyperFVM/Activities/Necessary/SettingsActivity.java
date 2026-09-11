package com.careful.HyperFVM.Activities.Necessary;

import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForSafety.BiometricAuthHelper;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import java.util.function.Consumer;

public class SettingsActivity extends BaseActivity {
    private DBHelper dbHelper;
    private BlurUtil blurUtil;

    private static final String CONTENT_IS_DYNAMIC_COLOR = "主题-是否动态取色";
    private static final String CONTENT_APP_THEME = "主题-自定义主题色";
    private String currentTheme;
    private View themeSelectorContainer;
    private TextView themeCurrentSelection;
    private View darkModeSelectorContainer;
    private View interfaceStyleSelectorContainer;

    public static final String CONTENT_DARK_MODE = "主题-深色主题";
    private String currentDarkMode;
    private TextView darkModeCurrentSelection;

    public static final String CONTENT_INTERFACE_STYLE = "界面风格";
    private String currentInterfaceStyle;
    private TextView interfaceStyleCurrentSelection;

    public static final String CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE = "跟随系统字体大小";
    public static final String CONTENT_DIY_FONT_SCALE = "自定义字体大小";
    private Slider fontScaleSlider;
    private float fontScale;

    public static final String CONTENT_IS_DYNAMIC_BACKGROUND = "动态背景";

    public static final String CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX = "提示语显示-防御卡全能数据库";
    public static final String CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST = "提示语显示-增幅卡名单";
    public static final String CONTENT_TOAST_IS_VISIBLE_REFRESH_DASHBOARD = "提示语显示-仪表盘刷新完成";

    public static final String CONTENT_IS_BIOMETRIC_AUTH = "安全-生物认证";

    // 使用标志位来防止循环调用
    private boolean isPermitSwitchChanging = false;

    private int savedScrollY = 0;// 用于保存/恢复的滚动位置

    // 记录最近一次触摸按下时的横向位置，供下拉菜单跟随手指弹出（-1 表示尚未触摸过）
    private float lastTouchDownX = -1;

    // 页面内容左右边距（手机/PAD 由 InsetsUtil 动态计算）。下拉菜单最右缘沿用此值，
    // 保证菜单不贴屏幕右缘，而是与页面内容右缘对齐
    private int pageContentSideMarginPx = 0;

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

        // 恢复之前保存的滚动位置（切换深浅色模式重建等场景）
        if (savedInstanceState != null) {
            savedScrollY = savedInstanceState.getInt("scrollY", 0);
        }

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
        darkModeCurrentSelection = findViewById(R.id.dark_mode_current_selection);
        darkModeSelectorContainer = findViewById(R.id.dark_mode_selector_container);
        interfaceStyleCurrentSelection = findViewById(R.id.interface_style_current_selection);
        interfaceStyleSelectorContainer = findViewById(R.id.interface_style_selector_container);

        // 从数据库获取当前主题值
        currentTheme = dbHelper.getSettingStringValue(CONTENT_APP_THEME);
        themeCurrentSelection.setText(currentTheme);
        // 从数据库获取深色模式
        currentDarkMode = dbHelper.getSettingStringValue(CONTENT_DARK_MODE);
        darkModeCurrentSelection.setText(currentDarkMode);
        // 从数据库获取界面风格
        currentInterfaceStyle = dbHelper.getSettingStringValue(CONTENT_INTERFACE_STYLE);
        interfaceStyleCurrentSelection.setText(currentInterfaceStyle);

        // 记录触摸位置，使下拉菜单跟随手指横向弹出
        bindDropdownRowTouch(themeSelectorContainer);
        bindDropdownRowTouch(darkModeSelectorContainer);
        bindDropdownRowTouch(interfaceStyleSelectorContainer);
        // 设置点击事件（动态取色开启时主题行不可点击）
        setThemeRowClickable(!dbHelper.getSettingBooleanValue(CONTENT_IS_DYNAMIC_COLOR));
        // 设置深色模式点击事件
        darkModeSelectorContainer.setOnClickListener(v -> showDarkModeDropdown());
        // 设置界面风格点击事件
        interfaceStyleSelectorContainer.setOnClickListener(v -> showInterfaceStyleDropdown());
    }

    private void showThemeDropdown() {
        showRowDropdown(themeSelectorContainer, R.array.theme_entries, currentTheme, CONTENT_APP_THEME,
                themeCurrentSelection, selectedEntries -> currentTheme = selectedEntries);
    }

    private void showDarkModeDropdown() {
        showRowDropdown(darkModeSelectorContainer, R.array.dark_mode_entries, currentDarkMode, CONTENT_DARK_MODE,
                darkModeCurrentSelection, selectedEntries -> currentDarkMode = selectedEntries);
    }

    private void showInterfaceStyleDropdown() {
        showRowDropdown(interfaceStyleSelectorContainer, R.array.interface_style_entries, currentInterfaceStyle,
                CONTENT_INTERFACE_STYLE, interfaceStyleCurrentSelection,
                selectedEntries -> currentInterfaceStyle = selectedEntries);
    }

    /**
     * 主题行是否可点击（动态取色开启时禁用）
     */
    private void setThemeRowClickable(boolean clickable) {
        themeSelectorContainer.setOnClickListener(clickable ? v -> showThemeDropdown() : null);
    }

    /**
     * 记录点击设置行时的横向触摸位置，供下拉菜单跟随手指横向弹出
     */
    @SuppressLint("ClickableViewAccessibility")
    private void bindDropdownRowTouch(View container) {
        container.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastTouchDownX = event.getX();
            }
            return false;// 不消费事件，保持原有按压反馈与点击行为
        });
    }

    /**
     * 以设置行为锚点，就地弹出简洁的下拉选项菜单（替代原先的中央列表弹窗）。
     * 菜单宽度自适应选项内容（wrap_content）且跟随手指横向弹出，选中项右侧显示√，点击行外区域自动关闭。
     */
    private void showRowDropdown(View anchor, int arrayId, String currentContent, String dbHelperUpdateContent,
                                 TextView currentSelection, Consumer<String> onSelected) {
        String[] entries = getResources().getStringArray(arrayId);
        ArrayAdapter<String> adapter = getStringArrayAdapter(currentContent, entries);
        // 实测选项宽高：菜单宽度取最宽选项（wrap_content 效果），高度用于限制总高避免超屏。
        // ListView 在 PopupWindow 中无法真正 wrap_content，需自行测量内容宽后按像素设置。
        // getView 的 parent 形参标注 @NonNull：这里传一个仅用于生成 LayoutParams 的空容器（不挂载子视图），避免传 null
        ViewGroup measureParent = new FrameLayout(this);
        int contentWidth = 0;
        int itemHeight = 0;
        for (int i = 0; i < entries.length; i++) {
            View itemView = adapter.getView(i, null, measureParent);
            itemView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            contentWidth = Math.max(contentWidth, itemView.getMeasuredWidth());
            itemHeight = itemView.getMeasuredHeight();
        }
        // 极端大字体下限制菜单宽度不超出屏幕
        contentWidth = Math.min(contentWidth, anchor.getRootView().getWidth());

        // 锚点下方剩余空间不足时收缩菜单高度
        int verticalOffset = (int) (4 * getResources().getDisplayMetrics().density);
        int[] location = new int[2];
        anchor.getLocationInWindow(location);
        int spaceBelow = anchor.getRootView().getHeight() - location[1] - anchor.getHeight() - verticalOffset;
        int popupHeight = Math.min(itemHeight * entries.length, Math.max(spaceBelow, itemHeight * 2));

        // 横向弹出位置跟随手指按下的位置；最右不得超过屏幕右缘减页面边距，使菜单右缘与页面内容右缘对齐
        int horizontalOffset = 0;
        if (lastTouchDownX >= 0) {
            int maxOffset = anchor.getRootView().getWidth() - contentWidth - location[0] - pageContentSideMarginPx;
            horizontalOffset = Math.max(0, Math.min((int) lastTouchDownX, maxOffset));
        }

        ListPopupWindow popup = new ListPopupWindow(this);
        popup.setAnchorView(anchor);
        popup.setWidth(contentWidth);
        popup.setHeight(popupHeight);
        popup.setVerticalOffset(verticalOffset);
        popup.setHorizontalOffset(horizontalOffset);
        popup.setModal(true);
        popup.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.popup_dropdown_background));
        popup.setAdapter(adapter);
        popup.setOnItemClickListener((parent, view, position, id) -> {
            String selectedEntries = entries[position];
            dbHelper.updateSettingValue(dbHelperUpdateContent, selectedEntries);
            currentSelection.setText(selectedEntries);
            // 使用回调，将selectedEntries传回调用方（同步成员变量）
            onSelected.accept(selectedEntries);
            popup.dismiss();
        });

        popup.show();
        // 弹出后隐藏滚动条（选中态由 adapter 渲染右侧对勾，无需 ListView 单选模式）
        ListView listView = popup.getListView();
        if (listView != null) {
            listView.setVerticalScrollBarEnabled(false);
        }
    }

    @NonNull
    private ArrayAdapter<String> getStringArrayAdapter(String currentContent, String[] entries) {
        int selectedIndex = 0;
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].equals(currentContent)) {
                selectedIndex = i;
                break;
            }
        }

        // 实测选项宽高：菜单宽度取最宽选项（wrap_content 效果），高度用于限制总高避免超屏。
        // ListView 在 PopupWindow 中无法真正 wrap_content，需自行测量内容宽后按像素设置
        final int currentSelectedIndex = selectedIndex;
        return new ArrayAdapter<>(SettingsActivity.this, R.layout.item_dropdown_selection, entries) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View row = convertView != null ? convertView
                        : getLayoutInflater().inflate(R.layout.item_dropdown_selection, parent, false);
                ((TextView) row.findViewById(R.id.option_text)).setText(getItem(position));
                // 仅当前选中项右侧显示对勾，其余不显示
                row.findViewById(R.id.option_check)
                        .setVisibility(position == currentSelectedIndex ? View.VISIBLE : View.GONE);
                return row;
            }
        };
    }

    /**
     * 重启App的方法
     */
    private void restartApp() {
        // 获取App的主Activity（通常是AndroidManifest中声明的LAUNCHER Activity）
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            // 清除之前的任务栈，避免重启后返回旧页面
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // 启动主Activity
            startActivity(intent);
            // 关闭当前所有Activity
            finishAffinity();
        }
    }

    /**
     * 从数据库读取状态并初始化开关和滑条
     */
    @SuppressLint("SetTextI18n")
    private void initSwitchesAndSliders() {
        MaterialSwitch materialSwitch;
        // 动态取色开关
        boolean isDynamicColor = dbHelper.getSettingBooleanValue(CONTENT_IS_DYNAMIC_COLOR);
        materialSwitch = findViewById(R.id.Switch_isDynamicColor);
        materialSwitch.setChecked(isDynamicColor);
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
        if (BiometricAuthHelper.isBiometricAvailable(this)) {
            // 设备支持生物认证
            boolean isBiometricAuth = dbHelper.getSettingBooleanValue(CONTENT_IS_BIOMETRIC_AUTH);
            materialSwitch.setChecked(isBiometricAuth);
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
        MaterialSwitch materialSwitch;
        // 动态取色开关
        materialSwitch = findViewById(R.id.Switch_isDynamicColor);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_IS_DYNAMIC_COLOR, Boolean.toString(isChecked));
            // 动态取色开启时禁用主题行点击，关闭时恢复
            setThemeRowClickable(!isChecked);
        });
        MaterialSwitch finalMaterialSwitch1 = materialSwitch;
        findViewById(R.id.Switch_isDynamicColor_Container).setOnClickListener(v -> {
            dbHelper.updateSettingValue(CONTENT_IS_DYNAMIC_COLOR, Boolean.toString(finalMaterialSwitch1.isChecked()));
            setThemeRowClickable(!finalMaterialSwitch1.isChecked());
            finalMaterialSwitch1.setChecked(!finalMaterialSwitch1.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        });
        // 跟随系统字体大小开关
        materialSwitch = findViewById(R.id.Switch_isFixedFontScale);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE, Boolean.toString(isChecked));
            fontScaleSlider.setEnabled(!isChecked);
        });
        MaterialSwitch finalMaterialSwitch2 = materialSwitch;
        findViewById(R.id.Switch_isFixedFontScale_Container).setOnClickListener(v -> {
            dbHelper.updateSettingValue(CONTENT_IS_FOLLOW_SYSTEM_FONT_SCALE, Boolean.toString(finalMaterialSwitch2.isChecked()));
            fontScaleSlider.setEnabled(!finalMaterialSwitch2.isChecked());
            finalMaterialSwitch2.setChecked(!finalMaterialSwitch2.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
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
                // 啥也不做
            }
        });
        // 动态背景开关
        materialSwitch = findViewById(R.id.Switch_isDynamicBackground);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSettingValue(CONTENT_IS_DYNAMIC_BACKGROUND, Boolean.toString(isChecked));
            HyperFVMApplication.setContentIsDynamicBackground(isChecked);
        });
        MaterialSwitch finalMaterialSwitch3 = materialSwitch;
        findViewById(R.id.Switch_isDynamicBackground_Container).setOnClickListener(v -> {
            dbHelper.updateSettingValue(CONTENT_IS_DYNAMIC_BACKGROUND, Boolean.toString(finalMaterialSwitch3.isChecked()));
            finalMaterialSwitch3.setChecked(!finalMaterialSwitch3.isChecked());
            // 同步更新 Application 中的缓存值，保证新打开的页面无需重启也能读取到最新设置
            HyperFVMApplication.setContentIsDynamicBackground(finalMaterialSwitch3.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        });
        // Toast显示设置开关
        materialSwitch = findViewById(R.id.Switch_isVisible_CardDataIndex);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX, Boolean.toString(isChecked)));
        MaterialSwitch finalMaterialSwitch4 = materialSwitch;
        findViewById(R.id.Switch_isVisible_CardDataIndex_Container).setOnClickListener(v -> {
            dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_INDEX, Boolean.toString(finalMaterialSwitch4.isChecked()));
            finalMaterialSwitch4.setChecked(!finalMaterialSwitch4.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        });
        materialSwitch = findViewById(R.id.Switch_isVisible_CardDataAuxiliaryList);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST, Boolean.toString(isChecked)));
        MaterialSwitch finalMaterialSwitch5 = materialSwitch;
        findViewById(R.id.Switch_isVisible_CardDataAuxiliaryList_Container).setOnClickListener(v -> {
            dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_CARD_DATA_AUXILIARY_LIST, Boolean.toString(finalMaterialSwitch5.isChecked()));
            finalMaterialSwitch5.setChecked(!finalMaterialSwitch5.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        });
        materialSwitch = findViewById(R.id.Switch_isVisible_RefreshDashboard);
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_REFRESH_DASHBOARD, Boolean.toString(isChecked)));
        MaterialSwitch finalMaterialSwitch6 = materialSwitch;
        findViewById(R.id.Switch_isVisible_RefreshDashboard_Container).setOnClickListener(v -> {
            dbHelper.updateSettingValue(CONTENT_TOAST_IS_VISIBLE_REFRESH_DASHBOARD, Boolean.toString(finalMaterialSwitch6.isChecked()));
            finalMaterialSwitch6.setChecked(!finalMaterialSwitch6.isChecked());
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        });
        // 生物认证开关
        materialSwitch = findViewById(R.id.Switch_BiometricAuth);
        MaterialSwitch finalMaterialSwitch7 = materialSwitch;
        materialSwitch.setOnClickListener(v -> {
            boolean isChecked = finalMaterialSwitch7.isChecked();
            BiometricAuthHelper.simpleBiometricAuth(this, getResources().getString(R.string.biometric_auth_title),
                    getResources().getString(R.string.biometric_auth_sub_title), () -> {
                        // 验证成功
                        dbHelper.updateSettingValue(CONTENT_IS_BIOMETRIC_AUTH, Boolean.toString(!isChecked));
                        isPermitSwitchChanging = true;
                        finalMaterialSwitch7.setChecked(!isChecked);
            });
        });
        findViewById(R.id.Switch_BiometricAuth_Container).setOnClickListener(v -> {
            boolean isChecked = finalMaterialSwitch7.isChecked();
            BiometricAuthHelper.simpleBiometricAuth(this, getResources().getString(R.string.biometric_auth_title),
                    getResources().getString(R.string.biometric_auth_sub_title), () -> {
                        // 验证成功
                        dbHelper.updateSettingValue(CONTENT_IS_BIOMETRIC_AUTH, Boolean.toString(!isChecked));
                        isPermitSwitchChanging = true;
                        finalMaterialSwitch7.setChecked(!isChecked);
                    });
            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        });
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isPermitSwitchChanging) {
                isPermitSwitchChanging = false;
            } else {
                finalMaterialSwitch7.setChecked(!isChecked);
            }
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
        MaterialCardView floatButtonBackContainer = findViewById(R.id.FloatButton_Back_Container);
        MaterialCardView topBarContainer = findViewById(R.id.TopBar_Container);
        MaterialCardView floatButtonRestartContainer = findViewById(R.id.FloatButton_Restart_Container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.topMargin = height;
            topBarContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonRestartContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonRestartContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout settings_container = findViewById(R.id.settings_container);
        InsetsUtil.setMarginHorizontal(this, settings_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) settings_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            settings_container.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonRestartContainer.getLayoutParams();
            params.rightMargin = layout_marginHorizontal;
            floatButtonRestartContainer.setLayoutParams(params);

            // 同步页面边距给下拉菜单：菜单最右缘与页面内容右缘对齐，统一在此处管理
            pageContentSideMarginPx = layout_marginHorizontal;
        });

        // 添加模糊材质
        setupBlurEffect();

        // 保存/恢复滚动位置（切换深浅色模式重建时保持上次位置）
        ScrollView scrollView = findViewById(R.id.scrollView);
        if (scrollView != null) {
            scrollView.post(() -> scrollView.setScrollY(savedScrollY));// 还原当前滚动位置
            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                    savedScrollY = scrollY);// 实时记录当前滚动位置
        }

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
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonRestart));

        // 顺便设置按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
        findViewById(R.id.FloatButton_Restart_Container).setOnClickListener(v -> {
            Toast.makeText(this, "重启App⏳⏳⏳", Toast.LENGTH_SHORT).show();
            // 重启App
            restartApp();
        });
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scrollY", savedScrollY);
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