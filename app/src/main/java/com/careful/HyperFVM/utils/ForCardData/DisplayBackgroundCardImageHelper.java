package com.careful.HyperFVM.utils.ForCardData;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DisplayBackgroundCardImageHelper {
    private static final String[][] CardImageFileNameArray = {
            {"card_data_index_1_1_2_2_big", "天秤座精灵", "card_data_4"},
            {"card_data_index_1_1_3_2_big", "呆呆鸡", "card_data_4"},
            {"card_data_index_1_1_4_2_big", "阿瑞斯神使", "card_data_3"},
            {"card_data_index_1_1_4_3_big", "阿瑞斯神使", "card_data_3"},
            {"card_data_index_1_1_5_2_big", "二哈汪", "card_data_4"},
            {"card_data_index_1_1_6_2_big", "双枪喵", "card_data_4"},
            {"card_data_index_1_1_7_2_big", "散弹牛", "card_data_4"},
            {"card_data_index_1_1_8_2_big", "威风虎", "card_data_4"},

            {"card_data_index_1_2_2_2_big", "射手座精灵", "card_data_4"},
            {"card_data_index_1_2_3_2_big", "砰砰鸡", "card_data_4"},
            {"card_data_index_1_2_4_2_big", "丘比特神使", "card_data_3"},
            {"card_data_index_1_2_4_3_big", "丘比特神使", "card_data_3"},
            {"card_data_index_1_2_5_2_big", "狩猎汪", "card_data_4"},
            {"card_data_index_1_2_6_2_big", "猪猪猎手", "card_data_4"},

            {"card_data_index_1_3_1_2_big", "枪塔喵", "card_data_4"},
            {"card_data_index_1_3_2_2_big", "弩箭牛", "card_data_4"},

            {"card_data_index_2_1_1_2_big", "勺勺兔", "card_data_4"},
            {"card_data_index_2_1_2_2_big", "窃蛋龙", "card_data_4"},
            {"card_data_index_2_1_3_2_big", "尤弥尔神使", "card_data_3"},
            {"card_data_index_2_1_3_3_big", "尤弥尔神使", "card_data_3"},
            {"card_data_index_2_1_4_2_big", "幻影蛇", "card_data_4"},
            {"card_data_index_2_1_6_2_big", "金乌马", "card_data_4"},

            {"card_data_index_2_2_3_2_big", "双鱼座精灵", "card_data_4"},
            {"card_data_index_2_2_4_2_big", "弹弹鸡", "card_data_4"},
            {"card_data_index_2_2_5_2_big", "索尔神使", "card_data_3"},
            {"card_data_index_2_2_5_3_big", "索尔神使", "card_data_3"},
            {"card_data_index_2_2_6_2_big", "机械汪", "card_data_4"},
            {"card_data_index_2_2_7_2_big", "投弹猪", "card_data_4"},
            {"card_data_index_2_2_9_2_big", "飞鱼喵", "card_data_4"},
            {"card_data_index_2_2_10_2_big", "壮壮牛", "card_data_4"},
            {"card_data_index_2_2_12_2_big", "投篮虎", "card_data_4"},

            {"card_data_index_3_1_1_2_big", "炭烧海星", "card_data_1"},
            {"card_data_index_3_1_2_2_big", "猪猪料理机", "card_data_4"},
            {"card_data_index_3_1_3_2_big", "陀螺喵", "card_data_4"},
            {"card_data_index_3_1_4_2_big", "哈迪斯神使", "card_data_3"},
            {"card_data_index_3_1_4_3_big", "哈迪斯神使", "card_data_3"},
            {"card_data_index_3_1_5_2_big", "查克拉兔", "card_data_4"},

            {"card_data_index_3_2_1_2_big", "厨师虎", "card_data_4"},
            {"card_data_index_3_2_2_2_big", "星星兔", "card_data_4"},
            {"card_data_index_3_2_4_2_big", "里格神使", "card_data_3"},
            {"card_data_index_3_2_4_3_big", "里格神使", "card_data_3"},
            {"card_data_index_3_2_6_2_big", "烟花虎", "card_data_4"},
            {"card_data_index_3_2_7_2_big", "风车龙", "card_data_4"},

            {"card_data_index_3_3_2_2_big", "便便汪", "card_data_4"},
            {"card_data_index_3_3_4_2_big", "饼干汪", "card_data_4"},
            {"card_data_index_3_3_6_1_big", "盾盾汪", "card_data_4"},

            {"card_data_index_4_1_2_2_big", "金牛座精灵", "card_data_4"},
            {"card_data_index_4_1_3_2_big", "洛基神使", "card_data_3"},
            {"card_data_index_4_1_3_3_big", "洛基神使", "card_data_3"},
            {"card_data_index_4_1_4_2_big", "暖炉汪", "card_data_4"},
            {"card_data_index_4_1_5_2_big", "能量喵", "card_data_4"},
            {"card_data_index_4_1_6_2_big", "坩埚蛇", "card_data_4"},
            {"card_data_index_4_1_7_2_big", "猪猪加强器", "card_data_4"},
            {"card_data_index_4_1_10_2_big", "欧若拉神使", "card_data_3"},
            {"card_data_index_4_1_10_3_big", "欧若拉神使", "card_data_3"},

            {"card_data_index_4_2_2_2_big", "香料虎", "card_data_4"},
            {"card_data_index_4_2_3_2_big", "塔利亚神使", "card_data_3"},
            {"card_data_index_4_2_3_3_big", "塔利亚神使", "card_data_3"},
            {"card_data_index_4_2_4_2_big", "精灵龙", "card_data_4"},
            {"card_data_index_4_2_7_2_big", "五行蛇", "card_data_4"},
            {"card_data_index_4_2_8_2_big", "弗雷神使", "card_data_3"},
            {"card_data_index_4_2_8_3_big", "弗雷神使", "card_data_3"},
            {"card_data_index_4_2_10_2_big", "魔杖蛇", "card_data_4"},
            {"card_data_index_4_2_11_2_big", "塔拉萨神使", "card_data_3"},
            {"card_data_index_4_2_11_3_big", "塔拉萨神使", "card_data_3"},
            {"card_data_index_4_2_12_2_big", "炎焱兔", "card_data_4"},

            {"card_data_index_4_3_2_2_big", "战旗马", "card_data_4"},

            {"card_data_index_5_1_4_2_big", "双子座精灵", "card_data_4"},
            {"card_data_index_5_1_5_2_big", "咕咕鸡", "card_data_4"},
            {"card_data_index_5_1_6_2_big", "暖暖鸡", "card_data_4"},
            {"card_data_index_5_1_7_2_big", "阿波罗神使", "card_data_3"},
            {"card_data_index_5_1_7_3_big", "阿波罗神使", "card_data_3"},
            {"card_data_index_5_1_9_2_big", "火焰牛", "card_data_4"},
            {"card_data_index_5_1_10_2_big", "花火龙", "card_data_4"},

            {"card_data_index_5_2_1_2_big", "钱罐猪", "card_data_4"},
            {"card_data_index_5_2_2_2_big", "罐罐牛", "card_data_4"},
            {"card_data_index_5_2_3_2_big", "烈火虎", "card_data_4"},

            {"card_data_index_6_1_3_2_big", "布丁汪", "card_data_4"},
            {"card_data_index_6_1_4_2_big", "凉粉牛", "card_data_4"},
            {"card_data_index_6_1_5_2_big", "忒提丝神使", "card_data_3"},
            {"card_data_index_6_1_5_3_big", "忒提丝神使", "card_data_3"},

            {"card_data_index_6_2_2_2_big", "盘盘鸡", "card_data_4"},
            {"card_data_index_6_2_3_2_big", "猫猫盘", "card_data_4"},

            {"card_data_index_7_1_2_2_big", "跳跳鸡", "card_data_4"},
            {"card_data_index_7_1_3_2_big", "防空喵", "card_data_4"},
            {"card_data_index_7_1_4_2_big", "赫丘利神使", "card_data_3"},
            {"card_data_index_7_1_4_3_big", "赫丘利神使", "card_data_3"},

            {"card_data_index_7_2_3_2_big", "弹簧虎", "card_data_4"},
            {"card_data_index_7_2_4_2_big", "泡泡龙", "card_data_4"},
            {"card_data_index_7_2_7_2_big", "埃罗斯神使", "card_data_3"},
            {"card_data_index_7_2_7_3_big", "埃罗斯神使", "card_data_3"},
            {"card_data_index_7_2_9_2_big", "奶茶猪", "card_data_4"},
            {"card_data_index_7_2_10_2_big", "科技喵", "card_data_4"},

            {"card_data_index_8_1_3_2_big", "烈焰龙", "card_data_4"},
            {"card_data_index_8_1_4_2_big", "赫斯提亚神使", "card_data_3"},
            {"card_data_index_8_1_4_3_big", "赫斯提亚神使", "card_data_3"},

            {"card_data_index_8_2_2_2_big", "狮子座精灵", "card_data_4"},
            {"card_data_index_8_2_3_2_big", "波塞冬神使", "card_data_3"},
            {"card_data_index_8_2_3_3_big", "波塞冬神使", "card_data_3"},
            {"card_data_index_8_2_4_2_big", "转转鸡", "card_data_4"},
            {"card_data_index_8_2_5_2_big", "可乐汪", "card_data_4"},
            {"card_data_index_8_2_6_2_big", "元气牛", "card_data_4"},
            {"card_data_index_8_2_7_2_big", "巫蛊蛇", "card_data_4"},

            {"card_data_index_9_1_2_2_big", "巨蟹座精灵", "card_data_4"},
            {"card_data_index_9_1_3_2_big", "忍忍鸡", "card_data_4"},
            {"card_data_index_9_1_4_2_big", "狄安娜神使", "card_data_3"},
            {"card_data_index_9_1_4_3_big", "狄安娜神使", "card_data_3"},
            {"card_data_index_9_1_5_2_big", "飞盘汪", "card_data_4"},
            {"card_data_index_9_1_6_2_big", "铁甲飞镖猪", "card_data_4"},
            {"card_data_index_9_1_7_2_big", "海盗兔", "card_data_4"},

            {"card_data_index_9_2_2_2_big", "雅典娜守护", "card_data_4"},
            {"card_data_index_9_2_3_2_big", "火箭猪", "card_data_4"},
            {"card_data_index_9_2_4_2_big", "宙斯神使", "card_data_3"},
            {"card_data_index_9_2_4_3_big", "宙斯神使", "card_data_3"},

            {"card_data_index_9_3_1_2_big", "魔法猪", "card_data_4"},
            {"card_data_index_9_3_2_2_big", "招财喵", "card_data_4"},
            {"card_data_index_9_3_3_2_big", "雪球兔", "card_data_4"},
            {"card_data_index_9_3_4_2_big", "典伊神使", "card_data_3"},
            {"card_data_index_9_3_4_3_big", "典伊神使", "card_data_3"},
            {"card_data_index_9_3_5_2_big", "冰晶龙", "card_data_4"},

            {"card_data_index_9_4_2_2_big", "风力空投猪", "card_data_4"},
            {"card_data_index_9_4_3_2_big", "电流虎", "card_data_4"},
            {"card_data_index_9_4_4_2_big", "霹雳马", "card_data_4"},
            {"card_data_index_9_4_5_2_big", "金刚马", "card_data_4"},
            {"card_data_index_9_4_6_2_big", "归元马", "card_data_4"},
            {"card_data_index_9_4_8_2_big", "萌海马", "card_data_4"},

            {"card_data_index_10_1_4_2_big", "铛铛虎", "card_data_4"},
            {"card_data_index_10_1_5_2_big", "祝融神使", "card_data_3"},
            {"card_data_index_10_1_5_3_big", "祝融神使", "card_data_3"},
            {"card_data_index_10_1_7_2_big", "霜霜蛇", "card_data_4"},

            {"card_data_index_10_2_3_2_big", "吞噬龙", "card_data_4"},
            {"card_data_index_10_2_5_2_big", "混沌神使", "card_data_3"},

            {"card_data_index_10_3_3_2_big", "功夫汪", "card_data_4"},

            {"card_data_index_10_4_2_2_big", "糖人马", "card_data_4"},

            {"card_data_index_11_1_13_2_big", "包包龙", "card_data_4"},
            {"card_data_index_11_1_16_2_big", "激光汪", "card_data_4"},

            {"card_data_index_11_2_1_2_big", "阴阳蛇", "card_data_4"},
            {"card_data_index_11_2_2_2_big", "焚寂马", "card_data_4"},

            {"card_data_index_11_3_1_1_big", "天蝎座精灵", "card_data_4"},
            {"card_data_index_11_3_2_2_big", "工程猪", "card_data_4"},
            {"card_data_index_11_3_3_2_big", "双刃蛇", "card_data_4"},
            {"card_data_index_11_3_4_2_big", "元素蛇", "card_data_4"},
            {"card_data_index_11_3_5_2_big", "御风马", "card_data_4"},
            {"card_data_index_11_3_6_2_big", "回旋虎", "card_data_4"},
            {"card_data_index_11_3_7_2_big", "大师兔", "card_data_4"},
            {"card_data_index_11_3_9_2_big", "赖皮蛇", "card_data_4"},
            {"card_data_index_11_3_11_2_big", "鲁班神使", "card_data_3"},
            {"card_data_index_11_3_12_2_big", "炎凰马", "card_data_4"},

            {"card_data_index_11_4_2_2_big", "喷壶汪", "card_data_4"},
            {"card_data_index_11_4_3_2_big", "派派鸡", "card_data_4"},
            {"card_data_index_11_4_4_2_big", "小猪米花机", "card_data_4"},
            {"card_data_index_11_4_5_2_big", "喷气牛", "card_data_4"},
            {"card_data_index_11_4_6_2_big", "卖萌喵", "card_data_4"},
            {"card_data_index_11_4_7_2_big", "奥丁神使", "card_data_3"},
            {"card_data_index_11_4_7_3_big", "奥丁神使", "card_data_3"},
            {"card_data_index_11_4_8_2_big", "法师蛇", "card_data_4"},
            {"card_data_index_11_4_10_2_big", "后羿神使", "card_data_3"},
            {"card_data_index_11_4_10_3_big", "后羿神使", "card_data_3"},

            {"card_data_index_12_1_2_2_big", "三指兔", "card_data_4"},
            {"card_data_index_12_1_3_2_big", "结界马", "card_data_4"},
            {"card_data_index_12_1_5_2_big", "导弹蛇", "card_data_4"},
            {"card_data_index_12_1_6_2_big", "盖亚神使", "card_data_3"},
            {"card_data_index_12_1_6_3_big", "盖亚神使", "card_data_3"},

            {"card_data_index_12_2_5_2_big", "潘多拉", "card_data_3"},
            {"card_data_index_12_2_11_2_big", "水瓶座精灵", "card_data_4"},
            {"card_data_index_12_2_12_2_big", "雷暴猪", "card_data_4"},
            {"card_data_index_12_2_15_2_big", "爆裂蛇", "card_data_4"},
            {"card_data_index_12_2_17_2_big", "烛阴龙", "card_data_4"},
            {"card_data_index_12_2_21_2_big", "娇娇虎", "card_data_4"},

            {"card_data_index_12_3_3_2_big", "爆炸汪", "card_data_4"},
            {"card_data_index_12_3_6_2_big", "芥末牛", "card_data_4"},

            {"card_data_index_13_1_7_0_big", "白羊座精灵", "card_data_4"},
            {"card_data_index_13_1_8_2_big", "酋长汪", "card_data_4"},
            {"card_data_index_13_1_9_2_big", "逗猫棒", "card_data_4"},
            {"card_data_index_13_1_11_2_big", "贪吃兔", "card_data_4"},
            {"card_data_index_13_1_12_2_big", "灵鱼摩蹉神使", "card_data_3"},
            {"card_data_index_13_1_12_3_big", "灵鱼摩蹉神使", "card_data_3"},

            {"card_data_index_13_2_3_2_big", "镭射喵", "card_data_4"},
            {"card_data_index_13_2_4_2_big", "黑暗神使", "card_data_3"},
            {"card_data_index_13_2_4_3_big", "黑暗神使", "card_data_3"},
            {"card_data_index_13_2_6_0_big", "摩羯座精灵", "card_data_4"},
            {"card_data_index_13_2_7_2_big", "龙珠果", "card_data_4"},
            {"card_data_index_13_2_8_2_big", "巴德尔神使", "card_data_3"},
            {"card_data_index_13_2_8_3_big", "巴德尔神使", "card_data_3"},

            {"card_data_index_13_3_2_2_big", "冰弹喵", "card_data_4"},
            {"card_data_index_13_3_5_2_big", "逆转牛", "card_data_4"},

            {"card_data_index_13_4_1_2_big", "蛋蛋兔", "card_data_4"},

            {"card_data_index_14_1_2_2_big", "转龙壶", "card_data_4"},
            {"card_data_index_14_1_4_2_big", "柯罗诺斯神使", "card_data_3"},
            {"card_data_index_14_1_4_3_big", "柯罗诺斯神使", "card_data_3"},
            {"card_data_index_14_1_5_2_big", "幻幻鸡", "card_data_4"},
            {"card_data_index_14_1_6_2_big", "百变蛇", "card_data_4"},
            {"card_data_index_14_1_7_2_big", "梵天神使", "card_data_3"},
            {"card_data_index_14_1_7_3_big", "梵天神使", "card_data_3"},
            {"card_data_index_14_1_8_2_big", "顽皮龙", "card_data_4"},
            {"card_data_index_14_1_10_2_big", "天使猪", "card_data_4"},
            {"card_data_index_14_1_12_2_big", "星穹马", "card_data_4"},
            {"card_data_index_14_1_14_2_big", "蛇蛇酒", "card_data_4"},
            {"card_data_index_14_1_15_2_big", "克洛托神使", "card_data_3"},
            {"card_data_index_14_1_15_3_big", "克洛托神使", "card_data_3"},

            {"card_data_index_14_2_5_2_big", "萤火蛇", "card_data_4"},
            {"card_data_index_14_2_9_2_big", "爆爆鸡", "card_data_4"},
            {"card_data_index_14_2_10_2_big", "清障猪", "card_data_4"},
            {"card_data_index_14_2_11_2_big", "旋风牛", "card_data_4"},
            {"card_data_index_14_2_15_2_big", "碎冰喵", "card_data_4"},

            {"card_data_index_14_3_3_2_big", "治愈喵", "card_data_4"},
            {"card_data_index_14_3_8_2_big", "傀儡马", "card_data_4"},

            {"card_data_index_14_4_5_2_big", "大福虎", "card_data_4"},

            {"card_data_index_15_2_2_2_big", "处女座精灵", "card_data_4"},
            {"card_data_index_15_2_3_2_big", "赫拉神使", "card_data_3"},
            {"card_data_index_15_2_3_3_big", "赫拉神使", "card_data_3"},
            {"card_data_index_15_2_4_2_big", "祥龙环", "card_data_4"},
            {"card_data_index_15_2_5_2_big", "守能汪", "card_data_4"},
            {"card_data_index_15_2_7_2_big", "喵喵炉", "card_data_4"},
            {"card_data_index_15_2_9_2_big", "彩虹蛇", "card_data_4"},

            {"card_data_index_16_1_1_3_big", "火炉菠萝面包", "card_data_2"},
            {"card_data_index_16_1_2_3_big", "雪芭煮蛋器", "card_data_2"},
            {"card_data_index_16_1_3_3_big", "火影怪味鱿鱼", "card_data_2"},
            {"card_data_index_16_1_4_3_big", "酱香锅烤栗子", "card_data_2"},
            {"card_data_index_16_1_5_3_big", "热狗耗油双菇", "card_data_2"},
            {"card_data_index_16_1_6_3_big", "子母三线酒架", "card_data_2"},
            {"card_data_index_16_1_7_3_big", "刺梨烧烤盘", "card_data_2"},
            {"card_data_index_16_1_8_3_big", "机枪咖啡杯", "card_data_2"},
            {"card_data_index_16_1_9_3_big", "葡萄味软糖", "card_data_2"},
    };

    /**
     * 随机选取若干个防御卡大图信息
     * @param fileNum 需要的图片数量
     * @return 选取的图片信息数组
     */
    public static String[][] giveRandomCardImageFileInfoArray(int fileNum) {
        Random random = new Random();
        int totalNum = CardImageFileNameArray.length;

        // 使用Set保证选取的图片是不重复的
        Set<Integer> pickedIndexes = new HashSet<>();
        while (pickedIndexes.size() < fileNum) {
            int index = random.nextInt(totalNum);
            pickedIndexes.add(index);
        }

        // 转换成索引数组
        Integer[] selectedIndexes = pickedIndexes.toArray(new Integer[0]);

        // 通过索引取得对应的文件名
        String[][] cardImageFileNameArray = new String[fileNum][3];
        for (int i = 0; i < fileNum; i++) {
            cardImageFileNameArray[i] = CardImageFileNameArray[selectedIndexes[i]];
        }

        return cardImageFileNameArray;
    }
}
