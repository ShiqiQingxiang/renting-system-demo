package com.rental.file.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 文件分类实体类
 */
@Entity
@Table(name = "file_category", indexes = {
    @Index(name = "idx_code", columnList = "code"),
    @Index(name = "idx_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50")
    private String name;

    @Column(nullable = false, length = 20, unique = true)
    @NotBlank(message = "分类代码不能为空")
    @Size(max = 20, message = "分类代码长度不能超过20")
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "allowed_extensions", columnDefinition = "TEXT")
    private String allowedExtensions;

    @Column(name = "max_file_size")
    private Long maxFileSize = 10485760L; // 默认10MB

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 便捷方法：获取允许的扩展名数组
    public String[] getAllowedExtensionsArray() {
        if (allowedExtensions == null || allowedExtensions.trim().isEmpty()) {
            return new String[0];
        }
        return allowedExtensions.split(",");
    }

    // 便捷方法：检查扩展名是否被允许
    public boolean isExtensionAllowed(String extension) {
        if (allowedExtensions == null || extension == null) {
            return false;
        }
        String[] allowed = getAllowedExtensionsArray();
        for (String ext : allowed) {
            if (ext.trim().equalsIgnoreCase(extension.trim())) {
                return true;
            }
        }
        return false;
    }
}