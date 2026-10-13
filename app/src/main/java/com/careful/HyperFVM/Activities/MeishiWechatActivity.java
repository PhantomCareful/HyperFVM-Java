package com.careful.HyperFVM.Activities;

import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;
import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.ActivityMeishiWechatBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Blur.DialogBackgroundBlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.ForMeishi.MeishiBackupUtil;
import com.careful.HyperFVM.utils.ForMeishi.MeishiLinkUtil;
import com.careful.HyperFVM.utils.ForSafety.BiometricAuthHelper;
import com.careful.HyperFVM.utils.OtherUtils.ClipboardPermissionUtil;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import eightbitlab.com.blurview.BlurView;

public class MeishiWechatActivity extends BaseActivity {

    // 顶部栏滚动联动的状态保存键与渐变区间
    private static final String STATE_SCROLL_Y = "state_meishi_wechat_scroll_y";
    private static final int TOP_BAR_FADE_RANGE_DP = 50;

    private DBHelper dbHelper;
    private BlurUtil blurUtil;
    private NestedScrollUtil nestedScrollUtil;
    private LinearLayout accountListContainer;
    private TextView accountCountText;

    private LinearLayout MeishiWechatContainer;
    private TransitionSet transition;

    private String openId;
    private String serverName;
    private String playerName;

    private boolean isShow = false;

    // 从文件还原：文件选择器（优先 HyperOS 安全访问 hyper.intent.action.OPEN_DOCUMENT，不可用则回退标准 ACTION_OPEN_DOCUMENT；仅授予所选单个文件的访问权；mime放开为*/*，内容格式由导入侧校验兜底）
    private final ActivityResultLauncher<Intent> importFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                android.net.Uri uri = data == null ? null : data.getData();
                if (uri == null) {
                    return; // 用户取消选择
                }
                String text = MeishiBackupUtil.readTextFromUri(this, uri);
                if (text == null) {
                    Toast.makeText(this, "读取备份文件失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                importFromJson(text);
            });

