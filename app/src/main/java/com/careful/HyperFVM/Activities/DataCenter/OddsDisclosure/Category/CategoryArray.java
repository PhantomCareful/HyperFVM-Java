package com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.Category;

import com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.IndexItem.IndexArray;

public class CategoryArray {
    public static final String[] titleArray = {
            "普通卡/纪念卡转职宝箱",
            "星座卡相关宝箱",
            "生肖卡相关宝箱",
            "金卡相关宝箱",
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
            new CategoryItem(titleArray[2], IndexArray.array2),
            new CategoryItem(titleArray[3], IndexArray.array3),
            new CategoryItem(titleArray[4], IndexArray.array4),
            new CategoryItem(titleArray[5], IndexArray.array5),
            new CategoryItem(titleArray[6], IndexArray.array6),
            new CategoryItem(titleArray[7], IndexArray.array7),
    };
}
