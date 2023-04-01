package com.webinar.service.impl;

import com.webinar.entity.dto.FileChunkDTO;
import com.webinar.entity.dto.FileChunkResultDTO;
import com.webinar.service.IUploadService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;


/**
 * @ProjectName UploadServiceImpl
 * @author Webinar
 * @version 1.0.0
 * @Description 附件分片上传
 * @createTime 2023/3/29 0013 15:59
 */
@Service
@SuppressWarnings("all")
public class UploadServiceImpl implements IUploadService {

    private Logger logger = LoggerFactory.getLogger(UploadServiceImpl.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${uploadFolder}")
    private String uploadFolder;

    /**
     * 检查文件是否存在，如果存在则跳过该文件的上传，如果不存在，返回需要上传的分片集合
     *
     * @param chunkDTO
     * @return
     */
    @Override
    public FileChunkResultDTO checkChunkExist(FileChunkDTO chunkDTO) {
        //1.检查文件是否已上传过
        //1.1)检查在磁盘中是否存在
        String fileFolderPath = getFileFolderPath(chunkDTO.getIdentifier());
        logger.info("fileFolderPath-->{}", fileFolderPath);
        String filePath = getFilePath(chunkDTO.getIdentifier(), chunkDTO.getFilename());
        File file = new File(filePath);
        boolean exists = file.exists();
        //1.2)检查Redis中是否存在,并且所有分片已经上传完成。
        Set<Integer> uploaded = (Set<Integer>) redisTemplate.opsForHash().get(chunkDTO.getIdentifier(), "uploaded");
        if (uploaded != null && uploaded.size() == chunkDTO.getTotalChunks() && exists) {
            return new FileChunkResultDTO(true);
        }
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            boolean mkdirs = fileFolder.mkdirs();
            logger.info("准备工作,创建文件夹,fileFolderPath:{},mkdirs:{}", fileFolderPath, mkdirs);
        }
        // 断点续传，返回已上传的分片
        return new FileChunkResultDTO(false, uploaded);
    }


    /**
     * 上传分片
     *
     * @param chunkDTO
     */
    @Override
    public void uploadChunk(FileChunkDTO chunkDTO) {
        //分块的目录
        String chunkFileFolderPath = getChunkFileFolderPath(chunkDTO.getIdentifier());
        logger.info("分块的目录 -> {}", chunkFileFolderPath);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            boolean mkdirs = chunkFileFolder.mkdirs();
            logger.info("创建分片文件夹:{}", mkdirs);
        }
        //写入分片
        try (
                InputStream inputStream = chunkDTO.getFile().getInputStream();
                FileOutputStream outputStream = new FileOutputStream(new File(chunkFileFolderPath + chunkDTO.getChunkNumber()))
        ) {
            IOUtils.copy(inputStream, outputStream);
            logger.info("文件标识:{},chunkNumber:{}", chunkDTO.getIdentifier(), chunkDTO.getChunkNumber());
            //将该分片写入redis
            long size = saveToRedis(chunkDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean mergeChunk(String identifier, String fileName, Integer totalChunks) throws IOException {
        return mergeChunks(identifier, fileName, totalChunks);
    }

    /**
     * 合并分片
     *
     * @param identifier
     * @param filename
     */
    private boolean mergeChunks(String identifier, String filename, Integer totalChunks) {
        String chunkFileFolderPath = getChunkFileFolderPath(identifier);
        String filePath = getFilePath(identifier, filename);
        // 检查分片是否都存在
        if (checkChunks(chunkFileFolderPath, totalChunks)) {
            File chunkFileFolder = new File(chunkFileFolderPath);
            File mergeFile = new File(filePath);
            File[] chunks = chunkFileFolder.listFiles();
            //排序
            List fileList = Arrays.asList(chunks);
            Collections.sort(fileList, (Comparator<File>) (o1, o2) -> {
                return Integer.parseInt(o1.getName()) - (Integer.parseInt(o2.getName()));
            });
            try {
                RandomAccessFile randomAccessFileWriter = new RandomAccessFile(mergeFile, "rw");
                byte[] bytes = new byte[1024];
                for (File chunk : chunks) {
                    RandomAccessFile randomAccessFileReader = new RandomAccessFile(chunk, "r");
                    int len;
                    while ((len = randomAccessFileReader.read(bytes)) != -1) {
                        randomAccessFileWriter.write(bytes, 0, len);
                    }
                    randomAccessFileReader.close();
                }
                randomAccessFileWriter.close();
                //合并成功后需要删除分片
                String[] split = chunkFileFolderPath.split("/");
                String deletePath = uploadFolder +split[split.length-4];
                FileSystemUtils.deleteRecursively(new File(deletePath));
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 检查分片是否都存在
     * @param chunkFileFolderPath
     * @param totalChunks
     * @return
     */
    private boolean checkChunks(String chunkFileFolderPath, Integer totalChunks) {
        try {
            for (int i = 1; i <= totalChunks + 1; i++) {
                File file = new File(chunkFileFolderPath + File.separator + i);
                if (file.exists()) {
                    continue;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 分片写入Redis
     *
     * @param chunkDTO
     */
    private synchronized long saveToRedis(FileChunkDTO chunkDTO) {
        Set<Integer> uploaded = (Set<Integer>) redisTemplate.opsForHash().get(chunkDTO.getIdentifier(), "uploaded");
        if (uploaded == null) {
            uploaded = new HashSet<>(Arrays.asList(chunkDTO.getChunkNumber()));
            HashMap<String, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("uploaded", uploaded);
            objectObjectHashMap.put("totalChunks", chunkDTO.getTotalChunks());
            objectObjectHashMap.put("totalSize", chunkDTO.getTotalSize());
//            objectObjectHashMap.put("path", getFileRelativelyPath(chunkDTO.getIdentifier(), chunkDTO.getFilename()));
            objectObjectHashMap.put("path", chunkDTO.getFilename());
            redisTemplate.opsForHash().putAll(chunkDTO.getIdentifier(), objectObjectHashMap);
        } else {
            uploaded.add(chunkDTO.getChunkNumber());
            redisTemplate.opsForHash().put(chunkDTO.getIdentifier(), "uploaded", uploaded);
        }
        return uploaded.size();
    }

    /**
     * 得到文件的绝对路径
     *
     * @param identifier
     * @param filename
     * @return
     */
    private String getFilePath(String identifier, String filename) {
        String ext = filename.substring(filename.lastIndexOf("."));
//        return getFileFolderPath(identifier) + identifier + ext;
        return uploadFolder + filename;
    }

    /**
     * 得到文件的相对路径
     *
     * @param identifier
     * @param filename
     * @return
     */
    private String getFileRelativelyPath(String identifier, String filename) {
        String ext = filename.substring(filename.lastIndexOf("."));
        return "/" + identifier.substring(0, 1) + "/" +
                identifier.substring(1, 2) + "/" +
                identifier + "/" + identifier
                + ext;
    }


    /**
     * 得到分块文件所属的目录
     *
     * @param identifier
     * @return
     */
    private String getChunkFileFolderPath(String identifier) {
        return getFileFolderPath(identifier) + "chunks" + File.separator;
    }

    /**
     * 得到文件所属的目录
     *
     * @param identifier
     * @return
     */
    private String getFileFolderPath(String identifier) {
        return uploadFolder + identifier.substring(0, 1) + File.separator +
                identifier.substring(1, 2) + File.separator +
                identifier + File.separator;
//        return uploadFolder;
    }
}

