package com.rental.file.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件工具类
 */
@Component
@Slf4j
public class FileUtil {
    
    private static final Tika tika = new Tika();
    
    /**
     * 生成唯一的存储文件名
     * 格式：日期_时间戳_UUID.扩展名
     * 
     * @param originalName 原始文件名
     * @return 存储文件名
     */
    public static String generateStoredName(String originalName) {
        String extension = getFileExtension(originalName);
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        if (extension.isEmpty()) {
            return String.format("%s_%s_%s", datePrefix, timestamp, uuid);
        } else {
            return String.format("%s_%s_%s.%s", datePrefix, timestamp, uuid, extension);
        }
    }
    
    /**
     * 根据分类代码生成存储路径
     * 格式：分类路径/年/月/
     * 
     * @param categoryCode 分类代码
     * @return 存储路径
     */
    public static String generateStoragePath(String categoryCode) {
        LocalDate now = LocalDate.now();
        String categoryPath = getCategoryPath(categoryCode);
        
        return String.format("%s%d/%02d/", 
            categoryPath, 
            now.getYear(), 
            now.getMonthValue());
    }
    
    /**
     * 根据分类代码获取分类路径
     * 
     * @param categoryCode 分类代码
     * @return 分类路径
     */
    public static String getCategoryPath(String categoryCode) {
        if (categoryCode == null) {
            return "documents/";
        }
        
        return switch (categoryCode.toUpperCase()) {
            case "AVATAR" -> "avatars/";
            case "ITEM_IMAGE" -> "items/";
            case "CONTRACT" -> "contracts/";
            case "DOCUMENT" -> "documents/";
            default -> "others/";
        };
    }
    
    /**
     * 计算文件的MD5哈希值
     * 
     * @param file 文件
     * @return MD5哈希值
     * @throws IOException IO异常
     */
    public static String calculateMD5(MultipartFile file) throws IOException {
        return DigestUtils.md5Hex(file.getInputStream());
    }
    
    /**
     * 检测文件的真实MIME类型
     * 
     * @param file 文件
     * @return MIME类型
     * @throws IOException IO异常
     */
    public static String detectContentType(MultipartFile file) throws IOException {
        return tika.detect(file.getInputStream(), file.getOriginalFilename());
    }
    
    /**
     * 获取文件扩展名（不带点）
     * 
     * @param filename 文件名
     * @return 扩展名
     */
    public static String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        return FilenameUtils.getExtension(filename).toLowerCase();
    }
    
    /**
     * 检查是否为图片文件
     * 
     * @param contentType MIME类型
     * @return 是否为图片
     */
    public static boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
    
    /**
     * 检查是否为文档文件
     * 
     * @param contentType MIME类型
     * @return 是否为文档
     */
    public static boolean isDocumentFile(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        return contentType.equals("application/pdf") ||
               contentType.contains("word") ||
               contentType.contains("excel") ||
               contentType.contains("powerpoint") ||
               contentType.equals("text/plain") ||
               contentType.contains("spreadsheet") ||
               contentType.contains("presentation");
    }
    
    /**
     * 格式化文件大小为友好显示
     * 
     * @param fileSize 文件大小（字节）
     * @return 友好显示的文件大小
     */
    public static String formatFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        double size = fileSize.doubleValue();
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        if (unitIndex == 0) {
            return String.format("%.0f %s", size, units[unitIndex]);
        } else {
            return String.format("%.1f %s", size, units[unitIndex]);
        }
    }
    
    /**
     * 生成下载URL
     * 
     * @param fileId 文件ID
     * @return 下载URL
     */
    public static String generateDownloadUrl(Long fileId) {
        return String.format("/api/files/download/%d", fileId);
    }
    
    /**
     * 生成预览URL
     * 
     * @param fileId 文件ID
     * @return 预览URL
     */
    public static String generatePreviewUrl(Long fileId) {
        return String.format("/api/files/preview/%d", fileId);
    }
    
    /**
     * 检查文件名是否安全
     * 
     * @param filename 文件名
     * @return 是否安全
     */
    public static boolean isSafeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否包含危险字符
        String[] dangerousChars = {"..", "/", "\\", ":", "*", "?", "\"", "<", ">", "|"};
        for (String dangerousChar : dangerousChars) {
            if (filename.contains(dangerousChar)) {
                return false;
            }
        }
        
        // 检查是否为系统保留文件名（Windows）
        String[] reservedNames = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", 
                                 "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", 
                                 "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
        
        String nameWithoutExt = FilenameUtils.getBaseName(filename).toUpperCase();
        for (String reservedName : reservedNames) {
            if (reservedName.equals(nameWithoutExt)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 清理文件名，移除危险字符
     * 
     * @param filename 原始文件名
     * @return 清理后的文件名
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unnamed";
        }
        
        // 替换危险字符
        String cleaned = filename.replaceAll("[^a-zA-Z0-9._\\-\\u4e00-\\u9fa5]", "_");
        
        // 限制长度（保留扩展名）
        String extension = FilenameUtils.getExtension(cleaned);
        String baseName = FilenameUtils.getBaseName(cleaned);
        
        if (baseName.length() > 100) {
            baseName = baseName.substring(0, 100);
        }
        
        if (extension.isEmpty()) {
            return baseName;
        } else {
            return baseName + "." + extension;
        }
    }
} 