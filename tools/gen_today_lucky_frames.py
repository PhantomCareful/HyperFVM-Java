# -*- coding: utf-8 -*-
"""
将动画 WebP 导出为“逐帧静态 WebP + 时长清单”，供 FrameSequenceView 按速率播放。

用法（在项目根目录执行）:
    python tools/gen_today_lucky_frames.py
    python tools/gen_today_lucky_frames.py [源webp] [输出目录] [缩放比例] [质量]

默认:
    源文件   tools/source/today_lucky.webp（素材源，不参与打包）
    输出目录 app/src/main/assets/today_lucky_frames/
    缩放比例 0.6（300x350，按显示尺寸约 1:1.2）
    质量     85

输出目录内容:
    f00.webp, f01.webp, ...
    meta.txt  第一行为帧数，之后每行为对应帧的时长（毫秒），运行时据此计算时间轴

依赖: pip install pillow
"""

import os
import shutil
import sys

from PIL import Image, ImageSequence

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

DEFAULT_SRC = os.path.join(ROOT, "tools", "source", "today_lucky.webp")
DEFAULT_OUT = os.path.join(ROOT, "app", "src", "main", "assets", "today_lucky_frames")
DEFAULT_SCALE = 0.6
DEFAULT_QUALITY = 85


def main():
    src = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_SRC
    out = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_OUT
    scale = float(sys.argv[3]) if len(sys.argv) > 3 else DEFAULT_SCALE
    quality = int(sys.argv[4]) if len(sys.argv) > 4 else DEFAULT_QUALITY

    if not os.path.isfile(src):
        print("源文件不存在:", src)
        sys.exit(1)

    im = Image.open(src)
    frames = []
    durations = []
    default_duration = im.info.get("duration") or 20
    for frame in ImageSequence.Iterator(im):
        duration = frame.info.get("duration") or default_duration
        durations.append(int(duration))
        # 帧已由解码器合成为完整画面；统一转 RGB（本素材无透明像素）
        frames.append(frame.convert("RGB"))

    if not frames:
        print("未解析到任何帧")
        sys.exit(1)

    width, height = frames[0].size
    target_w = max(1, round(width * scale))
    target_h = max(1, round(height * scale))
    print(f"源: {src}")
    print(f"帧数: {len(frames)}  原始尺寸: {width}x{height}  输出尺寸: {target_w}x{target_h}  质量: {quality}")

    if os.path.isdir(out):
        shutil.rmtree(out)
    os.makedirs(out)

    total = 0
    for i, frame in enumerate(frames):
        resized = frame.resize((target_w, target_h), Image.LANCZOS)
        path = os.path.join(out, f"f{i:02d}.webp")
        resized.save(path, quality=quality, method=6)
        total += os.path.getsize(path)

    with open(os.path.join(out, "meta.txt"), "w", encoding="utf-8") as f:
        f.write(f"{len(frames)}\n")
        for duration in durations:
            f.write(f"{duration}\n")

    print(f"已输出 {len(frames)} 帧到: {out}")
    print(f"帧总大小: {total} bytes ({total / 1024:.0f} KiB)  平均 {total // len(frames)} bytes/帧")
    print(f"动画总时长: {sum(durations)} ms")


if __name__ == "__main__":
    main()
