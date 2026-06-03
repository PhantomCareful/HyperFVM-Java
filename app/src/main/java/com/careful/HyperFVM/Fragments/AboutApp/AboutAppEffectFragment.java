package com.careful.HyperFVM.Fragments.AboutApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.Activities.CheckUpdateActivity;
import com.careful.HyperFVM.Activities.CoContributorTeamActivity;
import com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity;
import com.careful.HyperFVM.Activities.NecessaryThings.UsingInstructionActivity;
import com.careful.HyperFVM.Activities.ThanksList.ThanksAppActivity;
import com.careful.HyperFVM.Activities.ThanksList.ThanksGameActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.Activities.UpdateLogHistory.UpdateLogHistoryActivity;
import com.careful.HyperFVM.databinding.FragmentAboutAppEffectBinding;
import com.careful.HyperFVM.utils.ForDesign.Animation.ScrollEffectForBackgroundItem;
import android.widget.ScrollView;
import com.careful.HyperFVM.utils.ForDesign.BgEffect.BgEffectController;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForUpdate.BadgeDotUtil;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;

import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class AboutAppEffectFragment extends Fragment {
    private View root;

    private BgEffectController bgEffectController;

    private View logoView;                  // about_app_icon
    private TextView appNameText;           // about_app_name
    private TextView versionInfoText;       // about_app_version_info

    private int savedScrollY = 0;           // 用于保存/恢复的滚动位置

    private int logoMaxScroll;              // 判定完全消失的滚动距离（dp 转 px）
    private int appNameMaxScroll;           // 判定完全消失的滚动距离（dp 转 px）
    private int appVersionMaxScroll;        // 判定完全消失的滚动距离（dp 转 px）

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAboutAppEffectBinding binding = FragmentAboutAppEffectBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        // 恢复之前保存的滚动位置
        if (savedInstanceState != null) {
            savedScrollY = savedInstanceState.getInt("scrollY", 0);
        }

        // 初始化各种装饰效果
        initDecoration();

        // 从build.gradle中获取版本号
        getAppLocalVersion(root);

        // 跳转检查更新的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_check_update_container), CheckUpdateActivity.class);

        // 跳转浏览器，前往作者的Github主页
        root.findViewById(R.id.about_app_developer_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_about_app_developer_name_dialog),
                getResources().getString(R.string.label_about_app_developer_name_url)));

        // 跳转共建团的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_co_construction_team_container), CoContributorTeamActivity.class);

        // 跳转致谢-游戏相关的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_thanks_list_container_fvm), ThanksGameActivity.class);

        // 跳转致谢-App相关的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_thanks_list_container_app), ThanksAppActivity.class);

        // 跳转使用说明的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_using_instruction_container), UsingInstructionActivity.class);

        // 跳转设置的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_settings_container), SettingsActivity.class);

        // 跳转浏览器，前往App的Github主页
        root.findViewById(R.id.about_app_github_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_about_app_github_dialog),
                getResources().getString(R.string.label_about_app_github_url)));

        // 跳转浏览器，获取软件更新
        root.findViewById(R.id.about_app_get_update_123pan_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_about_app_get_update_123pan_dialog),
                getResources().getString(R.string.label_about_app_get_update_123pan_url)));

        // 跳转浏览器，前往作者B站主页
        root.findViewById(R.id.about_app_bilibili_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_about_app_bilibili_dialog),
                getResources().getString(R.string.label_about_app_bilibili_url)));

        // 跳转浏览器，前往App腾讯频道
        root.findViewById(R.id.about_app_tencent_channel_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_about_app_tencent_channel_dialog),
                getResources().getString(R.string.label_about_app_tencent_channel_url)));

        //跳转浏览器，前往App聊天群组
        root.findViewById(R.id.about_app_tencent_group_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_about_app_tencent_group_dialog),
                getResources().getString(R.string.title_about_app_tencent_group_url)));

        // 查看历史更新日志
        clickToNewActivity(root.findViewById(R.id.about_app_see_update_log_history), UpdateLogHistoryActivity.class);

        return root;
    }

    private void getAppLocalVersion(View root) {
        // 获取version信息
        long localVersionCode = LocalVersionUtil.getAppLocalVersionCode(requireContext());
        String localVersionName = LocalVersionUtil.getAppLocalVersionName(requireContext());

        // 判断是否为Beta版
        String betaOrRelease = Objects.equals(localVersionName.split("\\.")[2], "0") ? " | Release" : " | Beta";

        // 拼接最终版本信息
        TextView version_info = root.findViewById(R.id.about_app_version_info);
        String versionInfo = localVersionName + "(" + localVersionCode + ")" + betaOrRelease;
        version_info.setText(versionInfo);
    }

    private void checkUpdate(View root) {
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

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    private void initDecoration() {
        // 初始化流光背景
        View bgView = root.findViewById(R.id.bgEffectView);
        if (bgView != null) {
            bgEffectController = new BgEffectController(bgView);
            bgEffectController.setAboutAppColorType(requireContext());
        }
        if (bgEffectController != null) {
            bgEffectController.startAboutAppBgEffect();
        }

        // 获取需要渐隐的元素
        logoView = root.findViewById(R.id.about_app_icon);
        appNameText = root.findViewById(R.id.about_app_name);
        versionInfoText = root.findViewById(R.id.about_app_version_info);

        // 获取滚动视图ScrollView
        ScrollView scrollView = root.findViewById(R.id.ScrollView);

        // 设置一个合理的最大滚动距离，当滚动超过该值后元素完全消失
        logoMaxScroll = DensityUtil.dpToPx(requireContext(), 200);
        appNameMaxScroll = DensityUtil.dpToPx(requireContext(), 100);
        appVersionMaxScroll = DensityUtil.dpToPx(requireContext(), 50);

        // 监听滚动
        if (scrollView != null) {
            scrollView.post(() -> {
                scrollView.setScrollY(savedScrollY);// 还原当前滚动位置
                // 手动触发一次效果更新，让透明度与恢复的滚动位置同步
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(logoView, savedScrollY, logoMaxScroll);
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(appNameText, savedScrollY, appNameMaxScroll);
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(versionInfoText, savedScrollY, appVersionMaxScroll);

                // 给LOGO设置点击彩蛋
                // 注意：如果图片的透明度变为0了，需要将点击事件清除，否则会影响下层组件的点击
                ScrollEffectForBackgroundItem.updateBackgroundLogoClickable(requireContext(), logoView);
            });

            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                savedScrollY = scrollY;// 实时记录当前滚动位置
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(logoView, scrollY, logoMaxScroll);
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(appNameText, scrollY, appNameMaxScroll);
                ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(versionInfoText, scrollY, appVersionMaxScroll);

                // 给LOGO设置点击彩蛋
                // 注意：如果图片的透明度变为0了，需要将点击事件清除，否则会影响下层组件的点击
                ScrollEffectForBackgroundItem.updateBackgroundLogoClickable(requireContext(), logoView);
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scrollY", savedScrollY);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (bgEffectController != null) {
            bgEffectController.startAboutAppBgEffect();
        }

        // 检查更新
        checkUpdate(root);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bgEffectController != null) {
            bgEffectController.stop();
            bgEffectController = null;
        }
    }
}