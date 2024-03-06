package com.cbz.universityforumsystem.service;

import com.cbz.universityforumsystem.config.MinIOConfigProperties;
import com.cbz.universityforumsystem.dto.UploadFileDto;
import com.cbz.universityforumsystem.exception.BaseException;
import com.cbz.universityforumsystem.exception.UploadFileException;
import com.cbz.universityforumsystem.utils.Result;
import com.cbz.universityforumsystem.utils.UserContext;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 文件存储服务
 */
@Service
@Slf4j
public class FileService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinIOConfigProperties minIOConfigProperties;

    private static final String separator = "/";

    /**
     * @param filename yyyy/mm/dd/file.jpg
     * @return 文件路径
     */
    private String builderFilePath(String filename) {
        StringBuilder stringBuilder = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String todayStr = sdf.format(new Date());
        stringBuilder.append(todayStr).append(separator);
        stringBuilder.append(filename);
        return stringBuilder.toString();
    }

    /**
     * 上传图片文件
     *
     * @param filename    文件名
     * @param inputStream 文件输入流
     * @return 文件访问全路径
     */
    public String uploadImageFile(String filename, InputStream inputStream) {
        String filePath = builderFilePath(filename);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType("image/jpg")
                    .bucket(minIOConfigProperties.getBucket())
                    .stream(inputStream, inputStream.available(), -1)
                    .build();
            //上传
            minioClient.putObject(putObjectArgs);
            //返回全路径
            return minIOConfigProperties.getReadPath() + separator + minIOConfigProperties.getBucket() +
                    separator + filePath;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new UploadFileException();
        }
    }

    public Result uploadImage(UploadFileDto fileDto) {
        Long uploadUser = fileDto.getUploadUser();
        Long user = UserContext.getUser();
        if (!uploadUser.equals(user)) {
            throw new BaseException("请求非法!");
        }
        //上传文件
        MultipartFile file = fileDto.getFile();
        String filename = file.getOriginalFilename();
        filename = UUID.randomUUID() + filename
                .substring(filename.lastIndexOf("."));
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            throw new UploadFileException();
        }
        String fileUrl = uploadImageFile(filename, inputStream);

        return Result.success(fileUrl);
    }
}
