package com.careful.HyperFVM.utils.ForDesign.Markdown;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.careful.HyperFVM.utils.OtherUtils.AssetsTextFileReader;

import java.util.Objects;

import io.noties.markwon.Markwon;
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

    public static void getContentForMultiView(Context context, TextView textView, String content) {
        // 读取成功，展示到TextView
        Markwon markwon = Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())// 启用删除线支持
                .usePlugin(TablePlugin.create(context))// 启用表格支持
                .usePlugin(GlideImagesPlugin.create(context))// 启用图片支持
                .usePlugin(HtmlPlugin.create())// 启用HTML插件
                .build();
        markwon.setMarkdown(textView, content);

        textView.setMovementMethod(null);
    }

    public static void getContentFromAssets(Context context, TextView textView, String filename) {
        // 调用工具类异步读取
        AssetsTextFileReader.readAssetsTextAsync(
                context,
                filename, // assets下的文件名
                new AssetsTextFileReader.ReadCallback() {
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

    /**
     * 读取assets更新日志，并按当前渠道过滤条目：
     * Release版隐藏第三位版本号不为0的条目，Beta版隐藏第三位版本号为0的条目
     *
     * @param releaseChannel 当前是否为Release版（版本号第三位为0）
     */
    public static void getContentFromAssets(Context context, TextView textView, String filename, boolean releaseChannel) {
        // 调用工具类异步读取
        AssetsTextFileReader.readAssetsTextAsync(
                context,
                filename, // assets下的文件名
                new AssetsTextFileReader.ReadCallback() {
                    @Override
                    public void onReadSuccess(String content) {
                        // 读取成功，按渠道过滤后展示到TextView
                        getContent(context, textView, filterUpdateLogByChannel(content, releaseChannel));
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

    /**
     * 按渠道过滤更新日志：以"## x.y.z(日期)"行为分界切分条目，只保留属于当前渠道的条目
     */
    private static String filterUpdateLogByChannel(String content, boolean releaseChannel) {
        StringBuilder filtered = new StringBuilder();
        boolean keepCurrent = true;
        for (String line : content.split("\n", -1)) {
            // 遇到条目标题行时，判断该条目是否属于当前渠道
            if (line.startsWith("## ")) {
                keepCurrent = isChannelEntry(line, releaseChannel);
            }
            if (keepCurrent) {
                filtered.append(line).append('\n');
            }
        }
        return filtered.toString();
    }

    /**
     * 判断"## x.y.z(日期)"条目是否属于当前渠道：
     * 第三位版本号为0属于Release版，不为0属于Beta版
     */
    private static boolean isChannelEntry(String headerLine, boolean releaseChannel) {
        try {
            // 去掉"## "前缀与"(日期)"后缀，如"## 4.4.5(20261012)" -> "4.4.5"
            String versionName = headerLine.substring(3).split("\\(")[0].trim();
            boolean entryIsRelease = Objects.equals(versionName.split("\\.")[2], "0");
            return entryIsRelease == releaseChannel;
        } catch (Exception e) {
            // 解析失败的条目保留展示
            return true;
        }
    }
}
