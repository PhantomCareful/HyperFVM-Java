package com.careful.HyperFVM.utils.DBHelper;

public class DatabaseInfo {
    /**
     * DB_VERSION = 5
     * 装入了第1-4张数据图中的卡片信息
     * DB_VERSION = 6
     * 装入了第5-6张数据图中的卡片信息
     * 新增settings表，保存设置内容
     * DB_VERSION = 7
     * 新的星级和技能展示形式
     * 完善部分数据
     * DB_VERSION = 8
     * settings表增加“主题-是否动态取色”和“主题-自定义主题色”两条数据
     * 装入了第7-8张数据图中的卡片信息
     * DB_VERSION = 9
     * settings表增加“主题-深色主题”
     * settings表增加“自动任务”
     * 装入了第9-10张数据图中的卡片信息
     * DB_VERSION = 10
     * 装入了第11张数据图中的卡片信息
     * DB_VERSION = 11
     * 装入了第12张数据图中的卡片信息
     * DB_VERSION = 12
     * 装入了第13张数据图中的卡片信息
     * DB_VERSION = 13
     * 装入了第14张数据图中的卡片信息
     * DB_VERSION = 14
     * 装入了第15张数据图中的卡片信息
     * DB_VERSION = 15
     * 优化通知：仪表盘展示的文案和通知展示的文案分开存储
     * DB_VERSION = 16
     * 装入融合卡数据
     * DB_VERSION = 17
     * 装入金卡分解兑换数据
     * DB_VERSION = 18
     * 装入全部金卡分解兑换数据
     * DB_VERSION = 19
     * 修复管线类-枪塔类部分卡片配图错误
     * DB_VERSION = 20
     * 新增data_station表
     * DB_VERSION = 21
     * 修复深色模式设置失效问题...
     * DB_VERSION = 22
     * 在data_station表中新增：CurrentUpdateLogImage，用于存储图片资源的当前版本更新日志
     * DB_VERSION = 23
     * 在data_station表中新增：DownloadedApkFileName，用于存储上一次下载的安装包文件名，以防重复下载
     * 使用了新的增量更新策略，这个版本需要将DataImagesVersionCode的内容强行重置为0
     * DB_VERSION = 24
     * 防御卡数据新增阴阳蛇
     * DB_VERSION = 25
     * settings表增加“自动任务-增强”
     * DB_VERSION = 26
     * 让融合卡、金卡数据支持Markdown显示
     * DB_VERSION = 27
     * 修复后裔图片显示错误的问题
     * DB_VERSION = 28
     * 防御卡数据新增融合耗油双菇、至尊水神
     * DB_VERSION = 29
     * 让星座卡数据支持Markdown显示
     */
    public static final int DB_VERSION = 29;
}
