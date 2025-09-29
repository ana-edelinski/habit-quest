package com.example.habitquest.domain.model;

public class Category {
    private String id;
    private  Long userId;
    private String name;
    private String colorHex;
    private Long createdAt;
    private Long updatedAt;

    public Category() {}

    public Category(String id,Long userId, String name, String colorHex, Long createdAt, Long updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.colorHex = colorHex;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Category{" +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", colorHex='" + colorHex + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
