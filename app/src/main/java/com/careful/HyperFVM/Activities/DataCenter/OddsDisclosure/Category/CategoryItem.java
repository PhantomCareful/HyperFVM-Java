package com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.Category;

import com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.IndexItem.IndexItem;

/**
 * 概率公示信息目录，每个类别的标题+类别成员
 */
public class CategoryItem {
    private final String itemTitle;
    private final IndexItem[] indexArray;

    public CategoryItem(String itemTitle, IndexItem[] indexArray) {
        this.itemTitle = itemTitle;
        this.indexArray = indexArray;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public IndexItem[] getIndexArray() {
        return indexArray;
    }
}
