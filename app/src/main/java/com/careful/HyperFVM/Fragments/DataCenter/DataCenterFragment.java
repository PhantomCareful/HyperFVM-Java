package com.careful.HyperFVM.Fragments.DataCenter;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_BIOMETRIC_AUTH;
import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_PRESS_FEEDBACK_ANIMATION;
import static com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationHelper.setPressFeedbackAnimation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.careful.HyperFVM.Activities.PrestigeCalculatorActivity;
import com.careful.HyperFVM.Activities.DataCenter.TiramisuImageActivity;
import com.careful.HyperFVM.Activities.TodayLuckyActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDataCenterBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.EveryMonthAndEveryWeek.EveryMonthAndEveryWeek;
import com.careful.HyperFVM.utils.ForDashboard.ExecuteDailyTasks;
import com.careful.HyperFVM.utils.ForDesign.Animation.PressFeedbackAnimationUtils;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForSafety.BiometricAuthHelper;
import com.careful.HyperFVM.utils.ForUpdate.BilibiliFVMUtil;

public class DataCenterFragment extends Fragment {
    private DBHelper dbHelper;
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "app_preferences";
    private static final String FIRST_RUN_KEY = "first_run";

    private View root;

    // 仪表盘部分
    private ImageView buttonRefreshDashboard;

    private TextView dashboardLastDayOfMonth;

    private TextView dashboardDoubleExplosionRate;
    private TextView dashboardDoubleExplosionRateEmoji;
    private String doubleExplosionRateEmoji;

    private TextView dashboardMeishiWechat;
    private TextView dashboardMeishiWechatEmoji;

    private TextView dashboardBilibiliFVM;
    private TextView dashboardBilibiliFVMEmoji;
    private FrameLayout dashboardBilibiliFVMContainer;

    private TextView dashboardEveryday;
    private TextView dashboardEverydayEmoji;
    private String dashboardEverydayResult;
    private String everydayEmoji;

    private TextView dashboardFertilizationTask;
    private TextView dashboardFertilizationTaskEmoji;
    private String fertilizationTaskEmoji;

    private TextView dashboardBounty;
    private TextView dashboardBountyEmoji;
    private String bountyEmoji;

    private TextView dashboardMillionConsumption;
    private TextView dashboardMillionConsumptionEmoji;
    private String millionConsumptionEmoji;

    private TextView dashboardDailyRecharge;
    private TextView dashboardDailyRechargeEmoji;
    private String dailyRechargeEmoji;

    private TextView dashboardHappyHoliday;
    private TextView dashboardHappyHolidayEmoji;
    private String happyHolidayEmoji;

    private TextView dashboardFoodContest;
    private TextView dashboardFoodContestEmoji;
    private String foodContestEmoji;

    private TextView dashboardThreeIslands;
    private TextView dashboardThreeIslandsEmoji;
    private String threeIslandsEmoji;

    private TextView dashboardCrossServerTeamUp;
    private TextView dashboardCrossServerTeamUpEmoji;
    private String crossServerTeamUpEmoji;

    private TextView dashboardTransferDiscount;
    private TextView dashboardTransferDiscountEmoji;
    private String transferDiscountEmoji;

    private TextView dashboardLuckyMoney;
    private TextView dashboardLuckyMoneyEmoji;
    private String luckyMoneyEmoji;

    // 仪表盘工具类
    private EveryMonthAndEveryWeek everyMonthAndEveryWeek;
    private BilibiliFVMUtil bilibiliFVMUtil;
    private String latestBilibiliFVMUrl;

    private int pressFeedbackAnimationDelay;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentDataCenterBinding binding = FragmentDataCenterBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        // 初始化数据库类
        dbHelper = new DBHelper(requireContext());

        preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 初始化仪表盘组件
        buttonRefreshDashboard = root.findViewById(R.id.ButtonRefreshDashboard);
        LinearLayout dashboardRefreshDashboardContainer = root.findViewById(R.id.dashboard_RefreshDashboard_Container);

        dashboardLastDayOfMonth = root.findViewById(R.id.dashboard_LastDayOfMonth);

