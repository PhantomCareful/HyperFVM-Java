package com.careful.HyperFVM.Fragments.AboutApp;

import android.app.Activity;
import android.content.Intent;
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
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForUpdate.BadgeDotUtil;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;

import java.util.Objects;

public class AboutAppFragment extends Fragment {
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAboutAppBinding binding = FragmentAboutAppBinding.inflate(inflater, container, false);
        root = binding.getRoot();
/*

        // 适配导航栏高度
        LinearLayout aboutAppContainer = root.findViewById(R.id.about_app_container);
        View rootView = requireActivity().findViewById(android.R.id.content);
        // 动态获取导航栏高度（小白条/三键导航）
        InsetsUtil.getNavigationBarHeight(rootView, height -> {

            Log.d("height", "height in AboutAppFragment = " + height);

            // 获取原有的 left, top, right padding
            int left = aboutAppContainer.getPaddingLeft();
            int top = aboutAppContainer.getPaddingTop();
            int right = aboutAppContainer.getPaddingRight();

            aboutAppContainer.setPadding(left, top, right, height + DensityUtil.dpToPx(requireContext(), 72));
        });
*/

        //一个小彩蛋🥚
        setEasterEgg(root);

        //跳转检查更新的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_check_update_container), CheckUpdateActivity.class);

        //跳转浏览器，前往作者的Github主页
        root.findViewById(R.id.about_app_developer_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_about_app_developer_name_dialog),
                getResources().getString(R.string.label_about_app_developer_name_url)));

        //跳转共建团的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_co_construction_team_container), CoContributorTeamActivity.class);

        //跳转浏览器，前往陌路的哔哩哔哩主页
        root.findViewById(R.id.about_app_thanks_list_container_fvm_1).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_thanks_list_fvm_1_dialog),
                getResources().getString(R.string.label_thanks_list_fvm_1_url)));

        //跳转浏览器，前往夏夜的哔哩哔哩主页
        root.findViewById(R.id.about_app_thanks_list_container_fvm_2).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_thanks_list_fvm_2_dialog),
                getResources().getString(R.string.label_thanks_list_fvm_2_url)));

        //跳转浏览器，前往高清图楼帖子
        root.findViewById(R.id.about_app_thanks_list_container_fvm_3).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_thanks_list_fvm_3_dialog),
                getResources().getString(R.string.label_thanks_list_fvm_3_url)));

        //跳转浏览器，前往查黑系统网站
        root.findViewById(R.id.about_app_thanks_list_container_fvm_4).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_thanks_list_fvm_4_dialog),
                getResources().getString(R.string.label_thanks_list_fvm_4_url)));

        //跳转浏览器，前往miuix仓库
        root.findViewById(R.id.about_app_thanks_list_container_app_1).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_thanks_list_app_1_dialog),
                getResources().getString(R.string.label_thanks_list_app_1_url)));

        //跳转浏览器，前往BlurView仓库
        root.findViewById(R.id.about_app_thanks_list_container_app_2).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_thanks_list_app_2_dialog),
                getResources().getString(R.string.label_thanks_list_app_2_url)));

        //跳转浏览器，前往ZoomImageView仓库
        root.findViewById(R.id.about_app_thanks_list_container_app_3).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_thanks_list_app_3_dialog),
                getResources().getString(R.string.label_thanks_list_app_3_url)));

        //跳转浏览器，前往SpringBackScrollView文章
        root.findViewById(R.id.about_app_thanks_list_container_app_4).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_thanks_list_app_4_dialog),
                getResources().getString(R.string.label_thanks_list_app_4_url)));

        //跳转使用说明的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_using_instruction_container), UsingInstructionActivity.class);

        //跳转设置的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_settings_container), SettingsActivity.class);

        //跳转浏览器，前往App的Github主页
        root.findViewById(R.id.about_app_github_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_about_app_github_dialog),
                getResources().getString(R.string.label_about_app_github_url)));

        //跳转浏览器，获取软件更新
        root.findViewById(R.id.about_app_get_update_123pan_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_about_app_get_update_123pan_dialog),
                getResources().getString(R.string.label_about_app_get_update_123pan_url)));

        //跳转浏览器，前往作者B站主页
        root.findViewById(R.id.about_app_bilibili_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_about_app_bilibili_dialog),
                getResources().getString(R.string.label_about_app_bilibili_url)));

        //跳转浏览器，前往App腾讯频道
        root.findViewById(R.id.about_app_tencent_channel_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_about_app_tencent_channel_dialog),
                getResources().getString(R.string.label_about_app_tencent_channel_url)));

        //查看历史更新日志
        clickToNewActivity(root.findViewById(R.id.about_app_see_update_log_history), UpdateLogHistoryActivity.class);

        return root;
    }

    private void setEasterEgg(View root) {
        ImageView imageView = root.findViewById(R.id.about_app_icon);
        imageView.setOnClickListener(v -> Toast.makeText(requireContext(), "Make FVM Great Again\uD83C\uDF89\uD83C\uDF89\uD83C\uDF89", Toast.LENGTH_SHORT).show());
    }

    private void getAppLocalVersionAndCheckUpdate(View root) {
        // 获取version信息
        long localVersionCode = LocalVersionUtil.getAppLocalVersionCode(requireContext());
        String localVersionName = LocalVersionUtil.getAppLocalVersionName(requireContext());

        // 判断是否为Beta版
        String betaOrRelease = Objects.equals(localVersionName.split("\\.")[2], "0") ? " | Release" : " | Beta";

        // 拼接最终版本信息
        TextView version_info = root.findViewById(R.id.version_info);
        String versionInfo = localVersionName + "(" + localVersionCode + ")" + betaOrRelease;
        version_info.setText(versionInfo);

        // 检查更新
        TextView checkUpdateTitle1 = root.findViewById(R.id.about_app_check_update_title_1);
        TextView checkUpdateTitle2 = root.findViewById(R.id.about_app_check_update_title_2);

        BadgeDotUtil.checkUpdateAndShowRedDot(requireContext(), isShowRedDot -> {
            if (isShowRedDot) {
                checkUpdateTitle1.setText("发 现 新 版 本");
                checkUpdateTitle2.setText("速 速 更 新 \uD83D\uDCE2 \uD83D\uDCE2 \uD83D\uDCE2");
            } else {
                checkUpdateTitle1.setText(getResources().getString(R.string.title_about_app_check_update_1));
                checkUpdateTitle2.setText(getResources().getString(R.string.title_about_app_check_update_2));
            }
        });
    }

    private void clickToNewActivity(View view, Class<? extends Activity> activityClass) {
        view.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), activityClass);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //从build.gradle中获取版本号
        getAppLocalVersionAndCheckUpdate(root);
    }
}