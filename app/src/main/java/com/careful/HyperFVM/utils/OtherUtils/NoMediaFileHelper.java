package com.careful.HyperFVM.utils.OtherUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;

import com.careful.HyperFVM.R;

import java.io.File;

/**
 * 旧版本 .nomedia 隐藏标记清理工具（当前仅服务于“数据图查看”的兼容逻辑）。
 * <p>
 * 历史背景：旧版本为实现“导出的图片不出现在系统相册”，曾采用 SAF（存储访问框架）方案引导用户
 * 一次性授权相簿目录（Pictures/应用名）并创建 .nomedia 隐藏标记——Android 11 起普通应用
 * 无法在 Pictures 等媒体目录中直接创建非媒体文件（.nomedia），只能走 SAF。
 * <p>
 * 现状：新版本已彻底弃用 .nomedia：
 * 1.“一键导出”改为把图片打包成 ZIP 保存到 Download/应用名（压缩包不是媒体文件，相册不会收录）；
 * 2.“数据图查看”副本改用点号开头的文件名（媒体扫描器会跳过点号文件），看完即删。
 * <p>
 * 因此本类只保留一项职责：用旧版本已持久化的授权，静默删除设备上遗留的相簿根目录 .nomedia
 * （实测：隐藏目录中的媒体条目查看器无法访问，不清理则“导出副本再打开”的查看链路无法工作）。
 * 不再创建任何 .nomedia，也不再请求新的目录授权。
 * 各子目录（旧版本“一键导出”的历史批次）中的 .nomedia 保持不变，其图片继续对系统相册隐藏。
 */
public class NoMediaFileHelper {

    private static final String TAG = "ACTION_VIEW";
    private static final String NO_MEDIA_FILE_NAME = ".nomedia";
    private static final String PREFS_NAME = "no_media_file_helper";
    private static final String KEY_TREE_URI = "album_tree_uri";

    /**
     * 导出相簿目录的相对路径（Pictures/应用名）
     */
    public static String getAlbumPath(Context context) {
        return Environment.DIRECTORY_PICTURES + File.separator + context.getResources().getString(R.string.app_name);
    }

