package org.ligson.mirrordownload.config.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Job {
    private String cron;
    private int limit;
    private int retry;

}
