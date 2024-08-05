package com.ddang.usedauction.image.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.domain.ImageType;
import com.ddang.usedauction.image.exception.ImageErrorCode;
import com.ddang.usedauction.image.exception.ImageException;
import com.ddang.usedauction.image.repository.ImageRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final AmazonS3Client amazonS3Client;

    private static final String IMAGE_NAME = "imageName";
    private static final String IMAGE_URL = "imageUrl";

    @Value("${S3_BUCKET_NAME}")
    private String bucket;

    /**
     * 대표 이미지 업로드
     *
     * @param image 업로드할 이미지
     */
    public Image uploadThumbnail(MultipartFile image) {

        Map<String, String> fileNameAndImageUrlMap = getFileNameAndImageUrl(image);

        return Image.builder()
            .imageName(fileNameAndImageUrlMap.get(IMAGE_NAME))
            .imageType(ImageType.THUMBNAIL)
            .imageUrl(fileNameAndImageUrlMap.get(IMAGE_URL))
            .build();
    }

    // s3에 저장하고 저장된 이미지 이름과 url을 map으로 반환
    private Map<String, String> getFileNameAndImageUrl(MultipartFile image) {

        Map<String, String> map = new HashMap<>();

        String fileName = UUID.randomUUID()
            + image.getOriginalFilename(); // s3에 저장할 이미지 이름 uuid를 사용해 중복 이름을 방지
        String imageUrl = uploadToS3(image, fileName); // s3에 업로드

        map.put(IMAGE_NAME, fileName);
        map.put(IMAGE_URL, imageUrl);

        return map;
    }

    // s3에 이미지를 저장
    private String uploadToS3(MultipartFile image, String imageName) {

        try (InputStream inputStream = image.getInputStream()) {
            amazonS3Client.putObject(
                new PutObjectRequest(bucket, imageName, inputStream, null).withCannedAcl(
                    CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new ImageException(ImageErrorCode.FAIL_UPLOAD_IMAGE);
        }

        return amazonS3Client.getUrl(bucket, imageName).toString();
    }
}
