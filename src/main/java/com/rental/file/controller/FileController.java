package com.rental.file.controller;

import com.rental.common.response.ApiResponse;
import com.rental.file.DTO.FileUploadResponse;
import com.rental.file.service.FileService;
import com.rental.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文件控制器 - 处理文件上传、下载、预览等核心功能
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "文件管理", description = "文件上传、下载、预览等核心功能")
@SecurityRequirement(name = "bearerAuth")
public class FileController {
    
    private final FileService fileService;
    
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('FILE_UPLOAD')")
    @Operation(
        summary = "文件上传",
        description = "支持单文件和多文件上传，支持文件分类和关联实体"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "上传成功",
        content = @Content(schema = @Schema(implementation = FileUploadResponse.class))
    )
    public ApiResponse<List<FileUploadResponse>> uploadFiles(
            @Parameter(description = "上传的文件列表", required = true)
            @RequestParam("files") MultipartFile[] files,
            
            @Parameter(description = "文件分类ID")
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            
            @Parameter(description = "关联实体类型（ITEM/USER/CONTRACT等）")
            @RequestParam(value = "entityType", required = false) String entityType,
            
            @Parameter(description = "关联实体ID")
            @RequestParam(value = "entityId", required = false) Long entityId,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            List<FileUploadResponse> responses = fileService.uploadFiles(
                files, categoryId, entityType, entityId, userDetails.getUserId());

            log.info("用户 {} 成功上传 {} 个文件", userDetails.getUsername(), files.length);
            return ApiResponse.success("文件上传成功", responses);
            
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/upload/single")
    @PreAuthorize("hasAuthority('FILE_UPLOAD')")
    @Operation(
        summary = "单文件上传",
        description = "上传单个文件"
    )
    public ApiResponse<FileUploadResponse> uploadSingleFile(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "文件分类ID")
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            
            @Parameter(description = "关联实体类型")
            @RequestParam(value = "entityType", required = false) String entityType,
            
            @Parameter(description = "关联实体ID")
            @RequestParam(value = "entityId", required = false) Long entityId,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        FileUploadResponse response = fileService.uploadSingleFile(
            file, categoryId, entityType, entityId, userDetails.getUserId());

        return ApiResponse.success("文件上传成功", response);
    }
    
    @GetMapping("/download/{id}")
    @PreAuthorize("hasAuthority('FILE_DOWNLOAD')")
    @Operation(
        summary = "文件下载",
        description = "下载指定文件，会增加下载计数"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "下载成功",
        content = @Content(mediaType = "application/octet-stream")
    )
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long id,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        try {
            Resource resource = fileService.downloadFile(id, userDetails.getUserId());

            // 获取文件信息用于设置响应头
            var fileInfo = fileService.getFileInfo(id, userDetails.getUserId());

            String encodedFilename = URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                .contentLength(fileInfo.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(resource);
                
        } catch (Exception e) {
            log.error("文件下载失败: fileId={}, error={}", id, e.getMessage());
            throw e;
        }
    }
    
    @GetMapping("/preview/{id}")
    @Operation(
        summary = "文件预览",
        description = "预览文件（主要用于图片），不增加下载计数"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "预览成功",
        content = @Content(mediaType = "image/*")
    )
    public ResponseEntity<Resource> previewFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long id,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Resource resource = fileService.previewFile(id, userDetails.getUserId());
        var fileInfo = fileService.getFileInfo(id, userDetails.getUserId());

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
            .contentLength(fileInfo.getFileSize())
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000") // 缓存1年
            .body(resource);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FILE_DELETE')")
    @Operation(
        summary = "删除文件",
        description = "删除指定文件（逻辑删除）"
    )
    public ApiResponse<Void> deleteFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long id,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        fileService.deleteFile(id, userDetails.getUserId());
        return ApiResponse.success();
    }
    
    @GetMapping("/{id}/info")
    @Operation(
        summary = "获取文件信息",
        description = "获取文件的详细信息"
    )
    public ApiResponse<Object> getFileInfo(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long id,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        var fileInfo = fileService.getFileInfo(id, userDetails.getUserId());
        return ApiResponse.success("获取成功", fileInfo);
    }
    
    @PutMapping("/{id}/access-level")
    @PreAuthorize("hasAuthority('FILE_MANAGE')")
    @Operation(
        summary = "更新文件访问级别",
        description = "更新文件的访问级别（PUBLIC/PRIVATE）"
    )
    public ApiResponse<Void> updateAccessLevel(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "新的访问级别", required = true)
            @RequestParam String accessLevel,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        fileService.updateAccessLevel(id, accessLevel, userDetails.getUserId());
        return ApiResponse.success();
    }
    
    @PutMapping("/{id}/category")
    @PreAuthorize("hasAuthority('FILE_MANAGE')")
    @Operation(
        summary = "移动文件到新分类",
        description = "将文件移动到指定的新分类"
    )
    public ApiResponse<Void> moveFileToCategory(
            @Parameter(description = "文件ID", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "新分类ID", required = true)
            @RequestParam Long categoryId,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        fileService.moveFileToCategory(id, categoryId, userDetails.getUserId());
        return ApiResponse.success();
    }
    
    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(
        summary = "获取关联实体的文件列表",
        description = "获取指定实体关联的所有文件"
    )
    public ApiResponse<Object> getFilesByEntity(
            @Parameter(description = "实体类型", required = true)
            @PathVariable String entityType,
            
            @Parameter(description = "实体ID", required = true)
            @PathVariable Long entityId,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        var files = fileService.getFilesByEntity(entityType, entityId, userDetails.getUserId());
        return ApiResponse.success("获取成功", files);
    }
    
    @GetMapping("/statistics")
    @Operation(
        summary = "获取用户文件统计",
        description = "获取当前用户的文件统计信息"
    )
    public ApiResponse<Object> getUserFileStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        var statistics = fileService.getUserFileStatistics(userDetails.getUserId());
        return ApiResponse.success("获取成功", statistics);
    }
    
    @GetMapping("/popular")
    @Operation(
        summary = "获取热门文件",
        description = "获取下载次数最多的文件列表"
    )
    public ApiResponse<Object> getPopularFiles(
            @Parameter(description = "返回数量限制", required = false)
            @RequestParam(defaultValue = "10") int limit) {
        
        var popularFiles = fileService.getPopularFiles(limit);
        return ApiResponse.success("获取成功", popularFiles);
    }
}
