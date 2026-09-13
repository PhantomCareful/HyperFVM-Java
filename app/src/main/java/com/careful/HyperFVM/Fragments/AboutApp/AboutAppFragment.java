package com.careful.HyperFVM.Fragments.AboutApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.careful.HyperFVM.Activities.CheckUpdateActivity;
import com.careful.HyperFVM.Activities.Thanks.CoContributorTeamActivity;
import com.careful.HyperFVM.Activities.Necessary.SettingsActivity;
import com.careful.HyperFVM.Activities.Necessary.UsingInstruction.UsingInstructionActivity;
import com.careful.HyperFVM.Activities.Thanks.ThanksAppActivity;
import com.careful.HyperFVM.Activities.Thanks.ThanksGameActivity;
import com.careful.HyperFVM.Activities.UpdateLogHistory.UpdateLogHistoryActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.databinding.FragmentAboutAppBinding;
import com.careful.HyperFVM.utils.ForDesign.Animation.ScrollEffectForBackgroundItem;

import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.ForUpdate.BadgeDotUtil;
import com.careful.HyperFVM.utils.ForUpdate.LocalVersionUtil;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;

import java.util.Objects;

import eightbitlab.com.blurview.BlurView;

public class AboutAppFragment extends Fragment {
    // 保存/恢复滚动位置的key，用于深浅色切换等界面重建后恢复顶部栏透明度状态
    private static final String STATE_SCROLL_Y = "state_about_app_scroll_y";
    // 顶部栏渐变过渡区间（dp）：暂与Dashboard一致为50dp，待实测调整
    private static final int TOP_BAR_FADE_RANGE_DP = 250;

    private View root;

    private View logoView;                  // about_app_icon
    private TextView appNameText;           // about_app_name
    private TextView versionInfoText;       // about_app_version_info

    // 顶部栏滚动联动
    private NestedScrollUtil nestedScrollUtil;

    private int logoMaxScroll;              // 判定完全消失的滚动距离（dp 转 px）
    private int appNameMaxScroll;           // 判定完全消失的滚动距离（dp 转 px）
    private int appVersionMaxScroll;        // 判定完全消失的滚动距离（dp 转 px）

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAboutAppBinding binding = FragmentAboutAppBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        // 初始化各种装饰效果
        initDecoration();

        // 从build.gradle中获取版本号
        getAppLocalVersion(root);

        //跳转检查更新的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_check_update_container), CheckUpdateActivity.class);

