package com.careful.HyperFVM.utils.ForMeishi;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.careful.HyperFVM.utils.DBHelper.DBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 温馨礼包账号数据（区服、角色名、openid）备份与还原工具：
 * JSON 构建与格式校验、剪贴板读写、备份文件写入系统“下载”目录与文件内容读取
 */
public class MeishiBackupUtil {

    /** 导出到下载目录的备份文件名 */
    public static final String EXPORT_FILE_NAME = "温馨礼包数据备份.json";

    private static final String TAG = "MeishiBackupUtil";

    private MeishiBackupUtil() {
    }

    /**
     * 将账号列表构建为格式化 JSON 字符串
     *
     * @return JSON 文本；理论不可达的构建异常时返回 null
     */
    public static String buildJson(List<DBHelper.PlayerInfo> infos) {
        JSONArray array = new JSONArray();
        try {
            for (DBHelper.PlayerInfo info : infos) {
                JSONObject item = new JSONObject();
                item.put("openid", info.openid == null ? "" : info.openid);
                item.put("serverName", info.serverName == null ? "" : info.serverName);
                item.put("playerName", info.playerName == null ? "" : info.playerName);
                array.put(item);
            }
            return array.toString(4);
        } catch (JSONException e) {
            Log.e(TAG, "构建备份JSON失败", e);
            return null;
        }
    }

    /**
     * 解析并校验备份 JSON：
     * 顶层须为非空数组，每项须为对象且 openid/serverName/playerName 均为字符串（openid 非空）
     *
     * @return 通过校验的账号列表；null 表示格式不正确
     */
    public static List<DBHelper.PlayerInfo> parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            JSONArray array = new JSONArray(json.trim());
            if (array.length() == 0) {
                return null;
            }

            List<DBHelper.PlayerInfo> infos = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                Object element = array.get(i);
                if (!(element instanceof JSONObject item)) {
                    return null;
                }

                Object openid = item.opt("openid");
                Object serverName = item.opt("serverName");
                Object playerName = item.opt("playerName");

                // 三项均须为字符串且openid非空，任一不符即判定格式不正确
                if (!(openid instanceof String) || ((String) openid).isEmpty()
                        || !(serverName instanceof String)
                        || !(playerName instanceof String)) {
                    return null;
                }

                infos.add(new DBHelper.PlayerInfo((String) openid, (String) serverName, (String) playerName));
            }
            return infos;
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * 将备份文本写入系统剪贴板
     *
     * @return 是否写入成功
     */
    public static boolean writeClipboard(Context context, String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null) {
                return false;
            }
            clipboard.setPrimaryClip(ClipData.newPlainText("温馨礼包数据备份", text));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "写入剪贴板失败", e);
            return false;
        }
    }

    /**
     * 读取系统剪贴板中的文本内容
     *
     * @return 剪贴板文本；null 表示无剪贴板或无文本数据
     */
    public static String readClipboard(Context context) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null || !clipboard.hasPrimaryClip()) {
                return null;
            }
            ClipData clip = clipboard.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) {
                return null;
            }
            CharSequence text = clip.getItemAt(0).coerceToText(context);
            return text == null ? null : text.toString();
        } catch (Exception e) {
            Log.e(TAG, "读取剪贴板失败", e);
            return null;
        }
    }

    /**
     * 把备份 JSON 写入系统“下载”目录（Download 根目录）。
     * 采用 IS_PENDING 两段式写入：先以“待定”状态插入条目，内容写完后再置为就绪，保证用户拿到的一定是完整文件；
     * Android 10+ 非媒体文件的合规写入目录本就包含 Download，无需任何存储权限
     *
     * @return 是否成功保存
     */
    public static boolean saveToDownloads(Context context, String json) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, EXPORT_FILE_NAME);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri uri = null;
        try {
            uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                return false;
            }
            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                if (out == null) {
                    throw new IOException("打开下载目录输出流失败");
                }
                out.write(json.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            // 写入完成，清除待定状态，备份文件正式就绪
            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            context.getContentResolver().update(uri, values, null, null);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "保存备份文件到下载目录失败", e);
            // 清理写入失败的半成品条目
            if (uri != null) {
                try {
                    context.getContentResolver().delete(uri, null, null);
                } catch (Exception ignored) {
                }
            }
            return false;
        }
    }

    /**
     * 读取文件选择器返回的 Uri 中的文本内容（UTF-8）
     *
     * @return 文件文本；null 表示读取失败
     */
    public static String readTextFromUri(Context context, Uri uri) {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

            // else 分支用 toByteArray 是因为 toString(Charset) 需 API 33 而项目 minSdk 31，无法统一零拷贝写法
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return out.toString(StandardCharsets.UTF_8);
            } else {
                return new String(out.toByteArray(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            Log.e(TAG, "读取备份文件失败", e);
            return null;
        }
    }
}
