package com.careful.HyperFVM.utils.ForDesign.Markdown;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.careful.HyperFVM.utils.OtherUtils.UpdateLogReader;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;

public class MarkdownUtil {

    public static void getContent(Context context, TextView textView, String filename) {
        // 调用工具类异步读取
        UpdateLogReader.readAssetsTxtAsync(
                context,
                filename, // assets下的文件名
                new UpdateLogReader.ReadCallback() {
                    @Override
                    public void onReadSuccess(String content) {
                        // 读取成功，展示到TextView
                        Markwon markwon = Markwon.builder(context)
                                .usePlugin(StrikethroughPlugin.create())// 启用删除线支持
                                .build();
                        markwon.setMarkdown(textView, content);
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
