package com.careful.HyperFVM.utils.ForDashboard.FromGame.SweetIsland;

import android.util.Log;

import com.careful.HyperFVM.utils.ForDashboard.XMLHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 甜蜜岛/走马观花场次数据解析器
 * 数据源sweetisland.xml：
 * 1. 甜蜜岛：<systemOpen>包裹的若干行<open>（startTime/endTime时间戳，秒）
 * 2. 走马观花：<tjbz_Open>包裹的若干个<map>，只读取第一个<map>内的<open>
 */
public class SweetIslandCatcher {
    private static final String XML_URL = "https://cdn-qq-ms.123u.com/cdn.qq.123u.com/config/sweetisland.xml";
    private static final String TAG = "SweetIslandCatcher";

    // <open>场次标签的匹配模式（属性内不会出现>，可安全截取整个标签）
    private static final Pattern OPEN_TAG_PATTERN = Pattern.compile("<open\\b[^>]*>");

    // 缓存XML内容，避免重复网络请求
    private String cachedXmlContent;

    /**
     * 异步解析甜蜜岛开放场次内容（<systemOpen>包裹的<open>数据）
     */
    public void catchSweetIslandInfo(OpenTimeInfoCatchResultCallBack callBack) {
        // 网络请求必须在子线程执行，避免阻塞主线程
        new Thread(() -> {
            try {
                // 第1步：XML字符串并缓存
                cachedXmlContent = XMLHelper.getContentFromUrl(XML_URL);
                if (cachedXmlContent == null) {
                    Log.e(TAG, "catchSweetIslandInfo: 内容获取失败，请联系开发者。");
                    callBack.onResult(generateOpenTimeMap("获取失败", "❌", "出错了呢",
                            "内容获取失败，请联系开发者。", ""));
                    return;
                }

                // 第2步：获取<systemOpen>包裹的整块内容
                Matcher matcher = XMLHelper.getContentByRegularExpression(cachedXmlContent,
                        "<systemOpen\\b[^>]*>[\\s\\S]*?</systemOpen>");
                if (matcher == null) {
                    Log.e(TAG, "catchSweetIslandInfo：获取XML内容失败");
                    callBack.onResult(generateOpenTimeMap("获取失败", "❌", "出错了呢",
                            "内容获取失败，请联系开发者。", ""));
                    return;
                }

                String systemOpenBlock = matcher.group(0);
                if (systemOpenBlock == null || systemOpenBlock.trim().isEmpty()) {
                    Log.e(TAG, "catchSweetIslandInfo: 获取到的活动内容为空");
                    callBack.onResult(generateOpenTimeMap("获取失败", "❌", "出错了呢",
                            "获取到的活动内容为空，请联系开发者并提交此界面截图", ""));
                    return;
                }
                Log.d(TAG, "catchSweetIslandInfo: 原始内容：" + systemOpenBlock);

                // 第3步：逐个解析<open>场次数据（条目数量不固定）
                List<SweetIslandActivityInfo> activityInfoList = parseOpenTagList(systemOpenBlock);
                if (activityInfoList.isEmpty()) {
                    Log.e(TAG, "catchSweetIslandInfo: 未解析到有效场次");
                    callBack.onResult(generateOpenTimeMap("获取失败", "❌", "出错了呢",
                            "获取到的活动内容为空，请联系开发者并提交此界面截图", ""));
                    return;
                }

                callBack.onResult(generateStatusResult(activityInfoList, "甜蜜岛"));
            } catch (Exception e) {
                // 必须回调失败结果：否则聚合器（ExecuteDailyTask）永远等不到本任务完成，界面会一直处于等待状态
                Log.e(TAG, "捕获异常：" + e.getMessage(), e);
                callBack.onResult(generateOpenTimeMap("获取失败", "❌", "出错了呢",
                        "网络异常\n请检查网络后重试", ""));
            }
        }).start();
    }

