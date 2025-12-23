package com.careful.HyperFVM.utils.ForDesign.Markdown;

import android.content.Context;
import android.media.Image;
import android.text.style.ImageSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.careful.HyperFVM.utils.OtherUtils.UpdateLogReader;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;

public class MarkdownUtil {

    public static void getContent(Context context, TextView textView, String content) {
        // 读取成功，展示到TextView
        Markwon markwon = Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())// 启用删除线支持
                .usePlugin(TablePlugin.create(context))// 启用表格支持
                .usePlugin(GlideImagesPlugin.create(context))// 启用图片支持
                .usePlugin(HtmlPlugin.create())// 启用HTML插件
                .build();
        markwon.setMarkdown(textView, content);
    }

    public static void getContentFromAssets(Context context, TextView textView, String filename) {
        // 调用工具类异步读取
        UpdateLogReader.readAssetsTxtAsync(
                context,
                filename, // assets下的文件名
                new UpdateLogReader.ReadCallback() {
                    @Override
                    public void onReadSuccess(String content) {
                        // 读取成功，展示到TextView
                        getContent(context, textView, content);
                    }

                    @Override
                    public void onReadFailed(String errorMsg) {
                        // 读取失败，提示用户
                        textView.setText(errorMsg);
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
