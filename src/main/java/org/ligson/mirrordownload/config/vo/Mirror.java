package org.ligson.mirrordownload.config.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mirror {
    private String name;
    private String url;
    private String type;
    private String dest;
}
