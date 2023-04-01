package com.webinar.controller;

import com.webinar.common.RestApiResponse;
import com.webinar.entity.dto.FileChunkDTO;
import com.webinar.entity.dto.FileChunkResultDTO;
import com.webinar.service.IUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@CrossOrigin
@RestController
@RequestMapping("upload")
public class UploaderController {

    @Autowired
    private IUploadService uploadService;

    @GetMapping("hello")
    public String hello() {
        return "hello!";
    }

    /**
     * 检查分片是否存在
     *
     * @return
     */
    @GetMapping("chunk")
    public RestApiResponse<Object> checkChunkExist(FileChunkDTO chunkDTO) {
        FileChunkResultDTO fileChunkCheckDTO;
        try {
            fileChunkCheckDTO = uploadService.checkChunkExist(chunkDTO);
            return RestApiResponse.success(fileChunkCheckDTO);
        } catch (Exception e) {
            return RestApiResponse.error(e.getMessage());
        }
    }


    /**
     * 上传文件分片
     *
     * @param chunkDTO
     * @return
     */
    @PostMapping("chunk")
    public RestApiResponse<Object> uploadChunk(FileChunkDTO chunkDTO) {
        try {
            uploadService.uploadChunk(chunkDTO);
            return RestApiResponse.success(chunkDTO.getIdentifier());
        } catch (Exception e) {
            return RestApiResponse.error(e.getMessage());
        }
    }

    /**
     * 请求合并文件分片
     *
     * @param chunkDTO
     * @return
     */
    @PostMapping("merge")
    public RestApiResponse<Object> mergeChunks(@RequestBody FileChunkDTO chunkDTO) {
        try {
            boolean success = uploadService.mergeChunk(chunkDTO.getIdentifier(), chunkDTO.getFilename(), chunkDTO.getTotalChunks());
            return RestApiResponse.flag(success);
        } catch (Exception e) {
            return RestApiResponse.error(e.getMessage());
        }
    }

}
