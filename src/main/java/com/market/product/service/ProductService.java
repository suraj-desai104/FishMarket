package com.market.product.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.market.product.exception.BadRequestException;
import com.market.product.model.Category;
import com.market.product.model.Product;
import com.market.product.repository.CategoryRepository;
import com.market.product.repository.ProductRepository;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    
    
    public long getProductCount() {
        return productRepository.count();
    }

    // ✅ Create Product
    public Product createProduct(Product product) {
        if (productRepository.existsByName(product.getName())) {
            throw new BadRequestException("Product name already exists!");
        }

        // Generate unique slug
        String slug = generateUniqueSlug(product.getName());
        product.setSlug(slug);

        // Validate category
        if (product.getCategory() != null) {
            Optional<Category> categoryOpt = categoryRepository.findById(product.getCategory().getId());
            if (categoryOpt.isEmpty()) {
                throw new BadRequestException("Invalid category ID!");
            }
            product.setCategory(categoryOpt.get());
        }

        return productRepository.save(product);
    }

    // ✅ Get All Products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ✅ Get Product by Slug
    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new BadRequestException("Product not found with slug: " + slug));
    }

    // ✅ Update Product
    public Product updateProduct(String slug, Product updatedProduct) {
        Product existingProduct = getProductBySlug(slug);

        // If name changed, revalidate uniqueness and regenerate slug
        if (!existingProduct.getName().equalsIgnoreCase(updatedProduct.getName())) {
            if (productRepository.existsByName(updatedProduct.getName())) {
                throw new BadRequestException("Product name already exists!");
            }
            existingProduct.setSlug(generateUniqueSlug(updatedProduct.getName()));
        }

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setDiscountedPrice(updatedProduct.getDiscountedPrice());
        existingProduct.setImageUri(updatedProduct.getImageUri());
        existingProduct.setAvailable(updatedProduct.isAvailable());

        // Update category if provided
        if (updatedProduct.getCategory() != null) {
            Optional<Category> categoryOpt = categoryRepository.findById(updatedProduct.getCategory().getId());
            if (categoryOpt.isEmpty()) {
                throw new BadRequestException("Invalid category ID!");
            }
            existingProduct.setCategory(categoryOpt.get());
        }

        return productRepository.save(existingProduct);
    }

    // ✅ Delete Product
    public void deleteProduct(String slug) {
        Product product = getProductBySlug(slug);
        productRepository.delete(product);
    }

    // ✅ Helper: Generate Unique Slug
    private String generateUniqueSlug(String name) {
        String baseSlug = generateSlug(name);
        String slug = baseSlug;
        int suffix = 1;

        while (productRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + suffix++;
        }

        return slug;
    }

    // ✅ Helper: Generate Slug from Product Name
    private String generateSlug(String input) {
        String slug = Normalizer.normalize(input, Normalizer.Form.NFD);
        slug = slug.replaceAll("[^\\w\\s-]", "")
                   .trim()
                   .replaceAll("\\s+", "-")
                   .toLowerCase();
        slug = Pattern.compile("-+").matcher(slug).replaceAll("-");
        return slug;
    }
}