        // 跳转浏览器，前往作者的Github主页
        root.findViewById(R.id.about_app_developer_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_github),
                0,
                getResources().getString(R.string.dialog_title_github),
                getResources().getString(R.string.dialog_sub_title_developer),
                getResources().getString(R.string.dialog_url_about_app_developer)));

        //跳转共建团的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_co_construction_team_container), CoContributorTeamActivity.class);

        //跳转致谢-游戏相关的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_thanks_list_container_fvm), ThanksGameActivity.class);

        //跳转致谢-App相关的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_thanks_list_container_app), ThanksAppActivity.class);

        //跳转使用说明的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_using_instruction_container), UsingInstructionActivity.class);

        //跳转设置的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_settings_container), SettingsActivity.class);

        // 跳转浏览器，前往App的Github主页
        root.findViewById(R.id.about_app_github_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_github),
                0,
                getResources().getString(R.string.dialog_title_github),
                getResources().getString(R.string.dialog_title_github),
                getResources().getString(R.string.dialog_url_github)));

        // 跳转浏览器，获取软件更新
        root.findViewById(R.id.about_app_get_update_123pan_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_download),
                0,
                getResources().getString(R.string.dialog_title_123pan_dialog),
                getResources().getString(R.string.dialog_sub_title_123pan_dialog),
                getResources().getString(R.string.dialog_url_123pan)));

        // 跳转浏览器，前往作者B站主页
        root.findViewById(R.id.about_app_bilibili_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_bilibili),
                0,
                getResources().getString(R.string.dialog_title_bilibili),
                getResources().getString(R.string.dialog_sub_title_bilibili),
                getResources().getString(R.string.dialog_url_bilibili)));

        // 跳转浏览器，前往App腾讯频道
        root.findViewById(R.id.about_app_tencent_channel_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_qq),
                0,
                getResources().getString(R.string.dialog_title_tencent_channel),
                getResources().getString(R.string.dialog_sub_title_tencent_channel),
                getResources().getString(R.string.dialog_url_tencent_channel)));

        //跳转浏览器，前往App聊天群组
        root.findViewById(R.id.about_app_tencent_group_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_qq),
                0,
                getResources().getString(R.string.dialog_title_tencent_group),
                getResources().getString(R.string.dialog_sub_title_tencent_group),
                getResources().getString(R.string.dialog_url_tencent_group)));

        //查看历史更新日志
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
            if (!isAdded() || getActivity() == null) {
                return;
            }

            if (isShowRedDot) {
                checkUpdateTitle1.setText("发 现 新 版 本");
                checkUpdateTitle2.setText("速 速 更 新 \uD83D\uDCE2 \uD83D\uDCE2 \uD83D\uDCE2");
            } else {
                checkUpdateTitle1.setText(getResources().getString(R.string.title_about_app_check_update));
                checkUpdateTitle2.setText(getResources().getString(R.string.description_about_app_check_update));
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
        // 适配状态栏高度
        BlurView blurViewTopBar = root.findViewById(R.id.blurViewTopBar);
        TextView topBar = root.findViewById(R.id.topBar);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(requireContext(), root, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(requireContext(), 50);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        LinearLayout aboutAppContainer = root.findViewById(R.id.AboutApp_container);
        InsetsUtil.setMarginHorizontal(requireContext(), aboutAppContainer, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) aboutAppContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            aboutAppContainer.setLayoutParams(params);
        });

        // 获取需要渐隐的元素
        logoView = root.findViewById(R.id.about_app_icon);
        appNameText = root.findViewById(R.id.about_app_name);
        versionInfoText = root.findViewById(R.id.about_app_version_info);

        // 设置一个合理的最大滚动距离，当滚动超过该值后元素完全消失
        logoMaxScroll = DensityUtil.dpToPx(requireContext(), 200);
        appNameMaxScroll = DensityUtil.dpToPx(requireContext(), 100);
        appVersionMaxScroll = DensityUtil.dpToPx(requireContext(), 50);

        // 添加模糊材质
        setupBlurEffect();

        // 添加顶部栏滚动联动：上滑时悬浮小标题与模糊层淡入（本页无大标题topBarBottom，传0跳过淡出）
        // 页面自身的元素渐隐效果经滚动回调一并驱动，与顶部栏联动共用同一滚动监听
        nestedScrollUtil = NestedScrollUtil.attach(root, R.id.scrollView,
                0, R.id.topBar, R.id.blurViewTopBar, TOP_BAR_FADE_RANGE_DP,
                this::applyBackgroundScrollEffect);
    }

    /**
     * 内容区顶部元素（logo/应用名/版本号）的滚动渐隐效果，随滚动位置实时更新
     */
    private void applyBackgroundScrollEffect(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(logoView, scrollY, logoMaxScroll);
        ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(appNameText, scrollY, appNameMaxScroll);
        ScrollEffectForBackgroundItem.applyScrollAlphaAndScaleEffect(versionInfoText, scrollY, appVersionMaxScroll);

        // 给LOGO设置点击彩蛋
        // 注意：如果图片的透明度变为0了，需要将点击事件清除，否则会影响下层组件的点击
        ScrollEffectForBackgroundItem.updateBackgroundLogoClickable(requireContext(), logoView);
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(requireContext());
        blurUtil.setBlur(root.findViewById(R.id.blurViewTopBar), root.findViewById(R.id.targetView));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存滚动位置，供界面重建（旋转/深浅色切换等）后恢复顶部栏透明度状态
        if (nestedScrollUtil != null) {
            nestedScrollUtil.saveScrollY(outState, STATE_SCROLL_Y);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // 恢复滚动位置并同步透明度与页面渐隐效果（首帧绘制前按最终滚动位置同步，避免突变回初始状态）
        if (nestedScrollUtil != null) {
            nestedScrollUtil.restoreScrollY(savedInstanceState, STATE_SCROLL_Y);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // 检查更新
        if (root != null) {
            checkUpdate(root);
        }
    }
}