        dashboardDoubleExplosionRate = root.findViewById(R.id.dashboard_DoubleExplosionRate);
        dashboardDoubleExplosionRateEmoji = root.findViewById(R.id.dashboard_DoubleExplosionRate_Emoji);
        FrameLayout dashboardDoubleExplosionRateContainer = root.findViewById(R.id.dashboard_DoubleExplosionRate_Container);

        dashboardMeishiWechat = root.findViewById(R.id.dashboard_MeishiWechat);
        dashboardMeishiWechatEmoji = root.findViewById(R.id.dashboard_MeishiWechat_Emoji);
        FrameLayout dashboardMeishiWechatContainer = root.findViewById(R.id.dashboard_MeishiWechat_Container);

        dashboardBilibiliFVM = root.findViewById(R.id.dashboard_BilibiliFVM);
        dashboardBilibiliFVMEmoji = root.findViewById(R.id.dashboard_BilibiliFVM_Emoji);
        dashboardBilibiliFVMContainer = root.findViewById(R.id.dashboard_BilibiliFVM_Container);
        dashboardBilibiliFVMContainer.setEnabled(false);

        dashboardEveryday = root.findViewById(R.id.dashboard_Everyday);
        dashboardEverydayEmoji = root.findViewById(R.id.dashboard_Everyday_Emoji);
        FrameLayout dashboardEverydayContainer = root.findViewById(R.id.dashboard_Everyday_Container);

        dashboardFertilizationTask = root.findViewById(R.id.dashboard_FertilizationTask);
        dashboardFertilizationTaskEmoji = root.findViewById(R.id.dashboard_FertilizationTask_Emoji);
        FrameLayout dashboardFertilizationTaskContainer = root.findViewById(R.id.dashboard_FertilizationTask_Container);

        dashboardBounty = root.findViewById(R.id.dashboard_NewYearBounty);
        dashboardBountyEmoji = root.findViewById(R.id.dashboard_NewYearBounty_Emoji);
        FrameLayout dashboardBountyContainer = root.findViewById(R.id.dashboard_NewYearBounty_Container);

        dashboardMillionConsumption = root.findViewById(R.id.dashboard_NewYearMillionConsumption);
        dashboardMillionConsumptionEmoji = root.findViewById(R.id.dashboard_NewYearMillionConsumption_Emoji);
        FrameLayout dashboardMillionConsumptionContainer = root.findViewById(R.id.dashboard_NewYearMillionConsumption_Container);

        dashboardDailyRecharge = root.findViewById(R.id.dashboard_DailyRecharge);
        dashboardDailyRechargeEmoji = root.findViewById(R.id.dashboard_DailyRecharge_Emoji);
        FrameLayout dashboardDailyRechargeContainer = root.findViewById(R.id.dashboard_DailyRecharge_Container);

        dashboardHappyHoliday = root.findViewById(R.id.dashboard_HappyHoliday);
        dashboardHappyHolidayEmoji = root.findViewById(R.id.dashboard_HappyHoliday_Emoji);
        FrameLayout dashboardHappyHolidayContainer = root.findViewById(R.id.dashboard_HappyHoliday_Container);

        dashboardFoodContest = root.findViewById(R.id.dashboard_FoodContest);
        dashboardFoodContestEmoji = root.findViewById(R.id.dashboard_FoodContest_Emoji);
        FrameLayout dashboardFoodContestContainer = root.findViewById(R.id.dashboard_FoodContest_Container);

        dashboardThreeIslands = root.findViewById(R.id.dashboard_ThreeIslands);
        dashboardThreeIslandsEmoji = root.findViewById(R.id.dashboard_ThreeIslands_Emoji);
        FrameLayout dashboardThreeIslandsContainer = root.findViewById(R.id.dashboard_ThreeIslands_Container);

        dashboardCrossServerTeamUp = root.findViewById(R.id.dashboard_CrossServerTeamUp);
        dashboardCrossServerTeamUpEmoji = root.findViewById(R.id.dashboard_CrossServerTeamUp_Emoji);
        FrameLayout dashboardCrossServerTeamUpContainer = root.findViewById(R.id.dashboard_CrossServerTeamUp_Container);

