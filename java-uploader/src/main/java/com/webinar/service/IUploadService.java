package com.webinar.service;

import com.webinar.entity.dto.FileChunkDTO;
import com.webinar.entity.dto.FileChunkResultDTO;

import java.io.IOException;
/**
 * @ProjectName IUploadService
 * @author Webinar
 * @version 1.0.0
 * @Description 附件分片上传
 * @createTime 2023/3/29 0013 15:59
 */
public interface IUploadService {

    /**
     * 检查文件是否存在，如果存在则跳过该文件的上传，如果不存在，返回需要上传的分片集合
     * @param chunkDTO
     * @return
     */
    FileChunkResultDTO checkChunkExist(FileChunkDTO chunkDTO);


    /**
     * 上传文件分片
     * @param chunkDTO
     */
    void uploadChunk(FileChunkDTO chunkDTO) throws IOException;


    /**
     * 合并文件分片
     * @param identifier
     * @param fileName
     * @param totalChunks
     * @return
     * @throws IOException
     */
    boolean mergeChunk(String identifier,String fileName,Integer totalChunks)throws IOException;
}

