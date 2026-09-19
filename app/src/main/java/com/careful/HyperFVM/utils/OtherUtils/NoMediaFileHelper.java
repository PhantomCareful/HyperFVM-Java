package com.careful.HyperFVM.utils.OtherUtils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.ForDesign.MaterialDialog.DialogBuilderManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ".nomedia" 隐藏标记文件保障工具（"数据图查看"与"一键导出"功能共用）。
 * <p>
 * 背景说明（重要的系统限制）：
 * Android 11 起，普通应用无法在 Pictures 等媒体目录中直接创建非媒体文件（.nomedia）：
 * 通过 MediaStore 以普通文件插入会被 "Primary directory Pictures not allowed" 拒绝；
 * 而插入为图片类型又会被系统强制追加扩展名（.nomedia.png），均无法作为隐藏标记生效。
 * 因此本类采用官方推荐的 SAF（存储访问框架）方案：
 * 首次使用时弹窗引导用户一次性授权相簿目录（授权会被持久化保存，之后静默使用），
 * 随后即可随时在该目录中创建 .nomedia 文件，并触发媒体扫描让隐藏标记立即生效。
 * <p>
 * 用户可授权的目录限定为以下两种（在授权结果中校验，其余目录一律要求重选）：
 * 1.Pictures 文件夹：在其下自动创建/查找 应用名 子目录，并把 .nomedia 放入其中；
 * 2.Pictures/应用名 文件夹：直接把 .nomedia 放入其中。
 * 这样限制是为了避免把 .nomedia 写入无关目录（例如 DCIM），导致用户其它照片被误隐藏。
 * <p>
 * 生效后：
 * 1.目录中导出的图片不会被系统相册展示（媒体库扫描到该目录后会把其中条目标记为隐藏）；
 * 2.删除导出的图片不会被厂商相册的回收站机制拦截，空间立即彻底释放。
 * <p>
 * 重要（实测结论）：
 * 系统媒体库不会把父目录的 .nomedia 递归应用到子目录（例如一键导出创建的
 * Pictures/应用名/批次名 子目录），只要子目录中没有 .nomedia，其中的图片仍会被收录并显示在系统相册中。
 * 因此本类除了保障相簿目录根级外，还会为其下的每个子目录逐个放置 .nomedia 并逐个触发扫描。
 */
public class NoMediaFileHelper {

    private static final String TAG = "ACTION_VIEW";
    private static final String NO_MEDIA_FILE_NAME = ".nomedia";
    private static final String PREFS_NAME = "no_media_file_helper";
    private static final String KEY_TREE_URI = "album_tree_uri";

    // 打开"选择相簿目录"系统界面的请求码（App 内未使用其他 startActivityForResult 请求码）
    private static final int REQUEST_CODE_PICK_ALBUM_DIR = 0x4E4D;

    // 等待用户完成目录授权后要继续执行的动作（例如继续打开查看器、弹出导出成功提示）
    private static Runnable pendingContinueAction;

    // 本次操作即将写入图片的子目录名（如“一键导出”的批次目录名），
    // 用户授权等待期间暂存，授权完成后连同相簿根目录一起补齐 .nomedia（可为 null）
    private static String pendingChildFolder;

    /**
     * 导出相簿目录的相对路径（Pictures/应用名）
     */
    public static String getAlbumPath(Context context) {
        return Environment.DIRECTORY_PICTURES + File.separator + context.getResources().getString(R.string.app_name);
    }

