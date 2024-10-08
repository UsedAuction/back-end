package com.ddang.usedauction.image.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.domain.ImageType;
import com.ddang.usedauction.image.exception.ImageDeleteFailException;
import com.ddang.usedauction.image.exception.ImageUploadFailException;
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

    /**
     * 이미지 리스트를 s3에 업로드
     *
     * @param multipartFileList 업로드할 이미지 리스트
     * @return Image 엔티티로 변환된 리스트
     */
    public List<Image> uploadImageList(List<MultipartFile> multipartFileList) {

        List<Image> imageList = new ArrayList<>();

        multipartFileList.forEach(mf -> {
            Map<String, String> fileNameAndImageUrlMap = getFileNameAndImageUrl(mf);

            Image image = Image.builder()
                .imageName(fileNameAndImageUrlMap.get(IMAGE_NAME))
                .imageType(ImageType.NORMAL)
                .imageUrl(fileNameAndImageUrlMap.get(IMAGE_URL))
                .build();

            imageList.add(image);
        });

        return imageList;
    }

    /**
     * s3에 저장된 이미지 삭제
     *
     * @param fileName 이미지 파일 이름
     */
    public void deleteImage(String fileName) {

        try {
            amazonS3Client.deleteObject(bucket, fileName);
        } catch (Exception e) {
            throw new ImageDeleteFailException();
        }

        Image image = imageRepository.findByImageName(fileName)
            .orElseThrow(() -> new NullPointerException("존재하지 않는 이미지입니다."));

        image = image.toBuilder()
            .deletedAt(LocalDateTime.now())
            .build();

        imageRepository.save(image);
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
            throw new ImageUploadFailException();
        }

        return amazonS3Client.getUrl(bucket, imageName).toString();
    }
}
