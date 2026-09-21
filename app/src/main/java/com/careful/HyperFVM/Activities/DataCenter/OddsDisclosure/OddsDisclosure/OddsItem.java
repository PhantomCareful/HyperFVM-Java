package com.careful.HyperFVM.Activities.DataCenter.OddsDisclosure.OddsDisclosure;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/**
 * 概率公示信息数组成员
 * 每个成员应包含如下信息
 * 1.物品名字
 * 2.物品数量
 * 3.物品图片资源ID
 * 4.是否为期望得到的物品
 * 5.概率
 */
public class OddsItem implements Parcelable {
    // 概率文本（百分数形式）最多精确到的小数位数
    private static final int MAX_ODDS_TEXT_PRECISION = 3;

    // 判断小数是否已精确到整数所使用的浮点误差容差
    private static final double ODDS_PRECISION_EPSILON = 1e-6;

    private final String itemName;
    private final int itemCount;
    private final int itemImageId;
    private final boolean isExpected;
    private final double odds;

    public OddsItem(String itemName, int itemCount, int itemImageId, boolean isExpected, double odds) {
        this.itemName = itemName;
        this.itemCount = itemCount;
        this.itemImageId = itemImageId;
        this.isExpected = isExpected;
        this.odds = odds;
    }

    public String getItemNameAndCount() {
        return itemName + "×" + itemCount;
    }

    public int getItemImageId() {
        return itemImageId;
    }

    public boolean isExpected() {
        return isExpected;
    }

    /**
     * 计算整个数组中概率文本统一采用的小数位数
     * 取数组内各项概率所需位数的最大值（上限3位），保证同一数组中所有概率显示精度一致
     */
    public static int calculateOddsTextPrecision(OddsItem[] oddsItemInfos) {
        int maxPrecision = 1;
        for (OddsItem itemInfo : oddsItemInfos) {
            maxPrecision = Math.max(maxPrecision, itemInfo.calculateOddsPrecision());
        }
        return Math.min(maxPrecision, MAX_ODDS_TEXT_PRECISION);
    }

    /**
     * 计算该概率在百分数形式下无损显示所需的小数位数（最大3位）
     * 原理：概率乘以100后逐次乘10放大，能变为整数的最小放大次数即所需位数
     */
    private int calculateOddsPrecision() {
        double scaledOdds = odds * 100;
        for (int precision = 1; precision <= MAX_ODDS_TEXT_PRECISION; precision++) {
            scaledOdds *= 10;
            if (Math.abs(scaledOdds - Math.round(scaledOdds)) < ODDS_PRECISION_EPSILON) {
                return precision;
            }
        }
        return MAX_ODDS_TEXT_PRECISION;
    }

    /**
     * 获取百分数格式的概率文本，根据统一小数位数自动选择对应的格式化方法
     * precision 为小数位数（1~3），如3位时 0.030 -> "3.000%"
     */
    public String getOddsText(int precision) {
        return switch (precision) {
            case 2 -> getOddsText2();
            case 3 -> getOddsText3();
            default -> getOddsText1();
        };
    }

    public String getOddsText1() {
        return String.format(Locale.CHINA, "%.1f%%", odds * 100);
    }

    public String getOddsText2() {
        return String.format(Locale.CHINA, "%.2f%%", odds * 100);
    }

    public String getOddsText3() {
        return String.format(Locale.CHINA, "%.3f%%", odds * 100);
    }

    // ==============================以下为Activity之间传递数据用的==============================

    protected OddsItem(Parcel in) {
        itemName = in.readString();
        itemCount = in.readInt();
        itemImageId = in.readInt();
        isExpected = in.readByte() != 0;
        odds = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemName);
        dest.writeInt(itemCount);
        dest.writeInt(itemImageId);
        dest.writeByte((byte) (isExpected ? 1 : 0));
        dest.writeDouble(odds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OddsItem> CREATOR = new Creator<>() {
        @Override
        public OddsItem createFromParcel(Parcel in) {
            return new OddsItem(in);
        }

        @Override
        public OddsItem[] newArray(int size) {
            return new OddsItem[size];
        }
    };
}
