package com.market.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.market.product.model.Product;
import com.market.product.service.CategoryService;
import com.market.product.service.ProductService;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*") // ✅ Allow frontend requests
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;

    // ✅ Create a new Product
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> createProduct(
            @RequestPart("product") Product product,
            @RequestPart("image") MultipartFile imageFile) {

        // Upload image to Supabase / S3
        String imageUrl = categoryService.uploadToSupabaseS3(imageFile);

        // Save the URL inside product object
        product.setImageUri(imageUrl);

        // Save product in DB
        Product savedProduct = productService.createProduct(product);

        return ResponseEntity.ok(savedProduct);
    }


    // ✅ Get all Products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // ✅ Get Product by slug
    @GetMapping("/{slug}")
    public ResponseEntity<Product> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    // ✅ Update Product by slug
    @PutMapping("/{slug}")
    public ResponseEntity<Product> updateProduct(@PathVariable String slug, @RequestBody Product updatedProduct) {
        Product product = productService.updateProduct(slug, updatedProduct);
        return ResponseEntity.ok(product);
    }

    // ✅ Delete Product by slug
    @DeleteMapping("/{slug}")
    public ResponseEntity<String> deleteProduct(@PathVariable String slug) {
        productService.deleteProduct(slug);
        return ResponseEntity.ok("Product deleted successfully");
    }
    
    @GetMapping("/count")
    public long getProductCount() {
        return productService.getProductCount();
    }

}
