package org.ligson.mirrordownload.config.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class App {
    private String name;
    private Job job;
    private List<Mirror> mirrors;

    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public Job getJob() {
        return job;
    }

    @JsonProperty("job")
    public void setJob(Job job) {
        this.job = job;
    }

    public List<Mirror> getMirrors() {
        return mirrors;
    }

    @JsonProperty("mirrors")
    public void setMirrors(List<Mirror> mirrors) {
        this.mirrors = mirrors;
    }
}
