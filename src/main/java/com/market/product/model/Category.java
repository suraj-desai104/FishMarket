package com.market.product.model;


import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;


@Entity
@Table(name = "categories")

public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true)
    private String slug;

    private String description;
    private String imageUri;
    private boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    
    

    public Category() {
		super();
		// TODO Auto-generated constructor stub
	}

    
    
	public Category(Long id, String name, String slug, String description, String imageUri, boolean isActive,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.name = name;
		this.slug = slug;
		this.description = description;
		this.imageUri = imageUri;
		this.isActive = isActive;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}



	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getSlug() {
		return slug;
	}



	public void setSlug(String slug) {
		this.slug = slug;
	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public String getImageUri() {
		return imageUri;
	}



	public void setImageUri(String imageUri) {
		this.imageUri = imageUri;
	}



	public boolean isActive() {
		return isActive;
	}



	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}



	public LocalDateTime getCreatedAt() {
		return createdAt;
	}



	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}



	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}



	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}



	// Auto timestamps
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // ✅ Generate slug if missing
        if (this.slug == null || this.slug.isBlank()) {
            this.slug = generateSlug(this.name);
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ✅ Utility method to generate slug safely
    private String generateSlug(String name) {
        if (name == null) return null;
        String baseSlug = name.trim().toLowerCase().replaceAll("\\s+", "-");
        // Add a short random suffix to ensure uniqueness
        return baseSlug + "-" + UUID.randomUUID().toString().substring(0, 5);
    }
}
