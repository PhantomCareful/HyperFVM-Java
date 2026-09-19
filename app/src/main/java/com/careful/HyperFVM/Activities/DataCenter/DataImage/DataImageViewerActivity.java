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
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

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
 * <p>
 * 说明：Manifest 为本界面指定黑底专属主题作启动底色（打开瞬间不闪白），
 * 运行时由 ThemeManager 应用全局主题，顶部/底部装饰随主题着色。
 */
public class DataImageViewerActivity extends BaseActivity {

    // 数据图文件的绝对路径
    public static final String EXTRA_IMAGE_PATH = "extra_data_image_viewer_image_path";

    private BlurUtil blurUtil;

    private SubsamplingScaleImageView imageView;
    private ProgressBar loadingView;

    // 当前查看的数据图文件（保存与分享均直接作用于该原图）
    private File imageFile;
    // 保存进行中标记：防止连点导致重复写入
    private boolean isSaving;

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

        initImageView(imageFile);
    }

    /**
     * 初始化大图组件：区域解码显示 + 双击缩放 + 初始宽度适配并定位到顶部
     */
    private void initImageView(File imageFile) {
        // 缩放下限：完整显示整张图（区域解码在任何缩放级别都清晰）
        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
        // 双击放大到 1:1 原始像素；已放大到 1:1 及以上时双击还原
        imageView.setDoubleTapZoomScale(1f);
        // 双击缩放时以双击点为中心
        imageView.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_FIXED);

        imageView.setOnImageEventListener(new SubsamplingScaleImageView.DefaultOnImageEventListener() {
            @Override
            public void onReady() {
                // 图片信息就绪：隐藏加载指示器，并按“宽度适配、顶部对齐”显示
                loadingView.setVisibility(View.GONE);
                if (imageView.getWidth() > 0) {
                    applyInitialView();
                } else {
                    // 兜底：极端时序下 onReady 早于首帧布局，等布局完成后再设置
                    imageView.post(() -> {
                        if (imageView.isReady()) {
                            applyInitialView();
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

        // 添加模糊材质
        setupBlurEffect();
    }

    /**
     * 添加模糊效果
     */
    private void setupBlurEffect() {
        blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopBar), 0.5f);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonSave), 0f);
        blurUtil.setBlur(findViewById(R.id.blurViewButtonShare), 0f);
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
