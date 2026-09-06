package com.careful.HyperFVM.Activities.DataCenter;

import android.content.Context;

import com.careful.HyperFVM.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 防御卡目录页面的静态数据表。
 * <p>
 * 分节相关的所有静态配置都集中在这里，四个集合长度均为 47、下标即分节号（从 0 开始），
 * 增删分节时四个集合 + 弹窗布局需按同一位置同步增删：
 * <ul>
 *   <li>SECTION_PREFIXES[i]：第 i 个分节的公共 id 前缀（如 "card_data_index_1_1"）；</li>
 *   <li>SECTION_NAMES[i][j]：该分节第 j+1 张卡片在数据库中对应的卡名（行内连续编号 1..N），
 *       数组顺序即页面显示顺序；</li>
 *   <li>SECTION_TITLE_RES_IDS[i]：该分节标题的字符串资源 id {主标题, 子标题}
 *       （无子标题的分节第二项为 0，目前仅 16_1）；</li>
 *   <li>SECTION_BUTTON_RES_IDS[i]：标题导航弹窗中该分节按钮的 id。</li>
 * </ul>
 * <p>
 * 由此可推导：
 * - 单卡视图 id：  SECTION_PREFIXES[i] + "_" + (j + 1)
 * - 单卡布局文件： "card_" + SECTION_PREFIXES[i] + "_" + (j + 1)
 * （布局文件名与 id 命名一一对应，均由原 XML 体系保证）
 * <p>
 * 注意 1：个别分节不存在（如 11_1），数组中已按真实存在的分节顺序排列，
 * 共 47 个分节、367 张卡片，与原页面点击绑定一一对应。
 * <p>
 * 注意 2：strings.xml 中分节标题资源与弹窗按钮 id 存在历史命名错位
 * （13_x 系资源名为 text_data_images_index_card_12__n / button_card_category_index_12__n，
 * 14_x 系用 13_n、15_x 系用 14_n、16_1 只有 text_data_images_index_card_15 一个主标题），
 * 下列罗列按"真实资源名"照抄，行注释标的是数据表分节号，不要"修正"资源名。
 */
public final class CardDataCatalogData {

    private CardDataCatalogData() {
    }

    /** 47 个分节前缀 */
    public static final String[] SECTION_PREFIXES = {
            "card_data_index_1_1", "card_data_index_1_2", "card_data_index_1_3", "card_data_index_1_4",
            "card_data_index_2_1", "card_data_index_2_2", "card_data_index_2_3",
            "card_data_index_3_1", "card_data_index_3_2", "card_data_index_3_3",
            "card_data_index_4_1", "card_data_index_4_2", "card_data_index_4_3",
            "card_data_index_5_1", "card_data_index_5_2",
            "card_data_index_6_1", "card_data_index_6_2",
            "card_data_index_7_1", "card_data_index_7_2",
            "card_data_index_8_1", "card_data_index_8_2",
            "card_data_index_9_1", "card_data_index_9_2", "card_data_index_9_3", "card_data_index_9_4", "card_data_index_9_5",
            "card_data_index_10_1", "card_data_index_10_2", "card_data_index_10_3", "card_data_index_10_4",
            "card_data_index_11_2", "card_data_index_11_3", "card_data_index_11_4",
            "card_data_index_12_1", "card_data_index_12_2", "card_data_index_12_3",
            "card_data_index_13_1", "card_data_index_13_2", "card_data_index_13_3", "card_data_index_13_4",
            "card_data_index_14_1", "card_data_index_14_2", "card_data_index_14_3", "card_data_index_14_4",
            "card_data_index_15_1", "card_data_index_15_2",
            "card_data_index_16_1"
    };