    /**
     * 确保相簿目录中存在 .nomedia 隐藏标记文件，流程结束后（无论成功、跳过还是异常）执行 onReady。
     * 调用方应把"真正写入导出图片"的动作放在 onReady 里：先确保 .nomedia 就位再写入图片，
     * 图片从落盘那一刻起就不会被系统相册收录（包括首次使用、需要用户授权的情况）。
     * <p>
     * 三种情况：
     * 1.已授权且已创建：直接继续，无任何打扰；
     * 2.已授权但文件被手动删除：静默补建后继续；
     * 3.尚未授权：弹窗解释原因，用户选择【去授权】则打开系统目录选择界面，
     *   选择【暂不】则本次跳过（导出仍可正常进行），随后继续原流程。
     *
     * @param activity 用于弹窗与启动授权界面的 Activity
     * @param onReady  保障流程结束（成功或跳过）后要继续执行的动作，可为 null
     */
    public static void ensureNoMediaFile(Activity activity, Runnable onReady) {
        ensureNoMediaFile(activity, null, onReady);
    }

    /**
     * 同上；额外保障 childFolderName 子目录（相簿目录下的一个层级子目录）中也存在 .nomedia。
     * “一键导出”等会把图片写入子目录的场景必须使用本方法：
     * 系统媒体库不会把父目录的 .nomedia 递归应用到子目录，
     * 子目录没有 .nomedia 时其中的图片仍会出现在系统相册中。
     * 子目录不存在时会先创建，保证图片写入前隐藏标记已就位（不存在“先被相册收录”的窗口期）。
     *
     * @param childFolderName 本次即将写入图片的子目录名，可为 null
     */
    public static void ensureNoMediaFile(Activity activity, String childFolderName, Runnable onReady) {
        pendingChildFolder = childFolderName;
        try {
            if (ensureWithGrantedPermission(activity)) {
                runOnUiThread(activity, onReady);
                return;
            }

            // 尚未授权：解释原因，由用户决定是否授权
            DialogBuilderManager.showDialogWithFullCallBack(
                    activity,
                    "隐藏导出图片",
                    "🛡️",
                    "为了避免导出的图片出现在系统相册中，需要先在相簿目录里创建 .nomedia 隐藏标记文件，然后才会开始导出。\n\n受系统限制，App 无法自动创建该文件，需要您一次性授权：请选择 Pictures 文件夹（或其下的 "
                            + activity.getResources().getString(R.string.app_name)
                            + " 文件夹）。授权完成后将自动开始导出。\n\n选择【暂不】仍可正常导出，但导出的图片会出现在系统相册中。",
                    true,
                    "暂不", "去授权",
                    () -> runOnUiThread(activity, onReady),
                    () -> {
                        pendingContinueAction = onReady;
                        launchAlbumDirPicker(activity);
                    }
            );
        } catch (Exception e) {
            Log.d(TAG, "确保 .nomedia 流程异常：" + e.getMessage());
            runOnUiThread(activity, onReady);
        }
    }

