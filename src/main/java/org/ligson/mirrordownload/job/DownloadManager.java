package org.ligson.mirrordownload.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.ligson.mirrordownload.config.vo.AppConfig;
import org.ligson.mirrordownload.http.HttpHeaders;
import org.ligson.mirrordownload.http.SimpleHttpClient;

import java.io.File;
import java.util.concurrent.*;

@Slf4j
public class DownloadManager {

    private final BlockingQueue<Runnable> downloadQueue; // 下载任务的队列
    private final ExecutorService executorService; // 线程池，用于执行下载任务
    private int maxConcurrentDownloads = 5; // 最大并发下载数
    private int maxRetries = 3; // 最大重试次数
    private AppConfig appConfig;

    // 私有构造函数，防止外部实例化
    public DownloadManager(AppConfig appConfig) {
        this.downloadQueue = new LinkedBlockingQueue<>(); // 初始化下载队列
        this.maxConcurrentDownloads = appConfig.getApp().getJob().getLimit();
        this.maxRetries = appConfig.getApp().getJob().getRetry();
        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("download-worker-%d").build();
        this.executorService = Executors.newFixedThreadPool(maxConcurrentDownloads, threadFactory); // 创建固定大小的线程池
        startDownloadWorkers(); // 启动下载工作线程
    }

    // 启动多个下载工作线程
    private void startDownloadWorkers() {
        for (int i = 0; i < maxConcurrentDownloads; i++) {
            executorService.submit(this::processDownloadQueue); // 提交下载任务处理方法到线程池
        }
    }

    // 下载方法，接收URL、目标文件和HTTP头信息
    public void download(String url, File dest, HttpHeaders headers) {
        // 将下载任务添加到队列
        downloadQueue.offer(() -> attemptDownload(url, dest, headers, 0));
    }

    // 尝试下载方法，支持重试机制
    private void attemptDownload(String url, File dest, HttpHeaders headers, int attempt) {
        try {
            // 调用下载客户端进行下载
            SimpleHttpClient.download(url, dest, headers);
            log.info("download success, url={}, dest={}", url, dest.getAbsolutePath()); // 记录下载成功日志
        } catch (Exception e) {
            attempt++; // 增加尝试次数
            log.error("download error, url={}, dest={}, attempt={}", url, dest.getAbsolutePath(), attempt, e); // 记录错误日志
            if (attempt < maxRetries) { // 如果尝试次数未超过最大限制
                log.info("Retrying download, url={}, dest={}, attempt={}", url, dest.getAbsolutePath(), attempt); // 记录重试日志
                attemptDownload(url, dest, headers, attempt);  // 递归调用尝试下载
            } else {
                // 超过最大尝试次数，记录失败日志
                log.error("Failed to download after {} attempts, url={}, dest={}", maxRetries, url, dest.getAbsolutePath());
            }
        }
    }

    // 处理下载队列的方法
    private void processDownloadQueue() {
        while (true) {
            try {
                Runnable downloadTask = downloadQueue.take(); // 从队列中获取下载任务
                downloadTask.run(); // 运行下载任务
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 线程中断处理
                break; // 退出循环
            }
        }
    }

    // 优雅关闭线程池
    public void shutdown() {
        executorService.shutdown(); // 关闭线程池
        try {
            // 等待线程池中的线程终止
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // 超过等待时间，强制关闭
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow(); // 处理中断，强制关闭
        }
    }
}
