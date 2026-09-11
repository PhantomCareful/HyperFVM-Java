package com.careful.HyperFVM.utils.ForDesign.Animation;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 存放在 assets 中的帧序列（f00.webp、f01.webp ... 与 meta.txt），
 * 提供按时间轴取帧的能力，供 FrameSequenceView 以任意速率播放。
 * 帧文件由 tools/gen_today_lucky_frames.py 从动画 WebP 导出。
 * 注意：所有方法仅应在主线程调用（内部缓存未做线程同步）。
 */
public class FrameSequence {
    // LRU 缓存上限，缓存过多帧会明显占用内存（单帧约 300x350x4B ≈ 420KB）
    private static final int MAX_CACHED_FRAMES = 16;
    // meta.txt 缺失时的默认帧时长（毫秒）
    private static final int DEFAULT_FRAME_DURATION_MS = 20;

    private final AssetManager assetManager;
    private final List<String> framePaths;
    // 每帧的起始时间（由帧时长累加得到），配合总时长完成"时间 -> 帧"的映射
    private final long[] frameStartTimesMs;
    private final long totalDurationMs;
    // 帧的像素尺寸（所有帧一致），供视图做宽高比自适应
    private final int frameWidth;
    private final int frameHeight;

    // LRU 帧缓存：accessOrder=true，超出上限时自动淘汰最久未使用的帧（交给 GC 回收）
    private final LinkedHashMap<Integer, Bitmap> frameCache = new LinkedHashMap<>(8, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, Bitmap> eldest) {
            return size() > MAX_CACHED_FRAMES;
        }
    };

    private FrameSequence(AssetManager assetManager, List<String> framePaths,
                          long[] frameStartTimesMs, long totalDurationMs,
                          int frameWidth, int frameHeight) {
        this.assetManager = assetManager;
        this.framePaths = framePaths;
        this.frameStartTimesMs = frameStartTimesMs;
        this.totalDurationMs = totalDurationMs;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    /**
     * 从 assets 目录加载帧序列，目录内应包含 f*.webp 帧文件与 meta.txt
     * （首行为帧数，其后每行对应帧的时长，单位毫秒）。
     *
     * @return 加载失败（目录为空、meta 无效等）时返回 null
     */
    public static FrameSequence load(Context context, String assetDir) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] entries = assetManager.list(assetDir);
            if (entries == null) return null;

            List<String> names = new ArrayList<>();
            for (String name : entries) {
                if (name.matches("f\\d+\\.webp")) {
                    names.add(name);
                }
            }
            if (names.isEmpty()) return null;
            names.sort(Comparator.comparingInt(FrameSequence::frameNumberOf));

            List<String> paths = new ArrayList<>(names.size());
            for (String name : names) {
                paths.add(assetDir + "/" + name);
            }

            // 只读首帧的文件头获取帧尺寸，不解码像素
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream is = assetManager.open(paths.get(0))) {
                BitmapFactory.decodeStream(is, null, options);
            }
            if (options.outWidth <= 0 || options.outHeight <= 0) return null;

            int[] durations = readDurations(assetManager, assetDir + "/meta.txt", names.size());
            long[] startTimes = new long[names.size()];
            long total = 0;
            for (int i = 0; i < names.size(); i++) {
                startTimes[i] = total;
                total += durations[i];
            }
            return new FrameSequence(assetManager, paths, startTimes, total,
                    options.outWidth, options.outHeight);
        } catch (IOException e) {
            return null;
        }
    }

    /** 获取指定帧的位图（带 LRU 缓存），解码失败时返回 null */
    public Bitmap getFrame(int index) {
        Bitmap cached = frameCache.get(index);
        if (cached != null) return cached;

        try (InputStream is = assetManager.open(framePaths.get(index))) {
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                frameCache.put(index, bitmap);
            }
            return bitmap;
        } catch (IOException e) {
            return null;
        }
    }

    /** 将时间（毫秒，可为任意非负值）映射为对应的帧下标 */
    public int getFrameIndex(long timeMs) {
        long t = timeMs % totalDurationMs;
        if (t < 0) t += totalDurationMs;
        for (int i = frameStartTimesMs.length - 1; i >= 0; i--) {
            if (t >= frameStartTimesMs[i]) return i;
        }
        return 0;
    }

    public int getFrameCount() {
        return framePaths.size();
    }

    /** 帧的像素宽度（所有帧尺寸一致） */
    public int getFrameWidth() {
        return frameWidth;
    }

    /** 帧的像素高度（所有帧尺寸一致） */
    public int getFrameHeight() {
        return frameHeight;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    /** 读取 meta.txt 中的帧时长；缺失或损坏时按顺序回退到默认值 */
    private static int[] readDurations(AssetManager assetManager, String metaPath, int frameCount) {
        List<Integer> durations = new ArrayList<>();
        try (InputStream is = assetManager.open(metaPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) {
                    firstLine = false; // 首行为帧数，跳过
                    continue;
                }
                try {
                    durations.add(Integer.parseInt(line));
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException ignored) {
            // meta.txt 缺失时全部使用默认帧时长
        }

        int[] result = new int[frameCount];
        int last = DEFAULT_FRAME_DURATION_MS;
        for (int i = 0; i < frameCount; i++) {
            if (i < durations.size()) {
                last = Math.max(1, durations.get(i));
            }
            result[i] = last;
        }
        return result;
    }

    /** 从文件名（f012.webp）中取出帧编号，用于排序 */
    private static int frameNumberOf(String name) {
        return Integer.parseInt(name.substring(1, name.indexOf('.')));
    }
}
