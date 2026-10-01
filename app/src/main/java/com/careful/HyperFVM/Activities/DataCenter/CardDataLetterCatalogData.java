package com.careful.HyperFVM.Activities.DataCenter;

import android.util.Log;

import com.github.promeg.pinyinhelper.Pinyin;
import com.github.promeg.pinyinhelper.PinyinMapDict;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 防御卡目录页的字母分节数据层。
 * <p>
 * 把 {@link CardDataCatalogData} 的 47 个章节拍平成单卡条目后，按卡名的<b>完整拼音</b>
 * 以汉语字典序排序（先比首字母、再逐字比后续拼音，而非只看首字母），再装入
 * 0 / A-Z / # 分节桶：
 * <ul>
 *   <li>0：数字开头的卡名（如“8周年蛋糕”）；</li>
 *   <li>A-Z：汉字转拼音后按首字母归桶；</li>
 *   <li>#：其余无法归入 A-Z 的卡名（正常数据下应为空桶，为空时整节隐藏）。</li>
 * </ul>
 * <p>
 * 分节标签顺序恒为 0 → A → … → Z → #，空桶不产出分节（导航条也只显示有卡的桶）。
 * 排序使用 {@link Collections#sort}（稳定排序）：拼音完全相同的卡保持数据表原顺序，
 * 即原章节内先后不变。
 * <p>
 * 多音字处理：TinyPinyin 只内置单字读音表（取最常用音），<b>没有</b>内置词组词典；
 * 因此词组级别的读音纠正完全依赖 {@link #PINYIN_OVERRIDES}（按词登记，可精确到卡名整词），
 * 经 {@link #ensurePinyinReady()} 的 PinyinMapDict 挂载后，命中词的读音优先于单字默认音。
 * <p>
 * 去重：同一卡名在数据表中出现多次时只保留最先出现的一张（其布局即
 * prefix + "_" + row 对应的单卡文件）。
 * <p>
 * 结果带缓存：列表页每次进入只构建一次；Pinyin.init 同样只执行一次。
 */
public final class CardDataLetterCatalogData {

    private static final String TAG = "CardDataLetterCatalog";

    /** 数字开头卡片所在分节的标签 */
    public static final String LABEL_DIGIT = "0";
    /** 非字母、非数字开头卡片所在分节的标签 */
    public static final String LABEL_OTHER = "#";

    /**
     * 多音字校正表：每项 {词, 正确拼音（按字分隔，大写、无声调）}。
     * <p>
     * 例：{"重庆", "CHONG|QING"}。
     * TinyPinyin 无内置词组词典，凡是单字默认读音在卡名语境下不正确的多音字，
     * 都必须在此登记（登记整词优先于拆字，卡名片段如“弹弹鸡”可直接以卡名为词）。
     */
    private static final String[][] PINYIN_OVERRIDES = {
            // —— 实测校正（JVM 探针验证，2026-10）——
            // TinyPinyin 默认单字音：弹=DAN、长=ZHANG，以下词在卡名语境下读音不同：
            {"弹弹", "TAN|TAN"},      // 弹弹鸡（tántán）
            {"弹簧", "TAN|HUANG"},    // 弹簧虎（tánhuáng）
            {"反弹", "FAN|TAN"},      // 樱桃反弹布丁（fǎntán）
            {"长棍", "CHANG|GUN"},    // 雷电长棍面包（chánggùn；酋长汪的 ZHANG 默认音本就正确）
            // 弹珠汽水读 dàn zhū（D 桶），默认音 DAN 本就正确，勿登记 {"弹珠","TAN|ZHU"}；
    };

    private static final Object LOCK = new Object();
    private static boolean sPinyinReady = false;
    private static List<Section> sSections;

    private CardDataLetterCatalogData() {
    }

    /** 一张卡的定位信息（拍平后的条目） */
    public static final class CardEntry {
        /** 卡名：列表显示文案，也是点库跳转（CardDataHelper）的查询键 */
        public final String name;
        /** 单卡布局前缀（如 "card_data_index_1_1"） */
        public final String prefix;
        /** 单卡在原分节内的行号（1..N） */
        public final int row;

        CardEntry(String name, String prefix, int row) {
            this.name = name;
            this.prefix = prefix;
            this.row = row;
        }
    }

    /** 一个字母分节（0 / A-Z / #） */
    public static final class Section {
        /** 分节标签 */
        public final String label;
        /** 节内卡片（已按完整拼音字典序排好） */
        public final List<CardEntry> cards;

        Section(String label, List<CardEntry> cards) {
            this.label = label;
            this.cards = Collections.unmodifiableList(cards);
        }
    }

    /**
     * 取全部字母分节（懒加载 + 缓存，主线程首次调用时构建）。
     */
    public static List<Section> getSections() {
        synchronized (LOCK) {
            if (sSections == null) {
                sSections = build();
            }
            return sSections;
        }
    }

    /** 拍平 + 去重 + 拼音字典序排序 + 分桶 */
    private static List<Section> build() {
        ensurePinyinReady();

        // 1) 拍平：按数据表章节顺序展开，重复卡名只保留最先出现的一张
        List<CardEntry> entries = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < CardDataCatalogData.SECTION_PREFIXES.length; i++) {
            String prefix = CardDataCatalogData.SECTION_PREFIXES[i];
            String[] names = CardDataCatalogData.SECTION_NAMES[i];
            for (int j = 0; j < names.length; j++) {
                if (!seen.add(names[j])) {
                    Log.d(TAG, "重复卡名去重：" + names[j] + "（保留 " + prefix + "_" + (j + 1) + "）");
                    continue;
                }
                entries.add(new CardEntry(names[j], prefix, j + 1));
            }
        }

        // 2) 完整拼音字典序排序：先算好拼音（避免比较器内重复转换），稳定排序保持同音卡的原顺序
        List<SortableEntry> sortable = new ArrayList<>(entries.size());
        for (CardEntry entry : entries) {
            sortable.add(new SortableEntry(entry, pinyinOf(entry.name)));
        }
        sortable.sort(Comparator.comparing(a -> a.pinyin));

        // 3) 按首字符分桶
        Map<String, List<CardEntry>> buckets = new LinkedHashMap<>();
        for (SortableEntry item : sortable) {
            String label = labelOf(item.pinyin);
            List<CardEntry> bucket = buckets.computeIfAbsent(label, k -> new ArrayList<>());
            bucket.add(item.entry);
        }

        // 4) 按 0 → A-Z → # 组装分节（空桶隐藏）
        List<Section> sections = new ArrayList<>();
        appendSection(sections, buckets, LABEL_DIGIT);
        for (char c = 'A'; c <= 'Z'; c++) {
            appendSection(sections, buckets, String.valueOf(c));
        }
        appendSection(sections, buckets, LABEL_OTHER);
        return Collections.unmodifiableList(sections);
    }

    private static void appendSection(List<Section> sections,
                                      Map<String, List<CardEntry>> buckets, String label) {
        List<CardEntry> bucket = buckets.get(label);
        if (bucket != null && !bucket.isEmpty()) {
            sections.add(new Section(label, bucket));
        }
    }

    /** 卡名 → 排序用拼音串（大写、无声调、无分隔；非中文字符原样保留） */
    private static String pinyinOf(String name) {
        // toPinyin(str, separator) 按字输出拼音并以 separator 分隔；分隔符传空串即得完整拼音串
        return Pinyin.toPinyin(name, "").toUpperCase(Locale.ROOT);
    }

    /** 由拼音串首字符判定分节标签 */
    private static String labelOf(String pinyin) {
        if (pinyin.isEmpty()) {
            return LABEL_OTHER;
        }
        char first = pinyin.charAt(0);
        if (first >= '0' && first <= '9') {
            return LABEL_DIGIT;
        }
        if (first >= 'A' && first <= 'Z') {
            return String.valueOf(first);
        }
        return LABEL_OTHER;
    }

    /** 初始化 TinyPinyin 并挂载多音字校正表（只执行一次） */
    private static void ensurePinyinReady() {
        if (sPinyinReady) {
            return;
        }
        Map<String, String[]> overrides = new HashMap<>();
        for (String[] rule : PINYIN_OVERRIDES) {
            if (rule.length < 2) {
                continue;
            }
            overrides.put(rule[0], rule[1].split("\\|"));
        }
        Pinyin.init(Pinyin.newConfig().with(new PinyinMapDict() {
            @Override
            public Map<String, String[]> mapping() {
                return overrides;
            }
        }));
        sPinyinReady = true;
    }

    /** 排序用中间结构：条目 + 预先算好的拼音串 */
    private static final class SortableEntry {
        final CardEntry entry;
        final String pinyin;

        SortableEntry(CardEntry entry, String pinyin) {
            this.entry = entry;
            this.pinyin = pinyin;
        }
    }
}