    /**
     * 异步解析走马观花开放场次内容（<tjbz_Open>中第一个<map>包裹的<open>数据）
     * 后续<map>内容不读取
     */
    public void catchAnimalActivityInfo(OpenTimeInfoCatchResultCallBack callBack) {
        // 网络请求必须在子线程执行，避免阻塞主线程
        new Thread(() -> {
            try {
                // 第1步：XML字符串并缓存
                cachedXmlContent = XMLHelper.getContentFromUrl(XML_URL);
                if (cachedXmlContent == null) {
                    Log.e(TAG, "catchAnimalActivityInfo: 内容获取失败，请联系开发者。");
                    callBack.onResult(generateOpenTimeMap("获取失败", "❌", "出错了呢",
                            "内容获取失败，请联系开发者。", ""));
                    return;
                }

                // 第2步：获取<tjbz_Open>包裹的整块内容
                Matcher blockMatcher = XMLHelper.getContentByRegularExpression(cachedXmlContent,
                        "<tjbz_Open\\b[^>]*>[\\s\\S]*?</tjbz_Open>");
                if (blockMatcher == null) {
                    Log.e(TAG, "catchAnimalActivityInfo：获取XML内容失败");
                    callBack.onResult(generateOpenTimeMap("获取失败", "❌", "出错了呢",
                            "内容获取失败，请联系开发者。", ""));
                    return;
                }

                String tjbzBlock = blockMatcher.group(0);
                if (tjbzBlock == null || tjbzBlock.trim().isEmpty()) {
                    Log.e(TAG, "catchAnimalActivityInfo: 获取到的活动内容为空");
                    callBack.onResult(generateOpenTimeMap("获取失败", "❌", "出错了呢",
                            "获取到的活动内容为空，请联系开发者并提交此界面截图", ""));
                    return;
                }

                // 第3步：只取第一个<map>包裹的内容（Matcher天然按出现顺序返回首个匹配）
                Matcher mapMatcher = XMLHelper.getContentByRegularExpression(tjbzBlock,
                        "<map\\b[^>]*>[\\s\\S]*?</map>");
                if (mapMatcher == null) {
                    Log.e(TAG, "catchAnimalActivityInfo：未找到map块");
                    callBack.onResult(generateOpenTimeMap("获取失败", "❌", "出错了呢",
                            "获取到的活动内容为空，请联系开发者并提交此界面截图", ""));
                    return;
                }

                String firstMapBlock = mapMatcher.group(0);
                Log.d(TAG, "catchAnimalActivityInfo: 原始内容：" + firstMapBlock);

                // 第4步：逐个解析<open>场次数据（条目数量不固定）
                List<SweetIslandActivityInfo> activityInfoList = parseOpenTagList(firstMapBlock);
                if (activityInfoList.isEmpty()) {
                    Log.e(TAG, "catchAnimalActivityInfo: 未解析到有效场次");
                    callBack.onResult(generateOpenTimeMap("获取失败", "❌", "出错了呢",
                            "获取到的活动内容为空，请联系开发者并提交此界面截图", ""));
                    return;
                }

                callBack.onResult(generateStatusResult(activityInfoList, "走马观花"));
            } catch (Exception e) {
                // 必须回调失败结果：否则聚合器（ExecuteDailyTask）永远等不到本任务完成，界面会一直处于等待状态
                Log.e(TAG, "捕获异常：" + e.getMessage(), e);
                callBack.onResult(generateOpenTimeMap("获取失败", "❌", "出错了呢",
                        "网络异常\n请检查网络后重试", ""));
            }
        }).start();
    }

    /**
     * 解析文本块中所有<open>标签为场次列表，并按开始时间升序排序
     * 不假设属性顺序，按属性名逐个提取，无效条目直接跳过
     *
     * @param block 待解析的文本块
     * @return 解析出的场次列表（可能为空）
     */
    private List<SweetIslandActivityInfo> parseOpenTagList(String block) {
        List<SweetIslandActivityInfo> activityInfoList = new ArrayList<>();
        Matcher openMatcher = OPEN_TAG_PATTERN.matcher(block);
        while (openMatcher.find()) {
            SweetIslandActivityInfo activityInfo = parseOpenTag(openMatcher.group(0));
            if (activityInfo != null) {
                activityInfoList.add(activityInfo);
            }
        }

        // 按开始时间升序排序，保证展示顺序固定
        activityInfoList.sort(Comparator.comparingLong(SweetIslandActivityInfo::getStartTime));
        return activityInfoList;
    }

    /**
     * 解析单个<open>标签中的场次属性
     * 不依赖属性顺序，按属性名逐个提取；任一属性缺失或格式非法则返回null（跳过该条）
     *
     * @param openTag 单个<open>标签的完整文本
     * @return 解析成功返回场次数据，否则返回null
     */
    private SweetIslandActivityInfo parseOpenTag(String openTag) {
        String startTimeStr = extractOpenAttribute(openTag, "startTime");
        String endTimeStr = extractOpenAttribute(openTag, "endTime");

        if (startTimeStr == null || endTimeStr == null) {
            Log.e(TAG, "parseOpenTag：场次属性缺失，跳过该条：" + openTag);
            return null;
        }

        try {
            long startTime = Long.parseLong(startTimeStr);
            long endTime = Long.parseLong(endTimeStr);

            if (startTime <= 0 || endTime < startTime) {
                Log.e(TAG, "parseOpenTag：场次时间非法，跳过该条：" + openTag);
                return null;
            }

            return new SweetIslandActivityInfo(startTime, endTime);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseOpenTag：场次属性格式非法，跳过该条：" + openTag);
            return null;
        }
    }