        dashboardTransferDiscount = root.findViewById(R.id.dashboard_TransferDiscount);
        dashboardTransferDiscountEmoji = root.findViewById(R.id.dashboard_TransferDiscount_Emoji);
        FrameLayout dashboardTransferDiscountContainer = root.findViewById(R.id.dashboard_TransferDiscount_Container);

        dashboardLuckyMoney = root.findViewById(R.id.dashboard_NewYearLuckyMoney);
        dashboardLuckyMoneyEmoji = root.findViewById(R.id.dashboard_NewYearLuckyMoney_Emoji);
        FrameLayout dashboardLuckyMoneyContainer = root.findViewById(R.id.dashboard_NewYearLuckyMoney_Container);

        // 初始化仪表盘工具类
        everyMonthAndEveryWeek = new EveryMonthAndEveryWeek();
        bilibiliFVMUtil = BilibiliFVMUtil.getInstance();
        latestBilibiliFVMUrl = null;

        // 初始化查黑工具类

        // 读取数据库结果并显示
        loadResultsFromDatabase();

        // 处理每周和每月逻辑
        handleWeekAndMonthLogic();

        // 从仓库获取B站官方的最新公告
        getLatestBilibiliAnnouncement();

        // 刷新仪表盘按钮
        dashboardRefreshDashboardContainer.setOnClickListener(v -> {
            // 1. 主线程先更新UI：禁用按钮、显示“请等待”
            dashboardRefreshDashboardContainer.setEnabled(false);

            Animation rotateAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_360);
            buttonRefreshDashboard.startAnimation(rotateAnim);

            dashboardDoubleExplosionRate.setText("请等待...");
            dashboardDoubleExplosionRateEmoji.setText("⏳");
            dashboardMeishiWechat.setText("请等待...");
            dashboardMeishiWechatEmoji.setText("⏳");
            dashboardBilibiliFVM.setText("请等待...");
            dashboardBilibiliFVMEmoji.setText("⏳");
            dashboardBilibiliFVMContainer.setEnabled(false);
            dashboardEveryday.setText("请等待...");
            dashboardEverydayEmoji.setText("⏳");
            dashboardFertilizationTask.setText("请等待...");
            dashboardFertilizationTaskEmoji.setText("⏳");
            dashboardBounty.setText("请等待...");
            dashboardBountyEmoji.setText("⏳");
            dashboardMillionConsumption.setText("请等待...");
            dashboardMillionConsumptionEmoji.setText("⏳");
            dashboardDailyRecharge.setText("请等待...");
            dashboardDailyRechargeEmoji.setText("⏳");
            dashboardHappyHoliday.setText("请等待...");
            dashboardHappyHolidayEmoji.setText("⏳");
            dashboardFoodContest.setText("请等待...");
            dashboardFoodContestEmoji.setText("⏳");
            dashboardThreeIslands.setText("请等待...");
            dashboardThreeIslandsEmoji.setText("⏳");
            dashboardCrossServerTeamUp.setText("请等待...");
            dashboardCrossServerTeamUpEmoji.setText("⏳");
            dashboardTransferDiscount.setText("请等待...");
            dashboardTransferDiscountEmoji.setText("⏳");
            dashboardLuckyMoney.setText("请等待...");
            dashboardLuckyMoneyEmoji.setText("⏳");

            // 2. 子线程执行：sleep 1秒 + 执行任务 + 主线程更新结果
            new Thread(() -> {
                try {
                    // 执行每日任务（耗时操作放子线程）
                    ExecuteDailyTasks executeDailyTasks = new ExecuteDailyTasks(requireContext());
                    executeDailyTasks.executeDailyTasksForRefreshDashboard();

                    // 手动延迟1秒（让用户感知到“正在处理”，避免以为没反应）
                    Thread.sleep(1000);

                    // 重新从仓库获取B站官方的最新公告
                    getLatestBilibiliAnnouncement();

                    // 3. 切回主线程更新UI：读取数据 + 恢复按钮
                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            loadResultsFromDatabase(); // 刷新仪表盘数据
                            handleWeekAndMonthLogic(); // 更新每周/每月提示
                            dashboardRefreshDashboardContainer.setEnabled(true); // 恢复按钮
                            Toast.makeText(requireContext(), "刷新完成~", Toast.LENGTH_SHORT).show(); // 可选：提示刷新完成
                        });
                    }

                } catch (InterruptedException e) {
                    // 捕获sleep中断异常
                    requireActivity().runOnUiThread(() -> {
                        dashboardRefreshDashboardContainer.setEnabled(true);
                        Toast.makeText(requireContext(), "刷新被中断", Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    // 捕获其他异常（如数据库/任务执行异常）
                    requireActivity().runOnUiThread(() -> {
                        dashboardRefreshDashboardContainer.setEnabled(true);
                        Toast.makeText(requireContext(), "刷新失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        // ------------------------------这一部分统一设置点击事件------------------------------
        // 双爆信息
        dashboardDoubleExplosionRateContainer.setOnClickListener(v -> DialogBuilderManager.showDashboardDetailDialog(
                requireContext(),
                getResources().getString(R.string.title_dashboard_double_explosion_rate),
                doubleExplosionRateEmoji,
                dbHelper.getDashboardContent("double_explosion_rate_detail")));

        // 温馨礼包
        dashboardMeishiWechatContainer.setOnClickListener(v -> {
            if (dbHelper.getSettingValue(CONTENT_IS_BIOMETRIC_AUTH)) {
                // 指纹验证(如果开启的话)
                BiometricAuthHelper.simpleBiometricAuth(this, getResources().getString(R.string.biometric_auth_title),
                        getResources().getString(R.string.biometric_auth_sub_title), () -> {
                            // 验证成功后执行的操作
                            Intent intent = new Intent(requireActivity(), MeishiWechatActivity.class);
                            startActivity(intent);
                        });
            } else {
                // 直接进入
                Intent intent = new Intent(requireActivity(), MeishiWechatActivity.class);
                startActivity(intent);
            }
        });

        // 更新公告
        dashboardBilibiliFVMContainer.setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_tools_bilibili_fvm_dialog),
                latestBilibiliFVMUrl));

        // 每日签到
        dashboardEverydayContainer.setOnClickListener(v -> {
            String everydayContentDetail;
            if (dashboardEverydayResult.equals("可领取")) {
                everydayContentDetail = "\uD83E\uDEF0记得每天都要签到\uD83E\uDEF0\n\n本月签到礼包可以领取啦\n若有漏签请及时补签哦";
            } else {
                everydayContentDetail = "\uD83E\uDEF0记得每天都要签到\uD83E\uDEF0\n\n当前进度：" + dashboardEverydayResult;
            }
            DialogBuilderManager.showDashboardDetailDialog(
                    requireContext(),
                    getResources().getString(R.string.title_dashboard_everyday),
                    everydayEmoji,
                    everydayContentDetail);
        });

        // 施肥活动
        dashboardFertilizationTaskContainer.setOnClickListener(v -> DialogBuilderManager.showDashboardDetailDialog(
                requireContext(),
                getResources().getString(R.string.title_dashboard_fertilization_task),
                fertilizationTaskEmoji,
                dbHelper.getDashboardContent("fertilization_task_detail")));

        // 美食悬赏
        dashboardBountyContainer.setOnClickListener(v -> DialogBuilderManager.showDashboardDetailDialog(
                requireContext(),
                getResources().getString(R.string.title_dashboard_new_year_bounty),
                bountyEmoji,
                dbHelper.getDashboardContent("bounty_detail")));

        // 百万消费
        dashboardMillionConsumptionContainer.setOnClickListener(v -> DialogBuilderManager.showDashboardDetailDialogAndJumpToTiramisuImage(
                requireContext(),
                getResources().getString(R.string.title_dashboard_new_year_million_consumption),
                millionConsumptionEmoji,
                dbHelper.getDashboardContent("million_consumption_detail")));

        // 日氪
        dashboardDailyRechargeContainer.setOnClickListener(v -> DialogBuilderManager.showDashboardDetailDialogAndJumpToTiramisuImage(
                requireContext(),
                getResources().getString(R.string.title_dashboard_daily_recharge),
                dailyRechargeEmoji,
                dbHelper.getDashboardContent("daily_recharge_detail")));

        // 欢乐假期
        dashboardHappyHolidayContainer.setOnClickListener(v -> DialogBuilderManager.showDashboardDetailDialogAndJumpToTiramisuImage(
                requireContext(),
                getResources().getString(R.string.title_dashboard_happy_holiday),
                happyHolidayEmoji,
                dbHelper.getDashboardContent("happy_holiday_detail")));

        // 美食大赛
        dashboardFoodContestContainer.setOnClickListener(v -> DialogBuilderManager.showDashboardDetailDialogAndJumpToTiramisuImage(
                requireContext(),
                getResources().getString(R.string.title_dashboard_food_contest),
                foodContestEmoji,
                dbHelper.getDashboardContent("food_contest_detail")));

        // 三岛福利
        dashboardThreeIslandsContainer.setOnClickListener(v -> DialogBuilderManager.showDashboardDetailDialogAndJumpToTiramisuImage(
                requireContext(),
                getResources().getString(R.string.title_dashboard_three_islands),
                threeIslandsEmoji,
                dbHelper.getDashboardContent("three_islands_detail")));

        // 跨服助人为乐
        dashboardCrossServerTeamUpContainer.setOnClickListener(v -> DialogBuilderManager.showDashboardDetailDialog(
                requireContext(),
                getResources().getString(R.string.title_dashboard_cross_server_team_up),
                crossServerTeamUpEmoji,
                dbHelper.getDashboardContent("cross_server_team_up_detail")));

        // 二转打折
        dashboardTransferDiscountContainer.setOnClickListener(v -> DialogBuilderManager.showDashboardDetailDialog(
                requireContext(),
                getResources().getString(R.string.title_dashboard_transfer_discount),
                transferDiscountEmoji,
                dbHelper.getDashboardContent("transfer_discount_detail")));

        // 抢红包
        dashboardLuckyMoneyContainer.setOnClickListener(v -> DialogBuilderManager.showDashboardDetailDialog(
                requireContext(),
                getResources().getString(R.string.title_dashboard_new_year_lucky_money),
                luckyMoneyEmoji,
                dbHelper.getDashboardContent("lucky_money_detail")));

        // 防御卡全能数据库
        root.findViewById(R.id.DataCenter_CardDataIndex_Container).setOnClickListener(v -> v.postDelayed(() -> {
            TextView DataCenter_CardDataIndex_Content =  root.findViewById(R.id.DataCenter_CardDataIndex_Content);
            DataCenter_CardDataIndex_Content.setText(getResources().getString(R.string.label_data_center_card_data_index_loading));
            Intent intent = new Intent(requireActivity(), CardDataIndexActivity.class);
            startActivity(intent);
        }, pressFeedbackAnimationDelay));

        // 增幅卡名单
        root.findViewById(R.id.DataCenter_CardDataAuxiliaryList_Container).setOnClickListener(v -> v.postDelayed(() -> {
            TextView DataCenter_CardDataAuxiliaryList_Content =  root.findViewById(R.id.DataCenter_CardDataAuxiliaryList_Content);
            DataCenter_CardDataAuxiliaryList_Content.setText(getResources().getString(R.string.label_data_center_card_data_auxiliary_list_loading));
            Intent intent = new Intent(requireActivity(), CardDataAuxiliaryListActivity.class);
            startActivity(intent);
        }, pressFeedbackAnimationDelay));

        // 数据图合集
        root.findViewById(R.id.DataCenter_DataImagesIndex_Container).setOnClickListener(v -> v.postDelayed(() -> {
            Intent intent = new Intent(requireActivity(), DataImagesIndexActivity.class);
            startActivity(intent);
        }, pressFeedbackAnimationDelay));

        // 米鼠的图
        root.findViewById(R.id.DataCenter_TiramisuImage_Container).setOnClickListener(v -> v.postDelayed(() -> {
            Intent intent = new Intent(requireActivity(), TiramisuImageActivity.class);
            startActivity(intent);
        }, pressFeedbackAnimationDelay));

        // 提拉米鼠官网
        root.findViewById(R.id.card_tiramisu_container).setOnClickListener(v -> v.postDelayed(() -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_tools_tiramisu_dialog),
                getResources().getString(R.string.label_tools_tiramisu_url)), pressFeedbackAnimationDelay));

        // 陌路の综合数据表
        root.findViewById(R.id.card_molu_container).setOnClickListener(v -> v.postDelayed(() -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_tools_molu_dialog),
                getResources().getString(R.string.label_tools_molu_url)), pressFeedbackAnimationDelay));

