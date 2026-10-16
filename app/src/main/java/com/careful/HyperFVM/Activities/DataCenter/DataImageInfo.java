package com.careful.HyperFVM.Activities.DataCenter;

import java.io.Serial;
import java.io.Serializable;

public class DataImageInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String fileName;
    private long version;
    private String url;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
