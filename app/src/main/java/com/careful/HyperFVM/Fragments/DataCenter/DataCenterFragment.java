package com.careful.HyperFVM.Fragments.DataCenter;

import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.Activities.DataCenter.CardDataAuxiliaryListActivity;
import com.careful.HyperFVM.Activities.DataCenter.CardDataIndexActivity;
import com.careful.HyperFVM.Activities.DataCenter.DataImagesIndexActivity;
import com.careful.HyperFVM.Activities.MeishiWechatActivity;
import com.careful.HyperFVM.Activities.NecessaryThings.UsingInstructionActivity;
import com.careful.HyperFVM.Activities.PrestigeCalculatorActivity;
import com.careful.HyperFVM.Activities.DataCenter.TiramisuImageActivity;
import com.careful.HyperFVM.Activities.TodayLuckyActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataCenterBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.EveryMonthAndEveryWeek.EveryMonthAndEveryWeek;
import com.careful.HyperFVM.utils.ForDashboard.ExecuteDailyTasks;
import com.careful.HyperFVM.utils.ForDesign.Animation.ViewAnimationUtils;
import com.careful.HyperFVM.utils.ForUpdate.BilibiliFVMUtil;
import com.careful.HyperFVM.utils.OtherUtils.IcuHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class DataCenterFragment extends Fragment {
    private DBHelper dbHelper;
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "app_preferences";
    private static final String FIRST_RUN_KEY = "first_run";

    private View root;

    // 仪表盘部分
    private Button buttonRefreshDashboard;
    private TextView dashboardLastDayOfMonth;
    private TextView dashboardMeishiWechat;
    private TextView dashboardDoubleExplosionRate;
    private TextView dashboardFertilizationTask;
    private TextView dashboardEveryday;
    private TextView dashboardNewYear;
    private TextView dashboardBilibiliFVM;

    // 仪表盘工具类
    private EveryMonthAndEveryWeek everyMonthAndEveryWeek;
    private BilibiliFVMUtil bilibiliFVMUtil;
    private String latestBilibiliFVMUrl;

    // 查黑系统工具类
    private IcuHelper icuHelper;

    // 动画部分
    private TransitionSet transition;
    private LinearLayout dataCenterContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentDataCenterBinding binding = FragmentDataCenterBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        // 初始化数据库类
        dbHelper = new DBHelper(requireContext());

        preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 初始化仪表盘组件
        buttonRefreshDashboard = root.findViewById(R.id.ButtonRefreshDashboard);
        dashboardLastDayOfMonth = root.findViewById(R.id.dashboard_LastDayOfMonth);
        dashboardMeishiWechat = root.findViewById(R.id.dashboard_MeishiWechat);
        dashboardDoubleExplosionRate = root.findViewById(R.id.dashboard_DoubleExplosionRate);
        dashboardFertilizationTask = root.findViewById(R.id.dashboard_FertilizationTask);
        dashboardEveryday = root.findViewById(R.id.dashboard_Everyday);
        dashboardNewYear = root.findViewById(R.id.dashboard_NewYear);
        dashboardBilibiliFVM = root.findViewById(R.id.dashboard_BilibiliFVM);
        dashboardBilibiliFVM.setEnabled(false);

        // 初始化仪表盘工具类
        everyMonthAndEveryWeek = new EveryMonthAndEveryWeek();
        bilibiliFVMUtil = BilibiliFVMUtil.getInstance();
        latestBilibiliFVMUrl = null;

        // 初始化查黑工具类
        icuHelper = new IcuHelper(requireContext());

        // 初始化动画效果
        transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(800); // 动画时长800ms
        dataCenterContainer = root.findViewById(R.id.DataCenter_Container);

        // 读取数据库结果并显示
        loadResultsFromDatabase();

        // 处理每周和每月逻辑
        handleWeekAndMonthLogic();

        // 从仓库获取B站官方的最新公告
        getLatestBilibiliAnnouncement();

        // 刷新仪表盘按钮
        buttonRefreshDashboard.setOnClickListener(v -> {
            // 1. 主线程先更新UI：禁用按钮、显示“请等待”
            buttonRefreshDashboard.setEnabled(false);

            // 过渡动画 - 大的LinearLayout
            TransitionManager.beginDelayedTransition(dataCenterContainer, transition);

            dashboardMeishiWechat.setText("请等待...");
            dashboardDoubleExplosionRate.setText("请等待...");
            dashboardFertilizationTask.setText("请等待...");
            dashboardNewYear.setText("请等待...");
            dashboardEveryday.setText("请等待...");
            dashboardBilibiliFVM.setEnabled(false);

            // 2. 子线程执行：sleep 1秒 + 执行任务 + 主线程更新结果
            new Thread(() -> {
                try {
                    // 执行每日任务（耗时操作放子线程）
                    ExecuteDailyTasks executeDailyTasks = new ExecuteDailyTasks(requireContext());
                    executeDailyTasks.executeDailyTasksForRefreshDashboard();

                    // 重新从仓库获取B站官方的最新公告
                    getLatestBilibiliAnnouncement();

                    // 手动延迟1秒（让用户感知到“正在处理”，避免以为没反应）
                    Thread.sleep(1000);

                    // 3. 切回主线程更新UI：读取数据 + 恢复按钮
                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            loadResultsFromDatabase(); // 刷新仪表盘数据
                            handleWeekAndMonthLogic(); // 更新每周/每月提示
                            buttonRefreshDashboard.setEnabled(true); // 恢复按钮
                            Toast.makeText(requireContext(), "刷新完成~", Toast.LENGTH_SHORT).show(); // 可选：提示刷新完成
                        });
                    }

                } catch (InterruptedException e) {
                    // 捕获sleep中断异常
                    requireActivity().runOnUiThread(() -> {
                        buttonRefreshDashboard.setEnabled(true);
                        Toast.makeText(requireContext(), "刷新被中断", Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    // 捕获其他异常（如数据库/任务执行异常）
                    requireActivity().runOnUiThread(() -> {
                        buttonRefreshDashboard.setEnabled(true);
                        Toast.makeText(requireContext(), "刷新失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        // ------------------------------这一部分统一设置按压反馈动画 ------------------------------
        root.findViewById(R.id.ButtonRefreshDashboard).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_last_day_of_month_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_double_explosion_rate_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_everyday_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_bilibili_fvm_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_meishi_wechat_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_fertilization_task_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_new_year_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.DataCenter_CardDataIndex_Container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.DataCenter_CardDataAuxiliaryList_Container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.DataCenter_DataImagesIndex_Container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.DataCenter_TiramisuImage_Container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_tiramisu_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_faa_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_icu_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_card_calculator_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_molu_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_strategy_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_gem_calculator_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_today_lucky_container).setOnTouchListener(this::setPressAnimation);
        root.findViewById(R.id.card_prestige_calculator_container).setOnTouchListener(this::setPressAnimation);

        // ------------------------------这一部分统一设置点击事件------------------------------
        // 温馨礼包
        root.findViewById(R.id.card_meishi_wechat_container).setOnClickListener(v -> v.postDelayed(() -> {
            Intent intent = new Intent(requireActivity(), MeishiWechatActivity.class);
            startActivity(intent);
        }, 350));

        // B站最新更新公告
        root.findViewById(R.id.card_bilibili_fvm_container).setOnClickListener(v ->
                showDialogAndVisitUrl(getResources().getString(R.string.title_tools_bilibili_fvm_dialog), latestBilibiliFVMUrl));

        // 防御卡全能数据库
        root.findViewById(R.id.DataCenter_CardDataIndex_Container).setOnClickListener(v -> v.postDelayed(() -> {
            TextView DataCenter_CardDataIndex_Content =  root.findViewById(R.id.DataCenter_CardDataIndex_Content);
            DataCenter_CardDataIndex_Content.setText(getResources().getString(R.string.label_data_center_card_data_index_loading));
            Intent intent = new Intent(requireActivity(), CardDataIndexActivity.class);
            startActivity(intent);
        }, 350));

        // 增幅卡名单
        root.findViewById(R.id.DataCenter_CardDataAuxiliaryList_Container).setOnClickListener(v -> v.postDelayed(() -> {
            TextView DataCenter_CardDataAuxiliaryList_Content =  root.findViewById(R.id.DataCenter_CardDataAuxiliaryList_Content);
            DataCenter_CardDataAuxiliaryList_Content.setText(getResources().getString(R.string.label_data_center_card_data_auxiliary_list_loading));
            Intent intent = new Intent(requireActivity(), CardDataAuxiliaryListActivity.class);
            startActivity(intent);
        }, 350));

        // 数据图合集
        root.findViewById(R.id.DataCenter_DataImagesIndex_Container).setOnClickListener(v -> v.postDelayed(() -> {
            Intent intent = new Intent(requireActivity(), DataImagesIndexActivity.class);
            startActivity(intent);
        }, 350));

        // 米鼠的图
        root.findViewById(R.id.DataCenter_TiramisuImage_Container).setOnClickListener(v -> v.postDelayed(() -> {
            Intent intent = new Intent(requireActivity(), TiramisuImageActivity.class);
            startActivity(intent);
        }, 350));

        // 提拉米鼠官网
        root.findViewById(R.id.card_tiramisu_container).setOnClickListener(v ->
                showDialogAndVisitUrl(getResources().getString(R.string.title_tools_tiramisu_dialog),
                        getResources().getString(R.string.label_tools_tiramisu_url)));

        // 陌路の综合数据表
        root.findViewById(R.id.card_molu_container).setOnClickListener(v ->
                showDialogAndVisitUrl(getResources().getString(R.string.title_tools_molu_dialog),
                        getResources().getString(R.string.label_tools_molu_url)));

        // FAA米苏物流
        root.findViewById(R.id.card_faa_container).setOnClickListener(v ->
                showDialogAndVisitUrl(getResources().getString(R.string.title_tools_faa_dialog),
                        getResources().getString(R.string.label_tools_faa_url)));

        // 卡片鼠军对策表
        root.findViewById(R.id.card_strategy_container).setOnClickListener(v ->
                showDialogAndVisitUrl(getResources().getString(R.string.title_tools_strategy_dialog),
                        getResources().getString(R.string.label_tools_strategy_url)));

        // FVM查黑系统
        root.findViewById(R.id.card_icu_container).setOnClickListener(v -> showQQInputDialog());

        // 强卡最优路径计算器
        root.findViewById(R.id.card_card_calculator_container).setOnClickListener(v ->
                showDialogAndVisitUrl(getResources().getString(R.string.title_tools_card_calculator_dialog),
                        getResources().getString(R.string.label_tools_card_calculator_url)));

        // 宝石最优路径计算器
        root.findViewById(R.id.card_gem_calculator_container).setOnClickListener(v ->
                showDialogAndVisitUrl(getResources().getString(R.string.title_tools_gem_calculator_dialog),
                        getResources().getString(R.string.label_tools_gem_calculator_url)));

        // 今日运势
        root.findViewById(R.id.card_today_lucky_container).setOnClickListener(v -> v.postDelayed(() -> {
            Intent intent = new Intent(requireActivity(), TodayLuckyActivity.class);
            startActivity(intent);
        }, 350));

        // 威望计算器
        root.findViewById(R.id.card_prestige_calculator_container).setOnClickListener(v -> v.postDelayed(() -> {
            Intent intent = new Intent(requireActivity(), PrestigeCalculatorActivity.class);
            startActivity(intent);
        }, 350));

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkFirstRun();
        // 没有启用自动任务的话，才在这里执行每日任务
        if (!dbHelper.getSettingValue("自动任务")) {
            ExecuteDailyTasks executeDailyTasks = new ExecuteDailyTasks(requireContext());
            executeDailyTasks.executeDailyTasks();
        }
    }

    /**
     * 给按钮和卡片添加按压反馈动画
     * @return 是否拦截触摸事件
     */
    private boolean setPressAnimation(View v, MotionEvent event) {
        //setPress
        switch (event.getAction()) {
            // 按下：执行缩小动画（从当前大小开始）
            case MotionEvent.ACTION_DOWN:
                ViewAnimationUtils.playPressScaleAnimation(v, true);
                break;

            // 松开：执行恢复动画（从当前缩小的大小开始）
            case MotionEvent.ACTION_UP:
                ViewAnimationUtils.playPressScaleAnimation(v, false);
                break;

            // 取消（比如滑动离开View）：强制恢复动画
            case MotionEvent.ACTION_CANCEL:
                ViewAnimationUtils.playPressScaleAnimation(v, false);
                break;
        }

        return false;
    }

    /**
     * 从数据库读取结果并显示
     */
    @SuppressLint("SetTextI18n")
    private void loadResultsFromDatabase() {
        // 过渡动画 - 大的LinearLayout
        TransitionManager.beginDelayedTransition(dataCenterContainer, transition);

        // 读取礼包领取结果
        String giftResult = dbHelper.getDashboardContent("meishi_wechat_result_text");
        dashboardMeishiWechat.setText((giftResult.isEmpty() ? "null" : giftResult));
        // 读取双倍双爆结果
        String activityResult = dbHelper.getDashboardContent("double_explosion_rate");
        dashboardDoubleExplosionRate.setText((activityResult.isEmpty() ? "null" : activityResult));
        // 读取施肥活动结果
        String fertilizationTaskResult = dbHelper.getDashboardContent("fertilization_task");
        dashboardFertilizationTask.setText((fertilizationTaskResult.isEmpty() ? "null" : fertilizationTaskResult));
        // 读取美食悬赏活动结果
        String newYearResult = dbHelper.getDashboardContent("new_year");
        dashboardNewYear.setText((newYearResult.isEmpty() ? "null" : newYearResult));
    }

    /**
     * 处理每日签到提示、月末提示逻辑
     */
    @SuppressLint("SetTextI18n")
    private void handleWeekAndMonthLogic() {
        // （1）处理每日签到提示（根据1-25号/26号-月底区分显示）
        dashboardEveryday.setText(everyMonthAndEveryWeek.dailyNotifications());

        // （2）处理月末提示
        CardView card_dashboard_LastDayOfMonth = root.findViewById(R.id.card_last_day_of_month_container);
        if (everyMonthAndEveryWeek.isLastDayOfMonth()) {
            card_dashboard_LastDayOfMonth.setVisibility(View.VISIBLE);
            dashboardLastDayOfMonth.setText("月末了，请注意清空积分和金券⚠️");
        } else {
            card_dashboard_LastDayOfMonth.setVisibility(View.GONE);
        }
    }

    /**
     * 从仓库获取B站官方的最新公告
     */
    private void getLatestBilibiliAnnouncement() {
        // 启动子线程执行网络请求，避免阻塞主线程
        new Thread(() -> bilibiliFVMUtil.getLatestBilibiliFVMAnnouncement(new BilibiliFVMUtil.OnGetCallback() {
            @Override
            public void onSuccess(String content) {
                if (isAdded() && getActivity() != null) {
                    // 切换到主线程更新UI
                    requireActivity().runOnUiThread(() -> {
                        dashboardBilibiliFVM.setText("👉点击跳转B站美食大战老鼠官方的最新更新公告");
                        dashboardBilibiliFVM.setEnabled(true);
                        latestBilibiliFVMUrl = content;
                    });
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                if (isAdded() && getActivity() != null) {
                    // 切换到主线程更新UI
                    requireActivity().runOnUiThread(() -> {
                        dashboardBilibiliFVM.setText("❌获取失败，请检查网络或稍后再试");
                        dashboardBilibiliFVM.setEnabled(false);
                        latestBilibiliFVMUrl = null;
                    });
                }
            }
        })).start();
    }

    /**
     * 美食数据站：展示二次确认跳转弹窗
     * @param title 要前往的网站名字
     * @param url 网址链接
     */
    private void showDialogAndVisitUrl(String title, String url) {
        new MaterialAlertDialogBuilder(requireContext(), materialAlertDialogThemeStyleId)
                .setTitle("二次确认防误触")
                .setMessage("即将前往：" + title) // 显示要前往哪个网站
                .setPositiveButton("立即跳转\uD83E\uDD13", (dialog, which) -> {
                    // 确认后执行跳转
                    visitUrl(url);
                })
                .setNegativeButton("咱手滑了\uD83E\uDEE3", null) // 取消则不执行操作
                .show();
    }

    private void visitUrl(String url) {
        //创建打开浏览器的Intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        //启动浏览器（添加try-catch处理没有浏览器的异常）
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireActivity(), "无法打开浏览器", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 查黑系统：显示查询弹窗
     */
    private void showQQInputDialog() {
        // 加载自定义布局
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.item_dialog_input_layout_icu, null);
        // 获取布局中的输入框
        TextInputLayout inputLayout = dialogView.findViewById(R.id.inputLayout);
        TextInputEditText etQQ = (TextInputEditText) inputLayout.getEditText();

        new MaterialAlertDialogBuilder(requireContext(), materialAlertDialogThemeStyleId)
                .setTitle("查黑系统")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    if (etQQ != null) {
                        String qqNumber = Objects.requireNonNull(etQQ.getText()).toString().trim();
                        if (qqNumber.isEmpty()) {
                            Toast.makeText(requireContext(), "请输入QQ号", Toast.LENGTH_SHORT).show();
                        } else if (!qqNumber.matches("\\d+")) {
                            Toast.makeText(requireContext(), "QQ号只能包含数字", Toast.LENGTH_SHORT).show();
                        } else {
                            // 使用Icu类查询
                            icuHelper.queryFraudInfo(qqNumber, new IcuHelper.QueryCallback() {
                                @Override
                                public void onSuccess(IcuHelper.FraudResult result) {
                                    showResultDialog(result);
                                }

                                @Override
                                public void onError(String message) {
                                    new MaterialAlertDialogBuilder(requireContext(), materialAlertDialogThemeStyleId)
                                            .setTitle("查询失败")
                                            .setMessage(message)
                                            .setPositiveButton("确定", null)
                                            .show();
                                }
                            });
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 查黑系统：显示查询结果弹窗
     * @param result 把查询到的结果显示到弹窗上
     */
    private void showResultDialog(IcuHelper.FraudResult result) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext(), materialAlertDialogThemeStyleId);
        dialogBuilder.setTitle(result.isFraud ? "查询结果(骗子\uD83D\uDEAB)" : "查询结果(正常✅)");

        StringBuilder content = new StringBuilder();
        content.append("QQ号：").append(result.qq).append("\n\n");
        content.append("昵称：").append(result.nickname).append("\n\n");
        if (result.isFraud) {
            content.append("备注：").append(result.remark).append("\n\n");
            content.append("录入时间：").append(result.recordTime);
        } else {
            content.append("该QQ号暂未被标记为骗子。");
        }

        dialogBuilder.setMessage(content.toString())
                .setPositiveButton("确定", null)
                .show();
    }

    /**
     * 检查是否首次启动App
     */
    private void checkFirstRun() {
        if (preferences.getBoolean(FIRST_RUN_KEY, true)) {
            showWelcomeDialog();
            preferences.edit().putBoolean(FIRST_RUN_KEY, false).apply();
        }
    }

    private void showWelcomeDialog() {
        new MaterialAlertDialogBuilder(requireContext(), materialAlertDialogThemeStyleId)
                .setTitle("欢迎使用 HyperFVM")
                .setMessage("如果您是第一次使用，建议您先阅读使用说明，以快速了解本App。")
                .setPositiveButton("去阅读👉", (dialog, which) -> {
                    Intent intent = new Intent(requireActivity(), UsingInstructionActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("我是老手\uD83D\uDE0E", null)
                .setCancelable(false)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView DataCenter_CardDataIndex_Content =  root.findViewById(R.id.DataCenter_CardDataIndex_Content);
        DataCenter_CardDataIndex_Content.setText(getResources().getString(R.string.label_data_center_card_data_index));
        TextView DataCenter_CardDataAuxiliaryList_Content =  root.findViewById(R.id.DataCenter_CardDataAuxiliaryList_Content);
        DataCenter_CardDataAuxiliaryList_Content.setText(getResources().getString(R.string.label_data_center_card_data_auxiliary_list));
    }
}
