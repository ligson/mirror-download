package org.ligson.mirrordownload;

import org.ligson.mirrordownload.config.YamlParser;
import org.ligson.mirrordownload.config.vo.AppConfig;
import org.ligson.mirrordownload.job.JobManager;

public class AppBoot {
    public static void main(String[] args) throws Exception {
        AppConfig appCfg = YamlParser.parse();
        JobManager jobManager = new JobManager(appCfg);
        jobManager.run();
    }
}
