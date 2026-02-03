package com.careful.HyperFVM.Activities;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_PRESS_FEEDBACK_ANIMATION;
import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;
import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.ActivityMeishiWechatBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request; // 正确导入OkHttp的Request
import okhttp3.Response;

public class MeishiWechatActivity extends BaseActivity {
    private ActivityMeishiWechatBinding binding;

    private DBHelper dbHelper;
    private LinearLayout accountListContainer;
    private TextView accountCountText;

    private LinearLayout MeishiWechatContainer;
    private TransitionSet transition;

    private int pressFeedbackAnimationDelay;

    // 在Activity中定义主线程Handler
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    // 提取openid的正则表达式
    private static final Pattern OPENID_PATTERN = Pattern.compile("openid=([^&]+)");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);

        // 小白条沉浸
        EdgeToEdge.enable(this);
        if (NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }

        binding = ActivityMeishiWechatBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        // 初始化Handler（主线程的Looper）
        mainHandler = new Handler(Looper.getMainLooper());

        // 初始化数据库
        dbHelper = new DBHelper(this);

        // 初始化视图
        initViews();
        // 加载已保存的账号
        loadAccountList();

        Runnable transitionRunnable = () -> {
            TransitionManager.beginDelayedTransition(MeishiWechatContainer, transition);
            binding.TitleMeishiWechatInstructions.setVisibility(View.VISIBLE);
            binding.MeishiWechatInstructionsContainer.setVisibility(View.VISIBLE);
            binding.TitleMeishiWechatGiftContent.setVisibility(View.VISIBLE);
            binding.MeishiWechatGiftContentContainer.setVisibility(View.VISIBLE);
        };

        // 执行延迟任务
        root.postDelayed(transitionRunnable, 300);
    }

    private void initViews() {
        // 账号数量文本和列表容器
        accountCountText = findViewById(R.id.TitleMeishiWechatSavedAccount);
        accountListContainer = findViewById(R.id.LinearLayout_AccountList);

        // 配置模糊效果
        setupBlurEffect();

        // 添加按钮点击事件
        findViewById(R.id.FloatButton_MeishiWechat_Container).setOnClickListener(v -> v.postDelayed(this::showAddLinkDialog, pressFeedbackAnimationDelay));

        // 获取Markdown文本
        MarkdownUtil.getContentFromAssets(this, findViewById(R.id.TextMeishiWechatInstructions), "MeishiWechatInstructions.txt");
        MarkdownUtil.getContentFromAssets(this, findViewById(R.id.TextMeishiWechatGiftContent), "MeishiWechatGiftContent.txt");

        // 初始化动画效果
        MeishiWechatContainer = findViewById(R.id.MeishiWechatContainer);
        transition = new TransitionSet();
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.addTransition(new Fade()); // 淡入淡出
        transition.setDuration(400); // 动画时长400ms
    }

    private void showAddLinkDialog() {
        // 1. 加载自定义布局文件
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.item_dialog_input_layout_meishi_wechat, null);

        // 2. 从布局中获取TextInputLayout和输入框
        TextInputLayout inputLayout = dialogView.findViewById(R.id.inputLayout);
        TextInputEditText editText = (TextInputEditText) inputLayout.getEditText(); // 获取内部输入框

        // 3. 构建弹窗并设置自定义布局
        new MaterialAlertDialogBuilder(this, materialAlertDialogThemeStyleId)
                .setTitle("添加链接")
                .setView(dialogView) // 替换原来的setView(input)，使用自定义布局
                .setPositiveButton("确定", (dialog, which) -> {
                    // 4. 处理输入内容（与原来逻辑一致）
                    if (editText != null) {
                        String link = Objects.requireNonNull(editText.getText()).toString().trim();
                        if (!link.isEmpty()) {
                            handleLinkInput(link);
                        } else {
                            Toast.makeText(this, "请输入链接", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButton));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));

        // 顺便添加一个位移动画
        CardView cardView = findViewById(R.id.FloatButton_MeishiWechat_Container);
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                cardView,
                View.TRANSLATION_X,
                550f, 0f // 从1000px移动到0px
        );
        animator.setDuration(800);
        animator.start();

        // 顺便设置返回按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> v.postDelayed(this::finish, pressFeedbackAnimationDelay));
    }

    // 处理链接：提取openid并触发网络请求获取玩家信息
    private void handleLinkInput(String link) {
        if (!Patterns.WEB_URL.matcher(link).matches()) {
            Toast.makeText(this, "链接格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        Matcher matcher = OPENID_PATTERN.matcher(link);
        if (matcher.find()) {
            String openid = matcher.group(1);
            // 关键：调用网络请求方法获取区服和角色ID
            fetchPlayerInfo(openid);
        } else {
            Toast.makeText(this, "未找到openid，请检查链接", Toast.LENGTH_SHORT).show();
        }
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

        serverText.setText("区服：" + (info.serverName != null ? info.serverName : "未知区服"));
        playerText.setText("角色：" + (info.playerId != null ? info.playerId : "未知角色"));
        openidText.setText("openid：" + info.openid);

        // 长按删除逻辑
        cardView.setOnLongClickListener(v -> {
            new MaterialAlertDialogBuilder(this, materialAlertDialogThemeStyleId)
                    .setTitle("删除账号")
                    .setMessage("确定删除 " + (info.playerId != null ? info.playerId : info.openid) + " 吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        dbHelper.deleteMeishiWechat(info.openid);
                        loadAccountList();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });

        /*
          可能的Bug：
          因为这里是动态添加的按压动画，所以无法放到onResume阶段，可能调整设置后不生效。
          但是目前调整设置的话，这个活动一定是不会启动的，所以规避了这个Bug。
         */
        // 添加按压动画
        boolean isPressFeedbackAnimation = dbHelper.getSettingValue(CONTENT_IS_PRESS_FEEDBACK_ANIMATION);
        cardView.setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));

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
                            Toast.makeText(MeishiWechatActivity.this, "获取信息失败：服务器无响应", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (IOException e) {
                // 用Handler显示Toast（主线程）
                mainHandler.post(() ->
                        Toast.makeText(MeishiWechatActivity.this, "网络错误：无法连接服务器", Toast.LENGTH_SHORT).show()
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
                        dbHelper.updateDashboardContent("meishi_wechat_result", "失败");

                        // 刷新列表
                        loadAccountList();
                    });
                    return;
                }
            }

            // 解析失败提示（UI操作，用Handler）
            mainHandler.post(() ->
                    Toast.makeText(MeishiWechatActivity.this, "解析失败：未找到区服和角色信息", Toast.LENGTH_SHORT).show()
            );
        } catch (Exception e) {
            // 异常提示（UI操作，用Handler）
            mainHandler.post(() ->
                    Toast.makeText(MeishiWechatActivity.this, "解析错误：" + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 重新构建布局
        recreate();
    }

    /**
     * 在onResume阶段设置按压反馈动画
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onResume() {
        super.onResume();
        // 添加按压动画
        boolean isPressFeedbackAnimation;
        if (dbHelper.getSettingValue(CONTENT_IS_PRESS_FEEDBACK_ANIMATION)) {
            pressFeedbackAnimationDelay = 200;
            isPressFeedbackAnimation = true;
        } else {
            pressFeedbackAnimationDelay = 0;
            isPressFeedbackAnimation = false;
        }
        findViewById(R.id.FloatButton_MeishiWechat_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        findViewById(R.id.FloatButton_Back_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "onDestroy");
        dbHelper.close();
    }
}
