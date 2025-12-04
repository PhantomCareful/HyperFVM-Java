package com.careful.HyperFVM.utils.DBHelper;

import static com.careful.HyperFVM.utils.DBHelper.DatabaseInfo.DB_VERSION;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
    private Context mContext;

    // 表名常量
    public static final String TABLE_MEISHI_WECHAT = "meishi_wechat";
    public static final String TABLE_DASHBOARD = "dashboard";
    public static final String TABLE_SETTINGS = "settings";

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
        // 创建meishi_wechat表
        String createMeishi = "CREATE TABLE IF NOT EXISTS " + TABLE_MEISHI_WECHAT + " (" +
                "openid TEXT PRIMARY KEY," +
                "server_name TEXT," +
                "player_id TEXT)";
        db.execSQL(createMeishi);

        // 创建dashboard表
        String createDashboard = "CREATE TABLE IF NOT EXISTS " + TABLE_DASHBOARD + " (" +
                "id TEXT PRIMARY KEY," +
                "content TEXT)";
        db.execSQL(createDashboard);

        // 升级到版本5以后添加防御卡数据表
        createCardTables(db); // 创建表结构
        // 从5开始，后续版本都需要清空表并重新导入CSV
        clearAndImportCardData(db);

        // 创建表"settings"，用于记录设置内容
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SETTINGS + " (" +
                "content TEXT PRIMARY KEY," +
                "value TEXT)");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    // 数据库版本升级时调用（核心：处理表结构变更）
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 从5开始每次都要做的
        // 添加防御卡数据表
        createCardTables(db); // 创建表结构
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
    }

    // 升级到版本5+时添加防御卡数据表，操作为：如果表存在，则清空内容，再将csv的数据导入到表中。
    private void createCardTables(SQLiteDatabase db) {
        // 创建card_data_index表
        db.execSQL("CREATE TABLE IF NOT EXISTS card_data_index (" +
                "name TEXT PRIMARY KEY, " +
                "table_name TEXT NOT NULL)");

        // 创建card_data_1表（字段与CSV对应）
        db.execSQL("CREATE TABLE IF NOT EXISTS card_data_1 (" +
                "name TEXT PRIMARY KEY, " +
                "image_id TEXT, " +
                "base_info TEXT, " +
                "is_animal_card TEXT, " +
                "is_constellation_card TEXT, " +
                "is_golden_crad TEXT, " +
                "category TEXT, " +
                "price_0 TEXT, " +
                "sub_card TEXT, " +
                "star TEXT, " +
                "star_0 TEXT, star_1 TEXT, star_2 TEXT, star_3 TEXT, star_4 TEXT, " +
                "star_5 TEXT, star_6 TEXT, star_7 TEXT, star_8 TEXT, star_9 TEXT, " +
                "star_10 TEXT, star_11 TEXT, star_12 TEXT, star_13 TEXT, star_14 TEXT, " +
                "star_15 TEXT, star_16 TEXT, star_M TEXT, star_U TEXT, " +
                "skill TEXT, " +
                "skill_0 TEXT, skill_1 TEXT, skill_2 TEXT, skill_3 TEXT, skill_4 TEXT, " +
                "skill_5 TEXT, skill_6 TEXT, skill_7 TEXT, skill_8 TEXT, " +
                "transfer_change TEXT, " +
                "additional_info TEXT)");
    }

    // 清空表并重新导入CSV数据（每次升级都执行）
    private void clearAndImportCardData(SQLiteDatabase db) {
        // 清空现有数据
        db.execSQL("DELETE FROM card_data_index");
        db.execSQL("DELETE FROM card_data_1");

        // 重新导入CSV
        importCsvToDb(db, "card_data_index.csv", "card_data_index");
        importCsvToDb(db, "card_data_1.csv", "card_data_1");
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
                // Handle card_data_1 table
                if (tableName.equals("card_data_1")) {
                    // Validate column count (expected 40 columns)
                    if (rowData.length != 41) {
                        Log.e("DBHelper", "Column count mismatch for card_data_1. Expected 40, got " + rowData.length +
                                ". Row data: " + arrayToString(rowData));
                        continue;
                    }
                    // Insert row into database
                    try {
                        db.execSQL("INSERT OR IGNORE INTO card_data_1 (" +
                                        "name, image_id, base_info, is_animal_card, is_constellation_card, is_golden_crad, " +
                                        "category, price_0, sub_card, star, " +
                                        "star_0, star_1, star_2, star_3, star_4, star_5, star_6, star_7, star_8, star_9, " +
                                        "star_10, star_11, star_12, star_13, star_14, star_15, star_16, star_M, star_U, " +
                                        "skill, skill_0, skill_1, skill_2, skill_3, skill_4, skill_5, skill_6, skill_7, skill_8, " +
                                        "transfer_change, additional_info) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                new String[]{
                                        rowData[0], rowData[1], rowData[2], rowData[3], rowData[4],
                                        rowData[5], rowData[6], rowData[7], rowData[8], rowData[9],
                                        rowData[10], rowData[11], rowData[12], rowData[13], rowData[14],
                                        rowData[15], rowData[16], rowData[17], rowData[18], rowData[19],
                                        rowData[20], rowData[21], rowData[22], rowData[23], rowData[24],
                                        rowData[25], rowData[26], rowData[27], rowData[28], rowData[29],
                                        rowData[30], rowData[31], rowData[32], rowData[33], rowData[34],
                                        rowData[35], rowData[36], rowData[37], rowData[38], rowData[39],
                                        rowData[40]
                                });
                        importedCount++;
                    } catch (Exception e) {
                        Log.e("DBHelper", "Failed to insert row into card_data_1. Error: " + e.getMessage() +
                                ". Row data: " + arrayToString(rowData));
                    }
                }
                // Handle card_data_index table
                else if (tableName.equals("card_data_index")) {
                    // Validate column count (expected at least 2 columns)
                    if (rowData.length < 2) {
                        Log.e("DBHelper", "Insufficient columns for card_data_index. Expected at least 2, got " + rowData.length +
                                ". Row data: " + arrayToString(rowData));
                        continue;
                    }
                    // Insert row into database
                    try {
                        db.execSQL("INSERT OR IGNORE INTO card_data_index (name, table_name) VALUES (?, ?)",
                                new String[]{rowData[0].trim(), rowData[1].trim()});
                        importedCount++;
                    } catch (Exception e) {
                        Log.e("DBHelper", "Failed to insert row into card_data_index. Error: " + e.getMessage() +
                                ". Row data: " + arrayToString(rowData));
                    }
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
    // 模糊查询卡片名称
    public List<String> searchCardNames(String keyword) {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM card_data_index WHERE name LIKE ?",
                new String[]{"%" + keyword + "%"});
        if (cursor.moveToFirst()) {
            do {
                names.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return names;
    }

    // 获取卡片对应的表名
    public String getCardTable(String cardName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT table_name FROM card_data_index WHERE name = ?",
                new String[]{cardName});
        if (cursor.moveToFirst()) {
            String table = cursor.getString(0);
            cursor.close();
            return table;
        }
        cursor.close();
        return null;
    }

    // 查询指定表中的卡片数据
    public Cursor getCardData(String tableName, String name) {
        SQLiteDatabase db = getReadableDatabase();
        // 注意：表名和列名需与实际创建的一致，避免SQL语法错误
        return db.query(
                tableName,       // 表名
                null,            // 查询所有列
                "name = ?",      // 条件：按名称查询
                new String[]{name}, // 参数
                null, null, null
        );
    }

}
