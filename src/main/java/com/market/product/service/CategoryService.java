package com.market.product.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.market.product.exception.BadRequestException;
import com.market.product.model.Category;
import com.market.product.repository.CategoryRepository;


import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.regions.Region;   // ✅ correct import

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Value("${supabase.s3.endpoint}")
    private String endpoint;

    @Value("${supabase.s3.region}")
    private String region;

    @Value("${supabase.s3.accessKey}")
    private String accessKey;

    @Value("${supabase.s3.secretKey}")
    private String secretKey;

    @Value("${supabase.s3.bucket}")
    private String bucketName;

    public String uploadToSupabaseS3(MultipartFile file) {

        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

            S3Client s3 = S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .forcePathStyle(true)   // required for Supabase
                    .build();

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3.putObject(request, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

            // Supabase public URL
            return endpoint.replace("/storage/v1/s3", "")
                    + "/storage/v1/object/public/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("S3 Upload Error: " + e.getMessage());
        }
    }



    // ✅ Create new category (auto-generate slug)
    public Category createCategory(Category category) {

        // 1️⃣ Check if name already exists
        if (categoryRepository.existsByName(category.getName())) {
            throw new BadRequestException("Category name already exists: " + category.getName());
        }

        // 2️⃣ Generate slug from name
        String baseSlug = generateSlug(category.getName());
        String slug = baseSlug;

        // 3️⃣ Ensure slug uniqueness
        int count = 0;
        while (categoryRepository.existsBySlug(slug)) {
            count++;
            slug = baseSlug + "-" + count;
        }

        category.setSlug(slug);

        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Duplicate entry found. Please use a different category name or slug.");
        }
    }

    // ✅ Get all categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ✅ Get category by ID
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Category not found with id: " + id));
    }

    // ✅ Get category by slug
    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new BadRequestException("Category not found with slug: " + slug));
    }

    // ✅ Update category
    public Category updateCategory(Long id, Category updatedCategory) {
        Category category = getCategoryById(id);

        // Check duplicate name (if changed)
        if (!category.getName().equalsIgnoreCase(updatedCategory.getName())
                && categoryRepository.existsByName(updatedCategory.getName())) {
            throw new BadRequestException("Category name already exists: " + updatedCategory.getName());
        }

        category.setName(updatedCategory.getName());
        category.setDescription(updatedCategory.getDescription());
        category.setImageUri(updatedCategory.getImageUri());
        category.setActive(updatedCategory.isActive());

        // ✅ Update slug when name changes
        String newSlug = generateSlug(updatedCategory.getName());
        if (!category.getSlug().equals(newSlug)) {
            String uniqueSlug = newSlug;
            int count = 0;
            while (categoryRepository.existsBySlug(uniqueSlug)) {
                count++;
                uniqueSlug = newSlug + "-" + count;
            }
            category.setSlug(uniqueSlug);
        }

        return categoryRepository.save(category);
    }

    // ✅ Delete category
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new BadRequestException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    // 🔧 Utility method for slug generation
    private String generateSlug(String name) {
        return name.trim().toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "");
    }
}
