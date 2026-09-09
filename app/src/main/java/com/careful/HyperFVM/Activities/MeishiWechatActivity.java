package com.careful.HyperFVM.Activities;

import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;
import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eightbitlab.com.blurview.BlurView;
import okhttp3.OkHttpClient;
import okhttp3.Request; // 正确导入OkHttp的Request
import okhttp3.Response;

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

    // 在Activity中定义主线程Handler
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    // 提取openid的正则表达式
    private static final Pattern OPENID_PATTERN = Pattern.compile("openid=([^&]+)");

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

        // 初始化Handler（主线程的Looper）
        mainHandler = new Handler(Looper.getMainLooper());

        // 初始化数据库
        dbHelper = HyperFVMApplication.getDBHelper();

        // 初始化视图
        initViews();

        // 加载已保存的账号
        loadAccountList();

        // 初始化各种装饰效果
        initDecoration();
    }

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

        dialogView.findViewById(R.id.button_action).setOnClickListener(v -> {
            // 4. 处理输入内容
            if (editText != null) {
                String link = Objects.requireNonNull(editText.getText()).toString().trim();
                if (!link.isEmpty()) {
                    if (!Patterns.WEB_URL.matcher(link).matches()) {
                        Toast.makeText(this, "链接格式不正确", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Matcher matcher = OPENID_PATTERN.matcher(link);
                    if (matcher.find()) {
                        String openid = matcher.group(1);
                        // 调用网络请求方法获取区服和角色ID
                        fetchPlayerInfo(openid);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "链接有误，请检查链接格式是否正确", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "链接不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 4. 添加背景模糊
        DialogBackgroundBlurUtil.setDialogBackgroundBlur(dialog, 100);
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
        TextView serverText = cardView.findViewById(R.id.TextView_Server);
        TextView playerText = cardView.findViewById(R.id.TextView_PlayerId);
        TextView openidText = cardView.findViewById(R.id.TextView_Openid);

        serverText.setText("所在区服：" + (info.serverName != null ? info.serverName : "未知区服"));
        playerText.setText("角色ID：" + (info.playerId != null ? info.playerId : "未知角色"));
        openidText.setText("openid：" + info.openid);

        // 长按删除逻辑
        cardView.setOnLongClickListener(v -> {
            DialogBuilderManager.showDialogWithCallBack(
                    this, "删除账号", "🗑️", "确定要删除 " + (info.playerId != null ? info.playerId : info.openid) + " 吗？",
                    true, "咱手滑了", "删除", () -> {
                        dbHelper.deleteMeishiWechat(info.openid);
                        loadAccountList();
                    }
            );
            return true;
        });

        TransitionManager.beginDelayedTransition(MeishiWechatContainer, transition);
        accountListContainer.addView(cardView);
    }

    // 网络请求：获取网页内容解析区服和角色ID
    private void fetchPlayerInfo(String openid) {
        new Thread(() -> {
            String url = "http://meishi.wechat.123u.com/meishi/index?openid=" + openid;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String html = response.body().string();
                    // 子线程中仅解析数据，UI操作通过Handler切换
                    parseHtmlAndSave(openid, html);
                } else {
                    // 用Handler显示Toast（主线程）
                    mainHandler.post(() ->
                            Toast.makeText(MeishiWechatActivity.this, "获取信息失败\n服务器无响应", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (IOException e) {
                // 用Handler显示Toast（主线程）
                mainHandler.post(() ->
                        Toast.makeText(MeishiWechatActivity.this, "网络错误\n无法连接服务器", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    // 解析HTML提取区服和角色ID并保存到数据库
    private void parseHtmlAndSave(String openid, String html) {
        try {
            Pattern pattern = Pattern.compile("<h1 class=\"title\">(.*?)</h1>");
            Matcher matcher = pattern.matcher(html);

            if (matcher.find()) {
                String title = Objects.requireNonNull(matcher.group(1)).trim();
                String[] parts = title.split(" - ");

                if (parts.length == 2) {
                    String serverName = parts[0].trim();
                    String playerId = parts[1].trim();
                    // 数据库操作可在子线程执行（无需UI线程）
                    dbHelper.insertMeishiWechat(openid, serverName, playerId);
                    mainHandler.post(() -> {
                        Toast.makeText(MeishiWechatActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                        // 刷新列表
                        loadAccountList();
                    });
                    return;
                }
            }

            // 解析失败提示（UI操作，用Handler）
            mainHandler.post(() ->
                    Toast.makeText(MeishiWechatActivity.this, "解析失败\n未找到区服和角色信息", Toast.LENGTH_SHORT).show()
            );
        } catch (Exception e) {
            // 异常提示（UI操作，用Handler）
            mainHandler.post(() ->
                    DialogBuilderManager.showDialog(
                            this,
                            "出现问题",
                            "❌",
                            "以下为问题日志，请将此界面截图并向开发者反馈。\n" + e.getMessage(),
                            true,
                            "好的"
                    )
            );
        }
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

            params = (ViewGroup.MarginLayoutParams) floatButtonAdd.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonAdd.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());
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
        findViewById(R.id.content_meishi_wechat_rules_1).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.content_meishi_wechat_rules_2).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.content_meishi_wechat_rules_3).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, PressFeedbackAnimationUtils.PressFeedbackType.SINK));
        findViewById(R.id.tips_meishi_wechat).setOnTouchListener((v, event) ->
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
