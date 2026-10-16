package com.careful.HyperFVM.utils.ForDownload;

import java.net.HttpURLConnection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 单个下载任务的暂停/取消控制柄。
 * 下载循环在每个读写迭代中感知暂停/取消标志；
 * 暂停/取消时主动断开当前连接，让阻塞中的网络读立即退出，保证响应速度
 */
public class DownloadControl {
    private volatile boolean paused;
    private volatile boolean cancelled;
    // 图片批任务有4个线程并发下载，各自挂接一条连接，暂停/取消时需逐条断开
    private final CopyOnWriteArrayList<HttpURLConnection> connections = new CopyOnWriteArrayList<>();

    /**
     * 暂停下载：置位标志并断开当前连接（保留已下载的临时文件，恢复时断点续传）
     */
    public synchronized void pause() {
        if (cancelled || paused) return;
        paused = true;
        disconnectQuietly();
    }

    /**
     * 恢复下载：清除标志并唤醒等待中的下载线程
     */
    public synchronized void resume() {
        if (!paused || cancelled) return;
        paused = false;
        notifyAll();
    }

    /**
     * 取消下载：置位标志并断开当前连接（调用方负责清理临时文件）
     */
    public synchronized void cancel() {
        if (cancelled) return;
        cancelled = true;
        paused = false;
        disconnectQuietly();
        notifyAll();
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * 下载线程在开始每个文件前以及收到 PAUSED 结果后调用：
     * 暂停期间阻塞等待，恢复后返回 true 继续；已取消则返回 false
     */
    public synchronized boolean awaitResumed() {
        while (paused && !cancelled) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return true;
            }
        }
        return cancelled;
    }

    /**
     * 挂接当前活动连接，供暂停/取消时主动断开（挂接后若已处于暂停/取消态则立即断开）
     */
    public void attach(HttpURLConnection connection) {
        connections.addIfAbsent(connection);
        if (paused || cancelled) {
            disconnectQuietly();
        }
    }

    /**
     * 摘除连接，避免持有已关闭连接的引用
     */
    public void detach(HttpURLConnection connection) {
        connections.remove(connection);
    }

    private void disconnectQuietly() {
        // disconnect 会让阻塞在 read 上的线程立即抛 IOException 退出
        for (HttpURLConnection connection : connections) {
            connection.disconnect();
        }
    }
}
