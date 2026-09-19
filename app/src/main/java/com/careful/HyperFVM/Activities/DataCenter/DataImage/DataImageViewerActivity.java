package com.careful.HyperFVM.Activities.DataCenter.DataImage;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.DensityUtil;
import com.careful.HyperFVM.utils.OtherUtils.InsetsUtil;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import eightbitlab.com.blurview.BlurView;

/**
 * 数据图内置查看器：全屏黑底显示应用私有目录中的数据图。
 * 显示效果的关键：SubsamplingScaleImageView 采用区域解码（BitmapRegionDecoder），
 * 只按屏幕可视区域以 1:1 像素解码，约 6500 万像素的超大图在任意缩放级别都保持原生清晰，
 * 且内存占用恒定（不随图片总像素数增长）。
 * 查看过程不写入系统媒体库（相册零痕迹、无需存储授权）；仅当用户主动保存时，
 * 才把原图写入系统相册 Pictures/HyperFVM。
 * 单击图片可切换沉浸模式（隐藏装饰与系统栏、只显示图片，再单击恢复）。
 * 旋转（含物理旋转）不重建界面：大图无需重新加载，查看位置与沉浸状态自然保持；
 * 深浅色等其他配置变化仍走重建，自动保存并恢复查看位置与沉浸模式，切屏无缝续看。
 * <p>
 * 说明：Manifest 为本界面指定黑底专属主题作启动底色（打开瞬间不闪白），
 * 运行时由 ThemeManager 应用全局主题，顶部/底部装饰随主题着色。
 */
public class DataImageViewerActivity extends BaseActivity {

    // 数据图文件的绝对路径
    public static final String EXTRA_IMAGE_PATH = "extra_data_image_viewer_image_path";

    // 旋转重建时保存/恢复的查看状态键：缩放、视口中心（source 图像坐标）、沉浸模式
    private static final String STATE_VIEW_SCALE = "state_data_image_viewer_view_scale";
    private static final String STATE_VIEW_CENTER_X = "state_data_image_viewer_view_center_x";
    private static final String STATE_VIEW_CENTER_Y = "state_data_image_viewer_view_center_y";
    private static final String STATE_IMMERSIVE = "state_data_image_viewer_immersive";

    private BlurUtil blurUtil;

    private SubsamplingScaleImageView imageView;
    private ProgressBar loadingView;

    // 当前查看的数据图文件（保存与分享均直接作用于该原图）
    private File imageFile;
    // 保存进行中标记：防止连点导致重复写入
    private boolean isSaving;

    // 旋转重建场景：待恢复的查看位置（onCreate 读取、图片就绪后应用），及是否携带有效位置
    private float restoredViewScale;
    private PointF restoredViewCenter;
    private boolean hasRestoredViewState;

    // 查看器装饰组件（顶部模糊栏、标题、返回键、底部按钮栏）：沉浸模式下随系统栏一并淡出
    private final View[] viewerDecorationViews = new View[4];
    // 是否处于沉浸模式（隐藏装饰与系统栏、只显示图片），单击图片切换
    private boolean isImmersive;
    // 沉浸模式下的返回键回调：按返回先退出沉浸模式（与系统相册一致），需随模式动态启停
    private OnBackPressedCallback immersiveBackCallback;

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
        setContentView(R.layout.activity_data_image_viewer);

        // 旋转重建场景：读取上次保存的查看状态（沉浸模式立即生效，位置待图片就绪后恢复）
        if (savedInstanceState != null) {
            isImmersive = savedInstanceState.getBoolean(STATE_IMMERSIVE, false);
            if (savedInstanceState.containsKey(STATE_VIEW_CENTER_X)) {
                restoredViewScale = savedInstanceState.getFloat(STATE_VIEW_SCALE);
                restoredViewCenter = new PointF(
                        savedInstanceState.getFloat(STATE_VIEW_CENTER_X),
                        savedInstanceState.getFloat(STATE_VIEW_CENTER_Y));
                hasRestoredViewState = restoredViewScale > 0f;
            }
        }

