package com.tarsem.BookMyStay.Service.Interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CloudinaryService {

    String uploadImage(MultipartFile file) throws IOException;

    List<String> uploadImages(List<MultipartFile> files) throws IOException;

    void deleteImage(String imageUrl) throws IOException;
}