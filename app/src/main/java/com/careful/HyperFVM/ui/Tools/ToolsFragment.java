package com.careful.HyperFVM.ui.Tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.OtherUtils.IcuHelper;
import com.careful.HyperFVM.Tools.MeishiWechatActivity;
import com.careful.HyperFVM.Tools.PrestigeCalculatorActivity;
import com.careful.HyperFVM.Tools.TodayLuckyActivity;
import com.careful.HyperFVM.databinding.FragmentToolsBinding;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDashboard.EveryMonthAndEveryWeek.EveryMonthAndEveryWeek;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class ToolsFragment extends Fragment {
    private FragmentToolsBinding binding;
    private View root;

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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToolsBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        setTopAppBarTitle(getResources().getString(R.string.label_tools));

        // 初始化控件
        dashboardMeishiWechat = root.findViewById(R.id.dashboard_MeishiWechat);
        dashboardDoubleExplosionRate = root.findViewById(R.id.dashboard_DoubleExplosionRate);
        dashboardFertilizationTask = root.findViewById(R.id.dashboard_FertilizationTask);
        dashboardNewYear = root.findViewById(R.id.dashboard_NewYear);
        dbHelper = new DBHelper(requireContext()); // 初始化数据库工具

        dashboardWednesdayAndThursday = root.findViewById(R.id.dashboard_WednesdayAndThursday);
        dashboardEveryday = root.findViewById(R.id.dashboard_Everyday);
        dashboardLastDayOfMonth = root.findViewById(R.id.dashboard_LastDayOfMonth);
        everyMonthAndEveryWeek = new EveryMonthAndEveryWeek(requireContext());

        icuHelper = new IcuHelper(requireContext()); // 初始化查黑工具

        // 读取数据库结果并显示
        loadResultsFromDatabase();

        // 处理每周和每月逻辑
        handleWeekAndMonthLogic();

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
        // 加载自定义布局
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.item_dialog_input_layout_icu, null);
        // 获取布局中的输入框
        TextInputLayout inputLayout = dialogView.findViewById(R.id.inputLayout);
        TextInputEditText etQQ = (TextInputEditText) inputLayout.getEditText();

        new MaterialAlertDialogBuilder(requireContext())
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
                                    new MaterialAlertDialogBuilder(requireContext())
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

    // 显示查询结果弹窗（MaterialYou风格）
    private void showResultDialog(IcuHelper.FraudResult result) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext());
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

    // 从数据库读取结果并显示
    @SuppressLint("SetTextI18n")
    private void loadResultsFromDatabase() {
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
        if (everyMonthAndEveryWeek.isWednesday()) {
            //dashboardWednesdayAndThursdayTitle.setVisibility(View.VISIBLE);
            //dashboardWednesdayAndThursday.setVisibility(View.VISIBLE);
            dashboardWednesdayAndThursday.setText("今天是周三\n看看下午策划要端什么💩上来🙄");
        } else if (everyMonthAndEveryWeek.isThursday()) {
            //dashboardWednesdayAndThursdayTitle.setVisibility(View.VISIBLE);
            //dashboardWednesdayAndThursday.setVisibility(View.VISIBLE);
            dashboardWednesdayAndThursday.setText("今天是周四\n10:00-12:00更新维护，请合理安排刷图时间😎");
        } else {
            //dashboardWednesdayAndThursdayTitle.setVisibility(View.INVISIBLE);
            //dashboardWednesdayAndThursday.setVisibility(View.INVISIBLE);
            CardView card_dashboard_WednesdayAndThursday = root.findViewById(R.id.card_dashboard_WednesdayAndThursday);
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
        if (everyMonthAndEveryWeek.isAugust() && everyMonthAndEveryWeek.getCurrentYear() >= 2024) {
            TextView MMW_TEXT = binding.dashboardMiaomiaowu;
            MMW_TEXT.setText("🎉🎉🎉美食妙妙屋" + (everyMonthAndEveryWeek.getCurrentYear() - 2023) + "周年🎉🎉🎉\n" +
                             "2023.8.25 - " + everyMonthAndEveryWeek.getCurrentYear() + ".8.25");
            CardView MMW = binding.cardDashboardMiaomiaowu;
            MMW.setVisibility(View.VISIBLE);
        } else {
            CardView MMW = binding.cardDashboardMiaomiaowu;
            MMW.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        icuHelper.shutdown(); // 释放资源
    }
}