        imageView = findViewById(R.id.data_image_viewer_image);
        loadingView = findViewById(R.id.data_image_viewer_loading);

        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        imageFile = (imagePath == null) ? null : new File(imagePath);
        if (imageFile == null || !imageFile.exists()) {
            Toast.makeText(this, R.string.toast_data_image_viewer_load_failed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化各种装饰效果
        initDecoration();

        // 旋转重建且此前处于沉浸模式：直接以沉浸状态呈现（不带动画，避免重建后装饰闪现）
        if (isImmersive) {
            setImmersiveMode(true, false);
        }

        initImageView(imageFile);
    }

    /**
     * 初始化大图组件：区域解码显示 + 双击缩放 + 初始宽度适配并定位到顶部
     */
    private void initImageView(File imageFile) {
        // 加载指示器延迟展示：进程内重建（如深浅色切换）时图片就绪通常很快，
        // 300ms 内就绪则完全不出现进度动画，避免被误认为“图片重新加载”
        loadingView.postDelayed(() -> {
            if (imageView != null && !imageView.isReady()) {
                loadingView.setVisibility(View.VISIBLE);
            }
        }, 300);
        // 禁用组件自带的 View 状态自动保存：查看位置完全由 Activity 自管（onSaveInstanceState），
        // 避免两套机制重复恢复
        imageView.setSaveEnabled(false);
        // 缩放下限：完整显示整张图（区域解码在任何缩放级别都清晰）
        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
        // 双击放大到 1:1 原始像素；已放大到 1:1 及以上时双击还原
        imageView.setDoubleTapZoomScale(1f);
        // 双击缩放时以双击点为中心
        imageView.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_FIXED);

        imageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
            @Override
            public void onReady() {
                // 图片信息就绪：隐藏加载指示器，恢复查看位置或按初始视图显示
                loadingView.setVisibility(View.GONE);
                if (imageView.getWidth() > 0) {
                    restoreOrInitView();
                } else {
                    // 兜底：极端时序下 onReady 早于首帧布局，等布局完成后再设置
                    imageView.post(() -> {
                        if (imageView.isReady()) {
                            restoreOrInitView();
                        }
                    });
                }
            }

            @Override
            public void onImageLoadError(Exception e) {
                loadingView.setVisibility(View.GONE);
                Toast.makeText(DataImageViewerActivity.this, R.string.toast_data_image_viewer_load_failed, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        imageView.setImage(ImageSource.uri(Uri.fromFile(imageFile)));
    }

    /**
     * 图片就绪后的视图应用：有旋转重建前保存的位置则恢复（缩放 + 视口中心，坐标基于 source 图像，
     * 与屏幕方向无关），否则按初始视图显示。
     * 恢复值超出新方向下的缩放范围时由组件自动收敛（如横屏全览切回竖屏后钳制为最小缩放）
     */
    private void restoreOrInitView() {
        if (hasRestoredViewState) {
            imageView.setScaleAndCenter(restoredViewScale, restoredViewCenter);
        } else {
            applyInitialView();
        }
    }

    /**
     * 初始显示：宽度适配屏幕、定位到图片顶部。
     * 数据图为超长竖图（约 4988x12984 像素），宽度铺满后从顶部开始查看，信息密度与字体大小最合适；
     * 宽度适配的缩放值必然不低于最小缩放（后者取宽高适配中的较小者），不会越界
     */
    private void applyInitialView() {
        float initialScale = (float) imageView.getWidth() / imageView.getSWidth();
        imageView.setScaleAndCenter(
                initialScale,
                new PointF(imageView.getSWidth() / 2f, 0f));
    }

    /**
     * 此方法用于完成当前界面的各种花里胡哨的装饰，比如
     * 1.模糊材质
     * 2.背景动态流光
     * 3.背景组件滑动渐隐渐显
     * 等等等等
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initDecoration() {
        // 适配状态栏高度
        BlurView blurViewTopBar = findViewById(R.id.blurViewTopBar);
        TextView topBar = findViewById(R.id.topBar);
        ImageButton floatButtonBack = findViewById(R.id.FloatButton_Back);
        // 适配导航栏高度
        CardView bottomBarContainer = findViewById(R.id.bottomBarContainer);
        View rootView = findViewById(android.R.id.content);
        // 动态获取状态栏高度
        InsetsUtil.setStatusBarHeight(this, rootView, height -> {
            // 沉浸模式下系统栏隐藏（insets 归零），装饰正处于淡出/淡入：
            // 跳过布局更新，避免 0 值兜底逻辑重置装饰位置而造成可见的跳变
            if (isImmersive) {
                return;
            }
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) blurViewTopBar.getLayoutParams();
            params.height = height + DensityUtil.dpToPx(this, 50);
            blurViewTopBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            params.topMargin = height;
            topBar.setLayoutParams(params);

            params = (ViewGroup.MarginLayoutParams) floatButtonBack.getLayoutParams();
            params.topMargin = height + DensityUtil.dpToPx(this, 5);
            floatButtonBack.setLayoutParams(params);
        });
        // 动态获取导航栏高度（小白条/三键导航）
        InsetsUtil.setNavigationBarHeight(this, rootView, height -> {
            // 沉浸模式下系统栏隐藏（insets 归零），装饰正处于淡出/淡入：
            // 跳过布局更新，避免 0 值兜底逻辑重置装饰位置而造成可见的跳变
            if (isImmersive) {
                return;
            }
            Log.d("height", "height in MainActivity = " + height);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) bottomBarContainer.getLayoutParams();
            params.bottomMargin = height;
            params.rightMargin = height;
            bottomBarContainer.setLayoutParams(params);
        });

        // 顺便设置按钮的功能
        floatButtonBack.setOnClickListener(v -> this.finish());
        ImageButton floatButtonSave = findViewById(R.id.FloatButton_Save);
        ImageButton floatButtonShare = findViewById(R.id.FloatButton_Share);
        floatButtonSave.setOnClickListener(v -> saveImage());
        floatButtonShare.setOnClickListener(v -> shareImage());

        // 沉浸模式需要整体淡入淡出的装饰组件
        viewerDecorationViews[0] = blurViewTopBar;
        viewerDecorationViews[1] = topBar;
        viewerDecorationViews[2] = floatButtonBack;
        viewerDecorationViews[3] = bottomBarContainer;

        // 单击图片切换沉浸模式。
        // 库自身支持双击缩放，单击需等双击判定超时后再触发（onSingleTapConfirmed），避免误判冲突；
        // OnTouchListener 只旁观手势并返回 false，事件仍由图片组件正常处理（拖动、缩放不受影响）
        GestureDetector singleTapDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                        setImmersiveMode(!isImmersive);
                        return true;
                    }
                });
        imageView.setOnTouchListener((v, event) -> {
            singleTapDetector.onTouchEvent(event);
            return false;
        });

        // 沉浸模式下按返回键先退出沉浸模式（与系统相册一致），而非直接关闭查看器
        immersiveBackCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                setImmersiveMode(false);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, immersiveBackCallback);

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar), 0.5f);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonSave), 0.5f);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonShare), 0.5f);
    }

    /**
     * 设置沉浸模式：隐藏或恢复装饰组件与系统栏（状态栏 + 导航栏），只留图片本身。
     * 由单击图片触发切换；沉浸模式下滑动屏幕边缘可短暂呼出系统栏（松手自动隐藏）
     *
     * @param immersive 是否进入沉浸模式
     */
    private void setImmersiveMode(boolean immersive) {
        setImmersiveMode(immersive, true);
    }

    /**
     * 设置沉浸模式（可控制装饰显隐是否带动画）。
     *
     * @param immersive 是否进入沉浸模式
     * @param animate   装饰显隐是否带动画；旋转重建后直接以沉浸状态呈现时传 false，避免装饰闪现
     */
    private void setImmersiveMode(boolean immersive, boolean animate) {
        isImmersive = immersive;
        if (immersiveBackCallback != null) {
            immersiveBackCallback.setEnabled(immersive);
        }

        // 装饰组件淡入淡出
        for (View decoration : viewerDecorationViews) {
            if (decoration == null) {
                continue;
            }
            decoration.animate().cancel();
            if (immersive) {
                if (animate) {
                    decoration.animate().alpha(0f).setDuration(200)
                            .withEndAction(() -> decoration.setVisibility(View.GONE)).start();
                } else {
                    decoration.setAlpha(0f);
                    decoration.setVisibility(View.GONE);
                }
            } else {
                decoration.setVisibility(View.VISIBLE);
                if (animate) {
                    decoration.setAlpha(0f);
                    decoration.animate().alpha(1f).setDuration(200).start();
                } else {
                    decoration.setAlpha(1f);
                }
            }
        }

        // 系统栏显隐
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (immersive) {
            // 边缘滑动临时呼出系统栏（半透明浮层），随后自动隐藏
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            controller.hide(WindowInsetsCompat.Type.systemBars());
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars());
        }
    }

    /**
     * 保存当前数据图原图到系统相册（Pictures/HyperFVM）。
     * 拷贝在后台线程执行，完成后回到主线程提示结果；保存期间按钮防连点
     */
    private void saveImage() {
        if (isSaving) {
            return;
        }
        isSaving = true;

        new Thread(() -> {
            boolean success = writeImageToPictures();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                isSaving = false;
                Toast.makeText(this,
                        success ? R.string.toast_data_image_viewer_save_success : R.string.toast_data_image_viewer_save_failed,
                        Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    /**
     * 将私有目录中的原图以字节流原样拷贝到 Pictures/HyperFVM（不重新编解码，画质无损）。
     * 采用 IS_PENDING 两段式写入：先插入待定条目，内容写完后再置为就绪，相册中不会出现半成品；
     * 向公共图片目录写入媒体文件在 Android 10+ 无需任何存储权限
     *
     * @return 是否保存成功
     */
    private boolean writeImageToPictures() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_name));
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        Uri uri = null;
        try {
            uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                return false;
            }
            try (InputStream in = new FileInputStream(imageFile);
                 OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out == null) {
                    throw new IOException("打开相册输出流失败");
                }
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
            // 写入完成，清除待定状态，图片正式就绪
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);
            return true;
        } catch (Exception e) {
            // 清理写入失败的半成品条目
            if (uri != null) {
                try {
                    getContentResolver().delete(uri, null, null);
                } catch (Exception ignored) {
                }
            }
            return false;
        }
    }

