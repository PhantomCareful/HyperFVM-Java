package com.careful.HyperFVM.utils.ForDashboard.FromGame.NewYear;

import android.util.Log;

import com.careful.HyperFVM.utils.ForDashboard.XMLHelper;
import com.careful.HyperFVM.utils.OtherUtils.TimeUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewYearCatcher {
    private static final String XML_URL = "https://cdn-qq-ms.123u.com/cdn.qq.123u.com/config/new_year.xml";
    private static final String TAG = "NewYearCatcher";

    // 抢红包<li>场次标签的匹配模式（属性内不会出现>，可安全截取整个标签）
    private static final Pattern LI_TAG_PATTERN = Pattern.compile("<li\\b[^>]*>");

    // 缓存XML内容，避免重复网络请求
    private String cachedXmlContent;

    /**
     * 异步解析美食悬赏活动内容
     */
    public void catchBountyInfo(BountyInfoCatchResultCallBack callBack) {
        // 网络请求必须在子线程执行，避免阻塞主线程
        new Thread(() -> {
            String errorMsg;
            String contentStatus; // 最终生成的状态文本
            String contentDetail; // 最终生成的内容文本

            try {
                // 第1步：XML字符串并缓存
                cachedXmlContent = XMLHelper.getContentFromUrl(XML_URL);
                if (cachedXmlContent == null) {
                    errorMsg = "内容获取失败，请联系开发者。";
                    Log.e(TAG, "catchTodayActivityInfo: " + errorMsg);

                    callBack.onResult(
                            generateMap("获取失败", "❌失败", "❌", "出错了呢", errorMsg, false, -1)
                    );

                    return;
                }

                // 第2步：获取原始内容
                Matcher matcher = XMLHelper.getContentByRegularExpression(cachedXmlContent,
                        "\\s*(活动时间:\\s*\\d{1,2}月\\d{1,2}日-\\d{1,2}月\\d{1,2}日10:00)\\s*");
                if (matcher == null) {
                    Log.e(TAG, "获取XML内容失败");

                    callBack.onResult(
                            generateMap("获取失败", "❌失败", "❌", "出错了呢", "获取内容失败，请联系开发者并提交此界面截图。", false, -1)
                    );

                    return;
                }

                String bountyInfo = matcher.group(0);

                if (bountyInfo == null || bountyInfo.trim().isEmpty()) {
                    errorMsg = "获取到的活动内容为空，请联系开发者并提交此界面截图";
                    Log.e(TAG, "catchTodayActivityInfo: " + errorMsg);

                    callBack.onResult(
                            generateMap("获取失败", "❌失败", "❌", "出错了呢", errorMsg, false, -1)
                    );

                    return;
                }
                Log.d(TAG, "catchTodayActivityInfo: 原始内容：" + bountyInfo);

                // 第3步：从原始内容提取两个日期
                /*
                    原始内容形如：[活动时间: 1月22日-2月5日10:00]
                    需要进行一步步分割
                 */
                String startDate = bountyInfo.split(":")[1].trim()
                        .split("-")[0].trim();
                String endDate = bountyInfo.split(":")[1].trim()
                        .split("-")[1].trim()
                        .split("日")[0].trim() + "日";

                /*
                    第4步：由于这里给的日期不是形如2026-01-01的形式，我们需要进行手动转换
                    涉及到跨年的两种特殊情况（startDate比endDate大）需要注意
                    （1）currentMonth == startMonth：startDate和todayDate属于同一年，endDate属于第二年
                    （2）currentMonth == endMonth：endDate和todayDate属于同一年，startDate属于前一年
                 */
                int startYear = 0;
                int startMonth = Integer.parseInt(startDate.split("月")[0]);
                int startDay = Integer.parseInt(startDate.split("月")[1].split("日")[0]);
                int endYear = 0;
                int endMonth = Integer.parseInt(endDate.split("月")[0]);
                int endDay = Integer.parseInt(endDate.split("月")[1].split("日")[0]);
                int currentYear = TimeUtil.getCurrentYear();
                int currentMonth = TimeUtil.getCurrentMonth();
                // 开始处理特殊情况
                if (startMonth > endMonth) {
                    if (currentMonth == startMonth) {
                        startYear = currentYear;
                        endYear = currentYear + 1;
                    } else if (currentMonth == endMonth) {
                        startYear = currentYear - 1;
                        endYear = currentYear;
                    }
                } else {
                    startYear = currentYear;
                    endYear = currentYear;
                }
                startDate = TimeUtil.generateFormattedDate(startYear, startMonth, startDay);
                endDate = TimeUtil.generateFormattedDate(endYear, endMonth, endDay);

                // 第5步：将日期转换成Date类型
                String todayDate = TimeUtil.getCurrentDate();
                Date today = TimeUtil.transformStringToDate(todayDate);
                Date start = TimeUtil.transformStringToDate(startDate);
                Date end = TimeUtil.transformStringToDate(endDate);

                // 第6步：获取当前活动的单日可获取的最大声望值，判断是否已开启声望翻倍
                int dayMax = Integer.parseInt(cachedXmlContent.split("dayMax=\"")[1].split("\"")[0]);
                boolean isDouble = dayMax > 220;

                // 第7步：开始判断todayDate和startDate、endDate之间的关系，并生成结果
                if (today.before(start)) {
                    Log.d(TAG, "活动尚未开始");
                    contentStatus = "等等等等";
                    contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate;

                    callBack.onResult(
                            generateMap("尚未开始", "暂无", "⏳", contentStatus, contentDetail, isDouble, dayMax)
                    );
                } else if (today.after(end)) {
                    Log.d(TAG, "活动已结束");
                    contentStatus = "空空如也";
                    contentDetail = "还没有新的活动呢";

                    callBack.onResult(
                            generateMap("暂无", "暂无", "⏳", contentStatus, contentDetail, isDouble, dayMax)
                    );
                } else {
                    Log.d(TAG, "活动正在进行中");
                    int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
                    /*
                        还需要确定是2周的悬赏还是3周的悬赏
                     */
                    int length = TimeUtil.calculateDaysBetween(startDate, endDate) - 1;
                    contentStatus = "第" + duringCount + "天/持续" + length + "天";
                    contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate;

                    callBack.onResult(
                            generateMap(duringCount + "/" + length, duringCount + "/" + length, "✊", contentStatus, contentDetail, isDouble, dayMax)
                    );
                }
            } catch (Exception e) {
                // 必须回调失败结果：否则聚合器（ExecuteDailyTask）永远等不到本任务完成，界面会一直处于等待状态
                Log.e(TAG, "捕获异常：" + e.getMessage(), e);

                callBack.onResult(
                        generateMap("获取失败", "❌失败", "❌", "出错了呢", "网络异常\n请检查网络后重试", false, -1)
                );
            }
        }).start();
    }

    /**
     * 异步解析百万消费活动内容
     */
    public void catchMillionConsumptionInfo(MillionConsumptionInfoCatchResultCallBack callBack) {
        // 网络请求必须在子线程执行，避免阻塞主线程
        new Thread(() -> {
            String errorMsg;
            String contentStatus; // 最终生成的状态文本
            String contentDetail; // 最终生成的详细文本

            try {
                // 第1步：XML字符串并缓存
                cachedXmlContent = XMLHelper.getContentFromUrl(XML_URL);
                if (cachedXmlContent == null) {
                    errorMsg = "内容获取失败，请联系开发者。";
                    Log.e(TAG, "catchTodayActivityInfo: " + errorMsg);

                    callBack.onResult(
                            generateMap("获取失败", "❌", "出错了呢", errorMsg)
                    );

                    return;
                }

                // 第2步：获取原始内容
                Matcher matcher = XMLHelper.getContentByRegularExpression(cachedXmlContent,
                        "\\s*(\\s*\\d{1,2}月\\d{1,2}日-\\d{1,2}月\\d{1,2}日10:00\" reddes=\"以下方式消费点券可领取豪礼：诸神宝殿、幸运转转、法老宝藏、塔罗寻宝、大富翁、商城、保险金、月卡、婚礼、宠物开槽、公会捐献！\")\\s*");
                if (matcher == null) {
                    errorMsg = "获取内容失败，请联系开发者并提交此界面截图。";
                    Log.e(TAG, "获取XML内容失败");

                    callBack.onResult(
                            generateMap("获取失败", "❌", "出错了呢", errorMsg)
                    );

                    return;
                }

                String millionConsumptionInfo = matcher.group(0);

                if (millionConsumptionInfo == null || millionConsumptionInfo.trim().isEmpty()) {
                    errorMsg = "获取到的活动内容为空，请联系开发者并提交此界面截图";
                    Log.e(TAG, "catchTodayActivityInfo: " + errorMsg);

                    callBack.onResult(
                            generateMap("获取失败", "❌", "出错了呢", errorMsg)
                    );

                    return;
                }
                Log.d(TAG, "catchTodayActivityInfo: 原始内容：" + millionConsumptionInfo);

                // 第3步：从原始内容提取两个日期
                /*
                    原始内容形如：[01月29日-02月05日10:00" reddes="以下方式消费点券可领取豪礼：诸神宝殿、幸运转转、法老宝藏、塔罗寻宝、大富翁、商城、保险金、月卡、婚礼、宠物开槽、公会捐献！"]
                    需要进行一步步分割
                 */
                millionConsumptionInfo = millionConsumptionInfo.split("\"")[0];
                String startDate = millionConsumptionInfo.split("-")[0].trim();
                String endDate = millionConsumptionInfo.split("-")[1].trim()
                        .split("日")[0].trim() + "日";

                /*
                    第4步：由于这里给的日期不是形如2026-01-01的形式，我们需要进行手动转换
                    涉及到跨年的两种特殊情况（startDate比endDate大）需要注意
                    （1）currentMonth == startMonth：startDate和todayDate属于同一年，endDate属于第二年
                    （2）currentMonth == endMonth：endDate和todayDate属于同一年，startDate属于前一年
                 */
                int startYear = 0;
                int startMonth = Integer.parseInt(startDate.split("月")[0]);
                int startDay = Integer.parseInt(startDate.split("月")[1].split("日")[0]);
                int endYear = 0;
                int endMonth = Integer.parseInt(endDate.split("月")[0]);
                int endDay = Integer.parseInt(endDate.split("月")[1].split("日")[0]);
                int currentYear = TimeUtil.getCurrentYear();
                int currentMonth = TimeUtil.getCurrentMonth();
                // 开始处理特殊情况
                if (startMonth > endMonth) {
                    if (currentMonth == startMonth) {
                        startYear = currentYear;
                        endYear = currentYear + 1;
                    } else if (currentMonth == endMonth) {
                        startYear = currentYear - 1;
                        endYear = currentYear;
                    }
                } else {
                    startYear = currentYear;
                    endYear = currentYear;
                }
                startDate = TimeUtil.generateFormattedDate(startYear, startMonth, startDay);
                endDate = TimeUtil.generateFormattedDate(endYear, endMonth, endDay);

                // 第5步：开始判断todayDate和startDate、endDate之间的关系，并向数据库写入结果
                // 先转换成Date类型，方便比较
                String todayDate = TimeUtil.getCurrentDate();
                Date today = TimeUtil.transformStringToDate(todayDate);
                Date start = TimeUtil.transformStringToDate(startDate);
                Date end = TimeUtil.transformStringToDate(endDate);

                if (today.before(start)) {
                    Log.d(TAG, "活动尚未开始");
                    contentStatus = "等等等等";
                    contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n活动还没开始呢";

                    callBack.onResult(
                            generateMap("尚未开始", "⏳", contentStatus, contentDetail)
                    );
                } else if (today.after(end)) {
                    Log.d(TAG, "活动已结束");
                    contentStatus = "空空如也";
                    contentDetail = "还没有新的活动呢";

                    callBack.onResult(
                            generateMap("暂无", "⏳", contentStatus, contentDetail)
                    );
                } else {
                    Log.d(TAG, "活动正在进行中");
                    int duringCount = TimeUtil.calculateDaysBetween(startDate, todayDate);
                    /*
                        还需要确定消费的持续时间
                     */
                    int length = TimeUtil.calculateDaysBetween(startDate, endDate) - 1;
                    contentStatus = "第" + duringCount + "天/持续" + length + "天";
                    contentDetail = "开始日期：" + startDate + "\n结束日期：" + endDate + "\n\n🚨温馨提示🚨\n适度游戏，理性消费";

                    callBack.onResult(
                            generateMap(duringCount + "/" + length, "\uD83D\uDCB8", contentStatus, contentDetail)
                    );
                }

            } catch (Exception e) {
                // 必须回调失败结果：否则聚合器（ExecuteDailyTask）永远等不到本任务完成，界面会一直处于等待状态
                Log.e(TAG, "捕获异常：" + e.getMessage(), e);

                callBack.onResult(
                        generateMap("获取失败", "❌", "出错了呢", "网络异常\n请检查网络后重试")
                );
            }
        }).start();
    }

    /**
     * 异步解析抢红包活动内容
     * 读取<LuckyMoney>包裹的<li>场次数据（条目数量不固定），生成场次列表并按当前时间判断活动状态
     */
    public void catchLuckyConsumptionInfo(LuckyConsumptionInfoCatchResultCallBack callBack) {
        // 网络请求必须在子线程执行，避免阻塞主线程
        new Thread(() -> {
            String errorMsg;

            try {
                // 第1步：XML字符串并缓存
                cachedXmlContent = XMLHelper.getContentFromUrl(XML_URL);
                if (cachedXmlContent == null) {
                    errorMsg = "内容获取失败，请联系开发者。";
                    Log.e(TAG, "catchLuckyConsumptionInfo: " + errorMsg);

                    callBack.onResult(
                            generateLuckyMoneyMap("获取失败", "❌", "出错了呢", errorMsg, "")
                    );

                    return;
                }

                // 第2步：获取<LuckyMoney>包裹的整块内容
                Matcher matcher = XMLHelper.getContentByRegularExpression(cachedXmlContent,
                        "<LuckyMoney\\b[^>]*>[\\s\\S]*?</LuckyMoney>");
                if (matcher == null) {
                    errorMsg = "内容获取失败，请联系开发者。";
                    Log.e(TAG, "catchLuckyConsumptionInfo：获取XML内容失败");

                    callBack.onResult(
                            generateLuckyMoneyMap("获取失败", "❌", "出错了呢", errorMsg, "")
                    );

                    return;
                }

                String luckyMoneyBlock = matcher.group(0);

                if (luckyMoneyBlock == null || luckyMoneyBlock.trim().isEmpty()) {
                    errorMsg = "获取到的活动内容为空，请联系开发者并提交此界面截图";
                    Log.e(TAG, "catchLuckyConsumptionInfo: " + errorMsg);

                    callBack.onResult(
                            generateLuckyMoneyMap("获取失败", "❌", "出错了呢", errorMsg, "")
                    );

                    return;
                }
                Log.d(TAG, "catchLuckyConsumptionInfo: 原始内容：" + luckyMoneyBlock);

                /*
                    第3步：逐个解析<li>场次数据（条目数量不固定）
                    原始内容形如：[<li beginTime="1787115600" endTime="1787122800" total="8800" userNum="100" minGet="12" maxGet="520"/>]
                    不假设属性顺序，按属性名逐个提取，无效条目直接跳过
                 */
                List<LuckyMoneyActivityInfo> activityInfoList = new ArrayList<>();
                Matcher liMatcher = LI_TAG_PATTERN.matcher(luckyMoneyBlock);
                while (liMatcher.find()) {
                    LuckyMoneyActivityInfo activityInfo = parseLuckyMoneyLi(liMatcher.group(0));
                    if (activityInfo != null) {
                        activityInfoList.add(activityInfo);
                    }
                }

                if (activityInfoList.isEmpty()) {
                    errorMsg = "获取到的活动内容为空，请联系开发者并提交此界面截图";
                    Log.e(TAG, "catchLuckyConsumptionInfo: " + errorMsg);

                    callBack.onResult(
                            generateLuckyMoneyMap("获取失败", "❌", "出错了呢", errorMsg, "")
                    );

                    return;
                }

                // 按开始时间升序排序，保证展示顺序固定
                activityInfoList.sort(Comparator.comparingLong(LuckyMoneyActivityInfo::getBeginTime));
                String serializedList = LuckyMoneyActivityInfo.serialize(activityInfoList);

                /*
                    第4步：按北京时间自然日判断当天是否有抢红包活动
                    场次beginTime所在日期等于今天 → 今天有活动（不看当前时刻是否落在时段内）
                    否则取最近一场尚未开始的场次；都没有则活动已全部结束
                 */
                String today = LuckyMoneyActivityInfo.formatToday();
                long nowSeconds = System.currentTimeMillis() / 1000L;
                LuckyMoneyActivityInfo todayInfo = null; // 今天举行的场次
                LuckyMoneyActivityInfo nextInfo = null; // 最近一场尚未开始的场次
                for (LuckyMoneyActivityInfo activityInfo : activityInfoList) {
                    if (todayInfo == null && today.equals(activityInfo.formatDate())) {
                        todayInfo = activityInfo;
                    }
                    if (nextInfo == null && nowSeconds < activityInfo.getBeginTime()) {
                        nextInfo = activityInfo;
                    }
                }

                Map<String, String> resultMap;
                if (todayInfo != null) {
                    // 今天有抢红包活动，展示当天场次的具体时段
                    Log.d(TAG, "catchLuckyConsumptionInfo：今天有抢红包活动");
                    resultMap = generateLuckyMoneyMap("恭喜发财", "\uD83E\uDDE7", "恭喜发财",
                            "今天" + todayInfo.formatTimeOfDayRange() + "抢红包\n具体时刻请在游戏内查看\n\n👇全部场次👇\n(长按卡片可以添加日程)", serializedList);
                } else if (nextInfo != null) {
                    // 今天没有活动，展示最近一场的日期（主界面不再显示“暂无”）
                    Log.d(TAG, "catchLuckyConsumptionInfo：等待下一场");
                    resultMap = generateLuckyMoneyMap(nextInfo.formatDate(), "⏳", "等等等等",
                            "下一场：" + nextInfo.formatDate() + "\n\n👇全部场次👇\n(长按卡片可以添加日程)", serializedList);
                } else {
                    // 所有场次均已结束
                    Log.d(TAG, "catchLuckyConsumptionInfo：所有场次已结束");
                    resultMap = generateLuckyMoneyMap("暂无", "⏳", "空空如也",
                            "抢红包活动已结束\n敬请期待下一期", serializedList);
                }

                callBack.onResult(resultMap);

            } catch (Exception e) {
                // 必须回调失败结果：否则聚合器（ExecuteDailyTask）永远等不到本任务完成，界面会一直处于等待状态
                Log.e(TAG, "捕获异常：" + e.getMessage(), e);

                callBack.onResult(
                        generateLuckyMoneyMap("获取失败", "❌", "出错了呢", "网络异常\n请检查网络后重试", "")
                );
            }
        }).start();
    }

    /**
     * 解析单个<li>标签中的抢红包场次属性
     * 不依赖属性顺序，按属性名逐个提取；任一属性缺失或格式非法则返回null（跳过该条）
     *
     * @param liTag 单个<li>标签的完整文本
     * @return 解析成功返回场次数据，否则返回null
     */
    private LuckyMoneyActivityInfo parseLuckyMoneyLi(String liTag) {
        String beginTimeStr = extractLuckyMoneyAttribute(liTag, "beginTime");
        String endTimeStr = extractLuckyMoneyAttribute(liTag, "endTime");
        String totalStr = extractLuckyMoneyAttribute(liTag, "total");
        String userNumStr = extractLuckyMoneyAttribute(liTag, "userNum");
        String minGetStr = extractLuckyMoneyAttribute(liTag, "minGet");
        String maxGetStr = extractLuckyMoneyAttribute(liTag, "maxGet");

        if (beginTimeStr == null || endTimeStr == null || totalStr == null
                || userNumStr == null || minGetStr == null || maxGetStr == null) {
            Log.e(TAG, "parseLuckyMoneyLi：场次属性缺失，跳过该条：" + liTag);
            return null;
        }

        try {
            long beginTime = Long.parseLong(beginTimeStr);
            long endTime = Long.parseLong(endTimeStr);
            int total = Integer.parseInt(totalStr);
            int userNum = Integer.parseInt(userNumStr);
            int minGet = Integer.parseInt(minGetStr);
            int maxGet = Integer.parseInt(maxGetStr);

            if (beginTime <= 0 || endTime < beginTime) {
                Log.e(TAG, "parseLuckyMoneyLi：场次时间非法，跳过该条：" + liTag);
                return null;
            }

            return new LuckyMoneyActivityInfo(beginTime, endTime, total, userNum, minGet, maxGet);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseLuckyMoneyLi：场次属性格式非法，跳过该条：" + liTag);
            return null;
        }
    }

    /**
     * 从<li>标签文本中提取指定属性值
     *
     * @param liTag    单个<li>标签的完整文本
     * @param attrName 属性名
     * @return 属性值（已去除首尾空白），属性缺失时返回null
     */
    private String extractLuckyMoneyAttribute(String liTag, String attrName) {
        Matcher matcher = Pattern.compile(attrName + "\\s*=\\s*\"([^\"]*)\"").matcher(liTag);
        return matcher.find() ? Objects.requireNonNull(matcher.group(1)).trim() : null;
    }

    /**
     * 生成抢红包结果Map（在通用结果基础上追加场次列表序列化字段）
     * 所有分支（含失败分支）都必须携带resultList，避免下游拿到null
     *
     * @param resultSimple        显示在主界面的简要信息
     * @param resultEmoji         显示在主界面和弹窗上的表情
     * @param resultContentStatus 显示在弹窗上的状态信息
     * @param resultContentDetail 显示在弹窗上的详细信息
     * @param serializedList      序列化后的抢红包场次列表
     * @return 生成的Map格式的数据
     */
    private Map<String, String> generateLuckyMoneyMap(String resultSimple, String resultEmoji, String resultContentStatus, String resultContentDetail, String serializedList) {
        Map<String, String> resultMap = generateMap(resultSimple, resultEmoji, resultContentStatus, resultContentDetail);
        resultMap.put("resultList", serializedList == null ? "" : serializedList);
        return resultMap;
    }

    /**
     * 保存结果到Map，用于及时输出数据
     * @param resultSimple 显示在主界面的简要信息
     * @param resultNotification 显示在通知的简要信息
     * @param resultEmoji 显示在主界面和弹窗上的表情
     * @param resultContentStatus 显示在弹窗上的状态信息
     * @param resultContentDetail 显示在弹窗上的详细信息
     * @param isDouble 是否开启声望翻倍
     * @param dayMax 当前单日可获得的最大声望值
     * @return 生成的Map格式的数据
     */
    private Map<String, String> generateMap(String resultSimple, String resultNotification, String resultEmoji, String resultContentStatus, String resultContentDetail, boolean isDouble, int dayMax) {
        Map<String, String> result = new HashMap<>();

        result.put("resultSimple", resultSimple);
        if (!resultNotification.isEmpty()) {
            result.put("resultNotification", resultNotification);
        }
        result.put("resultEmoji", resultEmoji);
        result.put("resultContentStatus", resultContentStatus);
        result.put("resultContentDetail", resultContentDetail);
        result.put("resultIsDouble", String.valueOf(isDouble));
        result.put("resultDayMax", String.valueOf(dayMax));

        return result;
    }

    /**
     * 保存结果到Map，用于及时输出数据
     *
     * @param resultSimple        显示在主界面的简要信息
     * @param resultEmoji         显示在主界面和弹窗上的表情
     * @param resultContentStatus 显示在弹窗上的状态信息
     * @param resultContentDetail 显示在弹窗上的详细信息
     * @return 生成的Map格式的数据
     */
    private Map<String, String> generateMap(String resultSimple, String resultEmoji, String resultContentStatus, String resultContentDetail) {
        Map<String, String> result = new HashMap<>();

        result.put("resultSimple", resultSimple);
        result.put("resultEmoji", resultEmoji);
        result.put("resultContentStatus", resultContentStatus);
        result.put("resultContentDetail", resultContentDetail);

        return result;
    }

}
