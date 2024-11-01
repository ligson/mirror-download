package org.ligson.mirrordownload.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.ligson.mirrordownload.http.HttpHeaders;
import org.ligson.mirrordownload.http.SimpleHttpClient;

import java.io.File;

@Slf4j
public class HttpDownloader {
    private final DownloadManager downloadManager;

    public HttpDownloader(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    public void download(String baseUrl, File parentDir) throws Exception {
        //String baseUrl = "https://update.cs2c.com.cn/NS/V10/V10SP3-2403/os/adv/lic/updates/x86_64/repodata/";

        SimpleHttpClient client = new SimpleHttpClient();
        //User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36
        HttpHeaders headers = new HttpHeaders();
        headers.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        String html = client.doGet(baseUrl, headers);
        Document doc = Jsoup.parse(html, "UTF-8");
        Elements spanEle = doc.getElementsByTag("a");
        for (org.jsoup.nodes.Element element : spanEle) {
            String href = element.attr("href");
            String dstUrl = baseUrl + href;
            if (href.endsWith("/")) {
                if (!href.endsWith("../")) {
                    File dstDir = new File(parentDir, href.substring(0, href.length() - 1));
                    if (!dstDir.exists()) {
                        dstDir.mkdirs();
                    }
                    download(dstUrl, dstDir);
                }
            } else {
                File dstFile = new File(parentDir, href);
                try {
                    downloadManager.download(dstUrl, dstFile, null);
                    log.info("download success:{},file:{}", dstUrl, dstFile.getAbsolutePath());
                } catch (Exception e) {
                    log.error("download failed:{},error:{}", dstUrl, ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }
}
