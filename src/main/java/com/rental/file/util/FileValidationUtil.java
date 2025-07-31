package com.rental.file.util;

import com.rental.common.exception.BusinessException;
import com.rental.file.model.FileCategory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 文件验证工具类
 */
@Component
@Slf4j
public class FileValidationUtil {
    
    private static final Tika tika = new Tika();
    
    // 常见MIME类型映射
    private static final Map<String, List<String>> MIME_TYPE_MAP = Map.of(
        "jpg", List.of("image/jpeg", "image/jpg"),
        "jpeg", List.of("image/jpeg", "image/jpg"),
        "png", List.of("image/png"),
        "gif", List.of("image/gif"),
        "pdf", List.of("application/pdf"),
        "doc", List.of("application/msword"),
        "docx", List.of(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-word.document.macroenabled.12"
        ),
        "xls", List.of("application/vnd.ms-excel"),
        "xlsx", List.of(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel.sheet.macroenabled.12"
        ),
        "txt", List.of("text/plain")
    );
    
    // 危险文件扩展名
    private static final String[] DANGEROUS_EXTENSIONS = {
        "exe", "bat", "cmd", "com", "pif", "scr", "vbs", "js", "jar",
        "jsp", "php", "asp", "aspx", "sh", "bash", "ps1", "msi"
    };
    
    /**
     * 验证上传文件
     * 
     * @param file 上传的文件
     * @param category 文件分类（可为null）
     * @throws IOException IO异常
     * @throws BusinessException 业务异常
     */
    public static void validateFile(MultipartFile file, FileCategory category) throws IOException {
        // 1. 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }
        
        // 2. 检查文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        
        if (!FileUtil.isSafeFilename(originalFilename)) {
            throw new BusinessException("文件名包含不安全字符: " + originalFilename);
        }
        
        // 3. 检查文件大小
        long fileSize = file.getSize();
        if (fileSize <= 0) {
            throw new BusinessException("文件大小无效");
        }
        
        long maxSize = category != null ? category.getMaxFileSize() : 20 * 1024 * 1024; // 默认20MB
        if (fileSize > maxSize) {
            throw new BusinessException(
                String.format("文件大小超过限制，当前: %s，限制: %s",
                    FileUtils.byteCountToDisplaySize(fileSize),
                    FileUtils.byteCountToDisplaySize(maxSize))
            );
        }
        
        // 4. 检查文件扩展名
        String extension = FileUtil.getFileExtension(originalFilename);
        if (extension.isEmpty()) {
            throw new BusinessException("文件必须有扩展名");
        }
        
        if (isDangerousExtension(extension)) {
            throw new BusinessException("不允许上传此类型的文件: " + extension);
        }
        
        if (category != null && !category.isExtensionAllowed(extension)) {
            throw new BusinessException(
                String.format("文件类型不被此分类允许，当前类型: %s，允许类型: %s",
                    extension, category.getAllowedExtensions())
            );
        }
        
        // 5. 检查真实文件类型
        String detectedContentType = tika.detect(file.getInputStream(), originalFilename);
        if (!isValidMimeType(detectedContentType, extension)) {
            throw new BusinessException(
                String.format("文件类型验证失败，文件扩展名: %s，检测到的类型: %s",
                    extension, detectedContentType)
            );
        }
        
        // 6. 安全检查
        if (!isSecureFile(file)) {
            throw new BusinessException("检测到不安全的文件内容");
        }
        
        log.debug("文件验证通过: {} ({})", originalFilename, FileUtils.byteCountToDisplaySize(fileSize));
    }
    
    /**
     * 检查扩展名是否在允许列表中
     * 
     * @param extension 文件扩展名
     * @param allowedExtensions 允许的扩展名（逗号分隔）
     * @return 是否允许
     */
    public static boolean isAllowedExtension(String extension, String allowedExtensions) {
        if (allowedExtensions == null || allowedExtensions.trim().isEmpty()) {
            return true; // 如果没有限制，则允许所有类型
        }
        
        String[] allowed = allowedExtensions.split(",");
        return Arrays.stream(allowed)
            .map(String::trim)
            .map(String::toLowerCase)
            .anyMatch(ext -> ext.equals(extension.toLowerCase()));
    }
    
    /**
     * 验证MIME类型是否与扩展名匹配
     * 
     * @param mimeType 检测到的MIME类型
     * @param extension 文件扩展名
     * @return 是否匹配
     */
    public static boolean isValidMimeType(String mimeType, String extension) {
        if (mimeType == null || extension == null) {
            return false;
        }
        
        // 获取该扩展名对应的有效MIME类型列表
        List<String> validMimeTypes = MIME_TYPE_MAP.get(extension.toLowerCase());
        if (validMimeTypes == null) {
            // 如果没有预定义的MIME类型，进行宽松检查
            return isLooseMimeTypeMatch(mimeType, extension);
        }
        
        return validMimeTypes.contains(mimeType.toLowerCase());
    }
    