    /**
     * 从<open>标签文本中提取指定属性值
     *
     * @param openTag  单个<open>标签的完整文本
     * @param attrName 属性名
     * @return 属性值（已去除首尾空白），属性缺失时返回null
     */
    private String extractOpenAttribute(String openTag, String attrName) {
        Matcher matcher = Pattern.compile(attrName + "\\s*=\\s*\"([^\"]*)\"").matcher(openTag);
        return matcher.find() ? Objects.requireNonNull(matcher.group(1)).trim() : null;
    }

    /**
     * 按北京时间自然日判断活动状态并生成结果Map（三分支逻辑与抢红包一致）
     * 场次startTime所在日期等于今天 → 今天有活动（不看当前时刻是否落在时段内）
     * 否则取最近一场尚未开始的场次；都没有则活动已全部结束
     * 早于当天的场次不进入序列化列表（弹窗不展示，过滤后为空即全部结束）
     *
     * @param activityInfoList 已按开始时间升序排序的场次列表
     * @param activityName     活动名称（甜蜜岛/走马观花），用于结束文案
     * @return 生成的Map格式的数据
     */
    private Map<String, String> generateStatusResult(List<SweetIslandActivityInfo> activityInfoList, String activityName) {
        String today = SweetIslandActivityInfo.formatToday();

        // 过滤掉早于当天的场次：列表只展示今天及以后的场次（yyyy-MM-dd字符串字典序即日期先后序）
        // 过滤后列表为空与下方“所有场次已结束”分支严格等价，弹窗列表与卡片状态天然一致
        activityInfoList.removeIf(activityInfo -> activityInfo.formatDate().compareTo(today) < 0);
        String serializedList = SweetIslandActivityInfo.serialize(activityInfoList);

        long nowSeconds = System.currentTimeMillis() / 1000L;
        SweetIslandActivityInfo todayInfo = null; // 今天举行的场次
        SweetIslandActivityInfo nextInfo = null; // 最近一场尚未开始的场次
        for (SweetIslandActivityInfo activityInfo : activityInfoList) {
            if (todayInfo == null && today.equals(activityInfo.formatDate())) {
                todayInfo = activityInfo;
            }
            if (nextInfo == null && nowSeconds < activityInfo.getStartTime()) {
                nextInfo = activityInfo;
            }
        }

        if (todayInfo != null) {
            // 今天有活动，展示当天场次的具体时段
            Log.d(TAG, "generateStatusResult：今天有" + activityName + "活动");
            return generateOpenTimeMap("今日开启", activityName.equals("走马观花") ? "🐎" : "❤", "今日开启",
                    "请于" + todayInfo.formatTimeOfDayRange() + "期间上号肝图\n\n👇全部场次👇\n(长按卡片可以添加日程)",
                    serializedList);
        } else if (nextInfo != null) {
            // 今天没有活动，展示最近一场的日期
            Log.d(TAG, "generateStatusResult：等待下一场");
            return generateOpenTimeMap(nextInfo.formatDate(), "⏳", "等等等等",
                    "下一场：" + nextInfo.formatDate() + "\n\n👇全部场次👇\n(长按卡片可以添加日程)",
                    serializedList);
        } else {
            // 所有场次均已结束
            Log.d(TAG, "generateStatusResult：所有场次已结束");
            return generateOpenTimeMap("暂无", "⏳", "空空如也",
                    "还没有新的活动呢", serializedList);
        }
    }

    /**
     * 生成场次结果Map（在通用结果基础上追加场次列表序列化字段）
     * 所有分支（含失败分支）都必须携带resultList，避免下游拿到null
     *
     * @param resultSimple        显示在主界面的简要信息
     * @param resultEmoji         显示在主界面和弹窗上的表情
     * @param resultContentStatus 显示在弹窗上的状态信息
     * @param resultContentDetail 显示在弹窗上的详细信息
     * @param serializedList      序列化后的场次列表
     * @return 生成的Map格式的数据
     */
    private Map<String, String> generateOpenTimeMap(String resultSimple, String resultEmoji, String resultContentStatus, String resultContentDetail, String serializedList) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("resultSimple", resultSimple);
        resultMap.put("resultEmoji", resultEmoji);
        resultMap.put("resultContentStatus", resultContentStatus);
        resultMap.put("resultContentDetail", resultContentDetail);
        resultMap.put("resultList", serializedList == null ? "" : serializedList);
        return resultMap;
    }
}