    /** 47 个分节各自的卡名（顺序即页面显示顺序） */
    public static final String[][] SECTION_NAMES = {
            /* 1_1 */ {"双向水管", "天秤座精灵", "呆呆鸡", "阿瑞斯神使", "二哈汪", "双枪喵", "散弹牛", "威风虎"},
            /* 1_2 */ {"三线酒架", "射手座精灵", "砰砰鸡", "丘比特神使", "狩猎汪", "猪猪猎手", "炙烤灯笼鱼"},
            /* 1_3 */ {"小笼包", "双层小笼包", "三向小笼包", "机枪小笼包", "冰冻小笼包", "双层冰冻小笼包", "三向冰冻小笼包", "机枪冰冻小笼包", "国王小笼包", "三向国王小笼包", "贵族小笼包", "玉蜀黍", "包包龙", "咖啡杯", "水上茶杯", "激光汪"},
            /* 1_4 */ {"枪塔喵", "弩箭牛", "仙人掌刺身"},
            /* 2_1 */ {"勺勺兔", "窃蛋龙", "尤弥尔神使", "幻影蛇", "全能糖球投手", "金乌马"},
            /* 2_2 */ {"煮蛋器投手", "冰煮蛋器", "双鱼座精灵", "弹弹鸡", "索尔神使", "机械汪", "投弹猪", "雪糕投手", "飞鱼喵", "壮壮牛", "烤蜥蜴投手", "投篮虎", "钵钵鸡", "色拉投手", "巧克力投手", "臭豆腐投手", "8周年蛋糕"},
            /* 2_3 */ {"生煎锅", "铛铛虎", "祝融神使", "糖炒栗子", "霜霜蛇", "马卡龙烤箱"},
            /* 3_1 */ {"炭烧海星", "猪猪料理机", "陀螺喵", "哈迪斯神使", "查克拉兔"},
            /* 3_2 */ {"厨师虎", "星星兔", "坚果爆炒机", "里格神使", "邪恶牛油果", "怪味鱿鱼", "烟花虎", "风车龙", "元宝饺子鼎"},
            /* 3_3 */ {"鲈鱼", "便便汪", "烧鸡", "饼干汪", "牛角面包", "盾盾汪"},
            /* 4_1 */ {"火盆", "金牛座精灵", "洛基神使", "暖炉汪", "能量喵", "坩埚蛇", "猪猪加强器", "蓝莓信号塔塔", "美味水果塔", "欧若拉神使"},
            /* 4_2 */ {"莓果点心", "香料虎", "塔利亚神使", "精灵龙", "龙须面", "五谷丰登", "五行蛇", "弗雷神使", "加速榨汁机", "魔杖蛇", "塔拉萨神使", "音盒马", "浮生茶", "炎焱兔"},
            /* 4_3 */ {"11周年美食盒子", "战旗马"},
            /* 5_1 */ {"小火炉", "大火炉", "酒杯灯", "双子座精灵", "咕咕鸡", "暖暖鸡", "阿波罗神使", "7周年蜡烛", "火焰牛", "花火龙", "蛇羹煲", "禅心马"},
            /* 5_2 */ {"钱罐猪", "罐罐牛", "烈火虎"},
            /* 6_1 */ {"樱桃反弹布丁", "艾草粑粑", "布丁汪", "凉粉牛", "忒提丝神使"},
            /* 6_2 */ {"木盘子", "盘盘鸡", "猫猫盘", "魔法软糖", "棉花糖", "苏打气泡", "麦芽糖"},
            /* 7_1 */ {"糖葫芦炮弹", "跳跳鸡", "防空喵", "赫丘利神使"},
            /* 7_2 */ {"香肠", "热狗大炮", "弹簧虎", "泡泡龙", "爱心便当", "梦幻多拿滋", "埃罗斯神使", "耗油双菇", "奶茶猪", "科技喵"},
            /* 8_1 */ {"咖啡喷壶", "关东煮喷锅", "烈焰龙", "赫斯提亚神使", "肥牛火锅", "麻辣香锅"},
            /* 8_2 */ {"旋转咖啡喷壶", "狮子座精灵", "波塞冬神使", "转转鸡", "可乐汪", "元气牛", "巫蛊蛇"},
            /* 9_1 */ {"章鱼烧", "巨蟹座精灵", "忍忍鸡", "狄安娜神使", "飞盘汪", "铁甲飞镖猪", "海盗兔"},
            /* 9_2 */ {"咖喱龙虾炮", "雅典娜守护", "火箭猪", "宙斯神使"},
            /* 9_3 */ {"魔法猪", "招财喵", "雪球兔", "典伊神使", "冰晶龙", "冰块冷萃机"},
            /* 9_4 */ {"金刚马", "红柳烤串机", "俱吠罗神使"},
            /* 9_5 */ {"鼠鼠蛋糕空投器", "风力空投猪", "电流虎", "霹雳马", "归元马", "蜜糖陷阱", "仙笛马", "萌海马"},
            /* 10_1 */ {"汉堡包", "贪食蛙", "吞噬龙", "香辣年糕蟹", "混沌神使"},
            /* 10_2 */ {"雷电长棍面包", "三指兔", "结界马"},
            /* 10_3 */ {"新疆炒面", "丸子厨师", "功夫汪", "拳皇马", "鱼刺", "钢鱼刺", "糖渍刺梨"},
            /* 10_4 */ {"蜂蜜史莱姆", "糖人马"},
            /* 11_2 */ {"阴阳蛇", "焚寂马", "弹珠汽水"},
            /* 11_3 */ {"天蝎座精灵", "工程猪", "双刃蛇", "元素蛇", "御风马", "云霞马", "回旋虎", "大师兔", "15周年猴赛雷", "赖皮蛇", "迷你披萨炉", "鲁班神使", "炎凰马", "灯影花糕"},
            /* 11_4 */ {"焦油喷壶", "喷壶汪", "派派鸡", "小猪米花机", "喷气牛", "卖萌喵", "奥丁神使", "法师蛇", "街头烤肉大师", "后羿神使"},
            /* 12_1 */ {"巧克力大炮", "导弹蛇", "盖亚神使"},
            /* 12_2 */ {"可乐炸弹", "酒瓶炸弹", "开水壶炸弹", "威士忌炸弹", "潘多拉", "深水炸弹", "爆辣河豚", "爆竹", "美食烟花普通版", "美食烟花华丽版", "水瓶座精灵", "雷暴猪", "微波炉爆弹", "玉兔灯笼", "爆裂蛇", "糖果罐子", "烛阴龙", "老鼠夹子", "麻辣串炸弹", "竹筒粽子", "娇娇虎", "泡泡鸡尾酒"},
            /* 12_3 */ {"辣椒粉", "月蟾兔", "爆炸汪", "肉松清明粿", "10周年烟花", "芥末牛"},
            /* 13_1 */ {"钢丝球", "炸地鼠爆竹", "面粉袋", "椰子果", "青涩柿柿", "萌虎高压锅", "白羊座精灵", "酋长汪", "逗猫棒", "金牛烟花", "贪吃兔", "灵鱼摩蹉神使"},
            /* 13_2 */ {"榴莲", "美味电鳗", "镭射喵", "黑暗神使", "火龙果", "摩羯座精灵", "龙珠果", "巴德尔神使", "桥头米线", "泡椒春笋", "奇门马"},
            /* 13_3 */ {"冰桶炸弹", "冰弹喵", "冰兔菓子", "泡泡糖", "逆转牛"},
            /* 13_4 */ {"蛋蛋兔"},
            /* 14_1 */ {"冰激凌", "转龙壶", "美味计时器", "柯罗诺斯神使", "幻幻鸡", "百变蛇", "梵天神使", "顽皮龙", "圣诞包裹", "天使猪", "黯然销魂饭", "星穹马", "奥西里斯神使", "13周年时光机", "蛇蛇酒", "克洛托神使"},
            /* 14_2 */ {"油灯", "南瓜灯", "肉松清明粿", "防萤草灯笼", "萤火蛇", "换气扇", "9周年幸运草扇", "棕榈吹风机", "爆爆鸡", "清障猪", "旋风牛", "酸柠檬爆弹", "炸炸菇", "海盐粉", "碎冰喵"},
            /* 14_3 */ {"防风草沙拉", "金箔甜筒", "治愈喵", "12周年能量饮料", "木塞子", "咖啡粉", "尖叫马咖", "傀儡马"},
            /* 14_4 */ {"猫猫盒", "猫猫箱", "小丑盒子", "鼠乐宝味觉糖", "大福虎"},
            /* 15_1 */ {"土司面包", "月饼", "冰皮月饼", "巧克力面包", "菠萝爆炸面包", "老虎蟹面包", "桂花酒", "榴莲千层饼"},
            /* 15_2 */ {"瓜皮护罩", "处女座精灵", "赫拉神使", "祥龙环", "守能汪", "生日帽", "喵喵炉", "扑克牌护罩", "彩虹蛇"},
            /* 16_1 */ {"火炉菠萝面包", "雪芭煮蛋器", "火影怪味鱿鱼", "酱香锅烤栗子", "热狗耗油双菇", "子母三线酒架", "刺梨烧烤盘", "机枪咖啡杯", "葡萄味软糖", "脆心死神大炮", "仙人球海星刺身"}
    };

