package com.careful.HyperFVM.utils.ForCardData;

// 搜索建议项数据模型：包含卡片名称和对应图片ID（image_id 直接对应单卡布局）
public class CardSearchSuggestion {
    private final String name;
    private final String imageId;

    public CardSearchSuggestion(String name, String imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    // Getter
    public String getName() {
        return name;
    }

    public String getImageId() {
        return imageId;
    }
}