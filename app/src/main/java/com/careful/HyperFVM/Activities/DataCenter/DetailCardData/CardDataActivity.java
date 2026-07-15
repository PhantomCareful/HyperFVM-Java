package com.careful.HyperFVM.Activities.DataCenter.DetailCardData;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_DYNAMIC_BACKGROUND;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForDesign.BgEffect.BgEffectController;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.ImageExportUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.OtherUtils.TabLayoutFragmentStateAdapter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class CardDataActivity extends BaseActivity {
    private final DBHelper dbHelper = HyperFVMApplication.getDBHelper();
    private BlurUtil blurUtil;

    private BgEffectController bgEffectController;
    private boolean isDynamicBackground;

    private String cardName;// 待查询的防御卡名称
    private String tableName;// 待查询的防御卡所在的数据表名称
    private int tableId;
    private final List<ExportInfo> exportInfoList = new ArrayList<>();// 需要导出的图片清单
    private boolean isAdditionalInfoEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        // 小白条沉浸
        EdgeToEdge.enable(this);
        if(NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }
        setContentView(R.layout.activity_card_data);

        // 从Intent取出数据
        cardName = getIntent().getStringExtra("cardName");
        tableName = getIntent().getStringExtra("tableName");
        // 校验参数
        if (cardName == null || tableName == null) {
            Toast.makeText(this, "cardName或tableName为null", Toast.LENGTH_SHORT).show();
            finish(); // 参数错误直接关闭页面
            return;
        }

        // 来自第几张数据表
        tableId = Integer.parseInt(tableName.split("card_data_")[1]);

        // 获取需要导出的图片清单
        generateExportInfoList();

        // 是否启用动态背景
        isDynamicBackground = dbHelper.getSettingBooleanValue(CONTENT_IS_DYNAMIC_BACKGROUND) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.View_Page2);

        // 创建Fragment并传入参数
        TabLayoutFragmentStateAdapter adapter = new TabLayoutFragmentStateAdapter(this);
        initTabLayoutFragments(adapter);

        viewPager2.setAdapter(adapter);
        viewPager2.setUserInputEnabled(false);
        viewPager2.setOffscreenPageLimit(2);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) ->
                tab.setText(adapter.getPageTitle(position))
        ).attach();

        // 初始化各种装饰效果
        initDecoration();
    }

    private void initTabLayoutFragments(TabLayoutFragmentStateAdapter adapter) {
        // 添加Fragment对应的标题，按标签顺序
        adapter.addFragment(new CardDataBaseFragment().newInstance(cardName, tableName), getResources().getString(R.string.title_card_data_base));
        adapter.addFragment(new CardDataDataFragment().newInstance(cardName, tableName), getResources().getString(R.string.title_card_data_data));
        if (!isAdditionalInfoEmpty) {
            adapter.addFragment(new CardDataOtherFragment().newInstance(cardName, tableName), getResources().getString(R.string.title_card_data_other));
        }
    }

    @SuppressLint("Range")
    private void generateExportInfoList() {
        try (Cursor cursor = dbHelper.getCardData(tableName, cardName)) {
            if (cursor == null || !cursor.moveToFirst()) {
                // 无数据时提示
                Toast.makeText(this, "未找到卡片数据", Toast.LENGTH_SHORT).show();
                return;
            }

            isAdditionalInfoEmpty = cursor.getString(cursor.getColumnIndex("additional_info")).equals("无");

            String cardName0;
            String cardName1;
            String cardName2;
            String cardName3;
            switch (tableId) {
                case 1:
                    cardName0 = cursor.getString(cursor.getColumnIndex("name"));
                    cardName1 = cursor.getString(cursor.getColumnIndex("name_1"));
                    cardName2 = cursor.getString(cursor.getColumnIndex("name_2"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_0")), cardName0 + "(不转)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_0")) + "_big", cardName0 + "(不转, 大)"));
                    if (!cardName1.equals("无")) {
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_1")), cardName1 + "(一转)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_1")) + "_big", cardName1 + "(一转, 大)"));
                    }
                    if (!cardName2.equals("无")) {
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_2")), cardName2 + "(二转)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_2")) + "_big", cardName2 + "(二转, 大)"));
                    }
                    break;
                case 2:
                    cardName1 = cursor.getString(cursor.getColumnIndex("name"));
                    cardName2 = cursor.getString(cursor.getColumnIndex("name_2"));
                    cardName3 = cursor.getString(cursor.getColumnIndex("name_3"));
                    String fusionCardName_1_1 = cursor.getString(cursor.getColumnIndex("name_1_1"));
                    String fusionCardName_1_2 = cursor.getString(cursor.getColumnIndex("name_1_2"));
                    String fusionCardName_2_2 = cursor.getString(cursor.getColumnIndex("name_2_2"));
                    String fusionCardName_3_2 = cursor.getString(cursor.getColumnIndex("name_3_2"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_result_id_1")), cardName1 + "(初级融合)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_result_id_1")) + "_big", cardName1 + "(初级融合, 大)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_result_id_2")), cardName2 + "(深度融合)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_result_id_2")) + "_big", cardName2 + "(深度融合, 大)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_result_id_3")), cardName3 + "(灵魂融合)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_result_id_3")) + "_big", cardName3 + "(灵魂融合, 大)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_1_1")), fusionCardName_1_1 + "(主卡)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_1_2")), fusionCardName_1_2 + "(初级融合, 副卡)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_2_2")), fusionCardName_2_2 + "(深度融合, 副卡)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_3_2")), fusionCardName_3_2 + "(灵魂融合, 副卡)"));
                    break;
                case 3:
                    cardName0 = cursor.getString(cursor.getColumnIndex("name"));
                    cardName1 = cursor.getString(cursor.getColumnIndex("name_1"));
                    cardName2 = cursor.getString(cursor.getColumnIndex("name_2"));
                    cardName3 = cursor.getString(cursor.getColumnIndex("name_3"));
                    String subCardName1 = cursor.getString(cursor.getColumnIndex("name_1_1"));
                    String subCardName2 = cursor.getString(cursor.getColumnIndex("name_1_2"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_0")), cardName0 + "(不转)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_0")) + "_big", cardName0 + "(不转, 大)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_1")), cardName1 + "(三转)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_1")) + "_big", cardName1 + "(三转, 大)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_2")), cardName2 + "(四转)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_2")) + "_big", cardName2 + "(四转, 大)"));
                    if (!cardName3.equals("无")) {
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_3")), cardName3 + "(终转)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_3")) + "_big", cardName3 + "(终转, 大)"));
                    }
                    if (!subCardName1.equals("无")) {
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_1_1")), subCardName1 + "(进化用卡1)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_1_2")), subCardName2 + "(进化用卡2)"));
                    }
                    if (!cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_1")).equals("card_data_x")) {
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_1")), cardName0 + "(初级技能书)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_2")), cardName0 + "(高级技能书)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_3")), cardName0 + "(终级技能书)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_4")), cardName0 + "(究级技能书)"));
                    }
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_a")), cardName0 + "(三转凭证A)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_b")), cardName0 + "(三转凭证B)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_c")), cardName0 + "(三转凭证C)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_a")), cardName0 + "(四转凭证A)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_b")), cardName0 + "(四转凭证B)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_c")), cardName0 + "(四转凭证C)"));
                    if (!cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_a")).equals("card_data_x")) {
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_a")), cardName0 + "(终转凭证A)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_b")), cardName0 + "(终转凭证B)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_3_c")), cardName0 + "(终转凭证C)"));
                    }
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_compose")), cardName0 + "(进化凭证)"));
                    break;
                case 4:
                    cardName0 = cursor.getString(cursor.getColumnIndex("name"));
                    cardName1 = cursor.getString(cursor.getColumnIndex("name_1"));
                    cardName2 = cursor.getString(cursor.getColumnIndex("name_2"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_0")), cardName0 + "(不转)"));
                    exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_0")) + "_big", cardName0 + "(不转, 大)"));
                    if (!cardName1.equals("无")) {
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_1")), cardName1 + "(一转)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_1")) + "_big", cardName1 + "(一转, 大)"));
                    }
                    if (!cardName2.equals("无")) {
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_2")), cardName2 + "(二转)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("image_id_2")) + "_big", cardName2 + "(二转, 大)"));
                    }
                    if (!cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_1")).equals("card_data_x")) {
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_1")), cardName0 + "(初级技能书)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_2")), cardName0 + "(高级技能书)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_3")), cardName0 + "(终级技能书)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_skill_4")), cardName0 + "(究级技能书)"));
                    }
                    if (!cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_a")).equals("card_data_x")) {
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_a")), cardName0 + "(一转凭证A)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_1_b")), cardName0 + "(一转凭证B)"));
                    }
                    if (!cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_a")).equals("card_data_x")) {
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_a")), cardName0 + "(二转凭证A)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_b")), cardName0 + "(二转凭证B)"));
                        exportInfoList.add(ImageExportUtil.generateExportInfo(cursor.getString(cursor.getColumnIndex("decompose_image_id_transfer_2_c")), cardName0 + "(二转凭证C)"));
                    }
                    break;
            }

        } catch (Exception e) {
            Toast.makeText(this, "数据加载失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("NewApi")
    private void initDecoration() {
        // 适配状态栏高度
        MaterialCardView floatButtonBackContainer = findViewById(R.id.FloatButton_Back_Container);
        MaterialCardView topBarContainer = findViewById(R.id.TopBar_Container);
        MaterialCardView floatButtonExportContainer = findViewById(R.id.FloatButton_Export_Container);
        LinearLayout tabLayoutContainer = findViewById(R.id.TabLayout_Container);
        View rootView = findViewById(android.R.id.content);
        // 动态获取导航栏高度（小白条/三键导航）
        InsetsUtil.setNavigationBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tabLayoutContainer.getLayoutParams();
            params.bottomMargin = DensityUtil.dpToPx(this, 12) + height;
            tabLayoutContainer.setLayoutParams(params);
        });
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBarContainer.getLayoutParams();
            params.topMargin = height;
            topBarContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonExportContainer.getLayoutParams();
            params.topMargin = height;
            floatButtonExportContainer.setLayoutParams(params);
        });
        // 动态调整侧边距（手机/PAD）
        ConstraintLayout decompose_and_get_calculator_for_animal_card_container = findViewById(R.id.decompose_and_get_calculator_for_animal_card_container);
        InsetsUtil.setMarginHorizontal(this, decompose_and_get_calculator_for_animal_card_container, layout_marginHorizontal -> {
            Log.d("updateLog", String.valueOf(layout_marginHorizontal));
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) floatButtonBackContainer.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            floatButtonBackContainer.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonExportContainer.getLayoutParams();
            params.rightMargin = layout_marginHorizontal;
            floatButtonExportContainer.setLayoutParams(params);
        });

        // 设置顶栏标题
        TextView topBar = findViewById(R.id.topBar);
        topBar.setText(cardName);

        // 加载动态背景
        if (isDynamicBackground) {
            View bgView = findViewById(R.id.bgEffectView);
            if (bgView != null) {
                bgEffectController = new BgEffectController(bgView);
                switch (tableId) {
                    case 1, 4:
                        bgEffectController.setDetailAnimalCardDataColorType(this);
                        break;
                    case 2:
                        bgEffectController.setDetailFusionCardDataColorType(this);
                        break;
                    case 3:
                        bgEffectController.setDetailGoldenCardDataColorType(this);
                        break;
                }
            }
            if (bgEffectController != null) {
                switch (tableId) {
                    case 1, 4:
                        bgEffectController.startDetailAnimalCardDataBgEffect();
                        break;
                    case 2:
                        bgEffectController.startDetailFusionCardDataBgEffect();
                        break;
                    case 3:
                        bgEffectController.startDetailGoldenCardDataBgEffect();
                        break;
                }
            }
        }

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonBack));
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar));
        blurUtil.setBlur(findViewById(R.id.blurViewButtonExport));
        blurUtil.setBlur(findViewById(R.id.blurViewTabLayout));

        // 顺便设置返回按钮的功能
        findViewById(R.id.FloatButton_Back_Container).setOnClickListener(v -> this.finish());
        findViewById(R.id.FloatButton_Export_Container).setOnClickListener(v -> exportAllImages(exportInfoList));
    }

    /**
     * 批量导出图片
     */
    private void exportAllImages(List<ExportInfo> exportInfoList) {
        DialogBuilderManager.showDialogWithCallBack(
                this, "导出所有图片", "📦",
                "图片将保存到：\nPictures/" + getResources().getString(R.string.app_name) + "/" + cardName, true,
                "咱手滑了", "一键导出", () -> ImageExportUtil.exportAllImages(this, cardName, exportInfoList)
        );
    }

    @Override
    protected void onDestroy() {
        if (blurUtil != null) {
            blurUtil.release();
            blurUtil = null;
        }

        View rootView = findViewById(android.R.id.content);
        InsetsUtil.removeListener(rootView);
        setContentView(new FrameLayout(this));

        super.onDestroy();
    }
}