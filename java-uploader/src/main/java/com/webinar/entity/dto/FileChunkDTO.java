package com.webinar.entity.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ProjectName FileChunkDTO
 * @author webinar
 * @version 1.0.0
 * @Description 附件分片上传
 * @createTime 2022/4/13 0013 15:59
 */
@Data
public class FileChunkDTO {
    /**
     * 文件md5
     */
    private String identifier;
    /**
     * 分块文件
     */
    MultipartFile file;
    /**
     * 当前分块序号
     */
    private Integer chunkNumber;
    /**
     * 分块大小
     */
    private Long chunkSize;
    /**
     * 当前分块大小
     */
    private Long currentChunkSize;
    /**
     * 文件总大小
     */
    private Long totalSize;
    /**
     * 分块总数
     */
    private Integer totalChunks;
    /**
     * 文件名
     */
    private String filename;

}
