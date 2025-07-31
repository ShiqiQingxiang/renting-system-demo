package com.rental.file.controller;

import com.rental.common.response.ApiResponse;
import com.rental.common.response.PageResponse;
import com.rental.file.DTO.FileBatchOperationRequest;
import com.rental.file.DTO.FileInfoDTO;
import com.rental.file.DTO.FileSearchRequest;
import com.rental.file.service.FileService;
import com.rental.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件管理控制器 - 处理文件搜索、批量操作等管理功能
 */
@RestController
@RequestMapping("/api/files/manage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "文件管理", description = "文件搜索、批量操作等管理功能")
@SecurityRequirement(name = "bearerAuth")
public class FileManagementController {
    
    private final FileService fileService;
    
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('FILE_MANAGE')")
    @Operation(
        summary = "文件搜索",
        description = "根据多种条件搜索文件，支持分页排序"
    )
    public ApiResponse<PageResponse<FileInfoDTO>> searchFiles(
            @Parameter(description = "文件名（模糊匹配）")
            @RequestParam(required = false) String fileName,
            
            @Parameter(description = "文件分类ID")
            @RequestParam(required = false) Long categoryId,
            
            @Parameter(description = "上传者ID")
            @RequestParam(required = false) Long uploaderId,
            
            @Parameter(description = "关联实体类型")
            @RequestParam(required = false) String entityType,
            
            @Parameter(description = "关联实体ID")
            @RequestParam(required = false) Long entityId,
            
            @Parameter(description = "访问级别")
            @RequestParam(required = false) String accessLevel,
            
            @Parameter(description = "文件类型")
            @RequestParam(required = false) String contentType,
            
            @Parameter(description = "只显示图片文件")
            @RequestParam(required = false, defaultValue = "false") Boolean imageOnly,
            
            @Parameter(description = "只显示文档文件")
            @RequestParam(required = false, defaultValue = "false") Boolean documentOnly,
            
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "排序字段", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "排序方向", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // 构建搜索请求
        FileSearchRequest searchRequest = new FileSearchRequest();
        searchRequest.setFileName(fileName);
        searchRequest.setCategoryId(categoryId);
        searchRequest.setUploaderId(uploaderId);
        searchRequest.setRelatedEntityType(entityType);
        searchRequest.setRelatedEntityId(entityId);
        searchRequest.setAccessLevel(accessLevel);
        searchRequest.setContentType(contentType);
        searchRequest.setImageOnly(imageOnly);
        searchRequest.setDocumentOnly(documentOnly);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        
        // 构建分页排序参数
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        PageResponse<FileInfoDTO> result = fileService.searchFiles(searchRequest, pageable, userDetails.getUserId());

        log.info("用户 {} 执行文件搜索，条件: fileName={}, categoryId={}, 返回 {} 条记录", 
            userDetails.getUsername(), fileName, categoryId, result.getContent().size());
        
        return ApiResponse.success("搜索成功", result);
    }
    
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('FILE_MANAGE')")
    @Operation(
        summary = "批量文件操作",
        description = "支持批量删除、移动、更新访问级别等操作"
    )
    public ApiResponse<Void> batchOperateFiles(
            @Parameter(description = "批量操作请求", required = true)
            @Valid @RequestBody FileBatchOperationRequest request,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        fileService.batchOperateFiles(request, userDetails.getUserId());

        log.info("用户 {} 执行批量操作: {} 对 {} 个文件", 
            userDetails.getUsername(), request.getOperation(), request.getFileIds().size());
        
        return ApiResponse.success();
    }
    
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('FILE_DELETE')")
    @Operation(
        summary = "批量删除文件",
        description = "批量删除指定的文件列表"
    )
    public ApiResponse<Void> batchDeleteFiles(
            @Parameter(description = "文件ID列表", required = true)
            @RequestBody List<Long> fileIds,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        fileService.deleteFiles(fileIds, userDetails.getUserId());

        log.info("用户 {} 批量删除 {} 个文件", userDetails.getUsername(), fileIds.size());
        return ApiResponse.success();
    }
    
    @GetMapping("/my")
    @Operation(
        summary = "获取我的文件",
        description = "获取当前用户上传的所有文件"
    )
    public ApiResponse<PageResponse<FileInfoDTO>> getMyFiles(
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "排序字段", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "排序方向", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        FileSearchRequest searchRequest = new FileSearchRequest();
        searchRequest.setUploaderId(userDetails.getUserId());
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        PageResponse<FileInfoDTO> result = fileService.searchFiles(searchRequest, pageable, userDetails.getUserId());
        return ApiResponse.success("获取成功", result);
    }
    
    @GetMapping("/recent")
    @Operation(
        summary = "获取最近文件",
        description = "获取最近上传的文件列表"
    )
    public ApiResponse<PageResponse<FileInfoDTO>> getRecentFiles(
            @Parameter(description = "返回数量", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        FileSearchRequest searchRequest = new FileSearchRequest();
        searchRequest.setSortBy("createdAt");
        searchRequest.setSortDirection("desc");
        
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        PageResponse<FileInfoDTO> result = fileService.searchFiles(searchRequest, pageable, userDetails.getUserId());
        return ApiResponse.success("获取成功", result);
    }
    
    @GetMapping("/images")
    @Operation(
        summary = "获取图片文件",
        description = "获取所有图片类型的文件"
    )
    public ApiResponse<PageResponse<FileInfoDTO>> getImageFiles(
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        FileSearchRequest searchRequest = new FileSearchRequest();
        searchRequest.setImageOnly(true);
        searchRequest.setSortBy("createdAt");
        searchRequest.setSortDirection("desc");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        PageResponse<FileInfoDTO> result = fileService.searchFiles(searchRequest, pageable, userDetails.getUserId());
        return ApiResponse.success("获取成功", result);
    }
    
    @GetMapping("/documents")
    @Operation(
        summary = "获取文档文件",
        description = "获取所有文档类型的文件"
    )
    public ApiResponse<PageResponse<FileInfoDTO>> getDocumentFiles(
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size,
            
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        FileSearchRequest searchRequest = new FileSearchRequest();
        searchRequest.setDocumentOnly(true);
        searchRequest.setSortBy("createdAt");
        searchRequest.setSortDirection("desc");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        PageResponse<FileInfoDTO> result = fileService.searchFiles(searchRequest, pageable, userDetails.getUserId());
        return ApiResponse.success("获取成功", result);
    }
    
    @PostMapping("/cleanup")
    @PreAuthorize("hasAuthority('FILE_VIEW_ALL')")
    @Operation(
        summary = "清理无效文件",
        description = "清理数据库中存在但物理文件不存在的记录（管理员功能）"
    )
    public ApiResponse<Integer> cleanupInvalidFiles(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        int cleanedCount = fileService.cleanupInvalidFiles();
        
        log.info("管理员 {} 执行文件清理，清理了 {} 个无效记录", userDetails.getUsername(), cleanedCount);
        return ApiResponse.success("清理完成", cleanedCount);
    }
    
    @GetMapping("/count")
    @Operation(
        summary = "获取文件数量统计",
        description = "获取当前用户的文件数量统计信息"
    )
    public ApiResponse<Object> getFileCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        var statistics = fileService.getUserFileStatistics(userDetails.getUserId());
        return ApiResponse.success("获取成功", statistics);
    }
}