    /**
     * 移除旧版本遗留的相簿根目录 .nomedia 隐藏标记（一次性清理，删除后不做任何后续处理）。
     * <p>
     * 为什么必须清理（实测结论）：目录被 .nomedia 隐藏后，其中的媒体条目对本 App（owner）也完全不可访问——
     * 转发给图库的条目 URI 会被系统拒绝、查看器无法打开，因此“导出副本再打开”的高清查看链路
     * 只能在“未隐藏”状态下进行。使用旧版本已持久化的授权静默删除，不会弹出任何授权界面。
     * 仅处理相簿根目录；新版本已不再创建 .nomedia，无需考虑恢复。
     *
     * @return 是否已成功删除或目录本就没有隐藏标记；未获得目录授权等失败场景返回 false
     */
    public static boolean removeLegacyNoMediaFile(Context context) {
        try {
            File albumDir = new File(Environment.getExternalStorageDirectory(), getAlbumPath(context));
            if (!new File(albumDir, NO_MEDIA_FILE_NAME).exists()) {
                // 根目录本来就没有隐藏标记，无需清理
                return true;
            }
            Uri treeUri = getGrantedTreeUri(context);
            if (treeUri == null) {
                return false;
            }
            String albumDocId = resolveAlbumDocId(context, treeUri);
            if (albumDocId == null) {
                return false;
            }
            Uri noMediaDoc = findNoMediaDocument(context, treeUri, albumDocId);
            if (noMediaDoc == null) {
                // SAF 树内查不到（如文件刚放置、尚未被索引）：无法移除，按失败处理
                return false;
            }
            boolean deleted = DocumentsContract.deleteDocument(context.getContentResolver(), noMediaDoc);
            Log.d(TAG, "已移除旧版本遗留的相簿根目录 .nomedia：" + deleted);
            return deleted;
        } catch (Exception e) {
            Log.d(TAG, "移除旧版本遗留的相簿根目录 .nomedia 失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 获取已持久化的相簿目录授权 URI，未授权或授权已失效（例如用户撤销）时返回 null。
     * 仅接受受支持目录（Pictures 或 Pictures/应用名）的授权记录，过期的旧记录会被清除
     */
    private static Uri getGrantedTreeUri(Context context) {
        String saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_TREE_URI, null);
        Uri savedUri = saved != null ? Uri.parse(saved) : null;

        try {
            for (UriPermission permission : context.getContentResolver().getPersistedUriPermissions()) {
                if (!permission.isWritePermission() || !isSupportedAlbumTree(context, permission.getUri())) {
                    continue;
                }
                // 1.与记录完全一致
                if (permission.getUri().equals(savedUri)) {
                    return savedUri;
                }
                // 2.记录缺失或不一致时，按目录匹配恢复（例如记录未落盘、用户清除 App 数据等）
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putString(KEY_TREE_URI, permission.getUri().toString()).apply();
                return permission.getUri();
            }

            // 记录存在但已无对应的生效授权（授权被撤销/目录不再受支持）：清掉过期记录
            if (savedUri != null) {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().remove(KEY_TREE_URI).apply();
            }
        } catch (Exception e) {
            Log.d(TAG, "读取持久授权失败：" + e.getMessage());
        }
        return null;
    }

    /**
     * 判断持久授权对应的目录是否为受支持的相簿目录（Pictures 或 Pictures/应用名）
     */
    private static boolean isSupportedAlbumTree(Context context, Uri treeUri) {
        try {
            String docId = DocumentsContract.getTreeDocumentId(treeUri);
            String albumDocId = "primary:" + getAlbumPath(context);
            String picturesDocId = "primary:" + Environment.DIRECTORY_PICTURES;
            return albumDocId.equalsIgnoreCase(docId) || picturesDocId.equalsIgnoreCase(docId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验历史授权目录，并解析出相簿目录（Pictures/应用名）在授权树内的文档 ID。
     * 1.授权 Pictures：在其下查找 应用名 子目录（幂等）；
     * 2.授权 Pictures/应用名：直接使用；
     * 3.其余目录：返回 null（调用方自行处理）
     *
     * @return 相簿目录的文档 ID；目录不受支持或处理失败时返回 null
     */
    private static String resolveAlbumDocId(Context context, Uri treeUri) {
        try {
            String actualDocId = DocumentsContract.getTreeDocumentId(treeUri);
            String albumDocId = "primary:" + getAlbumPath(context);
            String picturesDocId = "primary:" + Environment.DIRECTORY_PICTURES;

            if (albumDocId.equalsIgnoreCase(actualDocId)) {
                return actualDocId;
            }
            if (picturesDocId.equalsIgnoreCase(actualDocId)) {
                String appDirName = context.getResources().getString(R.string.app_name);
                return findOrCreateChildDirectory(context, treeUri, actualDocId, appDirName);
            }
        } catch (Exception e) {
            Log.d(TAG, "校验授权目录异常：" + e.getMessage());
        }
        return null;
    }

    /**
     * 在授权树内的指定父目录下查找子目录，不存在则创建
     *（历史遗留：用户只授权了 Pictures 时，用于定位相簿目录 Pictures/应用名）
     *
     * @return 子目录的文档 ID；查找与创建均失败时返回 null
     */
    private static String findOrCreateChildDirectory(Context context, Uri treeUri, String parentDocId, String dirName) {
        ContentResolver resolver = context.getContentResolver();
        try {
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId);
            try (Cursor cursor = resolver.query(childrenUri, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null)) {
                while (cursor != null && cursor.moveToNext()) {
                    if (dirName.equals(cursor.getString(1))
                            && DocumentsContract.Document.MIME_TYPE_DIR.equals(cursor.getString(2))) {
                        return cursor.getString(0);
                    }
                }
            }

            // 不存在则创建
            Uri parentDoc = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentDocId);
            Uri created = DocumentsContract.createDocument(
                    resolver, parentDoc, DocumentsContract.Document.MIME_TYPE_DIR, dirName);
            if (created != null) {
                Log.d(TAG, "已创建相簿子目录：" + dirName);
                return DocumentsContract.getDocumentId(created);
            }
        } catch (Exception e) {
            Log.d(TAG, "查找/创建相簿子目录失败：" + e.getMessage());
        }
        return null;
    }

    /**
     * 查询相簿目录中是否已存在 .nomedia 文件
     */
    private static Uri findNoMediaDocument(Context context, Uri treeUri, String albumDocId) {
        ContentResolver resolver = context.getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, albumDocId);
        try (Cursor cursor = resolver.query(childrenUri, new String[]{
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                if (NO_MEDIA_FILE_NAME.equals(cursor.getString(1))) {
                    return DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(0));
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "查询 .nomedia 失败：" + e.getMessage());
        }
        return null;
    }
}
