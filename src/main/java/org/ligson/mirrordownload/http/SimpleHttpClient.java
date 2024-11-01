package org.ligson.mirrordownload.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

@Slf4j
public class SimpleHttpClient {
    public static void uploadFile(String url, File file, String... headers) throws Exception {
        long startTime = System.currentTimeMillis();
        log.debug("上传文件:{}到url:{}开始", file.getAbsolutePath(), url);
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMinutes(3)).build();
        client.send(HttpRequest.newBuilder(new URI(url))
                        .POST(HttpRequest.BodyPublishers.ofFile(file.toPath())).headers(headers)
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        log.debug("上传文件:{}到url:{}成功,耗时:{}s", file.getAbsolutePath(), url, (System.currentTimeMillis() - startTime) / 1000.0);
    }

    public String doGet(String url, HttpHeaders headers) throws Exception {
        long startTime = System.currentTimeMillis();
        log.debug("从url：{}获取内容开始", url);
        HttpClient client = HttpClient
                .newBuilder().connectTimeout(Duration.ofMinutes(3)).build();
        HttpRequest.Builder builder = HttpRequest.newBuilder(new URI(url))
                .GET();
        if (headers != null) {
            headers.getHeaders().forEach(builder::header);
        }
        HttpResponse<String> response = client.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());
        log.debug("从url：{}获取内容成功,耗时:{}s", url, (System.currentTimeMillis() - startTime) / 1000.0);
        return response.body();
    }


    public static void download(String url, File dest, HttpHeaders headers) throws Exception {
        long startTime = System.currentTimeMillis();
        System.out.println(url + "......." + dest.getAbsolutePath());
        log.debug("从url：{}下载文件:{}开始", url, dest.getAbsolutePath());

        // 创建父目录
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        // 获取目标文件大小
        long existingFileSize = dest.exists() ? dest.length() : 0;

        // 创建HttpClient
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMinutes(3)).build();

        // 构建请求，添加Range头
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(new URI(url))
                .GET()
                .header("Range", "bytes=" + existingFileSize + "-"); // 请求续传

        // 判断cookie是否为空，如果不为空则添加到请求头
        if (headers != null) {
            headers.getHeaders().forEach(requestBuilder::header);
        }

        // 构建请求
        HttpRequest request = requestBuilder.build();

        // 发送请求并处理响应
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(Path.of(dest.getPath()), StandardOpenOption.CREATE, StandardOpenOption.APPEND));

        // 检查响应状态和Content-Length
        long contentLength = response.headers().firstValue("Content-Length").map(Long::parseLong).orElse(0L);

        if (contentLength == 0) {
            log.debug("文件下载完成，无需下载或目标文件已是最新版本，跳过下载。");
        } else if (contentLength < existingFileSize) {
            // 如果远程文件小于本地文件，则删除本地文件并重新下载
            dest.delete();
            log.debug("远程文件小于现有文件，删除本地文件并重新下载。");
            download(url, dest, headers); // 重新下载
        } else {
            log.debug("从url：{}下载文件:{}成功,耗时:{}s", url, dest.getAbsolutePath(), (System.currentTimeMillis() - startTime) / 1000.0);
        }
    }
}

