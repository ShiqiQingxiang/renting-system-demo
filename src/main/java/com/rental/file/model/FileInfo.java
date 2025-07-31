package com.rental.file.model;

import com.rental.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 文件信息实体类
 */
@Entity
@Table(name = "file_info", indexes = {
    @Index(name = "idx_uploader", columnList = "uploader_id"),
    @Index(name = "idx_category", columnList = "category_id"),
    @Index(name = "idx_hash", columnList = "file_hash"),
    @Index(name = "idx_entity", columnList = "related_entity_type, related_entity_id"),
    @Index(name = "idx_path", columnList = "file_path"),
    @Index(name = "idx_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false, length = 255)
    @NotBlank(message = "原始文件名不能为空")
    @Size(max = 255, message = "原始文件名长度不能超过255")
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 255)
    @NotBlank(message = "存储文件名不能为空")
    @Size(max = 255, message = "存储文件名长度不能超过255")
    private String storedName;

    @Column(name = "file_path", nullable = false, length = 500)
    @NotBlank(message = "文件路径不能为空")
    @Size(max = 500, message = "文件路径长度不能超过500")
    private String filePath;

    @Column(name = "file_size", nullable = false)
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 100)
    @NotBlank(message = "文件类型不能为空")
    @Size(max = 100, message = "文件类型长度不能超过100")
    private String contentType;

    @Column(name = "file_extension", length = 10)
    @Size(max = 10, message = "文件扩展名长度不能超过10")
    private String fileExtension;

    @Column(name = "file_hash", length = 64)
    @Size(max = 64, message = "文件哈希值长度不能超过64")
    private String fileHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FileCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @Column(name = "related_entity_type", length = 50)
    @Size(max = 50, message = "关联实体类型长度不能超过50")
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "access_level", length = 20)
    @Size(max = 20, message = "访问级别长度不能超过20")
    private String accessLevel = "PRIVATE";

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 便捷方法：检查是否为图片文件
    public boolean isImageFile() {
        return contentType != null && contentType.startsWith("image/");
    }

    // 便捷方法：检查是否为文档文件
    public boolean isDocumentFile() {
        return contentType != null && (
            contentType.equals("application/pdf") ||
            contentType.contains("word") ||
            contentType.contains("excel") ||
            contentType.contains("powerpoint") ||
            contentType.equals("text/plain")
        );
    }

    // 便捷方法：获取文件大小的友好显示
    public String getFileSizeDisplay() {
        if (fileSize == null) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB"};
        double size = fileSize.doubleValue();
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }

    // 便捷方法：检查用户是否有访问权限
    public boolean hasAccessPermission(Long userId, boolean isAdmin) {
        if (isAdmin) return true;
        if ("PUBLIC".equals(accessLevel)) return true;
        return uploader != null && uploader.getId().equals(userId);
    }
}