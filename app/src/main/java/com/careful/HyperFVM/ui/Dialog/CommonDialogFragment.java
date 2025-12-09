package com.careful.HyperFVM.ui.Dialog;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.careful.HyperFVM.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

/**
 * 通用弹窗Fragment，支持动态配置标题、内容、布局、按钮等
 */
public class CommonDialogFragment extends DialogFragment {
    // 参数key
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_LAYOUT_RES = "layout_res"; // 自定义布局资源
    public static final String KEY_POSITIVE_TEXT = "positive_text";
    public static final String KEY_NEGATIVE_TEXT = "negative_text";

    // 回调接口（处理按钮点击、输入内容等）
    public interface Callback {
        // 确认按钮点击（可返回输入内容，如需要）
        default void onPositiveClick(String inputContent) {}
        // 取消按钮点击
        default void onNegativeClick() {}
    }

    private Callback callback;
    private TextInputEditText inputEditText; // 用于输入型弹窗

    // 设置回调（建议用getChildFragmentManager()时，通过此方法传递）
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    // 构建者模式：简化参数配置
    public static class Builder {
        private final Bundle args = new Bundle();

        public Builder setTitle(String title) {
            args.putString(KEY_TITLE, title);
            return this;
        }

        public Builder setMessage(String message) {
            args.putString(KEY_MESSAGE, message);
            return this;
        }

        // 设置自定义布局（如输入框布局）
        public Builder setCustomLayout(int layoutRes) {
            args.putInt(KEY_LAYOUT_RES, layoutRes);
            return this;
        }

        public Builder setPositiveButtonText(String text) {
            args.putString(KEY_POSITIVE_TEXT, text);
            return this;
        }

        public Builder setNegativeButtonText(String text) {
            args.putString(KEY_NEGATIVE_TEXT, text);
            return this;
        }

        public CommonDialogFragment build() {
            CommonDialogFragment fragment = new CommonDialogFragment();
            fragment.setArguments(args);
            return fragment;
        }
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args == null) {
            return super.onCreateDialog(savedInstanceState);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        // 设置标题
        String title = args.getString(KEY_TITLE);
        if (title != null) {
            builder.setTitle(title);
        }

        // 设置内容（优先使用自定义布局，否则用文本内容）
        int layoutRes = args.getInt(KEY_LAYOUT_RES, 0);
        if (layoutRes != 0) {
            // 加载自定义布局（如输入框布局）
            View customView = getLayoutInflater().inflate(layoutRes, null);
            builder.setView(customView);
            // 缓存输入框（如果布局中有）
            inputEditText = customView.findViewById(R.id.textInputEditText);
        } else {
            // 文本内容
            String message = args.getString(KEY_MESSAGE);
            builder.setMessage(message);
        }

        // 确认按钮
        String positiveText = args.getString(KEY_POSITIVE_TEXT, "确定");
        builder.setPositiveButton(positiveText, (dialog, which) -> {
            if (callback != null) {
                // 传递输入内容（如果是输入型弹窗）
                String input = inputEditText != null ?
                        Objects.requireNonNull(inputEditText.getText()).toString().trim() : "";
                callback.onPositiveClick(input);
            }
        });

        // 取消按钮
        String negativeText = args.getString(KEY_NEGATIVE_TEXT, "取消");
        builder.setNegativeButton(negativeText, (dialog, which) -> {
            if (callback != null) {
                callback.onNegativeClick();
            }
        });

        return builder.create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 清除回调，避免内存泄漏
        callback = null;
    }
}