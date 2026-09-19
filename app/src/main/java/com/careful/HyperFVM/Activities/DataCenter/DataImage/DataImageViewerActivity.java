package com.careful.HyperFVM.Activities.DataCenter.DataImage;

import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.careful.HyperFVM.BaseActivity;
import com.careful.HyperFVM.R;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;

/**
 * 数据图内置查看器：全屏黑底显示应用私有目录中的数据图。
 * 显示效果的关键：SubsamplingScaleImageView 采用区域解码（BitmapRegionDecoder），
 * 只按屏幕可视区域以 1:1 像素解码，约 6500 万像素的超大图在任意缩放级别都保持原生清晰，
 * 且内存占用恒定（不随图片总像素数增长）。
 * 全程不写入系统媒体库：相册（含回收站）零痕迹、无需任何存储授权。
 * <p>
 * 注意：本界面固定黑底（Manifest 中指定专属主题），不能调用 ThemeManager.applyTheme，
 * 否则主题会被全局主题覆盖、破坏黑底沉浸显示。
 */
public class DataImageViewerActivity extends BaseActivity {

    // 数据图文件的绝对路径
    public static final String EXTRA_IMAGE_PATH = "extra_data_image_viewer_image_path";

    private SubsamplingScaleImageView imageView;
    private ProgressBar loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 内容延伸到系统栏区域（配合下方隐藏系统栏实现全屏沉浸）
        EdgeToEdge.enable(this);
        // 隐藏状态栏与导航栏；用户从屏幕边缘滑动时短暂显示
        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.hide(WindowInsetsCompat.Type.systemBars());
        insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        // 查看图片期间保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_data_image_viewer);

        imageView = findViewById(R.id.data_image_viewer_image);
        loadingView = findViewById(R.id.data_image_viewer_loading);

        String imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        File imageFile = (imagePath == null) ? null : new File(imagePath);
        if (imageFile == null || !imageFile.exists()) {
            Toast.makeText(this, R.string.toast_data_image_viewer_load_failed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

    @Override
    protected void onDestroy() {
        if (imageView != null) {
            // 释放区域解码持有的内存与后台线程资源
            imageView.recycle();
            imageView = null;
        }
        super.onDestroy();
    }
}
