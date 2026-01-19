package com.careful.HyperFVM.Fragments.AboutApp;

import static com.careful.HyperFVM.HyperFVMApplication.materialAlertDialogThemeStyleId;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.Activities.CheckUpdateActivity;
import com.careful.HyperFVM.Activities.NecessaryThings.CoContributorTeamActivity;
import com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity;
import com.careful.HyperFVM.Activities.NecessaryThings.UsingInstructionActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.Activities.UpdateLogHistory.UpdateLogHistoryActivity;
import com.careful.HyperFVM.databinding.FragmentAboutAppBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AboutAppFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAboutAppBinding binding = FragmentAboutAppBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //一个小彩蛋🥚
        setEasterEgg(root);

        //从build.gradle中获取版本号
        getVersion(root);

        //跳转检查更新的Activity
        clickToNewActivity(root.findViewById(R.id.label_check_update), CheckUpdateActivity.class);

        //跳转浏览器，前往作者的Github主页
        root.findViewById(R.id.about_app_developer_container).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_about_app_developer_name_dialog),
                        getResources().getString(R.string.label_about_app_developer_name_url)
                )
        );

        //跳转共建团的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_co_construction_team_container), CoContributorTeamActivity.class);

        //跳转浏览器，前往陌路的哔哩哔哩主页
        root.findViewById(R.id.about_app_thanks_list_container_fvm_1).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_thanks_list_fvm_1_dialog),
                        getResources().getString(R.string.label_thanks_list_fvm_1_url)
                )
        );

        //跳转浏览器，前往夏夜的哔哩哔哩主页
        root.findViewById(R.id.about_app_thanks_list_container_fvm_2).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_thanks_list_fvm_2_dialog),
                        getResources().getString(R.string.label_thanks_list_fvm_2_url)
                )
        );

        //跳转浏览器，前往高清图楼帖子
        root.findViewById(R.id.about_app_thanks_list_container_fvm_3).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_thanks_list_fvm_3_dialog),
                        getResources().getString(R.string.label_thanks_list_fvm_3_url)
                )
        );

        //跳转浏览器，前往查黑系统网站
        root.findViewById(R.id.about_app_thanks_list_container_fvm_4).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_thanks_list_fvm_4_dialog),
                        getResources().getString(R.string.label_thanks_list_fvm_4_url)
                )
        );

        //跳转浏览器，前往miuix仓库
        root.findViewById(R.id.about_app_thanks_list_container_app_1).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_thanks_list_app_1_dialog),
                        getResources().getString(R.string.label_thanks_list_app_1_url)
                )
        );

        //跳转浏览器，前往BlurView仓库
        root.findViewById(R.id.about_app_thanks_list_container_app_2).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_thanks_list_app_2_dialog),
                        getResources().getString(R.string.label_thanks_list_app_2_url)
                )
        );

        //跳转浏览器，前往ZoomImageView仓库
        root.findViewById(R.id.about_app_thanks_list_container_app_3).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_thanks_list_app_3_dialog),
                        getResources().getString(R.string.label_thanks_list_app_3_url)
                )
        );

        //跳转浏览器，前往SpringBackScrollView文章
        root.findViewById(R.id.about_app_thanks_list_container_app_4).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_thanks_list_app_4_dialog),
                        getResources().getString(R.string.label_thanks_list_app_4_url)
                )
        );

        //跳转使用说明的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_using_instruction_container), UsingInstructionActivity.class);

        //跳转设置的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_settings_container), SettingsActivity.class);

        //跳转浏览器，前往App的Github主页
        root.findViewById(R.id.about_app_github_container).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_about_app_github_dialog),
                        getResources().getString(R.string.label_about_app_github_url)
                )
        );

        //跳转浏览器，获取软件更新
        root.findViewById(R.id.about_app_get_update_123pan_container).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_about_app_get_update_123pan_dialog),
                        getResources().getString(R.string.label_about_app_get_update_123pan_url)
                )
        );

        //跳转浏览器，前往作者B站主页
        root.findViewById(R.id.about_app_bilibili_container).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_about_app_bilibili_dialog),
                        getResources().getString(R.string.label_about_app_bilibili_url)
                )
        );

        //跳转浏览器，前往App腾讯频道
        root.findViewById(R.id.about_app_tencent_channel_container).setOnClickListener(v ->
                showDialogAndVisitUrl(
                        getResources().getString(R.string.title_about_app_tencent_channel_dialog),
                        getResources().getString(R.string.label_about_app_tencent_channel_url)
                )
        );

        //查看历史更新日志
        clickToNewActivity(root.findViewById(R.id.about_app_see_update_log_history), UpdateLogHistoryActivity.class);

        return root;
    }

    private void setEasterEgg(View root) {
        ImageView imageView = root.findViewById(R.id.about_app_icon);
        imageView.setOnClickListener(v -> Toast.makeText(v.getContext(), "Make FVM Great Again\uD83C\uDF89\uD83C\uDF89\uD83C\uDF89", Toast.LENGTH_SHORT).show());
    }

    private void getVersion(View root) {
        // 获取version信息
        long versionCode = 0;
        String versionName = "0.0.0";

        // 获取versionCode
        try {
            versionCode = requireActivity().getPackageManager()
                    .getPackageInfo(requireActivity().getPackageName(), 0)
                    .getLongVersionCode();
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        // 获取versionName
        try {
            versionName = requireActivity().getPackageManager()
                    .getPackageInfo(requireActivity().getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        // 判断是否为Beta版
        String versionSuffix = "";
        String[] versionParts = null; // 分割版本号
        if (versionName != null) {
            versionParts = versionName.split("\\.");
        }
        // 确保版本号格式正确（至少3段）
        if (versionParts != null && versionParts.length >= 3) {
            try {
                int c = Integer.parseInt(versionParts[2]);
                if (c != 0) {
                    versionSuffix = " | Beta"; // 不为0时添加Beta标识
                } else {
                    versionSuffix = " | Release"; // 不为0时添加Release标识
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // 拼接最终版本信息
        TextView version_info = root.findViewById(R.id.version_info);
        String versionInfo = versionName + "(" + versionCode + ")" + versionSuffix;
        version_info.setText(versionInfo);
    }

    private void showDialogAndVisitUrl(String title, String url) {
        new MaterialAlertDialogBuilder(requireContext(), materialAlertDialogThemeStyleId)
                .setTitle("二次确认防误触")
                .setMessage("即将前往：\n" + title) // 显示要前往哪个网站
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

    private void clickToNewActivity(View view, Class activity) {
        view.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), activity);
            startActivity(intent);
        });
    }
}