    /**
     * 调起系统分享面板分享当前数据图原图。
     * 通过 FileProvider 以 content:// URI 对外提供私有文件（Manifest 已注册、file_paths 已映射 data_images），
     * 零权限且不产生任何额外文件落盘
     */
    private void shareImage() {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            // 部分接收方需通过 ClipData 才能获得该 URI 的读取授权
            intent.setClipData(ClipData.newUri(getContentResolver(), imageFile.getName(), uri));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, getString(R.string.title_data_image_viewer_share)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_data_image_viewer_share_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 旋转等配置变化时不重建界面：Manifest 已声明对应 configChanges，
     * 大图无需重新加载，查看位置与沉浸状态自然保持；
     * 界面适配依赖全屏约束布局与 insets 回调自动完成
     */
    @Override
    protected boolean shouldRecreateOnConfigurationChanged() {
        return false;
    }

    /**
     * 保存查看状态（深浅色等配置变化触发重建时使用）：图片就绪时记录缩放与视口中心
     * （坐标基于 source 图像，与屏幕方向无关），连同沉浸模式一起，重建后无缝续看
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageView != null && imageView.isReady()) {
            outState.putFloat(STATE_VIEW_SCALE, imageView.getScale());
            PointF center = imageView.getCenter();
            if (center != null) {
                outState.putFloat(STATE_VIEW_CENTER_X, center.x);
                outState.putFloat(STATE_VIEW_CENTER_Y, center.y);
            }
        }
        outState.putBoolean(STATE_IMMERSIVE, isImmersive);
    }

    @Override
    protected void onDestroy() {
        if (blurUtil != null) {
            blurUtil.release();
            blurUtil = null;
        }

        if (imageView != null) {
            // 释放区域解码持有的内存与后台线程资源
            imageView.recycle();
            imageView = null;
        }

        View rootView = findViewById(android.R.id.content);
        InsetsUtil.removeListener(rootView);
        setContentView(new FrameLayout(this));

        super.onDestroy();
    }
}
