package ar.uba.fi.ingsoft1.sistema_comedores.image;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ar.uba.fi.ingsoft1.sistema_comedores.image.exception.ImageDeletionException;
import ar.uba.fi.ingsoft1.sistema_comedores.image.exception.ImageUploadException;
import ar.uba.fi.ingsoft1.sistema_comedores.image.exception.InvalidImageException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp",
        "image/gif"
    );
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final MinioClient minioClient;
    private final String bucket;
    private final String minioUrl;

    public ImageService(
            MinioClient minioClient,
            @Qualifier("defaultBucketName") String bucket,
            @Qualifier("minioPublicUrl") String minioUrl) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        this.minioUrl = minioUrl;
    }

    /**
     * Upload an image and return a public URL (no expiry).
     * @param file the image file to upload
     * @param objectPrefix optional path prefix within the bucket (e.g., "profiles", "products")
     * @return public URL of the uploaded image
     * @throws InvalidImageException if file validation fails
     * @throws ImageUploadException if upload fails
     */
    public String uploadImage(MultipartFile file, String objectPrefix) {
        validateImage(file);
        
        try {
            ensureBucketExists();
            
            String objectName = buildObjectName(file.getOriginalFilename(), objectPrefix);
            
            uploadToMinio(file.getInputStream(), file.getSize(), file.getContentType(), objectName);
            
            return buildPublicUrl(objectName);
            
        } catch (InvalidImageException e) {
            throw e;
        } catch (ErrorResponseException e) {
            log.error("MinIO error while uploading image: {}", e.getMessage(), e);
            throw new ImageUploadException(e);
        } catch (Exception e) {
            log.error("Unexpected error while uploading image", e);
            throw new ImageUploadException(e);
        }
    }

    /**
     * Upload an image from InputStream (for data loaders, migrations, etc.)
     * @param inputStream the image data stream
     * @param fileName original file name (used for extension detection)
     * @param contentType MIME type of the image
     * @param size size in bytes
     * @param objectPrefix optional path prefix within the bucket
     * @return public URL of the uploaded image
     * * @throws InvalidImageException if file validation fails
     * @throws ImageUploadException if upload fails
     */
    public String uploadImage(InputStream inputStream, String fileName, String contentType, long size, String objectPrefix) {
        validateImageMetadata(contentType, size);
        
        try {
            ensureBucketExists();
            
            String objectName = buildObjectName(fileName, objectPrefix);
            
            uploadToMinio(inputStream, size, contentType, objectName);
            
            return buildPublicUrl(objectName);
            
        } catch (InvalidImageException e) {
            throw e;
        } catch (ErrorResponseException e) {
            log.error("MinIO error while uploading image: {}", e.getMessage(), e);
            throw new ImageUploadException(e);
        } catch (Exception e) {
            log.error("Unexpected error while uploading image", e);
            throw new ImageUploadException(e);
        }
    }

    /**
     * Upload an image and return a public URL (no expiry).
     * Uses default root path.
     */
    public void deleteImage(String url) {
        if (url == null || url.isEmpty()) {
            throw new InvalidImageException("Image URL cannot be null or empty");
        }

        String objectName = extractObjectNameFromUrl(url);
        
        if (objectName == null || objectName.isEmpty()) {
            throw new InvalidImageException("Invalid image URL format: " + url);
        }

        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build()
            );
            log.info("Successfully deleted image: {}", objectName);
            
        } catch (Exception e) {
            log.error("Error deleting image: {}", objectName, e);
            throw new ImageDeletionException("Failed to delete image: " + e.getMessage(), e);
        }
    }

    // Private helper methods
    
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("Image file cannot be empty");
        }
        
        validateImageMetadata(file.getContentType(), file.getSize());
    }
    
    private void validateImageMetadata(String contentType, long size) {
        if (size > MAX_FILE_SIZE) {
            throw new InvalidImageException(
                String.format("Image size exceeds maximum allowed size of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }
        
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidImageException(
                "Invalid image format. Allowed formats: JPEG, PNG, WebP, GIF"
            );
        }
    }
    
    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
            BucketExistsArgs.builder().bucket(bucket).build()
        );
        
        if (!exists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucket).build()
            );
            log.info("Created bucket: {}", bucket);
        }
    }
    
    private String buildObjectName(String originalFileName, String objectPrefix) {
        String fileName = originalFileName != null ? originalFileName : "file";
        String uniqueFileName = UUID.randomUUID() + "-" + fileName;
        
        if (objectPrefix != null && !objectPrefix.isEmpty()) {
            String normalizedPath = objectPrefix.replaceAll("^/+|/+$", "");
            return normalizedPath + "/" + uniqueFileName;
        }
        
        return uniqueFileName;
    }
    
    private void uploadToMinio(InputStream inputStream, long size, String contentType, String objectName) throws Exception {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .stream(inputStream, size, -1)
                .contentType(contentType)
                .build()
        );
        log.info("Successfully uploaded image: {}", objectName);
    }
    
    private String buildPublicUrl(String objectName) {
        return minioUrl + "/" + bucket + "/" + objectName;
    }

    /**
     * Extract object name from a public URL.
     */
    private String extractObjectNameFromUrl(String url) {
        try {
            // Remove the base URL part
            String baseWithBucket = minioUrl + "/" + bucket + "/";
            if (url.startsWith(baseWithBucket)) {
                return url.substring(baseWithBucket.length());
            }
            
            // Fallback: try to extract from any URL containing /bucket/
            String pattern = "/" + bucket + "/";
            int index = url.indexOf(pattern);
            if (index != -1) {
                return url.substring(index + pattern.length());
            }
        } catch (Exception e) {
            log.warn("Failed to extract object name from URL: {}", url, e);
            return null;
        }
        return null;
    }
}