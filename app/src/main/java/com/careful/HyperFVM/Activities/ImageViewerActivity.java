package com.careful.HyperFVM.Activities;

import static android.content.ContentValues.TAG;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.Blur.BlurUtil;
import com.careful.HyperFVM.utils.ForDesign.ThemeManager.ThemeManager;
import com.careful.HyperFVM.utils.OtherUtils.NavigationBarForMIUIAndHyperOS;
import com.careful.HyperFVM.utils.OtherUtils.ZoomImageView;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;

public class ImageViewerActivity extends AppCompatActivity {

    private ZoomImageView zoomImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置主题（必须在super.onCreate前调用才有效）
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);
        // 加载布局文件
        setContentView(R.layout.activity_image_viewer);

        // 小白条沉浸
        EdgeToEdge.enable(this);
        if(NavigationBarForMIUIAndHyperOS.isMIUIOrHyperOS()) {
            NavigationBarForMIUIAndHyperOS.edgeToEdgeForMIUIAndHyperOS(this);
        }

        // 顶栏模糊
        setupBlurEffect();

        // 找到ZoomImageView并设置图片
        zoomImageView = findViewById(R.id.ZoomImageViewer);
        loadImageFromIntent();

        //设置顶栏标题、启用返回按钮
        setTopAppBarTitle(getResources().getString(R.string.label_data_img_viewer));
    }

    private void loadImageFromIntent() {
        // 1. 获取Fragment传递的图片路径（key必须和Fragment中一致："imgPath"）
        String imgPath = getIntent().getStringExtra("imgPath");
        if (imgPath == null || imgPath.isEmpty()) {
            Toast.makeText(this, "图片路径为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. 检查文件是否存在
        File imgFile = new File(imgPath);
        if (!imgFile.exists()) {
            String errorMsg = "图片文件不存在：" + imgPath;
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            Log.e(TAG, errorMsg);
            finish();
            return;
        }

        // 3. 直接加载图片（尺寸小，无需采样）
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
            if (bitmap == null) {
                throw new Exception("图片解码失败（格式不支持/文件损坏）");
            }

            // 核心：给ZoomImageView设置图片（必须用setImageBitmap）
            zoomImageView.setImageBitmap(bitmap);

            // 设置顶部和底部偏移量
            zoomImageView.setImageBitmap(bitmap);
            // 延迟一帧执行，确保图片已测量
            zoomImageView.post(() -> zoomImageView.setOffsets(500, 500));

        } catch (Exception e) {
            String errorMsg = "图片加载失败：" + e.getMessage();
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            Log.e(TAG, errorMsg, e);
            finish();
        }
    }

    private void setTopAppBarTitle(String title) {
        //设置顶栏标题、启用返回按钮
        MaterialToolbar toolbar = findViewById(R.id.Top_AppBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 设置返回按钮点击事件
        toolbar.setNavigationOnClickListener(v -> this.finish());
    }

    private void setupBlurEffect() {
        BlurUtil blurUtil = new BlurUtil(this);
        blurUtil.setBlur(findViewById(R.id.blurViewTopAppBar));
    }
}