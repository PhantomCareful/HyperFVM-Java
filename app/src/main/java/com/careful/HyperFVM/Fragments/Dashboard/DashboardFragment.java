package com.careful.HyperFVM.Fragments.Dashboard;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_BIOMETRIC_AUTH;
import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_TOAST_IS_VISIBLE_REFRESH_DASHBOARD;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.Activities.MeishiWechatActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentDashboardBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.ExecuteDailyTask;
import com.careful.HyperFVM.utils.ForDashboard.FromGame.EveryMonthAndEveryWeek.EveryMonthAndEveryWeek;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForSafety.BiometricAuthHelper;
import com.careful.HyperFVM.utils.ForUpdate.BilibiliFVMUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.TimeUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardFragment extends Fragment {
    private DBHelper dbHelper;
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "app_preferences";
    private static final String FIRST_RUN_KEY = "first_run";

    private View root;

    // 缓存数据和加载状态
    private static List<Map<String, String>> sCachedData = null;
    private static boolean sDataLoaded = false;
    private static boolean isShowWorldBossCard = false;

    private LinearLayout dashboardContainer;

    // 刷新按钮
    private ImageButton buttonRefreshDashboard;

    // 仪表盘部分
    private TextView dashboardLastDayOfMonth;

    private FrameLayout dashboardDoubleExplosionRateContainer;
    private TextView dashboardDoubleExplosionRate;
    private String doubleExplosionRateEmoji;

    private FrameLayout dashboardMeishiWechatContainer;
    private TextView dashboardMeishiWechat;

    private TextView dashboardBilibiliFVM;
    private FrameLayout dashboardBilibiliFVMContainer;

    private FrameLayout dashboardEverydayContainer;
    private TextView dashboardEveryday;
    private String everydayEmoji;

    private FrameLayout dashboardFertilizationTaskContainer;
    private TextView dashboardFertilizationTask;
    private String fertilizationTaskEmoji;

    private FrameLayout dashboardBountyContainer;
    private TextView dashboardBounty;
    private String bountyEmoji;

    private FrameLayout dashboardMillionConsumptionContainer;
    private TextView dashboardMillionConsumption;
    private String millionConsumptionEmoji;

    private FrameLayout dashboardDailyRechargeContainer;
    private TextView dashboardDailyRecharge;
    private String dailyRechargeEmoji;

    private FrameLayout dashboardHappyHolidayContainer;
    private TextView dashboardHappyHoliday;
    private String happyHolidayEmoji;

    private FrameLayout dashboardFoodContestContainer;
    private TextView dashboardFoodContest;
    private String foodContestEmoji;

    private FrameLayout dashboardThreeIslandsContainer;
    private TextView dashboardThreeIslands;
    private String threeIslandsEmoji;

    private FrameLayout dashboardCrossServerTeamUpContainer;
    private TextView dashboardCrossServerTeamUp;
    private String crossServerTeamUpEmoji;

    private FrameLayout dashboardTransferDiscountContainer;
    private TextView dashboardTransferDiscount;
    private String transferDiscountEmoji;

    private FrameLayout dashboardLuckyMoneyContainer;
    private TextView dashboardLuckyMoney;
    private String luckyMoneyEmoji;

    private FrameLayout dashboardWorldBossContainer;
    private TextView dashboardWorldBoss;

    private FrameLayout dashboardCryStoneDiscountContainer;
    private TextView dashboardCryStoneDiscount;
    private String cryStoneDiscountEmoji;

    private FrameLayout dashboardWeddingDiscountContainer;
    private TextView dashboardWeddingDiscount;
    private String weddingDiscountEmoji;

    private FrameLayout dashboardCampTaskContainer;
    private TextView dashboardCampTask;
    private String campTaskEmoji;

    // 仪表盘工具类
    private EveryMonthAndEveryWeek everyMonthAndEveryWeek;
    private BilibiliFVMUtil bilibiliFVMUtil;

    private TransitionSet transition;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentDashboardBinding binding = FragmentDashboardBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        // 初始化数据库类
        dbHelper = new DBHelper(requireContext());

        preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 初始化仪表盘组件
        buttonRefreshDashboard = root.findViewById(R.id.FloatButton_Refresh);

        dashboardLastDayOfMonth = root.findViewById(R.id.dashboard_LastDayOfMonth);

        dashboardDoubleExplosionRate = root.findViewById(R.id.dashboard_DoubleExplosionRate);
        dashboardDoubleExplosionRateContainer = root.findViewById(R.id.dashboard_DoubleExplosionRate_Container);

        dashboardMeishiWechat = root.findViewById(R.id.dashboard_MeishiWechat);
        dashboardMeishiWechatContainer = root.findViewById(R.id.dashboard_MeishiWechat_Container);

        dashboardBilibiliFVM = root.findViewById(R.id.dashboard_WeeklyRecharge);
        dashboardBilibiliFVMContainer = root.findViewById(R.id.dashboard_WeeklyRecharge_Container);
        dashboardBilibiliFVMContainer.setEnabled(false);

        dashboardEveryday = root.findViewById(R.id.dashboard_Everyday);
        dashboardEverydayContainer = root.findViewById(R.id.dashboard_Everyday_Container);

        dashboardFertilizationTask = root.findViewById(R.id.dashboard_FertilizationTask);
        dashboardFertilizationTaskContainer = root.findViewById(R.id.dashboard_FertilizationTask_Container);

        dashboardBounty = root.findViewById(R.id.dashboard_NewYearBounty);
        dashboardBountyContainer = root.findViewById(R.id.dashboard_NewYearBounty_Container);

        dashboardMillionConsumption = root.findViewById(R.id.dashboard_NewYearMillionConsumption);
        dashboardMillionConsumptionContainer = root.findViewById(R.id.dashboard_NewYearMillionConsumption_Container);

        dashboardDailyRecharge = root.findViewById(R.id.dashboard_DailyRecharge);
        dashboardDailyRechargeContainer = root.findViewById(R.id.dashboard_DailyRecharge_Container);

        dashboardHappyHoliday = root.findViewById(R.id.dashboard_HappyHoliday);
        dashboardHappyHolidayContainer = root.findViewById(R.id.dashboard_HappyHoliday_Container);

        dashboardFoodContest = root.findViewById(R.id.dashboard_FoodContest);
        dashboardFoodContestContainer = root.findViewById(R.id.dashboard_FoodContest_Container);

        dashboardThreeIslands = root.findViewById(R.id.dashboard_ThreeIslands);
        dashboardThreeIslandsContainer = root.findViewById(R.id.dashboard_ThreeIslands_Container);

        dashboardCrossServerTeamUp = root.findViewById(R.id.dashboard_CrossServerTeamUp);
        dashboardCrossServerTeamUpContainer = root.findViewById(R.id.dashboard_CrossServerTeamUp_Container);

        dashboardTransferDiscount = root.findViewById(R.id.dashboard_TransferDiscount);
        dashboardTransferDiscountContainer = root.findViewById(R.id.dashboard_TransferDiscount_Container);

        dashboardLuckyMoney = root.findViewById(R.id.dashboard_NewYearLuckyMoney);
        dashboardLuckyMoneyContainer = root.findViewById(R.id.dashboard_NewYearLuckyMoney_Container);

        dashboardWorldBossContainer = root.findViewById(R.id.dashboard_WorldBoss_Container);
        dashboardWorldBoss = root.findViewById(R.id.dashboard_WorldBoss);

        dashboardCryStoneDiscountContainer = root.findViewById(R.id.dashboard_CryStoneDiscount_Container);
        dashboardCryStoneDiscount = root.findViewById(R.id.dashboard_CryStoneDiscount);

        dashboardWeddingDiscountContainer = root.findViewById(R.id.dashboard_WeddingDiscount_Container);
        dashboardWeddingDiscount = root.findViewById(R.id.dashboard_WeddingDiscount);

        dashboardCampTaskContainer = root.findViewById(R.id.dashboard_CampTask_Container);
        dashboardCampTask = root.findViewById(R.id.dashboard_CampTask);

        // 初始化仪表盘工具类
        everyMonthAndEveryWeek = new EveryMonthAndEveryWeek();
        bilibiliFVMUtil = BilibiliFVMUtil.getInstance();

        // 初始化动画效果
        dashboardContainer = root.findViewById(R.id.Dashboard_Container);
        transition = new TransitionSet();
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.addTransition(new Fade());
        transition.setDuration(400); // 动画时长400ms

        // 初始化各种装饰效果
        initDecoration();

        // 刷新仪表盘结果并显示
        loadDashboardData();

        // 处理每周和每月逻辑
        loadAndDisplayDayAndMonthData();

        // 从仓库获取B站官方的最新公告
        getLatestBilibiliAnnouncement();

        // 刷新仪表盘按钮
        buttonRefreshDashboard.setOnClickListener(v -> {
            // 清除缓存，强制重新加载
            sDataLoaded = false;
            sCachedData = null;

            // 刷新仪表盘结果并显示
            loadDashboardData();

            // 处理每周和每月逻辑
            loadAndDisplayDayAndMonthData();

            // 从仓库获取B站官方的最新公告
            getLatestBilibiliAnnouncement();
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkFirstRun();
    }

    /**
     * 从数据库读取结果并显示
     */
    @SuppressLint("SetTextI18n")
    private void loadDashboardData() {
        // 如果已有缓存，直接显示，不重新请求
        if (sDataLoaded && sCachedData != null) {
            displayDashboardData(sCachedData);
            if (isShowWorldBossCard) {
                root.findViewById(R.id.card_world_boss_container).setVisibility(View.VISIBLE);
            } else {
                root.findViewById(R.id.card_world_boss_container).setVisibility(View.GONE);
            }
            return;
        }

        final List<Map<String, String>> data = new ArrayList<>(Collections.nCopies(1, null));

        // 1. 主线程先更新UI：禁用按钮、显示“请等待”
        buttonRefreshDashboard.setEnabled(false);

        // 加载按钮旋转动画
        final boolean[] isLoadDone = {false};
        final Handler handler = new Handler(Looper.getMainLooper());
        final Animation rotateAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_360);
        Runnable animTask = new Runnable() {
            @Override
            public void run() {
                if (!isLoadDone[0]) {
                    buttonRefreshDashboard.startAnimation(rotateAnim);
                    handler.postDelayed(this, 1000); // 1秒后再判断
                }
            }
        };
        handler.post(animTask);

        dashboardDoubleExplosionRate.setText("请等待...");
        dashboardMeishiWechat.setText("请等待...");
        dashboardBilibiliFVM.setText("请等待...");
        dashboardBilibiliFVMContainer.setEnabled(false);
        dashboardEveryday.setText("请等待...");
        dashboardFertilizationTask.setText("请等待...");
        dashboardBounty.setText("请等待...");
        dashboardMillionConsumption.setText("请等待...");
        dashboardDailyRecharge.setText("请等待...");
        dashboardHappyHoliday.setText("请等待...");
        dashboardFoodContest.setText("请等待...");
        dashboardThreeIslands.setText("请等待...");
        dashboardCrossServerTeamUp.setText("请等待...");
        dashboardTransferDiscount.setText("请等待...");
        dashboardLuckyMoney.setText("请等待...");
        dashboardWorldBoss.setText("请等待...");
        dashboardCryStoneDiscount.setText("请等待...");
        dashboardWeddingDiscount.setText("请等待...");
        dashboardCampTask.setText("请等待...");

        new Thread(() -> {
            try {
                // 执行每日任务（耗时操作放子线程）
                ExecuteDailyTask executeDailyTask = new ExecuteDailyTask(requireContext());
                executeDailyTask.executeDashboardTask(result -> {
                    // 缓存数据
                    sCachedData = Collections.singletonList(result);
                    sDataLoaded = true;

                    data.set(0, result);

                    // 切回主线程更新UI：读取数据 + 恢复按钮
                    if (isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> {
                            // 更新云端内容
                            displayDashboardData(data);

                            // 恢复按钮
                            buttonRefreshDashboard.setEnabled(true);

                            // 提示刷新完成
                            if (dbHelper.getSettingBooleanValue(CONTENT_TOAST_IS_VISIBLE_REFRESH_DASHBOARD)) {
                                Toast.makeText(requireContext(), "刷新完成~", Toast.LENGTH_SHORT).show();
                            }

                            // 停止按钮旋转动画
                            isLoadDone[0] = true;
                        });
                    }
                });
            } catch (Exception e) {
                // 捕获其他异常（如数据库/任务执行异常）
                requireActivity().runOnUiThread(() -> {
                    isLoadDone[0] = true;
                    buttonRefreshDashboard.setEnabled(true);
                    Toast.makeText(requireContext(), "刷新失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * 将从云端获取到的数据显示出来
     */
    @SuppressLint("SetTextI18n")
    private void displayDashboardData(List<Map<String, String>> data) {
        // 读取双倍双爆结果
        String activityResult = data.get(0).get("resultTodayActivityInfoSimple");
        doubleExplosionRateEmoji = data.get(0).get("resultTodayActivityInfoEmoji");
        dashboardDoubleExplosionRate.setText(activityResult);
        // 设置点击打开详情弹窗
        dashboardDoubleExplosionRateContainer.setOnClickListener(v ->
                DialogBuilderManager.showDashboardDetailDialog(
                        requireContext(),
                        getResources().getString(R.string.title_dashboard_double_explosion_rate),
                        doubleExplosionRateEmoji.isEmpty() ? "❌" : doubleExplosionRateEmoji,
                        data.get(0).get("resultTodayActivityInfoContentStatus"),
                        data.get(0).get("resultTodayActivityInfoContentDetail")
                )
        );

        // 读取温馨礼包领取结果
        String meishiWechatResult = data.get(0).get("resultMeishiWechatInfoSimple");
        dashboardMeishiWechat.setText(meishiWechatResult);
        // 设置点击打开详情弹窗
        dashboardMeishiWechatContainer.setOnClickListener(v -> {
            if (dbHelper.getSettingBooleanValue(CONTENT_IS_BIOMETRIC_AUTH)) {
                // 指纹验证(如果开启的话)
                BiometricAuthHelper.simpleBiometricAuth(
                        this, getResources().getString(R.string.biometric_auth_title), getResources().getString(R.string.biometric_auth_sub_title), () -> {
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

        // 读取施肥活动结果
        String fertilizationTaskResult = data.get(0).get("resultFertilizationTaskInfoSimple");
        fertilizationTaskEmoji = data.get(0).get("resultFertilizationTaskInfoEmoji");
        dashboardFertilizationTask.setText(Objects.requireNonNull(fertilizationTaskResult).isEmpty() ? "null" : fertilizationTaskResult);
        // 设置点击打开详情弹窗
        dashboardFertilizationTaskContainer.setOnClickListener(v ->
                DialogBuilderManager.showDashboardDetailDialog(
                        requireContext(),
                        getResources().getString(R.string.title_dashboard_fertilization_task),
                        fertilizationTaskEmoji.isEmpty() ? "❌" : fertilizationTaskEmoji,
                        data.get(0).get("resultFertilizationTaskInfoContentStatus"),
                        data.get(0).get("resultFertilizationTaskInfoContentDetail")
                )
        );

        // 读取抢红包活动结果
        String luckyMoneyResult = data.get(0).get("resultLuckyConsumptionInfoSimple");
        luckyMoneyEmoji = data.get(0).get("resultLuckyConsumptionInfoEmoji");
        dashboardLuckyMoney.setText(Objects.requireNonNull(luckyMoneyResult).isEmpty() ? "null" : luckyMoneyResult);
        // 设置点击打开详情弹窗
        dashboardLuckyMoneyContainer.setOnClickListener(v ->
                DialogBuilderManager.showDashboardDetailDialog(
                        requireContext(),
                        getResources().getString(R.string.title_dashboard_new_year_lucky_money),
                        luckyMoneyEmoji.isEmpty() ? "null" : luckyMoneyEmoji,
                        data.get(0).get("resultLuckyConsumptionInfoContentStatus"),
                        data.get(0).get("resultLuckyConsumptionInfoContentDetail")
                )
        );

        // 读取美食悬赏活动结果
        String bountyResult = data.get(0).get("resultBountyInfoSimple");
        bountyEmoji = data.get(0).get("resultBountyInfoEmoji");
        dashboardBounty.setText(Objects.requireNonNull(bountyResult).isEmpty() ? "null" : bountyResult);
        // 设置点击打开详情弹窗
        dashboardBountyContainer.setOnClickListener(v ->
                DialogBuilderManager.showDashboardDetailDialogAndSeeTiramisuImage(
                        requireContext(),
                        getResources().getString(R.string.title_dashboard_new_year_bounty),
                        bountyEmoji.isEmpty() ? "null" : bountyEmoji,
                        data.get(0).get("resultBountyInfoContentStatus"),
                        data.get(0).get("resultBountyInfoContentDetail"),
                        "悬赏声望图",
                        "tiramisu_image_2_2"
                )
        );

        // 读取跨服助人为乐活动结果
        String crossServerTeamUpResult = data.get(0).get("resultServerTeamUpSimple");
        crossServerTeamUpEmoji = data.get(0).get("resultServerTeamUpEmoji");
        dashboardCrossServerTeamUp.setText(Objects.requireNonNull(crossServerTeamUpResult).isEmpty() ? "null" : crossServerTeamUpResult);
        // 设置点击打开详情弹窗
        dashboardCrossServerTeamUpContainer.setOnClickListener(v ->
                DialogBuilderManager.showDashboardDetailDialog(
                        requireContext(),
                        getResources().getString(R.string.title_dashboard_cross_server_team_up),
                        crossServerTeamUpEmoji.isEmpty() ? "❌" : crossServerTeamUpEmoji,
                        data.get(0).get("resultServerTeamUpContentStatus"),
                        data.get(0).get("resultServerTeamUpContentDetail")
                )
        );

        // 读取三岛福利活动结果
        String threeIslandsResult = data.get(0).get("resultThreeIslandsSimple");
        threeIslandsEmoji = data.get(0).get("resultThreeIslandsEmoji");
        dashboardThreeIslands.setText(Objects.requireNonNull(threeIslandsResult).isEmpty() ? "null" : threeIslandsResult);
        // 设置点击打开详情弹窗
        dashboardThreeIslandsContainer.setOnClickListener(v -> {
                    String contentStatus = data.get(0).get("resultThreeIslandsContentStatus");
                    String contentDetail = data.get(0).get("resultThreeIslandsContentDetail");
                    String cardList = data.get(0).get("resultThreeIslandsCardList");
                    if (Objects.requireNonNull(contentStatus).equals("空空如也")) {
                        DialogBuilderManager.showDashboardDetailDialogAndSeeTiramisuImage(
                                requireContext(),
                                getResources().getString(R.string.title_dashboard_three_islands),
                                threeIslandsEmoji.isEmpty() ? "❌" : threeIslandsEmoji,
                                contentDetail,
                                contentDetail,
                                "去看米鼠的图",
                                "tiramisu_image_2_1"
                        );
                    } else {
                        List<String> list = new ArrayList<>(Arrays.asList(Objects.requireNonNull(cardList).split("\\|")));
                        DialogBuilderManager.showDashboardDetailDialogAndSeeTiramisuImageHappyHolidayAndThreeIslands(
                                requireContext(),
                                getResources().getString(R.string.title_dashboard_three_islands),
                                threeIslandsEmoji,
                                contentStatus,
                                contentDetail,
                                list,
                                "tiramisu_image_2_1"
                        );
                    }
                }
        );

        // 读取美食大赛活动结果
        String foodContestResult = data.get(0).get("resultFoodContestSimple");
        foodContestEmoji = data.get(0).get("resultFoodContestEmoji");
        dashboardFoodContest.setText(Objects.requireNonNull(foodContestResult).isEmpty() ? "null" : foodContestResult);
        // 设置点击打开详情弹窗
        dashboardFoodContestContainer.setOnClickListener(v -> {
                    String contentStatus = data.get(0).get("resultFoodContestContentStatus");
                    String contentDetail = data.get(0).get("resultFoodContestContentDetail");
                    String cardList = data.get(0).get("resultFoodContestCardList");
                    if (Objects.requireNonNull(contentStatus).equals("空空如也")) {
                        DialogBuilderManager.showDashboardDetailDialog(
                                requireContext(),
                                getResources().getString(R.string.title_dashboard_food_contest),
                                foodContestEmoji.isEmpty() ? "❌" : foodContestEmoji,
                                contentStatus,
                                contentDetail
                        );
                    } else {
                        List<String> list = new ArrayList<>(Arrays.asList(Objects.requireNonNull(cardList).split("\\|")));
                        DialogBuilderManager.showDashboardDetailDialogAndSeeTiramisuImageFoodContest(
                                requireContext(),
                                getResources().getString(R.string.title_dashboard_food_contest),
                                foodContestEmoji,
                                contentStatus,
                                contentDetail,
                                list
                        );
                    }
                }
        );

        // 读取营地任务活动结果
        String campTaskResult = data.get(0).get("resultCampTaskSimple");
        campTaskEmoji = data.get(0).get("resultCampTaskEmoji");
        dashboardCampTask.setText(Objects.requireNonNull(campTaskResult).isEmpty() ? "null" : campTaskResult);
        // 设置点击打开详情弹窗
        dashboardCampTaskContainer.setOnClickListener(v ->
                DialogBuilderManager.showDashboardDetailDialogAndSeeTiramisuImageCampTask(
                        requireContext(),
                        getResources().getString(R.string.title_dashboard_camp_task),
                        campTaskEmoji.isEmpty() ? "❌" : campTaskEmoji,
                        data.get(0).get("resultCampTaskContentStatus"),
                        data.get(0).get("resultCampTaskContentDetail"),
                        data.get(0).get("resultCampTaskUrl")
                )
        );

        // 读取欢乐假期活动结果
        String happyHolidayResult = data.get(0).get("resultHappyHolidaySimple");
        happyHolidayEmoji = data.get(0).get("resultHappyHolidayEmoji");
        dashboardHappyHoliday.setText(Objects.requireNonNull(happyHolidayResult).isEmpty() ? "null" : happyHolidayResult);
        // 设置点击打开详情弹窗
        dashboardHappyHolidayContainer.setOnClickListener(v -> {
                    String contentStatus = data.get(0).get("resultHappyHolidayContentStatus");
                    String contentDetail = data.get(0).get("resultHappyHolidayContentDetail");
                    String cardList = data.get(0).get("resultHappyHolidayCardList");
                    if (Objects.requireNonNull(contentStatus).equals("空空如也")) {
                        DialogBuilderManager.showDashboardDetailDialogAndSeeTiramisuImage(
                                requireContext(),
                                getResources().getString(R.string.title_dashboard_happy_holiday),
                                happyHolidayEmoji.isEmpty() ? "null" : happyHolidayEmoji,
                                contentDetail,
                                contentDetail,
                                "去看米鼠的图",
                                "tiramisu_image_1_7"
                        );
                    } else {
                        List<String> list = new ArrayList<>(Arrays.asList(Objects.requireNonNull(cardList).split("\\|")));
                        DialogBuilderManager.showDashboardDetailDialogAndSeeTiramisuImageHappyHolidayAndThreeIslands(
                                requireContext(),
                                getResources().getString(R.string.title_dashboard_happy_holiday),
                                happyHolidayEmoji,
                                contentStatus,
                                contentDetail,
                                list,
                                "tiramisu_image_1_7"
                        );
                    }
                }
        );

        // 读取世界BOSS活动结果
        if (Objects.equals(data.get(0).get("resultWorldBossIsShow"), "true")) {
            String dashboardWorldBossTitle = data.get(0).get("resultWorldBossTitle");
            String dashboardWorldBossContentDetail = data.get(0).get("resultWorldBossContentDetail");
            String dashboardWorldBossContentStatus = data.get(0).get("resultWorldBossContentStatus");
            TextView title = root.findViewById(R.id.dashboard_WorldBoss_Title);
            TextView contentDetail = root.findViewById(R.id.dashboard_WorldBoss_Content_Detail);
            TextView contentStatus = root.findViewById(R.id.dashboard_WorldBoss_Content_Status);
            title.setText(dashboardWorldBossTitle);
            contentDetail.setText(dashboardWorldBossContentDetail);

            // 开始确定当前位于哪个阶段
            // 将所有关键时间节点转换为日期类型
            String todayDate = TimeUtil.getCurrentDate();
            String startDate = data.get(0).get("resultWorldBossStartDate");
            String challengeDate = data.get(0).get("resultWorldBossChallengeDate");
            String settlementDate = data.get(0).get("resultWorldBossSettlementDate");
            String endDate = data.get(0).get("resultWorldBossEndDate");

            TextView dashboardWorldBossContentStatus1 = root.findViewById(R.id.dashboard_WorldBoss_Content_Status_1);
            TextView dashboardWorldBossContentStatus2 = root.findViewById(R.id.dashboard_WorldBoss_Content_Status_2);
            TextView dashboardWorldBossContentStatus3 = root.findViewById(R.id.dashboard_WorldBoss_Content_Status_3);
            dashboardWorldBossContentStatus1.setText("预热期\n" + startDate);
            dashboardWorldBossContentStatus2.setText("挑战期\n" + challengeDate);
            dashboardWorldBossContentStatus3.setText("结算期\n" + settlementDate);

            Date today = TimeUtil.transformStringToDate(todayDate);
            Date start = TimeUtil.transformStringToDate(startDate);
            Date challenge = TimeUtil.transformStringToDate(challengeDate);
            Date settlement = TimeUtil.transformStringToDate(settlementDate);
            Date end = TimeUtil.transformStringToDate(endDate);
            // 获取进度条组件
            LinearProgressIndicator dashboardWorldBossLinearProgressIndicator1 = root.findViewById(R.id.dashboard_WorldBoss_LinearProgressIndicator_1);
            LinearProgressIndicator dashboardWorldBossLinearProgressIndicator2 = root.findViewById(R.id.dashboard_WorldBoss_LinearProgressIndicator_2);
            LinearProgressIndicator dashboardWorldBossLinearProgressIndicator3 = root.findViewById(R.id.dashboard_WorldBoss_LinearProgressIndicator_3);

            if (today.before(start)) {
                // 情况1：早于预热期，进度设为0
                Log.d("WorldBoss", "情况1：早于预热期");
                dashboardWorldBossLinearProgressIndicator1.setProgressCompat(0, true);// 第二个参数表示是否使用动画
                dashboardWorldBossLinearProgressIndicator2.setProgressCompat(0, true);
                dashboardWorldBossLinearProgressIndicator3.setProgressCompat(0, true);

                contentStatus.setText(dashboardWorldBossContentStatus);
                dashboardWorldBoss.setText("即将开始");
            } else if (today.before(challenge)) {
                // 情况2：晚于预热期且早于挑战期，进度设为50
                Log.d("WorldBoss", "情况2：晚于预热期且早于挑战期");
                dashboardWorldBossLinearProgressIndicator1.setProgressCompat(50, true);
                dashboardWorldBossLinearProgressIndicator2.setProgressCompat(0, true);
                dashboardWorldBossLinearProgressIndicator3.setProgressCompat(0, true);

                contentStatus.setText("预热期：新赛季即将开始，请做好准备✊");
                dashboardWorldBoss.setText("预热期");
            } else if (today.before(settlement)) {
                // 情况3：晚于挑战期且早于结算期，进度按具体天数计算
                Log.d("WorldBoss", "情况3：晚于挑战期且早于结算期");
                int duringCount = TimeUtil.calculateDaysBetween(challengeDate, todayDate);
                // 不包含结算期，天数要减1
                int length = TimeUtil.calculateDaysBetween(challengeDate, settlementDate) - 1;
                dashboardWorldBossLinearProgressIndicator1.setProgressCompat(100, true);
                dashboardWorldBossLinearProgressIndicator2.setProgressCompat(100 * duringCount / (length + 1), true);
                dashboardWorldBossLinearProgressIndicator3.setProgressCompat(0, true);

                contentStatus.setText("挑战期：第" + duringCount + "天/持续" + length + "天\n" + "每天23:50关闭入口，请注意把握时间⏰");
                dashboardWorldBoss.setText(duringCount + "/" + length);
            } else if (today.before(end)) {
                // 情况4：晚于结算期且早于结束日期，进度按具体天数计算
                Log.d("WorldBoss", "情况4：晚于结算期且早于结束日期");
                int duringCount = TimeUtil.calculateDaysBetween(settlementDate, todayDate);
                // 不包含结算期，天数要减1
                int length = TimeUtil.calculateDaysBetween(settlementDate, endDate) - 1;
                dashboardWorldBossLinearProgressIndicator1.setProgressCompat(100, true);
                dashboardWorldBossLinearProgressIndicator2.setProgressCompat(100, true);
                dashboardWorldBossLinearProgressIndicator3.setProgressCompat(100 * duringCount / (length + 1), true);

                contentStatus.setText("结算期：第" + duringCount + "天/持续" + length + "天⏳");
                dashboardWorldBoss.setText("结算期");
            } else if (today.equals(end)) {
                // 情况5：等于结束日期，进度设为50
                Log.d("WorldBoss", "情况5：等于结束日期");
                dashboardWorldBossLinearProgressIndicator1.setProgressCompat(100, true);
                dashboardWorldBossLinearProgressIndicator2.setProgressCompat(100, true);
                dashboardWorldBossLinearProgressIndicator3.setProgressCompat(100, true);

                contentStatus.setText("活动关闭：今晚22:00领奖，22:15抢购🎉");
                dashboardWorldBoss.setText("今晚领奖");
            }

            // 为按钮设置点击事件
            Log.d("WorldBoss", data.get(0).get("resultWorldBossUrlRule") == null ? "null" : Objects.requireNonNull(data.get(0).get("resultWorldBossUrlRule")));
            Log.d("WorldBoss", data.get(0).get("resultWorldBossUrlReward") == null ? "null" : Objects.requireNonNull(data.get(0).get("resultWorldBossUrlReward")));
            root.findViewById(R.id.button_action_1).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                    requireContext(), ContextCompat.getDrawable(requireContext(), R.drawable.ic_bilibili), 0,
                    getResources().getString(R.string.dialog_title_bilibili), "巅峰对决赛季规则", data.get(0).get("resultWorldBossUrlRule")
            ));
            root.findViewById(R.id.button_action_2).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                    requireContext(), ContextCompat.getDrawable(requireContext(), R.drawable.ic_bilibili), 0,
                    getResources().getString(R.string.dialog_title_bilibili), "巅峰对决赛季奖励", data.get(0).get("resultWorldBossUrlReward")
            ));

            // 设置点击打开详情卡片
            dashboardWorldBossContainer.setOnClickListener(v -> {
                        if (root.findViewById(R.id.card_world_boss_container).getVisibility() == View.GONE) {
                            TransitionManager.beginDelayedTransition(dashboardContainer, transition);
                            root.findViewById(R.id.card_world_boss_container).setVisibility(View.VISIBLE);
                            isShowWorldBossCard = true;
                        } else {
                            TransitionManager.beginDelayedTransition(dashboardContainer, transition);
                            root.findViewById(R.id.card_world_boss_container).setVisibility(View.GONE);
                            isShowWorldBossCard = false;
                        }
                    }
            );

        } else {
            dashboardWorldBoss.setText("暂无");

            // 设置点击打开详情卡片
            dashboardWorldBossContainer.setOnClickListener(null);

            TransitionManager.beginDelayedTransition(dashboardContainer, transition);
            root.findViewById(R.id.card_world_boss_container).setVisibility(View.GONE);
            isShowWorldBossCard = false;
        }

        // 读取二转打折活动结果
        String transferDiscountResult = data.get(0).get("resultTransferDiscountSimple");
        transferDiscountEmoji = data.get(0).get("resultTransferDiscountEmoji");
        dashboardTransferDiscount.setText(Objects.requireNonNull(transferDiscountResult).isEmpty() ? "null" : transferDiscountResult);
        // 设置点击打开详情弹窗
        dashboardTransferDiscountContainer.setOnClickListener(v -> {
            String contentStatus = data.get(0).get("resultTransferDiscountContentStatus");
            String contentDetail = data.get(0).get("resultTransferDiscountContentDetail");
            String cardList = data.get(0).get("resultTransferDiscountCardList");
            if (Objects.requireNonNull(contentStatus).equals("空空如也")) {
                DialogBuilderManager.showDashboardDetailDialog(
                        requireContext(),
                        getResources().getString(R.string.title_dashboard_transfer_discount),
                        transferDiscountEmoji.isEmpty() ? "null" : transferDiscountEmoji,
                        contentStatus,
                        contentDetail
                );
            } else {
                List<String> discountList = new ArrayList<>(Arrays.asList(Objects.requireNonNull(cardList).split("\\|")));
                DialogBuilderManager.showDashboardTransferDiscountDialog(
                        requireContext(),
                        getResources().getString(R.string.title_dashboard_transfer_discount),
                        transferDiscountEmoji,
                        contentStatus,
                        contentDetail,
                        discountList
                );
            }
        });

        // 读取结晶打折活动结果
        String cryStoneDiscountResult = data.get(0).get("resultCryStoneDiscountSimple");
        cryStoneDiscountEmoji = data.get(0).get("resultCryStoneDiscountEmoji");
        dashboardCryStoneDiscount.setText(Objects.requireNonNull(cryStoneDiscountResult).isEmpty() ? "null" : cryStoneDiscountResult);
        // 设置点击打开详情弹窗
        dashboardCryStoneDiscountContainer.setOnClickListener(v ->
                DialogBuilderManager.showDashboardDetailDialog(
                        requireContext(),
                        getResources().getString(R.string.title_dashboard_cry_stone_discount),
                        cryStoneDiscountEmoji.isEmpty() ? "❌" : cryStoneDiscountEmoji,
                        data.get(0).get("resultCryStoneDiscountContentStatus"),
                        data.get(0).get("resultCryStoneDiscountContentDetail")
                )
        );

        // 读取豪华婚礼打折活动结果
        String weddingDiscountResult = data.get(0).get("resultWeddingDiscountSimple");
        weddingDiscountEmoji = data.get(0).get("resultWeddingDiscountEmoji");
        dashboardWeddingDiscount.setText(Objects.requireNonNull(weddingDiscountResult).isEmpty() ? "null" : weddingDiscountResult);
        // 设置点击打开详情弹窗
        dashboardWeddingDiscountContainer.setOnClickListener(v ->
                DialogBuilderManager.showDashboardDetailDialogAndSeeTiramisuImage(
                        requireContext(),
                        getResources().getString(R.string.title_dashboard_wedding_discount),
                        weddingDiscountEmoji.isEmpty() ? "❌" : weddingDiscountEmoji,
                        data.get(0).get("resultWeddingDiscountContentStatus"),
                        data.get(0).get("resultWeddingDiscountContentDetail"),
                        "去看米鼠的图",
                        "tiramisu_image_1_6"
                )
        );

        // 读取日氪活动结果
        String dailyRechargeResult = data.get(0).get("resultDailyRechargeSimple");
        dailyRechargeEmoji = data.get(0).get("resultDailyRechargeEmoji");
        dashboardDailyRecharge.setText(Objects.requireNonNull(dailyRechargeResult).isEmpty() ? "null" : dailyRechargeResult);
        // 设置点击打开详情弹窗
        dashboardDailyRechargeContainer.setOnClickListener(v ->
                DialogBuilderManager.showDashboardDetailDialogAndSeeTiramisuImage(
                        requireContext(),
                        getResources().getString(R.string.title_dashboard_daily_recharge),
                        dailyRechargeEmoji.isEmpty() ? "❌" : dailyRechargeEmoji,
                        data.get(0).get("resultDailyRechargeContentStatus"),
                        data.get(0).get("resultDailyRechargeContentDetail"),
                        "去看米鼠的图",
                        "tiramisu_image_1_2"
                )
        );

        // 读取百万消费活动结果
        String millionConsumptionResult = data.get(0).get("resultMillionConsumptionInfoSimple");
        millionConsumptionEmoji = data.get(0).get("resultMillionConsumptionInfoEmoji");
        dashboardMillionConsumption.setText(Objects.requireNonNull(millionConsumptionResult).isEmpty() ? "null" : millionConsumptionResult);
        // 设置点击打开详情弹窗
        dashboardMillionConsumptionContainer.setOnClickListener(v ->
                DialogBuilderManager.showDashboardDetailDialogAndSeeTiramisuImageMillionConsumption(
                        requireContext(),
                        getResources().getString(R.string.title_dashboard_new_year_million_consumption),
                        millionConsumptionEmoji.isEmpty() ? "null" : millionConsumptionEmoji,
                        data.get(0).get("resultMillionConsumptionInfoContentStatus"),
                        data.get(0).get("resultMillionConsumptionInfoContentDetail")
                )
        );

        // 读取App通知
        if (Objects.equals(data.get(0).get("resultGlobalNotificationIsShow"), "true")) {
            String dashboardGlobalNotificationTitle = data.get(0).get("resultGlobalNotificationTitle");
            String dashboardGlobalNotificationContent = data.get(0).get("resultGlobalNotificationContent");
            TextView title = root.findViewById(R.id.dashboard_GlobalNotification_Title);
            TextView content = root.findViewById(R.id.dashboard_GlobalNotification_Content);
            title.setText(dashboardGlobalNotificationTitle);
            content.setText(dashboardGlobalNotificationContent);
            TransitionManager.beginDelayedTransition(dashboardContainer, transition);
            root.findViewById(R.id.card_global_notification_container).setVisibility(View.VISIBLE);
        } else {
            TransitionManager.beginDelayedTransition(dashboardContainer, transition);
            root.findViewById(R.id.card_global_notification_container).setVisibility(View.GONE);
        }
    }

    /**
     * 处理每日签到提示、月末提示逻辑
     */
    @SuppressLint("SetTextI18n")
    private void loadAndDisplayDayAndMonthData() {
        // （1）处理每日签到提示（根据1-25号/26号-月底区分显示）
        String dashboardEverydayResult = everyMonthAndEveryWeek.generateDailyCheckingContentStatus();
        dashboardEveryday.setText(dashboardEverydayResult.equals("月签礼包可领取") ? "可领取" : dashboardEverydayResult.split("：")[1]);
        if (dashboardEverydayResult.equals("可领取")) {
            everydayEmoji = "🍾";
        } else {
            everydayEmoji = "✊";
        }

        // 设置点击打开详情弹窗
        dashboardEverydayContainer.setOnClickListener(v -> {
            String everydayContentStatus = everyMonthAndEveryWeek.generateDailyCheckingContentStatus();
            String everydayContentDetail = "\uD83E\uDEF0记得每天都要签到哦\uD83E\uDEF0";

            DialogBuilderManager.showDashboardDetailDialog(
                    requireContext(),
                    getResources().getString(R.string.title_dashboard_everyday),
                    everydayEmoji,
                    everydayContentStatus,
                    everydayContentDetail
            );
        });

        // （2）处理月末提示
        CardView card_dashboard_LastDayOfMonth = root.findViewById(R.id.card_last_day_of_month_container);
        if (everyMonthAndEveryWeek.isLastDayOfMonth()) {
            card_dashboard_LastDayOfMonth.setVisibility(View.VISIBLE);
            dashboardLastDayOfMonth.setText("⚠️月末了，请注意清空积分和金券");
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
                        // 设置点击打开详情弹窗
                        dashboardBilibiliFVMContainer.setOnClickListener(v ->
                                DialogBuilderManager.showDialogAndVisitUrl(
                                        requireContext(),
                                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_bilibili),
                                        0,
                                        getResources().getString(R.string.dialog_title_bilibili),
                                        getResources().getString(R.string.dialog_sub_title_bilibili_fvm),
                                        content
                                )
                        );

                        dashboardBilibiliFVM.setText("点击跳转");
                        dashboardBilibiliFVMContainer.setEnabled(true);
                    });
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                if (isAdded() && getActivity() != null) {
                    // 切换到主线程更新UI
                    requireActivity().runOnUiThread(() -> {
                        dashboardBilibiliFVM.setText("获取失败");
                        dashboardBilibiliFVM.setEnabled(false);
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
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    private void initDecoration() {
        // 适配状态栏高度
        MaterialCardView topBarContainer = root.findViewById(R.id.TopBar_Container);
        MaterialCardView floatButtonRefreshContainer = root.findViewById(R.id.FloatButton_Refresh_Container);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(requireContext(), root, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.topMargin = height;
            topBarContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonRefreshContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonRefreshContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        InsetsUtil.setMarginHorizontal(requireContext(), dashboardContainer, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) dashboardContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            dashboardContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            topBarContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonRefreshContainer.getLayoutParams();
            params.rightMargin = layout_marginHorizontal;
            floatButtonRefreshContainer.setLayoutParams(params);
        });

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(requireContext());
        blurUtil.setBlur(root.findViewById(R.id.blurViewButtonRefresh), root.findViewById(R.id.targetView));
        blurUtil.setBlur(root.findViewById(R.id.blurViewTopBar), root.findViewById(R.id.targetView));
    }
}