    /**
     * 47 个分节的标题字符串资源 id：每节 {主标题 id, 子标题 id}，无子标题的分节（16_1）第二项为 0。
     * 资源名存在历史错位（13 系=text_data_images_index_card_12__n 等），行注释为数据表分节号。
     */
    public static final int[][] SECTION_TITLE_RES_IDS = {
            /* 1_1 */ {R.string.title_data_images_index_card_1, R.string.text_data_images_index_card_1_1},
            /* 1_2 */ {R.string.title_data_images_index_card_1, R.string.text_data_images_index_card_1_2},
            /* 1_3 */ {R.string.title_data_images_index_card_1, R.string.text_data_images_index_card_1_3},
            /* 1_4 */ {R.string.title_data_images_index_card_1, R.string.text_data_images_index_card_1_4},
            /* 2_1 */ {R.string.title_data_images_index_card_2, R.string.text_data_images_index_card_2_1},
            /* 2_2 */ {R.string.title_data_images_index_card_2, R.string.text_data_images_index_card_2_2},
            /* 2_3 */ {R.string.title_data_images_index_card_2, R.string.text_data_images_index_card_2_3},
            /* 3_1 */ {R.string.title_data_images_index_card_3, R.string.text_data_images_index_card_3_1},
            /* 3_2 */ {R.string.title_data_images_index_card_3, R.string.text_data_images_index_card_3_2},
            /* 3_3 */ {R.string.title_data_images_index_card_3, R.string.text_data_images_index_card_3_3},
            /* 4_1 */ {R.string.title_data_images_index_card_4, R.string.text_data_images_index_card_4_1},
            /* 4_2 */ {R.string.title_data_images_index_card_4, R.string.text_data_images_index_card_4_2},
            /* 4_3 */ {R.string.title_data_images_index_card_4, R.string.text_data_images_index_card_4_3},
            /* 5_1 */ {R.string.title_data_images_index_card_5, R.string.text_data_images_index_card_5_1},
            /* 5_2 */ {R.string.title_data_images_index_card_5, R.string.text_data_images_index_card_5_2},
            /* 6_1 */ {R.string.title_data_images_index_card_6, R.string.text_data_images_index_card_6_1},
            /* 6_2 */ {R.string.title_data_images_index_card_6, R.string.text_data_images_index_card_6_2},
            /* 7_1 */ {R.string.title_data_images_index_card_7, R.string.text_data_images_index_card_7_1},
            /* 7_2 */ {R.string.title_data_images_index_card_7, R.string.text_data_images_index_card_7_2},
            /* 8_1 */ {R.string.title_data_images_index_card_8, R.string.text_data_images_index_card_8_1},
            /* 8_2 */ {R.string.title_data_images_index_card_8, R.string.text_data_images_index_card_8_2},
            /* 9_1 */ {R.string.title_data_images_index_card_9, R.string.text_data_images_index_card_9_1},
            /* 9_2 */ {R.string.title_data_images_index_card_9, R.string.text_data_images_index_card_9_2},
            /* 9_3 */ {R.string.title_data_images_index_card_9, R.string.text_data_images_index_card_9_3},
            /* 9_4 */ {R.string.title_data_images_index_card_9, R.string.text_data_images_index_card_9_4},
            /* 9_5 */ {R.string.title_data_images_index_card_9, R.string.text_data_images_index_card_9_5},
            /* 10_1 */ {R.string.title_data_images_index_card_10, R.string.text_data_images_index_card_10_1},
            /* 10_2 */ {R.string.title_data_images_index_card_10, R.string.text_data_images_index_card_10_2},
            /* 10_3 */ {R.string.title_data_images_index_card_10, R.string.text_data_images_index_card_10_3},
            /* 10_4 */ {R.string.title_data_images_index_card_10, R.string.text_data_images_index_card_10_4},
            /* 11_2 */ {R.string.title_data_images_index_card_11, R.string.text_data_images_index_card_11_2},
            /* 11_3 */ {R.string.title_data_images_index_card_11, R.string.text_data_images_index_card_11_3},
            /* 11_4 */ {R.string.title_data_images_index_card_11, R.string.text_data_images_index_card_11_4},
            /* 12_1 */ {R.string.title_data_images_index_card_12, R.string.text_data_images_index_card_12_1},
            /* 12_2 */ {R.string.title_data_images_index_card_12, R.string.text_data_images_index_card_12_2},
            /* 12_3 */ {R.string.title_data_images_index_card_12, R.string.text_data_images_index_card_12_3},
            /* 13_1 */ {R.string.title_data_images_index_card_12_, R.string.text_data_images_index_card_12__1},
            /* 13_2 */ {R.string.title_data_images_index_card_12_, R.string.text_data_images_index_card_12__2},
            /* 13_3 */ {R.string.title_data_images_index_card_12_, R.string.text_data_images_index_card_12__3},
            /* 13_4 */ {R.string.title_data_images_index_card_12_, R.string.text_data_images_index_card_12__4},
            /* 14_1 */ {R.string.title_data_images_index_card_13, R.string.text_data_images_index_card_13_1},
            /* 14_2 */ {R.string.title_data_images_index_card_13, R.string.text_data_images_index_card_13_2},
            /* 14_3 */ {R.string.title_data_images_index_card_13, R.string.text_data_images_index_card_13_3},
            /* 14_4 */ {R.string.title_data_images_index_card_13, R.string.text_data_images_index_card_13_4},
            /* 15_1 */ {R.string.title_data_images_index_card_14, R.string.text_data_images_index_card_14_1},
            /* 15_2 */ {R.string.title_data_images_index_card_14, R.string.text_data_images_index_card_14_2},
            /* 16_1 */ {R.string.title_data_images_index_card_15, 0}
    };

