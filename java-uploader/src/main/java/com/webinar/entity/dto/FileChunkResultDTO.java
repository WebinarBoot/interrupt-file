package com.webinar.entity.dto;


import lombok.Data;

import java.util.Set;

/**
 * @ProjectName FileChunkResultDTO
 * @author webinar
 * @version 1.0.0
 * @Description 附件分片上传
 * @createTime 2022/3/29 0013 15:59
 */
@Data
public class FileChunkResultDTO {
    /**
     * 是否跳过上传
     */
    private Boolean skipUpload;

    /**
     * 已上传分片的集合
     */
    private Set<Integer> uploaded;

    public FileChunkResultDTO(Boolean skipUpload, Set<Integer> uploaded) {
        this.skipUpload = skipUpload;
        this.uploaded = uploaded;
    }

    public FileChunkResultDTO(Boolean skipUpload) {
        this.skipUpload = skipUpload;
    }
}
