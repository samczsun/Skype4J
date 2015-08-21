package com.samczsun.skype4j.chat;

/**
 * Created by RMYakimenko on 20.08.2015.
 */
public class FileInfo
{
    private final String originalName;
    private final Long fileSize;
    private final Long tId;
    private final boolean cancelled;

    public FileInfo(String originalName, Long fileSize, Long tId, boolean cancelled)
    {
        this.originalName = originalName;
        this.fileSize = fileSize;
        this.tId = tId;
        this.cancelled = cancelled;
    }

    public String getOriginalName() {return originalName;}
    public Long getFileSize() {return fileSize;}
    public Long getTId() {return tId;}
    public boolean Cancelled() {return cancelled;}
}
