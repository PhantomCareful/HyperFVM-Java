package com.careful.HyperFVM.utils.ForDashboard;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * XML通用处理工具类
 * 基于OkHttp实现网络XML获取，XmlPullParser实现解析
 */
public class XMLHelper {
    private static final String TAG = "XMLHelper";

    // 单例OkHttpClient（避免重复创建连接池，提升性能）
    private static final OkHttpClient OK_HTTP_CLIENT;

    // 初始化OkHttpClient（设置超时、关闭重定向等）
    static {
        OK_HTTP_CLIENT = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)      // 连接超时
                .readTimeout(15, TimeUnit.SECONDS)         // 读取超时
                .writeTimeout(10, TimeUnit.SECONDS)        // 写入超时
                .retryOnConnectionFailure(true)            // 连接失败重试
                .build();
    }

    // 私有化构造方法，避免实例化
    private XMLHelper() {}

    /**
     * 从指定网络链接获取XML字符串内容
     * @param url XML文件的网络链接
     * @return XML字符串内容，获取失败返回null
     * @throws IOException 网络异常/IO异常（交给调用方处理）
     */
    public static String getXMLStringFromUrl(String url) throws IOException {
        // 1. 参数校验
        if (url == null || url.trim().isEmpty()) {
            Log.e(TAG, "获取xml的url为空");
            return "获取xml的url为空";
        }

        // 2.构建OkHttp请求
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/xml") // 指定接收XML格式
                .header("User-Agent", "CustomXMLClient")
                .build();

        Call call = OK_HTTP_CLIENT.newCall(request);
        try (Response response = call.execute()) {
            // 3. 执行同步请求（注意必须要在子线程上调用）

            // 4. 校验响应状态
            if (!response.isSuccessful()) {
                Log.e(TAG, "获取XML失败，响应码：" + response.code());
                return null;
            }

            // 5. 读取响应体
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                Log.e(TAG, "XML响应体为空");
                return null;
            }

            // 6. 转换为字符串（指定UTF-8编码，避免乱码）
            String XMLContent = responseBody.string();
            if (XMLContent.trim().isEmpty()) {
                Log.e(TAG, "XML内容为空");
                return null;
            }

            return XMLContent;
        } catch (IOException e) {
            Log.e(TAG, "捕获异常：" + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 重载方法：将XML字符串转换为XmlPullParser对象（方便后续解析）
     * @param XMLContent XML字符串
     * @return XmlPullParser实例，失败返回null
     */
    public static XmlPullParser getXmlPullParser(String XMLContent) {
        if (XMLContent == null || XMLContent.trim().isEmpty()) {
            Log.e(TAG, "XML内容为空，无法创建XmlPullParser");
            return null;
        }

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false); // 不启用命名空间感知
            XmlPullParser parser = factory.newPullParser();

            // 将字符串转为输入流，传入解析器
            InputStream inputStream = new ByteArrayInputStream(
                    XMLContent.getBytes(StandardCharsets.UTF_8)
            );
            parser.setInput(inputStream, StandardCharsets.UTF_8.name());

            return parser;
        } catch (XmlPullParserException e) {
            Log.e(TAG, "创建XmlPullParser失败：" + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 核心方法：从XML解析器中，按「层级路径+匹配属性」获取目标属性的值
     * @param parser XmlPullParser实例（已绑定XML内容）
     * @param targetPath 目标元素的层级路径（如 "root/activitys/activity"）
     * @param matchAttrKey 匹配的属性名（如 "time"）
     * @param matchAttrValue 匹配的属性值（如 "2026-01-01"）
     * @param targetAttrKey 想要获取的属性名（如 "content"）
     * @return 目标属性的值，无匹配返回null
     */
    public static String getAttrValueByPathAndMatchAttr(
            XmlPullParser parser,
            String targetPath,
            String matchAttrKey,
            String matchAttrValue,
            String targetAttrKey
    ) {
        // 1. 校验输入参数
        if (parser == null ||
        targetPath == null || targetPath.trim().isEmpty() ||
        matchAttrKey == null || matchAttrKey.trim().isEmpty() ||
        matchAttrValue == null || matchAttrValue.trim().isEmpty() ||
        targetAttrKey == null || targetAttrKey.trim().isEmpty()) {
            Log.e(TAG, "getAttrValueByPathAndMatchAttr: 输入参数不能为空");
            return null;
        }

        // 2. 拆分层级路径（如 "root/activitys/activity" -> [root, activitys, activity]）
        String[] targetLevels = targetPath.split("/");
        if (targetLevels.length == 0) {
            Log.e(TAG, "getAttrValueByPathAndMatchAttr: 目标路径格式错误（需要用“/”分隔层级）");
            return null;
        }

        int currentLevelIndex = 0; // 当前匹配到的层级索引
        try {
            int eventType = parser.getEventType();
            // 3. 流式遍历XML节点
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String currentTagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG: // 解析到开始标签
                        if (currentTagName == null)
                            break;

                        // 4. 匹配当前层级的元素名
                        if (currentTagName.equals(targetLevels[currentLevelIndex])) {
                            currentLevelIndex++;

                            // 5. 如果此时匹配到最后一层，即目标元素，开始获取该元素的[匹配属性值]
                            if (currentLevelIndex == targetLevels.length) {
                                // 获取该元素的[匹配属性值]
                                String currentMatchValue = parser.getAttributeValue(null, matchAttrKey);

                                // 校验匹配属性值是否一致
                                if (matchAttrValue.equals(currentMatchValue)) {
                                    // 一致：返回目标属性的值
                                    return parser.getAttributeValue(null, targetAttrKey);
                                }

                                // 匹配失败：重置层级索引（继续遍历下一个节点）
                            }

                        } else {
                            // 当前层级元素名不匹配，重置索引
                            currentLevelIndex = 0;
                        }
                        break;

                    case XmlPullParser.END_TAG: // 解析到结束标签：回退层级索引
                        if (currentLevelIndex > 0 && currentTagName.equals(targetLevels[currentLevelIndex - 1])) {
                            currentLevelIndex--;
                        }
                        break;

                    default:
                        break;
                }

                // 解析下一个节点
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "getAttrValueByPathAndMatchAttr: 解析XML失败：" + e.getMessage(), e);
        }

        // 未找到匹配的元素/属性
        Log.d(TAG, "getAttrValueByPathAndMatchAttr: 未找到符合条件的元素：路径=" + targetPath + "，匹配属性=" + matchAttrKey + "=" + matchAttrValue);
        return null;
    }
}