    /**
     * 目录授权结果回调（由 BaseActivity.onActivityResult 转发）
     */
    public static void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_PICK_ALBUM_DIR) {
            return;
        }

        boolean dirValid = false;
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri treeUri = data.getData();
            try {
                // 校验用户选择的目录并解析出相簿目录（Pictures/应用名）：
                // 1.授权 Pictures 时会在其下自动创建/查找 应用名 子目录；
                // 2.其余目录返回 null，不保存授权也不写入任何文件（防止误隐藏用户其它照片）
                String albumDocId = resolveAlbumDocId(activity, treeUri);
                if (albumDocId != null) {
                    activity.getContentResolver().takePersistableUriPermission(
                            treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    // 必须用 commit() 同步落盘：授权后紧接着可能触发其他流程甚至进程异常退出，
                    // apply() 的异步写入会来不及落盘导致授权记录丢失、下次误判为未授权
                    activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit().putString(KEY_TREE_URI, treeUri.toString()).apply();
                    createNoMediaFileAndScan(activity, treeUri, albumDocId, pendingChildFolder);
                    dirValid = true;
                }
            } catch (Exception e) {
                Log.d(TAG, "保存 .nomedia 授权失败：" + e.getMessage());
            }
        }

        if (resultCode == Activity.RESULT_OK && !dirValid) {
            // 用户确认了不受支持的目录：明确提示并引导重新选择。
            // 此时 pendingContinueAction 保持不变，等待重选结果或用户选择【暂不】
            DialogBuilderManager.showDialogWithFullCallBack(
                    activity,
                    "需要重新选择目录",
                    "⚠️",
                    "请选择 Pictures 文件夹（或其下的 " + activity.getResources().getString(R.string.app_name)
                            + " 文件夹）。\n\n选择其它目录不会生效，且可能隐藏其它位置的照片，因此未做任何改动。",
                    true,
                    "暂不", "重新选择",
                    () -> {
                        Runnable continueAction = pendingContinueAction;
                        pendingContinueAction = null;
                        if (continueAction != null) {
                            runOnUiThread(activity, continueAction);
                        }
                    },
                    () -> launchAlbumDirPicker(activity)
            );
            return;
        }

        Runnable continueAction = pendingContinueAction;
        pendingContinueAction = null;
        if (continueAction != null) {
            runOnUiThread(activity, continueAction);
        }
    }

    /**
     * 临时移除相簿根目录的 .nomedia 隐藏标记（"查看数据图"专用，看完返回后由 restoreNoMediaFile 恢复）。
     * <p>
     * 为什么查看前必须移除（实测结论）：目录被 .nomedia 隐藏后，其中的媒体条目对本 App（owner）也完全不可访问——
     * 转发给图库的条目 URI 会被系统拒绝、查看器无法打开，因此"导出副本再打开"的高清查看链路
     * 只能在"未隐藏"状态下进行；目录中没有 .nomedia 时插入的副本条目可正常访问与打开。
     * 仅移除相簿根目录的 .nomedia；各子目录（一键导出批次）中的 .nomedia 不受影响，其图片保持隐藏。
     *
     * @return 相簿根目录当前是否处于"未隐藏"状态（可插入可访问的查看副本）；
     *         未获得目录授权或移除失败时返回 false（调用方回退 FileProvider 方式）
     */
    public static boolean removeNoMediaFileForViewing(Context context) {
        try {
            File albumDir = new File(Environment.getExternalStorageDirectory(), getAlbumPath(context));
            if (!new File(albumDir, NO_MEDIA_FILE_NAME).exists()) {
                // 根目录本来就没有隐藏标记，无需移除
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
                // SAF 树内查不到（如用户手动创建的文件刚放置、尚未被索引）：无法移除，按失败处理
                return false;
            }
            boolean deleted = DocumentsContract.deleteDocument(context.getContentResolver(), noMediaDoc);
            Log.d(TAG, "已临时移除相簿根目录 .nomedia（查看数据图）：" + deleted);
            return deleted;
        } catch (Exception e) {
            Log.d(TAG, "临时移除相簿根目录 .nomedia 失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 恢复相簿根目录的 .nomedia 隐藏标记（若缺失）。
     * 由 DataImageViewerHelper.cleanUpExportedImages 在每次回到前台时调用：
     * "查看数据图"会临时移除根级 .nomedia（见 removeNoMediaFileForViewing），
     * 查看副本删除后必须重新隐藏，保证相簿回归"图片不出现在系统相册"的稳态。
     * 标记完好时只做一次文件存在检查，零开销直接返回；未获得目录授权（无法重建）时静默跳过。
     */
    public static void restoreNoMediaFile(Context context) {
        try {
            File albumDir = new File(Environment.getExternalStorageDirectory(), getAlbumPath(context));
            if (new File(albumDir, NO_MEDIA_FILE_NAME).exists()) {
                return;
            }
            Uri treeUri = getGrantedTreeUri(context);
            if (treeUri == null) {
                Log.d(TAG, "相簿根目录 .nomedia 缺失且未获得目录授权，无法恢复隐藏标记");
                return;
            }
            String albumDocId = resolveAlbumDocId(context, treeUri);
            if (albumDocId == null) {
                return;
            }
            ensureNoMediaInDirectory(context, treeUri, albumDocId);
            List<String> scanPaths = new ArrayList<>();
            scanPaths.add(albumDir.getAbsolutePath());
            scanDirectories(context, scanPaths);
            Log.d(TAG, "已恢复相簿根目录 .nomedia 隐藏标记");
        } catch (Exception e) {
            Log.d(TAG, "恢复相簿根目录 .nomedia 失败：" + e.getMessage());
        }
    }

    /**
     * 已获得持久授权时，静默确保 .nomedia 存在
     *
     * @return 是否已获得授权（获得授权即视为无需再打扰用户）
     */
    private static boolean ensureWithGrantedPermission(Context context) {
        Uri treeUri = getGrantedTreeUri(context);
        if (treeUri == null) {
            return false;
        }
        String albumDocId = resolveAlbumDocId(context, treeUri);
        if (albumDocId == null) {
            // 授权目录异常（正常已被 getGrantedTreeUri 过滤）：清除记录，重新走授权引导
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().remove(KEY_TREE_URI).apply();
            return false;
        }
        createNoMediaFileAndScan(context, treeUri, albumDocId, pendingChildFolder);
        return true;
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
     * 校验用户授权的目录，并解析出相簿目录（Pictures/应用名）在授权树内的文档 ID。
     * 1.授权 Pictures：在其下查找/创建 应用名 子目录（幂等），避免把 .nomedia 直接放进 Pictures；
     * 2.授权 Pictures/应用名：直接使用；
     * 3.其余目录：返回 null（调用方负责提示重选，绝不写入任何文件）
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
     * 在授权树内的指定父目录下查找子目录，不存在则创建（用于
     * 用户只授权了 Pictures 时创建相簿目录、以及“一键导出”时创建批次子目录）。
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
     * 启动系统"选择目录"界面，让用户一次性授权相簿目录
     */
    private static void launchAlbumDirPicker(Activity activity) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
            // 尽量让系统界面直接定位到 Pictures 文件夹（授权先行时相簿目录可能尚未创建，
            // 定位到 Pictures 后用户直接确认或下钻一层即可；若系统忽略该参数，用户按提示自行导航）
            try {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, DocumentsContract.buildDocumentUri(
                        "com.android.externalstorage.documents", "primary:" + Environment.DIRECTORY_PICTURES));
            } catch (Exception ignored) {
            }
            activity.startActivityForResult(intent, REQUEST_CODE_PICK_ALBUM_DIR);
        } catch (Exception e) {
            Log.d(TAG, "打开目录选择界面失败：" + e.getMessage());
            Runnable continueAction = pendingContinueAction;
            pendingContinueAction = null;
            if (continueAction != null) {
                runOnUiThread(activity, continueAction);
            }
        }
    }

    /**
     * 在相簿目录及其每个子目录中创建 .nomedia（若不存在），并逐个请求媒体扫描让隐藏标记立即生效。
     * <p>
     * 注意：系统媒体库不会把父目录的 .nomedia 递归应用到子目录，因此必须对
     * 相簿根目录、全部现存子目录（历史导出批次）、本次即将写入图片的子目录
     * 逐目录放置 .nomedia 并触发扫描，否则其中的图片仍会出现在系统相册中。
     *
     * @param albumDocId         相簿目录（Pictures/应用名）在授权树内的文档 ID（由 resolveAlbumDocId 校验后提供）
     * @param pendingChildFolder 本次即将写入图片的子目录名，可为 null；不存在时会先创建
     */
    private static void createNoMediaFileAndScan(Context context, Uri treeUri, String albumDocId, String pendingChildFolder) {
        try {
            // 1.相簿根目录
            ensureNoMediaInDirectory(context, treeUri, albumDocId);

            File albumDir = new File(Environment.getExternalStorageDirectory(), getAlbumPath(context));
            List<String> scanPaths = new ArrayList<>();
            scanPaths.add(albumDir.getAbsolutePath());

            // 2.全部现存子目录（覆盖历史导出批次：其子目录级 .nomedia 是后补的，图片可能仍被相册收录）
            boolean pendingExists = false;
            for (String[] child : listChildDirectories(context, treeUri, albumDocId)) {
                ensureNoMediaInDirectory(context, treeUri, child[0]);
                scanPaths.add(new File(albumDir, child[1]).getAbsolutePath());
                if (pendingChildFolder != null && pendingChildFolder.equals(child[1])) {
                    pendingExists = true;
                }
            }

            // 3.本次即将写入图片的子目录：不存在时先创建，保证图片写入前隐藏标记已就位
            if (pendingChildFolder != null && !pendingChildFolder.isEmpty() && !pendingExists) {
                String childDocId = findOrCreateChildDirectory(context, treeUri, albumDocId, pendingChildFolder);
                if (childDocId != null) {
                    ensureNoMediaInDirectory(context, treeUri, childDocId);
                    scanPaths.add(new File(albumDir, pendingChildFolder).getAbsolutePath());
                }
            }

            // 4.逐个路径请求媒体扫描：隐藏标记只有在媒体库重新扫描到对应目录后才会生效
            scanDirectories(context, scanPaths);
        } catch (Exception e) {
            Log.d(TAG, "创建 .nomedia 失败：" + e.getMessage());
        }
    }

    /**
     * 确保指定目录中存在 .nomedia 文件（不存在则创建）
     */
    private static void ensureNoMediaInDirectory(Context context, Uri treeUri, String dirDocId) {
        try {
            if (findNoMediaDocument(context, treeUri, dirDocId) == null) {
                Uri dirDocument = DocumentsContract.buildDocumentUriUsingTree(treeUri, dirDocId);
                Uri created = DocumentsContract.createDocument(
                        context.getContentResolver(), dirDocument, "application/octet-stream", NO_MEDIA_FILE_NAME);
                Log.d(TAG, "已创建 .nomedia 隐藏标记文件：" + created);
            }
        } catch (Exception e) {
            Log.d(TAG, "创建 .nomedia 失败：" + e.getMessage());
        }
    }

    /**
     * 枚举目录下的全部子目录
     *
     * @return 每个元素为 {文档 ID, 目录名}；查询失败时返回空列表
     */
    private static List<String[]> listChildDirectories(Context context, Uri treeUri, String parentDocId) {
        List<String[]> childDirectories = new ArrayList<>();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId);
        try (Cursor cursor = context.getContentResolver().query(childrenUri, new String[]{
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                if (DocumentsContract.Document.MIME_TYPE_DIR.equals(cursor.getString(2))) {
                    childDirectories.add(new String[]{cursor.getString(0), cursor.getString(1)});
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "枚举子目录失败：" + e.getMessage());
        }
        return childDirectories;
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

    /**
     * 请求系统重新扫描指定目录集合。
     * 隐藏标记只有在媒体库重新扫描到对应目录后才会生效（目录中已有图片会被清理/标记隐藏），
     * 因此创建或确认 .nomedia 后必须逐个触发扫描
     */
    private static void scanDirectories(Context context, List<String> dirPaths) {
        try {
            MediaScannerConnection.scanFile(context, dirPaths.toArray(new String[0]), null, null);
            Log.d(TAG, "已请求媒体扫描目录：" + dirPaths);
        } catch (Exception e) {
            Log.d(TAG, "请求媒体扫描失败：" + e.getMessage());
        }
    }

    private static void runOnUiThread(Activity activity, Runnable action) {
        if (action == null) {
            return;
        }
        // 包装一层异常兜底：后续动作中的失败不应导致整个 App 崩溃
        activity.runOnUiThread(() -> {
            try {
                action.run();
            } catch (Exception e) {
                Log.d(TAG, "执行后续动作异常:" + e.getMessage());
            }
        });
    }
}
