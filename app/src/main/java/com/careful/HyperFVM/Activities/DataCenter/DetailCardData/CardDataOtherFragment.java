package com.careful.HyperFVM.Activities.DataCenter.DetailCardData;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_IS_DYNAMIC_BACKGROUND;
import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContent;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;

public class CardDataOtherFragment extends Fragment {
    private final DBHelper dbHelper = HyperFVMApplication.getDBHelper();

    private View root;

    private String cardName;
    private String tableName;

    private int savedScrollY = 0;// 用于保存/恢复的滚动位置

    public CardDataOtherFragment newInstance(String cardName, String tableName) {
        CardDataOtherFragment fragment = new CardDataOtherFragment();
        Bundle bundle = new Bundle();
        bundle.putString("cardName", cardName);
        bundle.putString("tableName", tableName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 恢复之前保存的滚动位置
        if (savedInstanceState != null) {
            savedScrollY = savedInstanceState.getInt("scrollY", 0);
        }

        // 是否启用动态背景
        boolean isDynamicBackground = dbHelper.getSettingBooleanValue(CONTENT_IS_DYNAMIC_BACKGROUND) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;

        if (isDynamicBackground) {
            root = inflater.inflate(R.layout.fragment_card_data_other_effect, container, false);
        } else {
            root = inflater.inflate(R.layout.fragment_card_data_other, container, false);
        }

        if (getArguments() != null) {
            cardName = getArguments().getString("cardName");
            tableName = getArguments().getString("tableName");
        }

        // 查询卡片数据并显示
        queryAndShowCardData();

        // 初始化各种装饰效果
        initDecoration();

        return root;
    }

    @SuppressLint({"Range", "DiscouragedApi"})
    private void queryAndShowCardData() {
        try (Cursor cursor = dbHelper.getCardData(tableName, cardName)) {
            if (cursor == null || !cursor.moveToFirst()) {
                // 无数据时提示
                Toast.makeText(requireContext(), "未找到卡片数据", Toast.LENGTH_SHORT).show();
                return;
            }

            // 其他信息
            getContent(requireContext(), root.findViewById(R.id.additional_info), CardDataHelper.getStringFromCursor(cursor, "additional_info"));
        } catch (Exception e) {
            Log.e("Data", "捕捉到异常：" + e.getMessage());
            Toast.makeText(requireContext(), "数据加载失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    private void initDecoration() {
        // 动态调整侧边距（手机/PAD）
        LinearLayout card_data_container = root.findViewById(R.id.card_data_container);
        InsetsUtil.setMarginHorizontal(requireActivity(), card_data_container, layout_marginHorizontal -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) card_data_container.getLayoutParams();
            params.leftMargin = layout_marginHorizontal;
            params.rightMargin = layout_marginHorizontal;
            card_data_container.setLayoutParams(params);
        });

        // 获取滚动视图ScrollView
        ScrollView scrollView = root.findViewById(R.id.ScrollView);

        // 监听滚动
        if (scrollView != null) {
            scrollView.post(() -> {
                scrollView.setScrollY(savedScrollY);// 还原当前滚动位置
            });

            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                savedScrollY = scrollY;// 实时记录当前滚动位置
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scrollY", savedScrollY);
    }

}