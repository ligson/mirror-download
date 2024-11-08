package org.ligson.mirrordownload.http;

import lombok.extern.slf4j.Slf4j;

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

        // 创建HttpClient
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMinutes(3)).build();

        // 创建HttpRequest以获取远程文件大小
        HttpRequest headRequest = HttpRequest.newBuilder(new URI(url))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<Void> headResponse = client.send(headRequest, HttpResponse.BodyHandlers.discarding());

        // 检查响应状态
        if (headResponse.statusCode() != 200) {
            log.error("无法获取远程文件大小，状态码：{}", headResponse.statusCode());
            throw new Exception("获取远程文件大小失败，状态码：" + headResponse.statusCode());
        }

        // 获取远程文件大小
        long remoteFileSize = headResponse.headers().firstValue("Content-Length").map(Long::parseLong).orElse(0L);
        long existingFileSize = dest.exists() ? dest.length() : 0;

        // 如果本地文件存在并且文件大小相等，直接返回
        if (dest.exists() && existingFileSize >= remoteFileSize) {
            log.debug("本地文件已是最新版本，无需下载。");
            return;
        }

        // 构建请求，添加Range头
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(new URI(url))
                .GET();

        // 如果本地文件小于远程文件，添加Range头实现断点续传
        if (existingFileSize < remoteFileSize) {
            requestBuilder.header("Range", "bytes=" + existingFileSize + "-");
        }

        // 如果headers不为空，则添加到请求头
        if (headers != null) {
            headers.getHeaders().forEach(requestBuilder::header);
        }

        // 构建请求
        HttpRequest request = requestBuilder.build();

        // 发送请求并处理响应
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(Path.of(dest.getPath()),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND));

        if (response.statusCode() == 200 || response.statusCode() == 206) {
            log.debug("从url：{}下载文件:{}成功,耗时:{}s", url, dest.getAbsolutePath(), (System.currentTimeMillis() - startTime) / 1000.0);
        } else {
            log.error("从url：{}下载文件:{}失败,状态码：{}", url, dest.getAbsolutePath(), response.statusCode());
            throw new Exception("下载失败，状态码：" + response.statusCode());
        }
    }

}

