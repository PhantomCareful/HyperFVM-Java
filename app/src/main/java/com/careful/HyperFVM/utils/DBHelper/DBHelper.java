package com.careful.HyperFVM.utils.DBHelper;

import static com.careful.HyperFVM.Activities.NecessaryThings.SettingsActivity.CONTENT_INTERFACE_STYLE;
import static com.careful.HyperFVM.utils.DBHelper.DatabaseInfo.DB_VERSION;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.careful.HyperFVM.R;
import com.careful.HyperFVM.utils.OtherUtils.CardSuggestion;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DBHelper extends SQLiteOpenHelper {
    // 数据库基本信息（统一管理）
    private static final String DATABASE_NAME = "Hyper_FVM";
    private static final String ASSETS_DB_NAME = "Hyper_FVM.db"; // assets中的预制数据库
    private static final int DATABASE_VERSION = DB_VERSION; // 版本号
    private final Context mContext;

    // 表名常量
    public static final String TABLE_MEISHI_WECHAT = "meishi_wechat";
    public static final String TABLE_DASHBOARD = "dashboard";
    public static final String TABLE_SETTINGS = "settings";
    public static final String TABLE_DATA_STATION = "data_station";
    public static final String TABLE_CARD_DATA_INDEX = "card_data_index";

    // 构造方法
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        copyDatabaseIfNeeded(); // 首次运行时复制预制数据库
    }

    // 复制assets中的预制数据库到应用目录（仅首次运行时执行）
    private void copyDatabaseIfNeeded() {
        String dbDir = mContext.getFilesDir().getParent() + "/databases/";
        File dbDirFile = new File(dbDir);
        if (!dbDirFile.exists()) {
            dbDirFile.mkdirs();
        }
        File dbFile = new File(dbDir + DATABASE_NAME);
        if (!dbFile.exists()) {
            try {
                InputStream is = mContext.getAssets().open(ASSETS_DB_NAME);
                FileOutputStream fos = new FileOutputStream(dbFile);
                byte[] buffer = new byte[4096];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.flush();
                fos.close();
                is.close();
                Log.d("DBHelper", "预制数据库复制成功：" + dbFile.getAbsolutePath());
            } catch (IOException e) {
                Log.e("DBHelper", "复制失败：" + e.getMessage());
                if (dbFile.exists()) dbFile.delete();
                throw new RuntimeException("数据库初始化失败");
            }
        }
    }

    // 数据库首次创建时调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 升级到版本5以后添加防御卡数据表
        createCardTables(db);
        // 升级到版本16以后添加融合卡数据表
        createCardFusionTables(db);
        // 升级到版本17以后为金卡添加分解&兑换数据，金卡数据单独成表
        createGoldenCardTables(db);
        // 升级到版本29以后为星座卡、生肖卡添加分解&兑换数据，星座卡、生肖卡数据单独成表
        createConstellationCardAndAnimalCardTables(db);
        // 从5开始，后续版本都需要清空表并重新导入CSV
        clearAndImportCardData(db);
    }

    // 数据库版本升级时调用（核心：处理表结构变更）
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 从5开始每次都要做的
        // 添加防御卡数据表
        createCardTables(db);
        // 从16开始每次都要做的
        // 添加融合卡数据表
        createCardFusionTables(db);
        // 从17开始每次都要做的
        // 添加金卡数据表
        createGoldenCardTables(db);
        // 从29开始每次都要做的
        createConstellationCardAndAnimalCardTables(db);
        // 清空表并重新导入CSV
        clearAndImportCardData(db);

        // 版本15：优化通知：仪表盘展示的文案和通知展示的文案分开存储
        if (oldVersion < 15) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_DASHBOARD + " (id, content) " +
                    "VALUES ('meishi_wechat_result_text_notification', 'null')," +
                    "('double_explosion_rate_notification', 'null')," +
                    "('fertilization_task_notification', 'null')," +
                    "('new_year_notification', 'null')");
        }

        // 版本20：新增data_station表，管理数据图版本号
        if (oldVersion < 20) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DATA_STATION + "(" +
                    "content TEXT PRIMARY KEY," +
                    "value TEXT)");
            //版本号初始为0
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_DATA_STATION + " (content, value) " +
                    "VALUES ('DataImagesVersionCode', '0')");
        }

        // 版本21：修复：深色模式设置失效的问题
        if (oldVersion < 21) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_SETTINGS + " (content, value) " +
                    "VALUES ('主题-深色主题', '跟随系统\uD83C\uDF17')");
        }

        // 版本22：CurrentUpdateLogImage
        if (oldVersion < 22) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_DATA_STATION + " (content, value) " +
                    "VALUES ('CurrentUpdateLogImage', '- 啥都还没有哦~')");
        }

        // 版本23：新增DownloadedApkFileName
        // 版本23：强行重置DataImagesVersionCode为0
        if (oldVersion < 23) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_DATA_STATION + " (content, value) " +
                    "VALUES ('DownloadedApkFileName', '')");
            db.execSQL("UPDATE " + TABLE_DATA_STATION +
                    " SET value = '0' WHERE content = 'DataImagesVersionCode'");
        }

        // 版本25：settings表增加“自动任务-增强”
        if (oldVersion < 25) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_SETTINGS + " (content, value) " +
                    "VALUES ('自动任务-增强', 'false')");
        }

        // 版本34：settings表增加“提示语显示”3个设置
        if (oldVersion < 34) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_SETTINGS + " (content, value) " +
                    "VALUES ('提示语显示-防御卡全能数据库', 'true')," +
                    "('提示语显示-增幅卡名单', 'true')," +
                    "('提示语显示-数据图查看器', 'true')");
        }

        // 版本35：settings表增加“界面风格”设置
        if (oldVersion < 35) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_SETTINGS + " (content, value) " +
                    "VALUES ('界面风格', '鲜艳-立体')");
        }

        // 版本36：settings表增加“按压反馈动画”开关
        if (oldVersion < 36) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_SETTINGS + " (content, value) " +
                    "VALUES ('按压反馈动画', 'true')");
        }

        // 版本46：settings表增加“界面布局优化”开关
        if (oldVersion < 46) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_SETTINGS + " (content, value) " +
                    "VALUES ('界面布局优化', 'true')");
        }

        // 版本52：settings表增加“生物认证”开关
        if (oldVersion < 52) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_SETTINGS + " (content, value) " +
                    "VALUES ('安全-生物认证', 'false')");
        }

        // 版本53：重构仪表盘界面
        if (oldVersion < 53) {
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_DASHBOARD + " (id, content) " +
                    "VALUES ('meishi_wechat_result_text_notification', 'null')," +
                    "('double_explosion_rate_emoji', 'null')," +
                    "('fertilization_task_emoji', 'null')," +
                    "('new_year_emoji', 'null')," +
                    "('meishi_wechat_result_text_detail', 'null')," +
                    "('double_explosion_rate_detail', 'null')," +
                    "('meishi_wechat_result_detail', 'null')," +
                    "('fertilization_task_detail', 'null')," +
                    "('new_year_detail', 'null')");
        }

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //啥也不做
        // 从5开始每次都要做的
        // 添加防御卡数据表
        createCardTables(db);
        // 从16开始每次都要做的
        // 添加融合卡数据表
        createCardFusionTables(db);
        // 从17开始每次都要做的
        // 添加金卡数据表
        createGoldenCardTables(db);
        // 清空表并重新导入CSV
        clearAndImportCardData(db);
    }

    // 升级到版本5+时添加防御卡数据表，操作为：如果表存在，则清空内容，再将csv的数据导入到表中。
    private void createCardTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS card_data_index");
        // 创建card_data_index表
        db.execSQL("CREATE TABLE IF NOT EXISTS card_data_index (" +
                "name TEXT PRIMARY KEY, " +
                "base_name TEXT, " +
                "table_name TEXT, " +
                "image_id TEXT)");

        db.execSQL("DROP TABLE IF EXISTS card_data_1");
        // 创建card_data_1表（字段与CSV对应）
        db.execSQL("CREATE TABLE IF NOT EXISTS card_data_1 (" +
                "name TEXT PRIMARY KEY, " +
                "image_id_0 TEXT, " +
                "image_id_1 TEXT, " +
                "image_id_2 TEXT, " +
                "corresponding_golden_card_name TEXT, " +
                "corresponding_golden_card_image_id TEXT, " +
                "corresponding_fusion_card_name TEXT, " +
                "corresponding_fusion_card_image_id TEXT, " +
                "image_id TEXT, " +
                "base_info TEXT, " +
                "category TEXT, " +
                "price TEXT, " +
                "sub_card TEXT, " +
                "star TEXT, star_detail TEXT, " +
                "star_0 TEXT, star_1 TEXT, star_2 TEXT, star_3 TEXT, star_4 TEXT, " +
                "star_5 TEXT, star_6 TEXT, star_7 TEXT, star_8 TEXT, star_9 TEXT, " +
                "star_10 TEXT, star_11 TEXT, star_12 TEXT, star_13 TEXT, star_14 TEXT, " +
                "star_15 TEXT, star_16 TEXT, star_M TEXT, star_U TEXT, " +
                "skill TEXT, skill_detail TEXT, " +
                "skill_0 TEXT, skill_1 TEXT, skill_2 TEXT, skill_3 TEXT, skill_4 TEXT, " +
                "skill_5 TEXT, skill_6 TEXT, skill_7 TEXT, skill_8 TEXT, " +
                "transfer_change TEXT, " +
                "additional_info TEXT)");
    }

    // 升级到版本16+时添加融合卡数据表，操作为：如果表存在，则清空内容，再将csv的数据导入到表中。
    private void createCardFusionTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS card_data_2");
        // 创建card_data_2表（字段与CSV对应）
        db.execSQL("CREATE TABLE IF NOT EXISTS card_data_2 (" +
                "name TEXT PRIMARY KEY, " +
                "name_2 TEXT, " +
                "name_3 TEXT, " +
                "name_1_1 TEXT, " +
                "image_id_1_1 TEXT, " +
                "name_1_2 TEXT, " +
                "image_id_1_2 TEXT, " +
                "image_result_id_1 TEXT, " +
                "name_2_2 TEXT, " +
                "image_id_2_2 TEXT, " +
                "image_result_id_2 TEXT, " +
                "name_3_2 TEXT, " +
                "image_id_3_2 TEXT, " +
                "image_result_id_3 TEXT, " +
                "base_info TEXT, " +
                "fusion_info TEXT, " +
                "category TEXT, " +
                "price TEXT, " +
                "sub_card TEXT, " +
                "star TEXT, star_detail TEXT, " +
                "star_0 TEXT, star_1 TEXT, star_2 TEXT, star_3 TEXT, star_4 TEXT, " +
                "star_5 TEXT, star_6 TEXT, star_7 TEXT, star_8 TEXT, star_9 TEXT, " +
                "star_10 TEXT, star_11 TEXT, star_12 TEXT, star_13 TEXT, star_14 TEXT, " +
                "star_15 TEXT, star_16 TEXT, star_M TEXT, star_U TEXT, " +
                "star_fusion TEXT, star_fusion_detail TEXT, " +
                "star_fusion_0 TEXT, star_fusion_1 TEXT, star_fusion_2 TEXT, star_fusion_3 TEXT, star_fusion_4 TEXT, " +
                "star_fusion_5 TEXT, star_fusion_6 TEXT, star_fusion_7 TEXT, star_fusion_8 TEXT, star_fusion_9 TEXT, " +
                "star_fusion_10 TEXT, star_fusion_11 TEXT, star_fusion_12 TEXT, star_fusion_13 TEXT, star_fusion_14 TEXT, " +
                "star_fusion_15 TEXT, star_fusion_16 TEXT, star_fusion_M TEXT, star_fusion_U TEXT, " +
                "skill TEXT, skill_detail TEXT, " +
                "skill_0 TEXT, skill_1 TEXT, skill_2 TEXT, skill_3 TEXT, skill_4 TEXT, " +
                "skill_5 TEXT, skill_6 TEXT, skill_7 TEXT, skill_8 TEXT, " +
                "transfer_change TEXT, " +
                "additional_info TEXT)");
    }

    // 升级到版本17+时，为金卡装入分解&兑换数据，因此金卡数据表需要单独分离出来，操作为：如果表存在，则清空内容，再将csv的数据导入到表中。
    private void createGoldenCardTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS card_data_3");
        // 创建card_data_3表（字段与CSV对应）
        db.execSQL("CREATE TABLE IF NOT EXISTS card_data_3 (" +
                "name TEXT PRIMARY KEY, " +
                "name_1 TEXT, " +
                "name_2 TEXT, " +
                "name_3 TEXT, " +
                "image_id_0 TEXT, " +
                "image_id_1 TEXT, " +
                "image_id_2 TEXT, " +
                "image_id_3 TEXT, " +
                "name_1_1 TEXT, " +
                "image_id_1_1 TEXT, " +
                "name_1_2 TEXT, " +
                "image_id_1_2 TEXT, " +
                "base_info TEXT, " +
                "category TEXT, " +
                "price TEXT, " +
                "sub_card TEXT, " +
                "star TEXT, " +
                "star_detail TEXT, " +
                "star_0 TEXT, star_1 TEXT, star_2 TEXT, star_3 TEXT, star_4 TEXT, " +
                "star_5 TEXT, star_6 TEXT, star_7 TEXT, star_8 TEXT, star_9 TEXT, " +
                "star_10 TEXT, star_11 TEXT, star_12 TEXT, star_13 TEXT, star_14 TEXT, " +
                "star_15 TEXT, star_16 TEXT, star_M TEXT, star_U TEXT, " +
                "support_1 TEXT, " +
                "support_2 TEXT, " +
                "skill TEXT, " +
                "skill_detail TEXT, " +
                "skill_0 TEXT, skill_1 TEXT, skill_2 TEXT, skill_3 TEXT, skill_4 TEXT, " +
                "skill_5 TEXT, skill_6 TEXT, skill_7 TEXT, skill_8 TEXT, " +
                "transfer_change TEXT, " +
                "additional_info TEXT, " +
                "decompose_item TEXT, " +
                "decompose_card_1 TEXT, decompose_card_2 TEXT, decompose_card_3 TEXT, decompose_card_4 TEXT, " +
                "decompose_skill_1 TEXT, decompose_skill_2 TEXT, decompose_skill_3 TEXT, decompose_skill_4 TEXT, " +
                "decompose_transfer_1_a TEXT, decompose_transfer_1_b TEXT, decompose_transfer_1_c TEXT, " +
                "decompose_transfer_2_a TEXT, decompose_transfer_2_b TEXT, decompose_transfer_2_c TEXT, " +
                "decompose_transfer_3_a TEXT, decompose_transfer_3_b TEXT, decompose_transfer_3_c TEXT, " +
                "decompose_compose TEXT, " +
                "get_card_1 TEXT, get_card_2 TEXT, get_card_3 TEXT, get_card_4 TEXT, " +
                "get_skill_1 TEXT, get_skill_2 TEXT, get_skill_3 TEXT, get_skill_4 TEXT, " +
                "get_transfer_1_a TEXT, get_transfer_1_b TEXT, get_transfer_1_c TEXT, " +
                "get_transfer_2_a TEXT, get_transfer_2_b TEXT, get_transfer_2_c TEXT, " +
                "get_transfer_3_a TEXT, get_transfer_3_b TEXT, get_transfer_3_c TEXT, " +
                "get_compose TEXT, " +
                "decompose_image_id_card_1 TEXT, decompose_image_id_card_2 TEXT, decompose_image_id_card_3 TEXT, decompose_image_id_card_4 TEXT, " +
                "decompose_image_id_skill_1 TEXT, decompose_image_id_skill_2 TEXT, decompose_image_id_skill_3 TEXT, decompose_image_id_skill_4 TEXT, " +
                "decompose_image_id_transfer_1_a TEXT, decompose_image_id_transfer_1_b TEXT, decompose_image_id_transfer_1_c TEXT, " +
                "decompose_image_id_transfer_2_a TEXT, decompose_image_id_transfer_2_b TEXT, decompose_image_id_transfer_2_c TEXT, " +
                "decompose_image_id_transfer_3_a TEXT, decompose_image_id_transfer_3_b TEXT, decompose_image_id_transfer_3_c TEXT, " +
                "decompose_image_id_compose TEXT)");
    }

    // 升级到版本29+时，为星座卡、生肖卡装入分解&兑换数据，因此星座卡、生肖卡数据表需要单独分离出来，操作为：如果表存在，则清空内容，再将csv的数据导入到表中。
    private void createConstellationCardAndAnimalCardTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS card_data_4");
        // 创建card_data_4表（字段与CSV对应）
        db.execSQL("CREATE TABLE IF NOT EXISTS card_data_4 (" +
                "name TEXT PRIMARY KEY, " +
                "name_1 TEXT, " +
                "name_2 TEXT, " +
                "image_id_0 TEXT, " +
                "image_id_1 TEXT, " +
                "image_id_2 TEXT, " +
                "corresponding_golden_card_name TEXT, " +
                "corresponding_golden_card_image_id TEXT, " +
                "corresponding_fusion_card_name TEXT, " +
                "corresponding_fusion_card_image_id TEXT, " +
                "base_info TEXT, " +
                "category TEXT, " +
                "price TEXT, " +
                "sub_card TEXT, " +
                "star TEXT, " +
                "star_detail TEXT, " +
                "star_0 TEXT, star_1 TEXT, star_2 TEXT, star_3 TEXT, star_4 TEXT, " +
                "star_5 TEXT, star_6 TEXT, star_7 TEXT, star_8 TEXT, star_9 TEXT, " +
                "star_10 TEXT, star_11 TEXT, star_12 TEXT, star_13 TEXT, star_14 TEXT, " +
                "star_15 TEXT, star_16 TEXT, star_M TEXT, star_U TEXT, " +
                "skill TEXT, " +
                "skill_detail TEXT, " +
                "skill_0 TEXT, skill_1 TEXT, skill_2 TEXT, skill_3 TEXT, skill_4 TEXT, " +
                "skill_5 TEXT, skill_6 TEXT, skill_7 TEXT, skill_8 TEXT, " +
                "transfer_change TEXT, " +
                "additional_info TEXT, " +
                "decompose_item TEXT, " +
                "decompose_card_1 TEXT, decompose_card_2 TEXT, decompose_card_3 TEXT, " +
                "decompose_skill_1 TEXT, decompose_skill_2 TEXT, decompose_skill_3 TEXT, decompose_skill_4 TEXT, " +
                "decompose_transfer_1_a TEXT, decompose_transfer_1_b TEXT, " +
                "decompose_transfer_2_a TEXT, decompose_transfer_2_b TEXT, decompose_transfer_2_c TEXT, " +
                "get_card_1 TEXT, get_card_2 TEXT, get_card_3 TEXT, " +
                "get_skill_1 TEXT, get_skill_2 TEXT, get_skill_3 TEXT, get_skill_4 TEXT, " +
                "get_transfer_1_a TEXT, get_transfer_1_b TEXT, " +
                "get_transfer_2_a TEXT, get_transfer_2_b TEXT, get_transfer_2_c TEXT, " +
                "decompose_image_id_card_1 TEXT, decompose_image_id_card_2 TEXT, decompose_image_id_card_3 TEXT, " +
                "decompose_image_id_skill_1 TEXT, decompose_image_id_skill_2 TEXT, decompose_image_id_skill_3 TEXT, decompose_image_id_skill_4 TEXT, " +
                "decompose_image_id_transfer_1_a TEXT, decompose_image_id_transfer_1_b TEXT, " +
                "decompose_image_id_transfer_2_a TEXT, decompose_image_id_transfer_2_b TEXT, decompose_image_id_transfer_2_c TEXT)");
    }

    // 清空表并重新导入CSV数据（每次升级都执行）
    private void clearAndImportCardData(SQLiteDatabase db) {
        // 清空现有数据
        db.execSQL("DELETE FROM card_data_index");
        db.execSQL("DELETE FROM card_data_1");
        db.execSQL("DELETE FROM card_data_2");
        db.execSQL("DELETE FROM card_data_3");
        db.execSQL("DELETE FROM card_data_4");

        // 重新导入CSV
        importCsvToDb(db, "card_data_index.csv", "card_data_index");
        importCsvToDb(db, "card_data_1.csv", "card_data_1");
        importCsvToDb(db, "card_data_2.csv", "card_data_2");
        importCsvToDb(db, "card_data_3.csv", "card_data_3");
        importCsvToDb(db, "card_data_4.csv", "card_data_4");
        Log.d("DBHelper", "Card data csv files updated.");
    }

    // 从assets导入CSV数据到数据库
    private void importCsvToDb(SQLiteDatabase db, String csvFileName, String tableName) {
        CSVReader csvReader = null;
        int importedCount = 0; // Number of successfully imported rows
        try {
            // Open CSV file from assets
            InputStream inputStream = mContext.getAssets().open(csvFileName);
            // Use CSVReader to handle complex formats (commas inside fields, line breaks)
            csvReader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            // Read header row
            String[] header = csvReader.readNext();
            if (header == null) {
                Log.e("DBHelper", "CSV file is empty: " + csvFileName);
                return;
            }
            Log.d("DBHelper", "CSV header loaded. Column count: " + header.length + " (File: " + csvFileName + ")");
            // Start transaction for batch insertion
            db.beginTransaction();
            String[] rowData;
            while ((rowData = csvReader.readNext()) != null) {
                // Skip empty rows
                if (rowData.length == 0) continue;

                // Handle card_data_index table
                switch (tableName) {
                    case "card_data_index":
                        try {
                            db.execSQL("INSERT OR IGNORE INTO card_data_index (name, base_name, table_name, image_id) VALUES (?, ?, ?, ?)",
                                    new String[]{rowData[0], rowData[1], rowData[2], rowData[3]});
                            importedCount++;
                        } catch (Exception e) {
                            Log.e("DBHelper", "Failed to insert row into card_data_index. Error: " + e.getMessage() +
                                    ". Row data: " + arrayToString(rowData));
                        }
                        break;
                    // Handle card_data_1 table
                    case "card_data_1":
                        try {
                            db.execSQL("INSERT OR IGNORE INTO card_data_1 (" +
                                            "name, image_id_0, image_id_1, image_id_2, " +
                                            "corresponding_golden_card_name, corresponding_golden_card_image_id, " +
                                            "corresponding_fusion_card_name, corresponding_fusion_card_image_id, " +
                                            "base_info, category, price, sub_card, star, star_detail, " +
                                            "star_0, star_1, star_2, star_3, star_4, star_5, star_6, star_7, star_8, star_9, " +
                                            "star_10, star_11, star_12, star_13, star_14, star_15, star_16, star_M, star_U, " +
                                            "skill, skill_detail, skill_0, skill_1, skill_2, skill_3, skill_4, skill_5, skill_6, skill_7, skill_8, " +
                                            "transfer_change, additional_info) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                    new String[]{
                                            rowData[0], rowData[1], rowData[2], rowData[3], rowData[4],
                                            rowData[5], rowData[6], rowData[7], rowData[8], rowData[9],
                                            rowData[10], rowData[11], rowData[12], rowData[13], rowData[14],
                                            rowData[15], rowData[16], rowData[17], rowData[18], rowData[19],
                                            rowData[20], rowData[21], rowData[22], rowData[23], rowData[24],
                                            rowData[25], rowData[26], rowData[27], rowData[28], rowData[29],
                                            rowData[30], rowData[31], rowData[32], rowData[33], rowData[34],
                                            rowData[35], rowData[36], rowData[37], rowData[38], rowData[39],
                                            rowData[40], rowData[41], rowData[42], rowData[43], rowData[44],
                                            rowData[45]
                                    });
                            importedCount++;
                        } catch (Exception e) {
                            Log.e("DBHelper", "Failed to insert row into card_data_1. Error: " + e.getMessage() +
                                    ". Row data: " + arrayToString(rowData));
                        }
                        break;
                    // Handle card_data_2 table
                    case "card_data_2":
                        try {
                            db.execSQL("INSERT OR IGNORE INTO card_data_2 (" +
                                            "name, name_2, name_3, " +
                                            "name_1_1, image_id_1_1, name_1_2, image_id_1_2, image_result_id_1, " +
                                            "name_2_2, image_id_2_2, image_result_id_2, " +
                                            "name_3_2, image_id_3_2, image_result_id_3, " +
                                            "base_info, fusion_info, " +
                                            "category, price, sub_card, " +
                                            "star, star_detail, " +
                                            "star_0, star_1, star_2, star_3, star_4, star_5, star_6, star_7, star_8, star_9, " +
                                            "star_10, star_11, star_12, star_13, star_14, star_15, star_16, star_M, star_U, " +
                                            "star_fusion, star_fusion_detail, " +
                                            "star_fusion_0, star_fusion_1, star_fusion_2, star_fusion_3, star_fusion_4, star_fusion_5, star_fusion_6, star_fusion_7, star_fusion_8, star_fusion_9, " +
                                            "star_fusion_10, star_fusion_11, star_fusion_12, star_fusion_13, star_fusion_14, star_fusion_15, star_fusion_16, star_fusion_M, star_fusion_U, " +
                                            "skill, skill_detail, skill_0, skill_1, skill_2, skill_3, skill_4, skill_5, skill_6, skill_7, skill_8, " +
                                            "transfer_change, additional_info) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                    new String[]{
                                            rowData[0], rowData[1], rowData[2], rowData[3], rowData[4],
                                            rowData[5], rowData[6], rowData[7], rowData[8], rowData[9],
                                            rowData[10], rowData[11], rowData[12], rowData[13], rowData[14],
                                            rowData[15], rowData[16], rowData[17], rowData[18], rowData[19],
                                            rowData[20], rowData[21], rowData[22], rowData[23], rowData[24],
                                            rowData[25], rowData[26], rowData[27], rowData[28], rowData[29],
                                            rowData[30], rowData[31], rowData[32], rowData[33], rowData[34],
                                            rowData[35], rowData[36], rowData[37], rowData[38], rowData[39],
                                            rowData[40], rowData[41], rowData[42], rowData[43], rowData[44],
                                            rowData[45], rowData[46], rowData[47], rowData[48], rowData[49],
                                            rowData[50], rowData[51], rowData[52], rowData[53], rowData[54],
                                            rowData[55], rowData[56], rowData[57], rowData[58], rowData[59],
                                            rowData[60], rowData[61], rowData[62], rowData[63], rowData[64],
                                            rowData[65], rowData[66], rowData[67], rowData[68], rowData[69],
                                            rowData[70], rowData[71], rowData[72], rowData[73]
                                    });
                            importedCount++;
                        } catch (Exception e) {
                            Log.e("DBHelper", "Failed to insert row into card_data_2. Error: " + e.getMessage() +
                                    ". Row data: " + arrayToString(rowData));
                        }
                        break;
                    // Handle card_data_3 table
                    case "card_data_3":
                        try {
                            db.execSQL("INSERT OR IGNORE INTO card_data_3 (" +
                                            "name, name_1, name_2, name_3, " +
                                            "image_id_0, image_id_1, image_id_2, image_id_3, " +
                                            "name_1_1, image_id_1_1, name_1_2, image_id_1_2, " +
                                            "base_info, category, price, sub_card, " +
                                            "star, star_detail, " +
                                            "star_0, star_1, star_2, star_3, star_4, star_5, star_6, star_7, star_8, star_9, " +
                                            "star_10, star_11, star_12, star_13, star_14, star_15, star_16, star_M, star_U, " +
                                            "support_1, support_2, " +
                                            "skill, skill_detail, " +
                                            "skill_0, skill_1, skill_2, skill_3, skill_4, skill_5, skill_6, skill_7, skill_8, " +
                                            "transfer_change, additional_info, decompose_item, " +
                                            "decompose_card_1, decompose_card_2, decompose_card_3, decompose_card_4, " +
                                            "decompose_skill_1, decompose_skill_2, decompose_skill_3, decompose_skill_4, " +
                                            "decompose_transfer_1_a, decompose_transfer_1_b, decompose_transfer_1_c, " +
                                            "decompose_transfer_2_a, decompose_transfer_2_b, decompose_transfer_2_c, " +
                                            "decompose_transfer_3_a, decompose_transfer_3_b, decompose_transfer_3_c, " +
                                            "decompose_compose, " +
                                            "get_card_1, get_card_2, get_card_3, get_card_4, " +
                                            "get_skill_1, get_skill_2, get_skill_3, get_skill_4, " +
                                            "get_transfer_1_a, get_transfer_1_b, get_transfer_1_c, " +
                                            "get_transfer_2_a, get_transfer_2_b, get_transfer_2_c, " +
                                            "get_transfer_3_a, get_transfer_3_b, get_transfer_3_c, " +
                                            "get_compose, " +
                                            "decompose_image_id_card_1, decompose_image_id_card_2, decompose_image_id_card_3, decompose_image_id_card_4, " +
                                            "decompose_image_id_skill_1, decompose_image_id_skill_2, decompose_image_id_skill_3, decompose_image_id_skill_4, " +
                                            "decompose_image_id_transfer_1_a, decompose_image_id_transfer_1_b, decompose_image_id_transfer_1_c, " +
                                            "decompose_image_id_transfer_2_a, decompose_image_id_transfer_2_b, decompose_image_id_transfer_2_c, " +
                                            "decompose_image_id_transfer_3_a, decompose_image_id_transfer_3_b, decompose_image_id_transfer_3_c, " +
                                            "decompose_image_id_compose) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                    new String[]{
                                            rowData[0], rowData[1], rowData[2], rowData[3], rowData[4],
                                            rowData[5], rowData[6], rowData[7], rowData[8], rowData[9],
                                            rowData[10], rowData[11], rowData[12], rowData[13], rowData[14],
                                            rowData[15], rowData[16], rowData[17], rowData[18], rowData[19],
                                            rowData[20], rowData[21], rowData[22], rowData[23], rowData[24],
                                            rowData[25], rowData[26], rowData[27], rowData[28], rowData[29],
                                            rowData[30], rowData[31], rowData[32], rowData[33], rowData[34],
                                            rowData[35], rowData[36], rowData[37], rowData[38], rowData[39],
                                            rowData[40], rowData[41], rowData[42], rowData[43], rowData[44],
                                            rowData[45], rowData[46], rowData[47], rowData[48], rowData[49],
                                            rowData[50], rowData[51], rowData[52], rowData[53], rowData[54],
                                            rowData[55], rowData[56], rowData[57], rowData[58], rowData[59],
                                            rowData[60], rowData[61], rowData[62], rowData[63], rowData[64],
                                            rowData[65], rowData[66], rowData[67], rowData[68], rowData[69],
                                            rowData[70], rowData[71], rowData[72], rowData[73], rowData[74],
                                            rowData[75], rowData[76], rowData[77], rowData[78], rowData[79],
                                            rowData[80], rowData[81], rowData[82], rowData[83], rowData[84],
                                            rowData[85], rowData[86], rowData[87], rowData[88], rowData[89],
                                            rowData[90], rowData[91], rowData[92], rowData[93], rowData[94],
                                            rowData[95], rowData[96], rowData[97], rowData[98], rowData[99],
                                            rowData[100], rowData[101], rowData[102], rowData[103], rowData[104],
                                            rowData[105], rowData[106]
                                    });
                            importedCount++;
                        } catch (Exception e) {
                            Log.e("DBHelper", "Failed to insert row into card_data_3. Error: " + e.getMessage() +
                                    ". Row data: " + arrayToString(rowData));
                        }
                        break;
                    // Handle card_data_4 table
                    case "card_data_4":
                        try {
                            db.execSQL("INSERT OR IGNORE INTO card_data_4 (" +
                                            "name, name_1, name_2, image_id_0, image_id_1, image_id_2, " +
                                            "corresponding_golden_card_name, corresponding_golden_card_image_id, " +
                                            "corresponding_fusion_card_name, corresponding_fusion_card_image_id, " +
                                            "base_info, category, price, sub_card, " +
                                            "star, star_detail, " +
                                            "star_0, star_1, star_2, star_3, star_4, star_5, star_6, star_7, star_8, star_9, " +
                                            "star_10, star_11, star_12, star_13, star_14, star_15, star_16, star_M, star_U, " +
                                            "skill, skill_detail, " +
                                            "skill_0, skill_1, skill_2, skill_3, skill_4, skill_5, skill_6, skill_7, skill_8, " +
                                            "transfer_change, additional_info, decompose_item, " +
                                            "decompose_card_1, decompose_card_2, decompose_card_3, " +
                                            "decompose_skill_1, decompose_skill_2, decompose_skill_3, decompose_skill_4, " +
                                            "decompose_transfer_1_a, decompose_transfer_1_b, " +
                                            "decompose_transfer_2_a, decompose_transfer_2_b, decompose_transfer_2_c, " +
                                            "get_card_1, get_card_2, get_card_3, " +
                                            "get_skill_1, get_skill_2, get_skill_3, get_skill_4, " +
                                            "get_transfer_1_a, get_transfer_1_b, " +
                                            "get_transfer_2_a, get_transfer_2_b, get_transfer_2_c, " +
                                            "decompose_image_id_card_1, decompose_image_id_card_2, decompose_image_id_card_3, " +
                                            "decompose_image_id_skill_1, decompose_image_id_skill_2, decompose_image_id_skill_3, decompose_image_id_skill_4, " +
                                            "decompose_image_id_transfer_1_a, decompose_image_id_transfer_1_b, " +
                                            "decompose_image_id_transfer_2_a, decompose_image_id_transfer_2_b, decompose_image_id_transfer_2_c)" +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                    new String[]{
                                            rowData[0], rowData[1], rowData[2], rowData[3], rowData[4], rowData[5],
                                            rowData[6], rowData[7], rowData[8], rowData[9], rowData[10], rowData[11],
                                            rowData[12], rowData[13], rowData[14], rowData[15], rowData[16], rowData[17],
                                            rowData[18], rowData[19], rowData[20], rowData[21], rowData[22], rowData[23],
                                            rowData[24], rowData[25], rowData[26], rowData[27], rowData[28], rowData[29],
                                            rowData[30], rowData[31], rowData[32], rowData[33], rowData[34], rowData[35],
                                            rowData[36], rowData[37], rowData[38], rowData[39], rowData[40], rowData[41],
                                            rowData[42], rowData[43], rowData[44], rowData[45], rowData[46], rowData[47],
                                            rowData[48], rowData[49], rowData[50], rowData[51], rowData[52], rowData[53],
                                            rowData[54], rowData[55], rowData[56], rowData[57], rowData[58], rowData[59],
                                            rowData[60], rowData[61], rowData[62], rowData[63], rowData[64], rowData[65],
                                            rowData[66], rowData[67], rowData[68], rowData[69], rowData[70], rowData[71],
                                            rowData[72], rowData[73], rowData[74], rowData[75], rowData[76], rowData[77],
                                            rowData[78], rowData[79], rowData[80], rowData[81], rowData[82], rowData[83],
                                            rowData[84]
                                    });
                            importedCount++;
                        } catch (Exception e) {
                            Log.e("DBHelper", "Failed to insert row into card_data_4. Error: " + e.getMessage() +
                                    ". Row data: " + arrayToString(rowData));
                        }
                        break;
                }
            }
            // Commit transaction if all operations succeed
            db.setTransactionSuccessful();
            Log.d("DBHelper", "Successfully imported " + importedCount + " rows into " + tableName);
        } catch (IOException e) {
            Log.e("DBHelper", "Failed to read CSV file: " + csvFileName + ". Error: " + e.getMessage());
        } catch (Exception e) {
            Log.e("DBHelper", "Unexpected error during CSV import. Error: " + e.getMessage());
        } finally {
            // Close CSV reader and end transaction
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (IOException e) {
                    Log.e("DBHelper", "Failed to close CSV reader. Error: " + e.getMessage());
                }
            }
            db.endTransaction();
        }
    }

    // 将字符串数组转换为可读字符串的辅助方法
    private String arrayToString(String[] array) {
        if (array == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            sb.append("'").append(s).append("', ");
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "";
    }

    // ====================== 以下为data_station表的操作方法 ======================
    public String getDataStationValue(String content) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_DATA_STATION,
                new String[]{"value"},
                "content = ?",
                new String[]{content},
                null, null, null
        );
        String value = "";
        if (cursor.moveToFirst()) {
            value = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return value;
    }

    public void updateDataStationValue(String content, String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("value", value);
        db.update(TABLE_DATA_STATION, values, "content = ?", new String[]{content});
        db.close();
    }

    // ====================== 以下为settings表的操作方法 ======================
    public boolean getSettingValue(String content) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_SETTINGS,
                new String[]{"value"},
                "content = ?",
                new String[]{content},
                null, null, null
        );
        String value = "";
        if (cursor.moveToFirst()) {
            value = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return Objects.equals(value, "true");
    }

    public String getSettingValueString(String content) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_SETTINGS,
                new String[]{"value"},
                "content = ?",
                new String[]{content},
                null, null, null
        );
        String value = "";
        if (cursor.moveToFirst()) {
            value = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return value;
    }

    public int getCurrentMaterialAlertDialogThemeStyle() {
        String currentStyle = getSettingValueString(CONTENT_INTERFACE_STYLE);
        // 界面风格ID
        int themeStyleId;
        if ("鲜艳-立体".equals(currentStyle)) {
            themeStyleId = R.style.MaterialAlertDialog_Shadow; // 鲜艳主题
        } else {
            themeStyleId = R.style.MaterialAlertDialog_NoShadow; // 素雅主题
        }
        return themeStyleId;
    }

    public void updateSettingValue(String content, String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("value", value);
        db.update(TABLE_SETTINGS, values, "content = ?", new String[]{content});
        db.close();
    }

    // ====================== 以下为meishi_wechat表的操作方法 ======================
    public void insertMeishiWechat(String openid, String serverName, String playerId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("openid", openid);
        values.put("server_name", serverName);
        values.put("player_id", playerId);
        db.insertWithOnConflict(TABLE_MEISHI_WECHAT, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public void deleteMeishiWechat(String openid) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MEISHI_WECHAT, "openid = ?", new String[]{openid});
        db.close();
    }

    public List<PlayerInfo> getAllMeishiWechat() {
        List<PlayerInfo> infos = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_MEISHI_WECHAT,
                new String[]{"openid", "server_name", "player_id"},
                null, null, null, null, null
        );
        if (cursor.moveToFirst()) {
            do {
                infos.add(new PlayerInfo(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return infos;
    }

    // 玩家信息辅助类
    public static class PlayerInfo {
        public String openid;
        public String serverName;
        public String playerId;

        public PlayerInfo(String openid, String serverName, String playerId) {
            this.openid = openid;
            this.serverName = serverName;
            this.playerId = playerId;
        }
    }

    // ====================== 以下为dashboard表的操作方法 ======================
    public String getDashboardContent(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_DASHBOARD,
                new String[]{"content"},
                "id = ?",
                new String[]{id},
                null, null, null
        );
        String content = "";
        if (cursor.moveToFirst()) {
            content = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return content;
    }

    public void updateDashboardContent(String id, String content) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("content", content);
        db.update(TABLE_DASHBOARD, values, "id = ?", new String[]{id});
        db.close();
    }

    // ====================== 以下为防御卡数据表的操作方法 ======================
    // 模糊查询卡片名称和对应图片ID
    public List<CardSuggestion> searchCards(String keyword) {
        List<CardSuggestion> suggestions = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        // 同时查询name和image_id两列
        Cursor cursor = db.rawQuery(
                "SELECT name, image_id, table_name FROM " + TABLE_CARD_DATA_INDEX + " WHERE name LIKE ?",
                new String[]{"%" + keyword + "%"});

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String imageId = cursor.getString(1);
                String tableName = cursor.getString(2);
                // 过滤空名称或空图片ID（可选，根据业务需求调整）
                if (name != null && !name.isEmpty()) {
                    int lastNum = Character.getNumericValue(imageId.charAt(imageId.length() - 1));
                    int tableNameNum = Character.getNumericValue(tableName.charAt(tableName.length() - 1));
                    String transferCategory = null;
                    switch (lastNum) {
                        case 0:
                            transferCategory = "不转形态";
                            break;
                        case 1:
                            if (tableNameNum == 3) {
                                transferCategory = "三转形态";
                            } else {
                                transferCategory = "一转形态";
                            }
                            break;
                        case 2:
                            if (tableNameNum == 3) {
                                transferCategory = "四转形态";
                            } else {
                                transferCategory = "二转形态";
                            }
                            break;
                        case 3:
                            transferCategory = "终转形态";
                            break;
                    };
                    suggestions.add(new CardSuggestion(name, transferCategory, imageId));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return suggestions;
    }

    // 获取卡片对应的表名
    public String getCardTable(String cardName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT table_name FROM " + TABLE_CARD_DATA_INDEX + " WHERE name = ?",
                new String[]{cardName});
        if (cursor.moveToFirst()) {
            String tableName = cursor.getString(0);
            cursor.close();
            return tableName;
        }
        cursor.close();
        return null;
    }

    // 获取卡片对应的表名
    public String getCardBaseName(String cardName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT base_name FROM card_data_index WHERE name = ?",
                new String[]{cardName});
        if (cursor.moveToFirst()) {
            String baseName = cursor.getString(0);
            cursor.close();
            return baseName;
        }
        cursor.close();
        return null;
    }

    // 查询指定表中的卡片数据
    public Cursor getCardData(String tableName, String name) {
        SQLiteDatabase db = getReadableDatabase();

        Log.d("CardData", "tableName = " + tableName);
        Log.d("CardData", "cardName = " + name);

        // 注意：表名和列名需与实际创建的一致，避免SQL语法错误
        return db.query(
                tableName,          // 表名
                null,               // 查询所有列
                "name = ?",         // 条件：按名称查询
                new String[]{name}, // 参数
                null, null, null
        );
    }

}
