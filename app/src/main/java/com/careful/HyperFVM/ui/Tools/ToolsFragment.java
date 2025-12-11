package com.careful.HyperFVM.ui.Tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.ui.Dialog.CommonDialogFragment;
import com.careful.HyperFVM.utils.ForDashboard.ExecuteDailyTasks;
import com.careful.HyperFVM.utils.OtherUtils.IcuHelper;
import com.careful.HyperFVM.Activities.MeishiWechatActivity;
import com.careful.HyperFVM.Activities.PrestigeCalculatorActivity;
import com.careful.HyperFVM.Activities.TodayLuckyActivity;
import com.careful.HyperFVM.databinding.FragmentToolsBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.EveryMonthAndEveryWeek.EveryMonthAndEveryWeek;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ToolsFragment extends Fragment {
    private FragmentToolsBinding binding;
    private View root;

    private CommonDialogFragment dialog;

    private Button buttonRefreshDashboard;

    private TextView dashboardMeishiWechat;
    private TextView dashboardDoubleExplosionRate;
    private TextView dashboardFertilizationTask;
    private TextView dashboardNewYear;
    private DBHelper dbHelper; //读取dashboard表

    private TextView dashboardWednesdayAndThursday;
    private TextView dashboardEveryday;
    private TextView dashboardLastDayOfMonth;
    private EveryMonthAndEveryWeek everyMonthAndEveryWeek;

    private IcuHelper icuHelper;

    private TransitionSet transition;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToolsBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        setTopAppBarTitle(getResources().getString(R.string.label_tools));

        // 初始化控件
        buttonRefreshDashboard = root.findViewById(R.id.ButtonRefreshDashboard);

        dashboardMeishiWechat = root.findViewById(R.id.dashboard_MeishiWechat);
        dashboardDoubleExplosionRate = root.findViewById(R.id.dashboard_DoubleExplosionRate);
        dashboardFertilizationTask = root.findViewById(R.id.dashboard_FertilizationTask);
        dashboardNewYear = root.findViewById(R.id.dashboard_NewYear);
        dbHelper = new DBHelper(requireContext()); // 初始化数据库工具

        dashboardWednesdayAndThursday = root.findViewById(R.id.dashboard_WednesdayAndThursday);
        dashboardEveryday = root.findViewById(R.id.dashboard_Everyday);
        dashboardLastDayOfMonth = root.findViewById(R.id.dashboard_LastDayOfMonth);
        everyMonthAndEveryWeek = new EveryMonthAndEveryWeek();

        icuHelper = new IcuHelper(requireContext()); // 初始化查黑工具

        // 初始化动画效果
        transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(800); // 动画时长300ms

        // 读取数据库结果并显示
        loadResultsFromDatabase();

        // 处理每周和每月逻辑
        handleWeekAndMonthLogic();

        //刷新仪表盘按钮
        buttonRefreshDashboard.setOnClickListener(v -> {
            // 1. 主线程先更新UI：禁用按钮、显示“请等待”
            buttonRefreshDashboard.setEnabled(false);

            // 过渡动画 - 大的LinearLayout
            LinearLayout dashboard_Container = root.findViewById(R.id.dashboard_Container);
            TransitionManager.beginDelayedTransition(dashboard_Container, transition);


            dashboardMeishiWechat.setText("请等待...");
            dashboardDoubleExplosionRate.setText("请等待...");
            dashboardFertilizationTask.setText("请等待...");
            dashboardNewYear.setText("请等待...");
            dashboardEveryday.setText("请等待...");
            dashboardWednesdayAndThursday.setText("请等待...");

            // 2. 子线程执行：sleep 1秒 + 执行任务 + 主线程更新结果
            new Thread(() -> {
                try {
                    // 执行每日任务（耗时操作放子线程）
                    ExecuteDailyTasks executeDailyTasks = new ExecuteDailyTasks(requireContext());
                    executeDailyTasks.executeDailyTasksForRefreshDashboard();

                    // 手动延迟1秒（让用户感知到“正在处理”，避免以为没反应）
                    Thread.sleep(1000);

                    // 3. 切回主线程更新UI：读取数据 + 恢复按钮
                    requireActivity().runOnUiThread(() -> {
                        loadResultsFromDatabase(); // 刷新仪表盘数据
                        handleWeekAndMonthLogic(); // 更新每周/每月提示
                        buttonRefreshDashboard.setEnabled(true); // 恢复按钮
                        Toast.makeText(requireContext(), "刷新完成~", Toast.LENGTH_SHORT).show(); // 可选：提示刷新完成
                    });

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

        //今日运势
        root.findViewById(R.id.Img_ToolsTodayLuckyButton).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), TodayLuckyActivity.class);
            startActivity(intent);
        });

        //温馨礼包
        root.findViewById(R.id.Img_ToolsMeishiWechatButton).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), MeishiWechatActivity.class);
            startActivity(intent);
        });

        //威望计算器
        root.findViewById(R.id.Img_ToolsPrestigeCalculatorButton).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), PrestigeCalculatorActivity.class);
            startActivity(intent);
        });

        //查黑系统
        root.findViewById(R.id.Img_ToolsIcuButton).setOnClickListener(v -> showQQInputDialog());

        //妙屋满勤公会点公示
        root.findViewById(R.id.Img_ToolsMMWFullContributionButton).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("二次确认防误触")
                .setMessage("即将前往：" + getResources().getString(R.string.label_tools_MMW_full_contribution)) // 显示链接预览
                .setPositiveButton("确定", (dialog, which) -> {
                    // 确认后执行跳转
                    visitUrl(getResources().getString(R.string.label_tools_MMW_full_contribution_url));
                })
                .setNegativeButton("取消", null) // 取消则不执行操作
                .show());

        //妙屋值班时间表
        root.findViewById(R.id.Img_ToolsMMWHelperButton).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("二次确认防误触")
                .setMessage("即将前往：" + getResources().getString(R.string.label_tools_MMW_helper)) // 显示链接预览
                .setPositiveButton("确定", (dialog, which) -> {
                    // 确认后执行跳转
                    visitUrl(getResources().getString(R.string.label_tools_MMW_helper_url));
                })
                .setNegativeButton("取消", null) // 取消则不执行操作
                .show());

        //提拉米鼠官网
        root.findViewById(R.id.Img_ToolsTiramisuButton).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("二次确认防误触")
                .setMessage("即将前往：" + getResources().getString(R.string.label_tools_mishu)) // 显示链接预览
                .setPositiveButton("确定", (dialog, which) -> {
                    // 确认后执行跳转
                    visitUrl(getResources().getString(R.string.label_tools_mishu_url));
                })
                .setNegativeButton("取消", null) // 取消则不执行操作
                .show());

        //提拉米鼠官网
        root.findViewById(R.id.Img_ToolsStrategyButton).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("二次确认防误触")
                .setMessage("即将前往：" + getResources().getString(R.string.label_tools_strategy)) // 显示链接预览
                .setPositiveButton("确定", (dialog, which) -> {
                    // 确认后执行跳转
                    visitUrl(getResources().getString(R.string.label_tools_strategy_url));
                })
                .setNegativeButton("取消", null) // 取消则不执行操作
                .show());

        //陌路の综合数据表
        root.findViewById(R.id.Img_ToolsMoluButton).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("二次确认防误触")
                .setMessage("即将前往：" + getResources().getString(R.string.label_tools_molu)) // 显示链接预览
                .setPositiveButton("确定", (dialog, which) -> {
                    // 确认后执行跳转
                    visitUrl(getResources().getString(R.string.label_tools_molu_url));
                })
                .setNegativeButton("取消", null) // 取消则不执行操作
                .show());

        //FAA米苏物流
        root.findViewById(R.id.Img_ToolsFAAButton).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("二次确认防误触")
                .setMessage("即将前往：" + getResources().getString(R.string.label_tools_faa)) // 显示链接预览
                .setPositiveButton("确定", (dialog, which) -> {
                    // 确认后执行跳转
                    visitUrl(getResources().getString(R.string.label_tools_faa_url));
                })
                .setNegativeButton("取消", null) // 取消则不执行操作
                .show());

        //强卡最优路径计算器
        root.findViewById(R.id.Img_ToolsCardCalculatorButton).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("二次确认防误触")
                .setMessage("即将前往：" + getResources().getString(R.string.label_tools_card_calculator_long)) // 显示链接预览
                .setPositiveButton("确定", (dialog, which) -> {
                    // 确认后执行跳转
                    visitUrl(getResources().getString(R.string.label_tools_card_calculator_url));
                })
                .setNegativeButton("取消", null) // 取消则不执行操作
                .show());

        //宝石最优路径计算器
        root.findViewById(R.id.Img_ToolsGemCalculatorButton).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("二次确认防误触")
                .setMessage("即将前往：" + getResources().getString(R.string.label_tools_gem_calculator_long)) // 显示链接预览
                .setPositiveButton("确定", (dialog, which) -> {
                    // 确认后执行跳转
                    visitUrl(getResources().getString(R.string.label_tools_gem_calculator_url));
                })
                .setNegativeButton("取消", null) // 取消则不执行操作
                .show());

        //美食换装模拟器
        root.findViewById(R.id.Img_ToolsNuannuanButton).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("二次确认防误触")
                .setMessage("即将前往：" + getResources().getString(R.string.label_tools_nuannuan)) // 显示链接预览
                .setPositiveButton("确定", (dialog, which) -> {
                    // 确认后执行跳转
                    visitUrl(getResources().getString(R.string.label_tools_nuannuan_url));
                })
                .setNegativeButton("取消", null) // 取消则不执行操作
                .show());

        return root;
    }

    private void setTopAppBarTitle(String title) {
        Activity activity = getActivity();
        if (activity != null) {
            MaterialToolbar toolbar = activity.findViewById(R.id.Top_AppBar);
            toolbar.setTitle(title);
        }
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

    private void showQQInputDialog() {
        // 关闭可能存在的弹窗（关键：避免叠加）
        dismissAllDialogs();

        dialog = new CommonDialogFragment.Builder()
                .setTitle("查黑系统")
                .setCustomLayout(R.layout.item_dialog_input_layout_icu)
                .setPositiveButtonText("查询")
                .setNegativeButtonText("取消")
                .build();

        // 设置回调
        dialog.setCallback(new CommonDialogFragment.Callback() {
            @Override
            public void onPositiveClick(String qqNumber) {
                if (qqNumber.isEmpty()) {
                    Toast.makeText(requireContext(), "请输入QQ号", Toast.LENGTH_SHORT).show();
                } else if (!qqNumber.matches("\\d+")) {
                    Toast.makeText(requireContext(), "QQ号只能包含数字", Toast.LENGTH_SHORT).show();
                } else {
                    icuHelper.queryFraudInfo(qqNumber, new IcuHelper.QueryCallback() {
                        @Override
                        public void onSuccess(IcuHelper.FraudResult result) {
                            showResultDialog(result);
                        }

                        @Override
                        public void onError(String message) {
                            dialog = new CommonDialogFragment.Builder()
                                    .setTitle("查询失败")
                                    .setPositiveButtonText("确定")
                                    .build();
                        }
                    });
                }
            }
        });

        // 显示弹窗（使用getChildFragmentManager，与当前Fragment生命周期绑定）
        if (isAdded() && !isDetached()) {
            dialog.show(getChildFragmentManager(), "qq_input_dialog");
        }
    }

    // 显示查询结果弹窗
    private void showResultDialog(IcuHelper.FraudResult result) {
        // 构建结果弹窗
        StringBuilder content = new StringBuilder();
        content.append("QQ号：").append(result.qq).append("\n\n");
        content.append("昵称：").append(result.nickname).append("\n\n");
        if (result.isFraud) {
            content.append("备注：").append(result.remark).append("\n\n");
            content.append("录入时间：").append(result.recordTime);
        } else {
            content.append("该QQ号暂未被标记为骗子。");
        }

        CommonDialogFragment resultDialog = new CommonDialogFragment.Builder()
                .setTitle(result.isFraud ? "查询结果(骗子\uD83D\uDEAB)" : "查询结果(正常✅)")
                .setMessage(content.toString())
                .setPositiveButtonText("确定")
                .build();

        if (isAdded() && !isDetached()) {
            resultDialog.show(getChildFragmentManager(), "result_dialog");
        }
    }

    // 从数据库读取结果并显示
    @SuppressLint("SetTextI18n")
    private void loadResultsFromDatabase() {
        // 过渡动画 - 大的LinearLayout
        LinearLayout dashboard_Container = root.findViewById(R.id.dashboard_Container);
        TransitionManager.beginDelayedTransition(dashboard_Container, transition);

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

    // 处理周三/周四、每日签到提示、月末提示逻辑
    @SuppressLint("SetTextI18n")
    private void handleWeekAndMonthLogic() {
        // （1）处理周三/周四显示逻辑
        CardView card_dashboard_WednesdayAndThursday = root.findViewById(R.id.card_dashboard_WednesdayAndThursday);
        if (everyMonthAndEveryWeek.isWednesday()) {
            dashboardWednesdayAndThursday.setText("今天是周三\n看看下午策划要端什么💩上来🙄");
            card_dashboard_WednesdayAndThursday.setVisibility(View.VISIBLE);
        } else if (everyMonthAndEveryWeek.isThursday()) {
            dashboardWednesdayAndThursday.setText("今天是周四\n10:00-12:00更新维护，请合理安排刷图时间😎");
            card_dashboard_WednesdayAndThursday.setVisibility(View.VISIBLE);
        } else {
            card_dashboard_WednesdayAndThursday.setVisibility(View.GONE);
        }

        // （2）处理每日签到提示（根据1-25号/26号-月底区分显示）
        dashboardEveryday.setText(everyMonthAndEveryWeek.dailyNotifications());

        // （3）处理月末提示
        CardView card_dashboard_LastDayOfMonth = root.findViewById(R.id.card_dashboard_LastDayOfMonth);
        if (everyMonthAndEveryWeek.isLastDayOfMonth()) {
            card_dashboard_LastDayOfMonth.setVisibility(View.VISIBLE);
            dashboardLastDayOfMonth.setText("月末了，请注意清空积分和金券⚠️");
        } else {
            card_dashboard_LastDayOfMonth.setVisibility(View.GONE);
        }

        // （4）处理8月公会周年庆提示
        CardView MMW = binding.cardDashboardMiaomiaowu;
        if (everyMonthAndEveryWeek.isAugust() && everyMonthAndEveryWeek.getCurrentYear() >= 2024) {
            TextView MMW_TEXT = binding.dashboardMiaomiaowu;
            MMW_TEXT.setText("🎉🎉🎉美食妙妙屋" + (everyMonthAndEveryWeek.getCurrentYear() - 2023) + "周年🎉🎉🎉\n" +
                             "2023.8.25 - " + everyMonthAndEveryWeek.getCurrentYear() + ".8.25");
            MMW.setVisibility(View.VISIBLE);
        } else {
            MMW.setVisibility(View.GONE);
        }
    }

    // 关闭所有弹窗
    private void dismissAllDialogs() {
        // 方式1：通过FragmentManager关闭所有弹窗
        getChildFragmentManager().getFragments().forEach(fragment -> {
            if (fragment instanceof CommonDialogFragment) {
                ((CommonDialogFragment) fragment).dismiss();
            }
        });
    }

    // 在生命周期关键节点关闭弹窗
    @Override
    public void onPause() {
        super.onPause();
        dismissAllDialogs(); // 页面不可见时关闭
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissAllDialogs(); // View销毁时关闭
        if (icuHelper != null) {
            icuHelper.shutdown(); // 释放资源
        }
        binding = null;
    }
}
