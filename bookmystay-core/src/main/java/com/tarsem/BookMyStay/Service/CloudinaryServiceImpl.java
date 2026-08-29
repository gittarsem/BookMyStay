package com.tarsem.BookMyStay.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tarsem.BookMyStay.Service.Interfaces.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) throws IOException {

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.emptyMap()
        );

        return uploadResult.get("secure_url").toString();
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files) throws IOException {

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            imageUrls.add(uploadImage(file));
        }

        return imageUrls;
    }

    @Override
    public void deleteImage(String imageUrl) throws IOException {

        String publicId = extractPublicId(imageUrl);

        cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.emptyMap()
        );
    }

    private String extractPublicId(String imageUrl) {

        String[] parts = imageUrl.split("/");

        String filename = parts[parts.length - 1];

        return filename.substring(0, filename.lastIndexOf("."));
    }
}