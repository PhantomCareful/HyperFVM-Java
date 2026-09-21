package com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.Category;

import com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.IndexItem.IndexArray;

public class CategoryArray {
    public static final String[] titleArray = {
            "普通卡/纪念卡转职宝箱",
            "星座卡相关宝箱",
            "生肖卡相关宝箱",
            "武器相关宝箱",
            "日氪相关宝箱",
            "高终级古书箱",
            "其他",
    };

    /**
     * 概率公示信息目录，每个类别的标题+类别对应的目录成员
     * 一定要后于categoryTitleArray定义
     */
    public static CategoryItem[] array = {
            new CategoryItem(titleArray[0], IndexArray.array0),
            new CategoryItem(titleArray[1], IndexArray.array1),
    };
}
