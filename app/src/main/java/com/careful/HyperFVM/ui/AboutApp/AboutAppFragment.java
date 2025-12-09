package com.careful.HyperFVM.ui.AboutApp;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;

import android.app.Activity;
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

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.Activities.UpdateLogHistory.UpdateLogHistoryActivity;
import com.careful.HyperFVM.databinding.FragmentAboutAppBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AboutAppFragment extends Fragment {

    private FragmentAboutAppBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //初始化binding
        binding = FragmentAboutAppBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //设置顶栏标题
        setTopAppBarTitle(getResources().getString(R.string.label_about_app));

        //一个小彩蛋🥚
        setEasterEgg(root);

        //从build.gradle中获取版本号
        getVersion(root);

        //显示致谢名单和当前版本更新日志
        getContent(requireContext(), root.findViewById(R.id.about_app_thanks_list), "ThanksList.txt");
        getContent(requireContext(), root.findViewById(R.id.about_app_current_update_log), "CurrentUpdateLog.txt");

        //跳转浏览器，前往App的Github主页
        jumpToGithub(root);

        //跳转浏览器，获取软件更新
        getNewVersion(root);

        //查看历史更新日志
        seeUpdateLogHistory(root);

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

    private void jumpToGithub(View root) {
        TextView textView = root.findViewById(R.id.text_about_app_github);
        textView.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("二次确认防误触")
                .setMessage("点击确定，前往HyperFVM的Github主页") // 显示链接预览
                .setPositiveButton("确定", (dialog, which) -> {
                    // 确认后执行跳转
                    visitUrl(getResources().getString(R.string.url_jump_to_github));
                })
                .setNegativeButton("取消", null) // 取消则不执行操作
                .show());
    }

    private void getNewVersion(View root) {
        TextView textView = root.findViewById(R.id.text_about_app_get_update_123pan);
        textView.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("二次确认防误触")
                .setMessage("点击确定，前往123网盘分享页获取App最新版本或历史版本") // 显示链接预览
                .setPositiveButton("确定", (dialog, which) -> {
                    // 确认后执行跳转
                    visitUrl(getResources().getString(R.string.url_get_new_version));
                })
                .setNegativeButton("取消", null) // 取消则不执行操作
                .show());
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

    private void seeUpdateLogHistory(View root) {
        root.findViewById(R.id.text_about_app_see_update_log_history).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), UpdateLogHistoryActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}