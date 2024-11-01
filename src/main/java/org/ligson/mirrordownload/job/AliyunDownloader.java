package org.ligson.mirrordownload.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ligson.mirrordownload.http.SimpleHttpClient;

import java.io.File;

@Slf4j
public class AliyunDownloader {
    private DownloadManager downloadManager;

    public AliyunDownloader(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    private void downloadUrl(String href, String baseUrl, File parentDir) {
        if ((!href.equals("../"))&&(!href.equals("Parent directory/"))) {
            String dstUrl = baseUrl + href;
            if (href.endsWith("/")) {
                File dstDir = new File(parentDir, href.substring(0, href.length() - 1));
                if (!dstDir.exists()) {
                    dstDir.mkdirs();
                }
                try {
                    download(dstUrl, dstDir);
                } catch (Exception e) {
                    log.error("download failed:{},error:{}", dstUrl, ExceptionUtils.getStackTrace(e));
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


    public void download(String baseUrl, File parentDir) throws Exception {
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        SimpleHttpClient client = new SimpleHttpClient();
        String html = client.doGet(baseUrl, null);
        Document doc = Jsoup.parse(html, "UTF-8");
        Elements tdEle = doc.getElementsByClass("link");
        if (!tdEle.isEmpty()) {
            tdEle.iterator().forEachRemaining(td -> {
                if (td.children().size() == 1) {
                    Element hrefEle = td.child(0);
                    String href = hrefEle.attr("href").trim();
                    downloadUrl(href, baseUrl, parentDir);
                }
            });
        }
    }

    public static void main(String[] args) throws Exception {
        String baseUrl = "https://mirrors.aliyun.com/alpine/v3.11/releases/aarch64/netboot/dtbs-lts/";
        File parentDir = new File("dtbs-lts");
        //AliyunDownloader aliyunDownloader = new AliyunDownloader();
        //aliyunDownloader.download(baseUrl, parentDir);

    }
}
