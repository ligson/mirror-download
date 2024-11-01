package org.ligson.mirrordownload.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ligson.mirrordownload.config.vo.AppConfig;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;

@Slf4j
public class CrawlerJob implements Job {
    private AliyunDownloader aliyunDownloader;
    private HttpDownloader httpDownloader;
    private DownloadManager downloadManager;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        AppConfig appConfig = (AppConfig) jobExecutionContext.getJobDetail().getJobDataMap().get("appConfig");
        if (downloadManager == null) {
            downloadManager = new DownloadManager(appConfig);
        }
        if (aliyunDownloader == null) {
            aliyunDownloader = new AliyunDownloader(downloadManager);
        }
        if (httpDownloader == null) {
            httpDownloader = new HttpDownloader(downloadManager);
        }
        appConfig.getApp().getMirrors().forEach(mirror -> {
            if ("aliyun".equals(mirror.getType())) {
                try {
                    aliyunDownloader.download(mirror.getUrl(), new File(mirror.getDest()));
                    log.info("aliyun download {} success", mirror.getUrl());
                } catch (Exception e) {
                    log.error("download {},error:{},stack:{}", mirror.getUrl(), e.getMessage(), ExceptionUtils.getStackTrace(e));
                }
            } else if ("http".equals(mirror.getType())) {
                log.info("http");
                try {
                    httpDownloader.download(mirror.getUrl(), new File(mirror.getDest()));
                    log.info("http download {} success", mirror.getUrl());
                } catch (Exception e) {
                    log.error("http download {},error:{},stack:{}", mirror.getUrl(), e.getMessage(), ExceptionUtils.getStackTrace(e));
                }
            }
        });
    }
}
