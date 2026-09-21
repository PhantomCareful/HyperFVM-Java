package com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.IndexItem;

import com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.OddsDisclosure.OddsItem;

/**
 * 概率公示信息目录数组成员
 * 每个成员应包含如下信息
 * 1.物品名字
 * 2.物品图片资源ID
 * 3.对应的topBar名字
 * 4.描述（可以是箱子本身的描述，也可以是特别说明）
 * 5.对应的概率公示数组
 */
public class IndexItem {
    private final String itemName;
    private final int itemImageId;
    private final String topBarName;
    private final String description;
    private final OddsItem[] oddsArray;

    public IndexItem(String itemName, int itemImageId, String topBarName, String description, OddsItem[] oddsArray) {
        this.itemName = itemName;
        this.itemImageId = itemImageId;
        this.topBarName = topBarName;
        this.description = description;
        this.oddsArray = oddsArray;
    }

    public String getItemName() {
        return itemName;
    }

    public int getItemImageId() {
        return itemImageId;
    }

    public String getTopBarName() {
        return topBarName;
    }

    public String getDescription() {
        return description;
    }

    public OddsItem[] getOddsArray() {
        return oddsArray;
    }
}
