package com.careful.HyperFVM.Activities.DataCenter.DetailCardData;

import static com.careful.HyperFVM.utils.ForDesign.Markdown.MarkdownUtil.getContentForMultiView;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.careful.HyperFVM.HyperFVMApplication;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.DBHelper.DBHelper;
import com.careful.HyperFVM.utils.ForCardData.CardDataHelper;
import com.careful.HyperFVM.utils.ForDesign.Scroll.NestedScrollUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;

public class CardDataOtherFragment extends Fragment {
    private final DBHelper dbHelper = HyperFVMApplication.getDBHelper();

    private View root;

    private String cardName;
    private String tableName;

    private int savedScrollY = 0;// 用于保存/恢复的滚动位置

    private static final int TOP_BAR_FADE_RANGE_DP = 25;// 顶部模糊遮罩层完整显现的滚动区间（dp）
    private static final int TOP_BAR_FADE_ANIM_MS = 250;// 切换页面时模糊遮罩层过渡动画时长（ms）
    private NestedScrollUtil nestedScrollUtil;// 顶部栏滚动联动（模糊层在宿主 Activity，随当前选中 Fragment 切换生效）

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

        if (HyperFVMApplication.isContentDynamicBackgroundEnabled()) {
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

    @SuppressLint({"Range", "DiscouragedApi", "SetTextI18n"})
    private void queryAndShowCardData() {
        try (Cursor cursor = dbHelper.getCardData(tableName, cardName)) {
            if (cursor == null || !cursor.moveToFirst()) {
                // 无数据时提示
                Toast.makeText(requireContext(), "未找到卡片数据", Toast.LENGTH_SHORT).show();
                return;
            }

            // 其他信息
            String[] additionalInfoArray = CardDataHelper.getStringFromCursor(cursor, "additional_info").split("## ");

            LayoutInflater layoutInflater = LayoutInflater.from(requireContext());
            LinearLayout card_data_other_container = root.findViewById(R.id.card_data_other_container);

            card_data_other_container.removeAllViews();

            for (int i = 1; i < additionalInfoArray.length; i++) {
                CardView cardView;
                if (additionalInfoArray[i].contains("😋食神谱")) {
                    if (HyperFVMApplication.isContentDynamicBackgroundEnabled()) {
                        cardView = (CardView) layoutInflater.inflate(R.layout.item_card_data_other_cookery_container_effect, card_data_other_container, false);
                    } else {
                        cardView = (CardView) layoutInflater.inflate(R.layout.item_card_data_other_cookery_container, card_data_other_container, false);
                    }

                    // 绑定好需要用到的组件
                    LinearLayout cookery_container = cardView.findViewById(R.id.cookery_container);
                    TextView cookery_title = cardView.findViewById(R.id.cookery_title);
                    TextView cookery_description = cardView.findViewById(R.id.cookery_description);
                    ImageView cookery_image = cardView.findViewById(R.id.cookery_image);

                    cookery_title.setText("😋食神谱：" + additionalInfoArray[i].split("- ")[1].split("；")[0]);
                    getContentForMultiView(requireContext(), cookery_description, "- " + additionalInfoArray[i].split("- ")[1].split("；")[1]);

                    String imageIdStr = additionalInfoArray[i].split("- ")[1].split("；")[2].replace("\n", "");
                    // 根据image_id获取资源ID（如"card_splash_logo" → R.drawable.card_splash_logo）
                    int imageResId = getResources().getIdentifier(
                            imageIdStr,
                            "drawable",
                            requireContext().getPackageName()
                    );
                    cookery_image.setImageResource(imageResId);

                    CardDataHelper.selectCookeryByName(additionalInfoArray[i].split("- ")[1].split("；")[0], cookery_container, getParentFragmentManager());

                } else if (additionalInfoArray[i].contains("🔥秒产说明")) {
                    if (HyperFVMApplication.isContentDynamicBackgroundEnabled()) {
                        cardView = (CardView) layoutInflater.inflate(R.layout.item_card_data_other_container_effect, card_data_other_container, false);
                    } else {
                        cardView = (CardView) layoutInflater.inflate(R.layout.item_card_data_other_container, card_data_other_container, false);
                    }

                    // 绑定好需要用到的组件
                    TextView additional_info_title = cardView.findViewById(R.id.additional_info_title);
                    TextView additional_info = cardView.findViewById(R.id.additional_info);

                    additional_info_title.setText(additionalInfoArray[i].split("\n")[0] + "\n" + additionalInfoArray[i + 1].split("\n")[0] + "\n" + additionalInfoArray[i + 2].split("\n")[0]);
                    String additional_info_str = additionalInfoArray[i + 2].split("\n", 2)[1];
                    getContentForMultiView(requireContext(), additional_info, additional_info_str);

                    i = i + 2;
                } else {
                    if (HyperFVMApplication.isContentDynamicBackgroundEnabled()) {
                        cardView = (CardView) layoutInflater.inflate(R.layout.item_card_data_other_container_effect, card_data_other_container, false);
                    } else {
                        cardView = (CardView) layoutInflater.inflate(R.layout.item_card_data_other_container, card_data_other_container, false);
                    }

                    // 绑定好需要用到的组件
                    TextView additional_info_title = cardView.findViewById(R.id.additional_info_title);
                    TextView additional_info = cardView.findViewById(R.id.additional_info);

                    additional_info_title.setText(additionalInfoArray[i].split("\n")[0]);
                    String additional_info_str = additionalInfoArray[i].split("\n", 2)[1];
                    if (!additional_info_str.isEmpty()) {
                        getContentForMultiView(requireContext(), additional_info, additional_info_str);
                    } else {
                        additional_info.setVisibility(View.GONE);
                    }
                }

                card_data_other_container.addView(cardView);
            }

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
        ScrollView scrollView = root.findViewById(R.id.scrollView);

        // 监听滚动
        if (scrollView != null) {
            scrollView.post(() -> {
                scrollView.setScrollY(savedScrollY);// 还原当前滚动位置
            });

            // 顶部栏滚动联动：模糊层 blurViewTopBar 悬浮于宿主 Activity 中，跨视图树直接传入 View；
            // 页面自身的滚动位置记录经 pageScrollListener 与联动共用同一滚动监听（覆盖语义不能各自注册）
            nestedScrollUtil = NestedScrollUtil.attach(scrollView, null, null,
                    requireActivity().findViewById(R.id.blurViewTopBar),
                    TOP_BAR_FADE_RANGE_DP,
                    (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                        savedScrollY = scrollY;// 实时记录当前滚动位置
                    });
            // 共享的模糊层同时被多个 Fragment 持有：非当前可见页先关闭写入，待 onResume 启用后再仲裁
            nestedScrollUtil.setSyncEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 成为当前可见页时启用本页写入权；仲裁须 post 延迟：同一批次内其他 Fragment 的 attach
        // 初始同步（scrollY=0 写0）晚于本页 onResume 执行，延迟到消息队列可保证仲裁在最后、不被覆盖
        if (nestedScrollUtil != null) {
            nestedScrollUtil.setSyncEnabled(true);
            root.post(() -> nestedScrollUtil.animateSyncAlpha(TOP_BAR_FADE_ANIM_MS));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 离开当前可见页后关闭本页写入权，避免不可见页的滚动/位置恢复影响当前页的模糊层
        if (nestedScrollUtil != null) {
            nestedScrollUtil.setSyncEnabled(false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("scrollY", savedScrollY);
    }

}