    /**
     * 47 个分节在标题导航弹窗中的按钮 id（顺序与 SECTION_PREFIXES 一一对应）。
     * 按钮 id 与分节号同样存在历史错位（13 系=button_card_category_index_12__n 等），
     * 行注释为数据表分节号。
     */
    public static final int[] SECTION_BUTTON_RES_IDS = {
            /* 1_1 */ R.id.button_card_category_index_1_1,
            /* 1_2 */ R.id.button_card_category_index_1_2,
            /* 1_3 */ R.id.button_card_category_index_1_3,
            /* 1_4 */ R.id.button_card_category_index_1_4,
            /* 2_1 */ R.id.button_card_category_index_2_1,
            /* 2_2 */ R.id.button_card_category_index_2_2,
            /* 2_3 */ R.id.button_card_category_index_2_3,
            /* 3_1 */ R.id.button_card_category_index_3_1,
            /* 3_2 */ R.id.button_card_category_index_3_2,
            /* 3_3 */ R.id.button_card_category_index_3_3,
            /* 4_1 */ R.id.button_card_category_index_4_1,
            /* 4_2 */ R.id.button_card_category_index_4_2,
            /* 4_3 */ R.id.button_card_category_index_4_3,
            /* 5_1 */ R.id.button_card_category_index_5_1,
            /* 5_2 */ R.id.button_card_category_index_5_2,
            /* 6_1 */ R.id.button_card_category_index_6_1,
            /* 6_2 */ R.id.button_card_category_index_6_2,
            /* 7_1 */ R.id.button_card_category_index_7_1,
            /* 7_2 */ R.id.button_card_category_index_7_2,
            /* 8_1 */ R.id.button_card_category_index_8_1,
            /* 8_2 */ R.id.button_card_category_index_8_2,
            /* 9_1 */ R.id.button_card_category_index_9_1,
            /* 9_2 */ R.id.button_card_category_index_9_2,
            /* 9_3 */ R.id.button_card_category_index_9_3,
            /* 9_4 */ R.id.button_card_category_index_9_4,
            /* 9_5 */ R.id.button_card_category_index_9_5,
            /* 10_1 */ R.id.button_card_category_index_10_1,
            /* 10_2 */ R.id.button_card_category_index_10_2,
            /* 10_3 */ R.id.button_card_category_index_10_3,
            /* 10_4 */ R.id.button_card_category_index_10_4,
            /* 11_2 */ R.id.button_card_category_index_11_2,
            /* 11_3 */ R.id.button_card_category_index_11_3,
            /* 11_4 */ R.id.button_card_category_index_11_4,
            /* 12_1 */ R.id.button_card_category_index_12_1,
            /* 12_2 */ R.id.button_card_category_index_12_2,
            /* 12_3 */ R.id.button_card_category_index_12_3,
            /* 13_1 */ R.id.button_card_category_index_12__1,
            /* 13_2 */ R.id.button_card_category_index_12__2,
            /* 13_3 */ R.id.button_card_category_index_12__3,
            /* 13_4 */ R.id.button_card_category_index_12__4,
            /* 14_1 */ R.id.button_card_category_index_13_1,
            /* 14_2 */ R.id.button_card_category_index_13_2,
            /* 14_3 */ R.id.button_card_category_index_13_3,
            /* 14_4 */ R.id.button_card_category_index_13_4,
            /* 15_1 */ R.id.button_card_category_index_14_1,
            /* 15_2 */ R.id.button_card_category_index_14_2,
            /* 16_1 */ R.id.button_card_category_index_15_1
    };

    /**
     * 组装全部 47 个分节的标题文案，顺序与 SECTION_PREFIXES 一一对应。
     * 标题 = 主标题 + " - " + 子标题（无子标题的分节只显示主标题）。
     */
    public static List<String> buildSectionTitles(Context context) {
        List<String> titles = new ArrayList<>(SECTION_TITLE_RES_IDS.length);
        for (int[] resIds : SECTION_TITLE_RES_IDS) {
            String title = context.getString(resIds[0]);
            if (resIds[1] != 0) {
                title += " - " + context.getString(resIds[1]);
            }
            titles.add(title);
        }
        return titles;
    }
}
