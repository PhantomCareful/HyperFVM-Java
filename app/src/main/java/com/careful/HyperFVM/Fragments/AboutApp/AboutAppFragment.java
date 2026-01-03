package com.careful.HyperFVM.Fragments.AboutApp;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentFromAssets;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.Activities.CheckUpdateActivity;
import com.careful.HyperFVM.Activities.UsingInstructionActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.Activities.UpdateLogHistory.UpdateLogHistoryActivity;
import com.careful.HyperFVM.databinding.FragmentAboutAppBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

public class AboutAppFragment extends Fragment {

    private FragmentAboutAppBinding binding;

    private TransitionSet transition;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //初始化binding
        binding = FragmentAboutAppBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //设置顶栏标题
        setTopAppBarTitle(getResources().getString(R.string.label_about_app));

        // 初始化动画效果
        transition = new TransitionSet();
        transition.addTransition(new Fade()); // 淡入淡出
        transition.addTransition(new ChangeBounds()); // 边界变化（高度、位置）
        transition.setDuration(300); // 动画时长300ms

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

        //显示致谢名单
        getContentFromAssets(requireContext(), root.findViewById(R.id.about_app_thanks_list), "ThanksList.txt");

        //跳转使用说明的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_see_using_instruction_container), UsingInstructionActivity.class);

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

        // 初始化延迟任务，添加binding非空检查
        // 执行前检查binding是否已销毁
        Runnable transitionRunnable = () -> {
            // 执行前检查binding是否已销毁
            if (binding == null) {
                return;
            }
            TransitionManager.beginDelayedTransition(binding.aboutAppContainer, transition);
            Objects.requireNonNull(binding.aboutAppPlaceholder).setVisibility(View.GONE);
            Objects.requireNonNull(binding.aboutAppLabelThanks).setVisibility(View.VISIBLE);
            Objects.requireNonNull(binding.aboutAppThanksListContainer).setVisibility(View.VISIBLE);
            Objects.requireNonNull(binding.aboutAppLabelUsingInstruction).setVisibility(View.VISIBLE);
            Objects.requireNonNull(binding.aboutAppUsingInstructionContainer).setVisibility(View.VISIBLE);
            Objects.requireNonNull(binding.aboutAppLabelMore).setVisibility(View.VISIBLE);
            Objects.requireNonNull(binding.aboutAppMoreContainer).setVisibility(View.VISIBLE);
        };

        // 执行延迟任务
        root.postDelayed(transitionRunnable, 300);

        return root;
    }

    private void setTopAppBarTitle(String title) {
        //设置顶栏标题
        Activity activity = getActivity();
        if (activity != null) {
            MaterialToolbar toolbar = activity.findViewById(R.id.Top_AppBar);
            toolbar.setTitle(title);
        }
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
        new MaterialAlertDialogBuilder(requireContext())
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

    private void clickToNewActivity(View view, Class activity) {
        view.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), activity);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}