    /**
     * 宽松的MIME类型匹配（用于未预定义的扩展名）
     * 
     * @param mimeType MIME类型
     * @param extension 扩展名
     * @return 是否匹配
     */
    private static boolean isLooseMimeTypeMatch(String mimeType, String extension) {
        // 图片类型检查
        if (Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(extension.toLowerCase())) {
            return mimeType.startsWith("image/");
        }
        
        // 文档类型检查
        if (Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx").contains(extension.toLowerCase())) {
            return mimeType.equals("application/pdf") ||
                   mimeType.contains("word") ||
                   mimeType.contains("excel") ||
                   mimeType.contains("powerpoint") ||
                   mimeType.contains("spreadsheet") ||
                   mimeType.contains("presentation");
        }
        
        // 文本类型检查
        if (Arrays.asList("txt", "csv").contains(extension.toLowerCase())) {
            return mimeType.startsWith("text/");
        }
        
        return true; // 对于其他类型，采用宽松策略
    }
    
    /**
     * 检查是否为危险文件扩展名
     * 
     * @param extension 文件扩展名
     * @return 是否为危险扩展名
     */
    public static boolean isDangerousExtension(String extension) {
        if (extension == null) {
            return false;
        }
        
        return Arrays.stream(DANGEROUS_EXTENSIONS)
            .anyMatch(dangerous -> dangerous.equalsIgnoreCase(extension));
    }
    
    /**
     * 检查文件是否安全
     * 
     * @param file 文件
     * @return 是否安全
     */
    public static boolean isSecureFile(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null) {
                return false;
            }
            
            // 检查文件名安全性
            if (!FileUtil.isSafeFilename(filename)) {
                return false;
            }
            
            // 检查扩展名安全性
            String extension = FileUtil.getFileExtension(filename);
            if (isDangerousExtension(extension)) {
                return false;
            }
            
            // 检查文件内容（简单的字节检查）
            byte[] header = new byte[Math.min(1024, (int) file.getSize())];
            int bytesRead = file.getInputStream().read(header);
            
            if (bytesRead > 0) {
                // 检查是否包含可执行文件的特征字节
                if (containsExecutableSignature(header)) {
                    return false;
                }
            }
            
            return true;
            
        } catch (IOException e) {
            log.error("文件安全检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查字节数组是否包含可执行文件签名
     * 
     * @param bytes 字节数组
     * @return 是否包含可执行文件签名
     */
    private static boolean containsExecutableSignature(byte[] bytes) {
        if (bytes.length < 2) {
            return false;
        }
        
        // 检查常见的可执行文件头
        // PE文件 (Windows .exe, .dll)
        if (bytes.length >= 2 && bytes[0] == 0x4D && bytes[1] == 0x5A) { // MZ
            return true;
        }
        
        // ELF文件 (Linux可执行文件)
        if (bytes.length >= 4 && 
            bytes[0] == 0x7F && bytes[1] == 0x45 && bytes[2] == 0x4C && bytes[3] == 0x46) { // .ELF
            return true;
        }
        
        // Mach-O文件 (macOS可执行文件)
        if (bytes.length >= 4 && 
            ((bytes[0] == (byte)0xFE && bytes[1] == (byte)0xED && bytes[2] == (byte)0xFA && bytes[3] == (byte)0xCE) ||
             (bytes[0] == (byte)0xFE && bytes[1] == (byte)0xED && bytes[2] == (byte)0xFA && bytes[3] == (byte)0xCF))) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 验证文件上传批量操作请求
     * 
     * @param files 文件数组
     * @param maxCount 最大文件数量
     * @param maxTotalSize 最大总大小
     */
    public static void validateBatchUpload(MultipartFile[] files, int maxCount, long maxTotalSize) {
        if (files == null || files.length == 0) {
            throw new BusinessException("没有选择要上传的文件");
        }
        
        if (files.length > maxCount) {
            throw new BusinessException(
                String.format("一次最多只能上传 %d 个文件，当前选择了 %d 个", maxCount, files.length)
            );
        }
        
        long totalSize = Arrays.stream(files)
            .mapToLong(MultipartFile::getSize)
            .sum();
        
        if (totalSize > maxTotalSize) {
            throw new BusinessException(
                String.format("文件总大小超过限制，当前: %s，限制: %s",
                    FileUtils.byteCountToDisplaySize(totalSize),
                    FileUtils.byteCountToDisplaySize(maxTotalSize))
            );
        }
    }
} 