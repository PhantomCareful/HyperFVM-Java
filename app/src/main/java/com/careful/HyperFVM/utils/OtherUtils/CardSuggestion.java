package com.careful.HyperFVM.utils.OtherUtils;

// 搜索建议项数据模型：包含卡片名称和对应图片ID
public class CardSuggestion {
    private final String name;
    private final String transferCategory;
    private final String imageId;

    public CardSuggestion(String name, String transferCategory, String imageId) {
        this.name = name;
        this.transferCategory = transferCategory;
        this.imageId = imageId;
    }

    // Getter
    public String getName() {
        return name;
    }

    public String getTransferCategory() {
        return transferCategory;
    }

    public String getImageId() {
        return imageId;
    }
}