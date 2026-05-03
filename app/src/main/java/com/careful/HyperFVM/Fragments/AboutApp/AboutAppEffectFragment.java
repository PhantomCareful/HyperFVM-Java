package com.careful.HyperFVM.Fragments.AboutApp;

import static com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager.KEY_DARK_MODE;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.Animation.SpringBackScrollView;
import com.careful.HyperFVM.utils.ForDesign.BgEffect.BgEffectController;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.SmallestWidth.SmallestWidthUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.DarkModeManager;
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

    private int logoMaxScroll;              // 判定完全消失的滚动距离（dp 转 px）
    private int appNameMaxScroll;           // 判定完全消失的滚动距离（dp 转 px）
    private int appVersionMaxScroll;        // 判定完全消失的滚动距离（dp 转 px）

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAboutAppEffectBinding binding = FragmentAboutAppEffectBinding.inflate(inflater, container, false);
        root = binding.getRoot();
/*

        // 适配导航栏高度
        LinearLayout aboutAppContainer = root.findViewById(R.id.about_app_container);
        View rootView = requireActivity().findViewById(android.R.id.content);
        // 动态获取导航栏高度（小白条/三键导航）
        InsetsUtil.getNavigationBarHeight(rootView, height -> {

            Log.d("height", "height in AboutAppEffectFragment = " + height);

            // 获取原有的 left, top, right padding
            int left = aboutAppContainer.getPaddingLeft();
            int top = aboutAppContainer.getPaddingTop();
            int right = aboutAppContainer.getPaddingRight();

            aboutAppContainer.setPadding(left, top, right, height + DensityUtil.dpToPx(requireContext(), 72));
        });
*/

        // 获取需要渐隐的元素
        logoView = root.findViewById(R.id.about_app_icon);
        appNameText = root.findViewById(R.id.about_app_name);
        versionInfoText = root.findViewById(R.id.about_app_version_info);

        // 获取滚动视图SpringBackScrollView
        View scrollView = root.findViewById(R.id.ScrollViewAboutApp);

        // 设置一个合理的最大滚动距离，当滚动超过该值后元素完全消失
        logoMaxScroll = DensityUtil.dpToPx(requireContext(), 200);
        appNameMaxScroll = DensityUtil.dpToPx(requireContext(), 100);
        appVersionMaxScroll = DensityUtil.dpToPx(requireContext(), 50);

        // 监听滚动
        if (scrollView instanceof SpringBackScrollView && SmallestWidthUtil.getSmallestWidthDp() < 600) {
            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                applyScrollEffect(logoView, scrollY, logoMaxScroll);
                applyScrollEffect(appNameText, scrollY, appNameMaxScroll);
                applyScrollEffect(versionInfoText, scrollY, appVersionMaxScroll);
            });
        }

        // 初始化流光背景
        View bgView = root.findViewById(R.id.bgEffectView);
        if (bgView != null) {
            bgEffectController = new BgEffectController(bgView);
            // 使用默认参数（浅色手机效果），可根据主题切换
            try (DBHelper dbHelper = new DBHelper(requireContext())) {
                // 2. 读取深色模式设置
                String darkMode = dbHelper.getSettingValueString(KEY_DARK_MODE);

                switch (darkMode) {
                    case "总是开启\uD83C\uDF1A":
                        bgEffectController.setType(requireContext(), BgEffectController.DeviceType.PHONE, BgEffectController.ThemeMode.DARK);
                        break;
                    case "总是关闭\uD83C\uDF1D":
                        bgEffectController.setType(requireContext(), BgEffectController.DeviceType.PHONE, BgEffectController.ThemeMode.LIGHT);
                        break;
                    case "跟随系统\uD83C\uDF17":
                        bgEffectController.setType(requireContext(), BgEffectController.DeviceType.PHONE,
                                DarkModeManager.isDarkTheme(requireContext()) ? BgEffectController.ThemeMode.DARK : BgEffectController.ThemeMode.LIGHT
                        );
                        break;
                }
            }
        }
        if (bgEffectController != null) {
            bgEffectController.start();
        }

        //一个小彩蛋🥚
        setEasterEgg(root);

        //从build.gradle中获取版本号
        getAppLocalVersionAndCheckUpdate(root);

        //跳转检查更新的Activity
        clickToNewActivity(root.findViewById(R.id.about_app_check_update_container), CheckUpdateActivity.class);

        //跳转浏览器，前往作者的Github主页
        root.findViewById(R.id.about_app_developer_container).setOnClickListener(v -> DialogBuilderManager.showDialogAndVisitUrl(
                requireContext(),
                getResources().getString(R.string.title_about_app_developer_name_dialog),
                getResources().getString(R.string.label_about_app_developer_name_url)));

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

    private void applyScrollEffect(View view, int scrollY, int maxScroll) {
        // 计算渐变因子：0（未滚动）→ 1（完全消失）
        float fraction = Math.min(1f, Math.max(0f, scrollY / (float) maxScroll));

        // 渐隐 Logo、名称、版本号，同时可选地做轻微缩小
        float alpha = 1f - fraction;
        float scale = 1f - 0.1f * fraction;   // 缩小到 90%

        setViewAlphaScale(view, alpha, scale);
    }

    private void setViewAlphaScale(View view, float alpha, float scale) {
        if (view == null) return;
        view.setAlpha(alpha);
        view.setScaleX(scale);
        view.setScaleY(scale);
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
        TextView version_info = root.findViewById(R.id.about_app_version_info);
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

        if (bgEffectController != null) {
            bgEffectController.start();
        }

        //从build.gradle中获取版本号
        getAppLocalVersionAndCheckUpdate(root);
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