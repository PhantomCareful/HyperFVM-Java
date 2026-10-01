package com.careful.HyperFVM.utils.ForCardData;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库改造升级（图片 token -> card_desc id）遗留静态表。
 * 原由 tools/migrate_rename.py 一次性生成（脚本已完成使命并删除），
 * TAIL_LEVEL_EXCEPTIONS 仍被转职等级推断（inferTransferLevel）使用，请勿手改。
 */
public final class CardIndexIdTables {
    private CardIndexIdTables() {}

    /** 末位主规则例外表：新图片 id -> 形态 level */
    public static final Map<String, Integer> TAIL_LEVEL_EXCEPTIONS = new HashMap<>();
    static {
        TAIL_LEVEL_EXCEPTIONS.put("x11110014", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11110054", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11110064", 1);
        TAIL_LEVEL_EXCEPTIONS.put("x11110354", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11111024", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11111034", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11111324", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11120034", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11120044", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11120054", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11120064", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11120074", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11120084", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11120234", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11120334", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11130014", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11130064", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11130074", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11130094", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x111300a4", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x111300c4", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x111300d4", 1);
        TAIL_LEVEL_EXCEPTIONS.put("x111300e4", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x111300f1", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11130104", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11130114", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11130124", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11130164", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11130234", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x111302a4", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11130304", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11130364", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x111303a4", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x111303c4", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x111303e4", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11131034", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11140014", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11140034", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x111400f4", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11150014", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11150024", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11150034", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11150044", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11150054", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11190141", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11190154", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11190164", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11190171", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11190254", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11210024", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11220024", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11230024", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11240014", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11370015", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11370025", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11370034", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11370044", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x113903d4", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x113e0025", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11430034", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11431014", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11930074", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11930084", 0);
        TAIL_LEVEL_EXCEPTIONS.put("x11930374", 0);
    }

    /**
     * 形态 level 推断（替代旧 Character.getNumericValue(图片 id 末位)）。
     * 返回 0 不转 / 1 一转(初级融合) / 2 二转(深度融合) / 3 终转(灵魂融合)，
     * 具体语义由调用方结合表名 switch 解释；未知返回 -1（等价旧行为不命中）。
     */
    public static int inferTransferLevel(String imageId, String tableName) {
        if (imageId == null || imageId.isEmpty() || tableName == null || tableName.isEmpty()) {
            return -1;
        }
        Integer byId = TAIL_LEVEL_EXCEPTIONS.get(imageId);
        if (byId != null) {
            return byId;
        }
        char tail = imageId.charAt(imageId.length() - 1);
        return switch (tableName.charAt(tableName.length() - 1)) {
            case '1', '4' -> tail == '0' ? 0 : tail == 'e' ? 1 : tail == 'f' ? 2 : -1;
            case '2' -> tail == '0' ? 1 : tail == 'e' ? 2 : tail == 'f' ? 3 : -1;
            case '3' -> tail == 'a' ? 0 : tail == 'b' ? 1 : tail == 'c' ? 2 : tail == 'd' ? 3 : -1;
            default -> -1;
        };
    }
}
