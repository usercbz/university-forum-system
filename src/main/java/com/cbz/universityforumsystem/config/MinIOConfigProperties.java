package com.cbz.universityforumsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "university-forum.minio")
public class MinIOConfigProperties {
    private String accessKey;
    private String secretKey;

    private String bucket;
    private String endpoint;
    private String readPath;
}