    /**
     * 拉起文件选择器：优先尝试安全访问（金标联盟通用规范在 HyperOS 的落地组件，无需区分厂商），
     * 无安全访问组件（旧版本系统等）时回退标准 SAF 文档选择器；标准接口 API 19 起系统必有，若仍异常则 Toast 提示
     */
    private void launchImportPicker() {
        try {
            importFileLauncher.launch(new Intent("hyper.intent.action.OPEN_DOCUMENT")
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("*/*"));
            return;
        } catch (ActivityNotFoundException ignored) {
            // 无安全访问组件，继续走标准 SAF
        }
        try {
            importFileLauncher.launch(new Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("*/*")
                    .putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"*/*"}));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "系统缺少可用的文件选择器", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        // 小白条沉浸
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }

        super.onCreate(savedInstanceState);

        ActivityMeishiWechatBinding binding = ActivityMeishiWechatBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        // 初始化数据库
        dbHelper = HyperFVMApplication.getDBHelper();

        // 初始化视图
        initViews();

        // 加载已保存的账号
        loadAccountList();

        // 初始化各种装饰效果
        initDecoration();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void initViews() {
        // 账号数量文本和列表容器
        accountCountText = findViewById(R.id.TitleMeishiWechatSavedAccount);
        accountListContainer = findViewById(R.id.LinearLayout_AccountList);

        // 初始化动画效果
        MeishiWechatContainer = findViewById(R.id.MeishiWechatContainer);
        transition = new TransitionSet();
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.addTransition(new Fade()); // 淡入淡出
        transition.setDuration(400); // 动画时长400ms

        // 跳转系统“默认打开”页面，手动勾选受支持的链接（域名无法自动验证，勾选后链接直接跳转进App）
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

        // 备份与还原入口（PAD 双栏布局暂未放置备份区块，容器判空绑定）
        bindBackupClick(R.id.export_to_clipboard_container, v -> exportToClipboard());
        bindBackupClick(R.id.import_from_clipboard_container, v -> {
            if (ensureClipboardPermission()) {
                importFromClipboard();
            }
        });
        bindBackupClick(R.id.export_to_file_container, v -> exportToFile());
        bindBackupClick(R.id.import_from_file_container, v -> launchImportPicker());
    }

    /**
     * 备份与还原入口的判空绑定：PAD 双栏布局未放置备份区块时静默跳过
     */
    private void bindBackupClick(int viewId, View.OnClickListener listener) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    /**
     * 添加链接的弹窗
     * 这个弹窗和当前Activity联系非常紧密，为了方便起见，不归到DialogBuilderManager中去
     */
    private void showAddLinkDialog() {
        // 1. 加载自定义布局文件
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.item_dialog_input_meishi_wechat, null);

        // 2. 从布局中获取TextInputLayout和输入框
        TextInputLayout inputLayout = dialogView.findViewById(R.id.inputLayout);
        TextInputEditText editText = (TextInputEditText) inputLayout.getEditText(); // 获取内部输入框

        // 3. 构建弹窗并设置自定义布局
        Dialog dialog = new MaterialAlertDialogBuilder(this, materialAlertDialogThemeStyleId)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.button_close).setOnClickListener(v -> dialog.dismiss());

        // 4. 处理输入内容：测试链接
        dialogView.findViewById(R.id.button_action_1).setOnClickListener(v -> {
            if (editText != null) {
                String inputLink = Objects.requireNonNull(editText.getText()).toString().trim();
                if (!inputLink.isEmpty()) {
                    openId = MeishiLinkUtil.checkLink(inputLink);
                    if (openId.isEmpty()) {
                        Toast.makeText(this, "链接格式不正确", Toast.LENGTH_SHORT).show();
                    } else {
                        fetchPlayerInfo(true);
                    }
                } else {
                    Toast.makeText(this, "链接不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 5. 处理输入内容：添加链接
        dialogView.findViewById(R.id.button_action_2).setOnClickListener(v -> {
            if (editText != null) {
                String inputLink = Objects.requireNonNull(editText.getText()).toString().trim();
                if (!inputLink.isEmpty()) {
                    openId = MeishiLinkUtil.checkLink(inputLink);
                    if (openId.isEmpty()) {
                        Toast.makeText(this, "链接格式不正确", Toast.LENGTH_SHORT).show();
                    } else {
                        fetchPlayerInfo(false);
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(this, "链接不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 6. 添加窗口遮罩
        DialogBackgroundBlurUtil.setDialogBackgroundScrim(dialog);
        dialog.show();
    }

    // 加载玩家信息列表
    @SuppressLint("SetTextI18n")
    private void loadAccountList() {
        accountListContainer.removeAllViews();
        List<DBHelper.PlayerInfo> infos = dbHelper.getAllMeishiWechat();
        accountCountText.setText("已保存 " + infos.size() + " 个账号");

        for (DBHelper.PlayerInfo info : infos) {
            addAccountCard(info);
        }
    }

    // 添加包含区服、角色ID和openid的卡片
    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void addAccountCard(DBHelper.PlayerInfo info) {
        CardView cardView = (CardView) LayoutInflater.from(this)
                .inflate(R.layout.item_account_card, accountListContainer, false);

        // 绑定控件并设置内容（处理空值）
        TextView serverText = cardView.findViewById(R.id.TextView_ServerName);
        TextView playerText = cardView.findViewById(R.id.TextView_PlayerName);

        serverText.setText("所在区服：" + (info.serverName != null ? info.serverName : "未知区服"));
        playerText.setText("角色ID：" + (info.playerName != null ? isShow ? info.playerName : "🙅🙅🙅🙅🙅" : "未知角色"));

        // 长按删除逻辑
        cardView.setOnLongClickListener(v -> {
            DialogBuilderManager.showDialogWithCallBack(
                    this, "删除账号", "🗑️", "确定要删除 " + (info.playerName != null ? isShow ? info.playerName : "🙅🙅🙅🙅🙅" : "未知角色") + " 吗？",
                    true, "咱手滑了", "删除", () -> {
                        if (HyperFVMApplication.isContentBiometricAuthEnabled(this)) {
                            BiometricAuthHelper.simpleBiometricAuth(this, () -> {
                                dbHelper.deleteMeishiWechat(info.openid);
                                loadAccountList();
                            });
                        } else {
                            dbHelper.deleteMeishiWechat(info.openid);
                            loadAccountList();
                        }
                    }
            );
            return true;
        });

        TransitionManager.beginDelayedTransition(MeishiWechatContainer, transition);
        accountListContainer.addView(cardView);
    }

    /**
     * 网络请求：获取网页内容解析区服和角色ID（请求与解析复用MeishiLinkUtil，失败提示由其展示）
     * isTest：仅显示欢迎Toast不入库；否则解析成功后存入数据库并刷新列表
     */
    private void fetchPlayerInfo(boolean isTest) {
        MeishiLinkUtil.fetchPlayerInfo(this, openId, new MeishiLinkUtil.FetchCallback() {
            @Override
            public void onSuccess(String server, String player) {
                serverName = server;
                playerName = player;

                if (isTest) {
                    // 测试链接：显示Toast，不存入数据库
                    Toast.makeText(MeishiWechatActivity.this, "欢迎您：" + serverName + " - " + playerName, Toast.LENGTH_SHORT).show();
                } else {
                    // 手动添加：存入数据库，并刷新账号列表
                    savePlayerInfo();
                }
            }

            @Override
            public void onFailure(Dialog failureDialog) {
                // 失败提示（Toast或错误弹窗）已由MeishiLinkUtil展示，这里无需处理
            }
        });
    }

    /**
     * 将解析到的区服和角色存入数据库，并刷新账号列表
     * 解析回调已在主线程，可直接操作UI
     */
    private void savePlayerInfo() {
        dbHelper.insertMeishiWechat(openId, serverName, playerName);
        Toast.makeText(MeishiWechatActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
        // 刷新列表
        loadAccountList();
    }

    // ======================以下为温馨礼包数据备份与还原 ======================

    /**
     * 剪贴板读写权限未授权时 Toast 提示并跳转系统权限管理页
     * （与设置页剪贴板卡片一致：米系直跳本应用权限管理页，其他设备回退应用详情页）
     *
     * @return true 表示已授权，可继续操作剪贴板
     */
    @SuppressLint("QueryPermissionsNeeded")
    private boolean ensureClipboardPermission() {
        if (ClipboardPermissionUtil.hasPermission(this)) {
            return true;
        }
        Toast.makeText(this, "请先授予剪贴板权限", Toast.LENGTH_SHORT).show();

        Intent intent;
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            intent = new Intent("miui.intent.action.APP_PERM_EDITOR")
                    .putExtra("extra_pkgname", getPackageName());
        } else {
            intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(android.net.Uri.parse("package:" + getPackageName()));
        }

        // 检查是否有可以处理此Intent的应用
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        return false;
    }

    /**
     * 开启生物认证时先验证，通过后才执行导出动作（与长按删除账号一致）
     */
    private void runWithBiometricAuth(Runnable action) {
        if (HyperFVMApplication.isContentBiometricAuthEnabled(this)) {
            BiometricAuthHelper.simpleBiometricAuth(this, action);
        } else {
            action.run();
        }
    }

    /**
     * 备份到剪贴板：数据非空校验 → 剪贴板权限 → 生物认证 → 写入剪贴板
     */
    private void exportToClipboard() {
        List<DBHelper.PlayerInfo> infos = dbHelper.getAllMeishiWechat();
        if (infos.isEmpty()) {
            Toast.makeText(this, "暂无已保存的账号数据", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ensureClipboardPermission()) {
            return;
        }

        runWithBiometricAuth(() -> {
            String json = MeishiBackupUtil.buildJson(infos);
            if (json != null && MeishiBackupUtil.writeClipboard(this, json)) {
                Toast.makeText(this, "已备份到剪贴板", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "备份失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 从剪贴板还原：剪贴板权限 → 读取文本 → 格式校验入库
     */
    private void importFromClipboard() {
        String text = MeishiBackupUtil.readClipboard(this);
        if (text == null) {
            Toast.makeText(this, "剪贴板中没有可导入的数据", Toast.LENGTH_SHORT).show();
            return;
        }
        importFromJson(text);
    }

    /**
     * 备份到文件：数据非空校验 → 生物认证 → 二次确认弹窗 → 写入 Download 根目录
     */
    private void exportToFile() {
        List<DBHelper.PlayerInfo> infos = dbHelper.getAllMeishiWechat();
        if (infos.isEmpty()) {
            Toast.makeText(this, "暂无已保存的账号数据", Toast.LENGTH_SHORT).show();
            return;
        }

        runWithBiometricAuth(() -> DialogBuilderManager.showDialogWithCallBack(
                this, "导出温馨礼包数据", "📦",
                "数据将保存到：\nDownload/" + MeishiBackupUtil.EXPORT_FILE_NAME, true,
                "咱手滑了", "一键导出", () -> {
                    String json = MeishiBackupUtil.buildJson(infos);
                    if (json != null && MeishiBackupUtil.saveToDownloads(this, json)) {
                        DialogBuilderManager.showDialog(
                                this,
                                "导出成功",
                                "🎉",
                                "数据已保存到：\nDownload/" + MeishiBackupUtil.EXPORT_FILE_NAME,
                                true,
                                "好耶"
                        );
                    } else {
                        Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
                    }
                }
        ));
    }

    /**
     * 还原备份数据：格式校验 → 逐条入库（相同 openid 跳过，仅补新增）→ 立即刷新账号列表
     */
    private void importFromJson(String json) {
        List<DBHelper.PlayerInfo> imported = MeishiBackupUtil.parseJson(json);
        if (imported == null) {
            Toast.makeText(this, "备份数据格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        // 以现有 openid集合判断重复，仅补插入缺失的账号
        Set<String> existingOpenids = new HashSet<>();
        for (DBHelper.PlayerInfo info : dbHelper.getAllMeishiWechat()) {
            existingOpenids.add(info.openid);
        }

        int addedCount = 0;
        int skippedCount = 0;
        for (DBHelper.PlayerInfo info : imported) {
            if (existingOpenids.contains(info.openid)) {
                skippedCount++;
                continue;
            }
            dbHelper.insertMeishiWechat(info.openid, info.serverName, info.playerName);
            addedCount++;
        }

        Toast.makeText(this, "导入完成：新增" + addedCount + "条，跳过已存在" + skippedCount + "条",
                Toast.LENGTH_SHORT).show();
        // 导入成功后立即刷新账号列表
        loadAccountList();
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
        ImageButton floatButtonShow = findViewById(R.id.FloatButton_Show);
        ImageButton floatButtonAdd = findViewById(R.id.FloatButton_Add);
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

            params = (ViewGroup.MarginLayoutParams) floatButtonShow.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonShow.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonAdd.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonAdd.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());
        floatButtonShow.setOnClickListener(v -> {
            if (HyperFVMApplication.isContentBiometricAuthEnabled(this) && !isShow) {
                BiometricAuthHelper.simpleBiometricAuth(this, () -> {
                    isShow = !isShow;
                    loadAccountList();
                });
            } else {
                isShow = !isShow;
                loadAccountList();
            }
        });
        floatButtonAdd.setOnClickListener(v -> showAddLinkDialog());

        // 添加模糊材质
        setupBlurEffect();

        // 接入顶部栏滚动联动（仅手机布局含 scrollView，PAD 双栏布局结构特殊不接入）
        if (findViewById(R.id.scrollView) != null) {
            nestedScrollUtil = NestedScrollUtil.attach(
                    findViewById(R.id.scrollView),
                    findViewById(R.id.topBarBottom),
                    findViewById(R.id.topBar),
                    findViewById(R.id.blurViewTopBar),
                    TOP_BAR_FADE_RANGE_DP);
        }

        // 添加按压动画
        findViewById(R.id.tips_meishi_wechat_1).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.tips_meishi_wechat_2).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.content_meishi_wechat_rules_1).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.content_meishi_wechat_rules_2).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.content_meishi_wechat_rules_3).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.content_meishi_wechat_get_gift_rules_1).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.content_meishi_wechat_get_gift_rules_2).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.content_meishi_wechat_get_gift_rules_3).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.meishi_wechat_gift_item_1).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.meishi_wechat_gift_item_2).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.meishi_wechat_gift_item_3).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.meishi_wechat_gift_item_4).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.meishi_wechat_gift_item_5).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.meishi_wechat_gift_item_6).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.meishi_wechat_gift_item_7).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.meishi_wechat_gift_item_8).setOnTouchListener((v, event) ->
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
     * 保存顶部栏滚动联动的滚动位置，界面重建（旋转/深浅色切换）后恢复
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新构建布局
        recreate();
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