        // FAA米苏物流
        root.findViewById(R.id.card_faa_container).setOnClickListener(v -> v.postDelayed(() -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_tools_faa_dialog),
                getResources().getString(R.string.label_tools_faa_url)), pressFeedbackAnimationDelay));

        // 卡片鼠军对策表
        root.findViewById(R.id.card_strategy_container).setOnClickListener(v -> v.postDelayed(() -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_tools_strategy_dialog),
                getResources().getString(R.string.label_tools_strategy_url)), pressFeedbackAnimationDelay));

        // 巅峰对决部分机制解析
        root.findViewById(R.id.card_strategy_world_boss_container).setOnClickListener(v -> v.postDelayed(() -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_tools_strategy_world_boss_dialog),
                getResources().getString(R.string.label_tools_strategy_world_boss_url)), pressFeedbackAnimationDelay));

        // FVM查黑系统
        root.findViewById(R.id.card_icu_container).setOnClickListener(v -> v.postDelayed(() -> DialogBuilderManager.showQQInputDialog(requireContext()),
                pressFeedbackAnimationDelay));

        // 强卡最优路径计算器
        root.findViewById(R.id.card_card_calculator_container).setOnClickListener(v -> v.postDelayed(() -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_tools_card_calculator_dialog),
                getResources().getString(R.string.label_tools_card_calculator_url)), pressFeedbackAnimationDelay));

        // 宝石最优路径计算器
        root.findViewById(R.id.card_gem_calculator_container).setOnClickListener(v -> v.postDelayed(() -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_tools_gem_calculator_dialog),
                getResources().getString(R.string.label_tools_gem_calculator_url)), pressFeedbackAnimationDelay));

        // 今日运势
        root.findViewById(R.id.card_today_lucky_container).setOnClickListener(v -> v.postDelayed(() -> {
            Intent intent = new Intent(requireActivity(), TodayLuckyActivity.class);
            startActivity(intent);
        }, pressFeedbackAnimationDelay));

        // 威望计算器
        root.findViewById(R.id.card_prestige_calculator_container).setOnClickListener(v -> v.postDelayed(() -> {
            Intent intent = new Intent(requireActivity(), PrestigeCalculatorActivity.class);
            startActivity(intent);
        }, pressFeedbackAnimationDelay));

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkFirstRun();
        // 没有启用自动任务的话，才在这里执行每日任务
        if (!dbHelper.getSettingValue("自动任务")) {
            new Thread(() -> {
                ExecuteDailyTasks executeDailyTasks = new ExecuteDailyTasks(requireContext());
                executeDailyTasks.executeDailyTasks();
            }).start();
        }
    }

    /**
     * 从数据库读取结果并显示
     */
    @SuppressLint("SetTextI18n")
    private void loadResultsFromDatabase() {
        // 读取双倍双爆结果
        String activityResult = dbHelper.getDashboardContent("double_explosion_rate");
        doubleExplosionRateEmoji = dbHelper.getDashboardContent("double_explosion_rate_emoji");
        dashboardDoubleExplosionRate.setText(activityResult.isEmpty() ? "null" : activityResult);
        dashboardDoubleExplosionRateEmoji.setText(doubleExplosionRateEmoji.isEmpty() ? "❌" : doubleExplosionRateEmoji);
        // 读取礼包领取结果
        String meishiWechatResult = dbHelper.getDashboardContent("meishi_wechat_result_text");
        String meishiWechatResultEmoji = dbHelper.getDashboardContent("meishi_wechat_result_emoji");
        dashboardMeishiWechat.setText(meishiWechatResult.isEmpty() ? "null" : meishiWechatResult);
        dashboardMeishiWechatEmoji.setText(meishiWechatResultEmoji.isEmpty() ? "❌" : meishiWechatResultEmoji);
        // 读取施肥活动结果
        String fertilizationTaskResult = dbHelper.getDashboardContent("fertilization_task");
        fertilizationTaskEmoji = dbHelper.getDashboardContent("fertilization_task_emoji");
        dashboardFertilizationTask.setText(fertilizationTaskResult.isEmpty() ? "null" : fertilizationTaskResult);
        dashboardFertilizationTaskEmoji.setText(fertilizationTaskEmoji.isEmpty() ? "❌" : fertilizationTaskEmoji);
        // 读取美食悬赏活动结果
        String bountyResult = dbHelper.getDashboardContent("bounty");
        bountyEmoji = dbHelper.getDashboardContent("bounty_emoji");
        dashboardBounty.setText(bountyResult.isEmpty() ? "null" : bountyResult);
        dashboardBountyEmoji.setText(bountyEmoji.isEmpty() ? "null" : bountyEmoji);
        // 读取百万消费活动结果
        String millionConsumptionResult = dbHelper.getDashboardContent("million_consumption");
        millionConsumptionEmoji = dbHelper.getDashboardContent("million_consumption_emoji");
        dashboardMillionConsumption.setText(millionConsumptionResult.isEmpty() ? "null" : millionConsumptionResult);
        dashboardMillionConsumptionEmoji.setText(millionConsumptionEmoji.isEmpty() ? "null" : millionConsumptionEmoji);
        // 读取日氪活动结果
        String dailyRechargeResult = dbHelper.getDashboardContent("daily_recharge");
        dailyRechargeEmoji = dbHelper.getDashboardContent("daily_recharge_emoji");
        dashboardDailyRecharge.setText(dailyRechargeResult.isEmpty() ? "null" : dailyRechargeResult);
        dashboardDailyRechargeEmoji.setText(dailyRechargeEmoji.isEmpty() ? "null" : dailyRechargeEmoji);
        // 读取欢乐假期活动结果
        String happyHolidayResult = dbHelper.getDashboardContent("happy_holiday");
        happyHolidayEmoji = dbHelper.getDashboardContent("happy_holiday_emoji");
        dashboardHappyHoliday.setText(happyHolidayResult.isEmpty() ? "null" : happyHolidayResult);
        dashboardHappyHolidayEmoji.setText(happyHolidayEmoji.isEmpty() ? "null" : happyHolidayEmoji);
        // 读取美食大赛活动结果
        String foodContestResult = dbHelper.getDashboardContent("food_contest");
        foodContestEmoji = dbHelper.getDashboardContent("food_contest_emoji");
        dashboardFoodContest.setText(foodContestResult.isEmpty() ? "null" : foodContestResult);
        dashboardFoodContestEmoji.setText(foodContestEmoji.isEmpty() ? "null" : foodContestEmoji);
        // 读取三岛福利活动结果
        String threeIslandsResult = dbHelper.getDashboardContent("three_islands");
        threeIslandsEmoji = dbHelper.getDashboardContent("three_islands_emoji");
        dashboardThreeIslands.setText(threeIslandsResult.isEmpty() ? "null" : threeIslandsResult);
        dashboardThreeIslandsEmoji.setText(threeIslandsEmoji.isEmpty() ? "null" : threeIslandsEmoji);
        // 读取跨服助人为乐活动结果
        String crossServerTeamUpResult = dbHelper.getDashboardContent("cross_server_team_up");
        crossServerTeamUpEmoji = dbHelper.getDashboardContent("cross_server_team_up_emoji");
        dashboardCrossServerTeamUp.setText(crossServerTeamUpResult.isEmpty() ? "null" : crossServerTeamUpResult);
        dashboardCrossServerTeamUpEmoji.setText(crossServerTeamUpEmoji.isEmpty() ? "null" : crossServerTeamUpEmoji);
        // 读取二转打折活动结果
        String transferDiscountResult = dbHelper.getDashboardContent("transfer_discount");
        transferDiscountEmoji = dbHelper.getDashboardContent("transfer_discount_emoji");
        dashboardTransferDiscount.setText(transferDiscountResult.isEmpty() ? "null" : transferDiscountResult);
        dashboardTransferDiscountEmoji.setText(transferDiscountEmoji.isEmpty() ? "null" : transferDiscountEmoji);
        // 读取抢红包活动结果
        String luckyMoneyResult = dbHelper.getDashboardContent("lucky_money");
        luckyMoneyEmoji = dbHelper.getDashboardContent("lucky_money_emoji");
        dashboardLuckyMoney.setText(luckyMoneyResult.isEmpty() ? "null" : luckyMoneyResult);
        dashboardLuckyMoneyEmoji.setText(luckyMoneyEmoji.isEmpty() ? "null" : luckyMoneyEmoji);
    }

    /**
     * 处理每日签到提示、月末提示逻辑
     */
    @SuppressLint("SetTextI18n")
    private void handleWeekAndMonthLogic() {
        // （1）处理每日签到提示（根据1-25号/26号-月底区分显示）
        dashboardEverydayResult = everyMonthAndEveryWeek.dailyNotifications();
        dashboardEveryday.setText(dashboardEverydayResult);
        if (dashboardEverydayResult.equals("可领取")) {
            everydayEmoji = "🍾";
        } else {
            everydayEmoji = "✊";
        }
        dashboardEverydayEmoji.setText(everydayEmoji);

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
                        dashboardBilibiliFVM.setText("点击跳转");
                        dashboardBilibiliFVMEmoji.setText("👉");
                        dashboardBilibiliFVMContainer.setEnabled(true);
                        latestBilibiliFVMUrl = content;
                    });
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                if (isAdded() && getActivity() != null) {
                    // 切换到主线程更新UI
                    requireActivity().runOnUiThread(() -> {
                        dashboardBilibiliFVM.setText("获取失败");
                        dashboardBilibiliFVMEmoji.setText("❌");
                        dashboardBilibiliFVM.setEnabled(false);
                        latestBilibiliFVMUrl = null;
                    });
                }
            }
        })).start();
    }

    /**
     * 检查是否首次启动App
     */
    private void checkFirstRun() {
        if (preferences.getBoolean(FIRST_RUN_KEY, true)) {
            DialogBuilderManager.showWelcomeDialog(requireContext());
            preferences.edit().putBoolean(FIRST_RUN_KEY, false).apply();
        }
    }

    /**
     * 在onResume阶段：
     * 1. 还原卡片状态
     * 2. 设置按压反馈动画
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onResume() {
        super.onResume();
        // 还原卡片状态
        TextView DataCenter_CardDataIndex_Content =  root.findViewById(R.id.DataCenter_CardDataIndex_Content);
        DataCenter_CardDataIndex_Content.setText(getResources().getString(R.string.label_data_center_card_data_index));
        TextView DataCenter_CardDataAuxiliaryList_Content =  root.findViewById(R.id.DataCenter_CardDataAuxiliaryList_Content);
        DataCenter_CardDataAuxiliaryList_Content.setText(getResources().getString(R.string.label_data_center_card_data_auxiliary_list));

        // ------------------------------这一部分统一设置按压反馈动画 ------------------------------
        boolean isPressFeedbackAnimation;
        if (dbHelper.getSettingValue(CONTENT_IS_PRESS_FEEDBACK_ANIMATION)) {
            pressFeedbackAnimationDelay = 200;
            isPressFeedbackAnimation = true;
        } else {
            pressFeedbackAnimationDelay = 0;
            isPressFeedbackAnimation = false;
        }
        root.findViewById(R.id.ButtonRefreshDashboard).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.SINK : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.card_last_day_of_month_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.DataCenter_CardDataIndex_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.DataCenter_CardDataAuxiliaryList_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.DataCenter_DataImagesIndex_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.DataCenter_TiramisuImage_Container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.card_tiramisu_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.card_faa_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.card_icu_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.card_card_calculator_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.card_molu_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.card_strategy_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.card_strategy_world_boss_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.card_gem_calculator_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.card_today_lucky_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
        root.findViewById(R.id.card_prestige_calculator_container).setOnTouchListener((v, event) ->
                setPressFeedbackAnimation(v, event, isPressFeedbackAnimation ? PressFeedbackAnimationUtils.PressFeedbackType.TILT : PressFeedbackAnimationUtils.PressFeedbackType.NONE));
    }
}
