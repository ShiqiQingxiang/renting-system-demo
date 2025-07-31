package com.rental.file.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量文件操作请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileBatchOperationRequest {
    
    /**
     * 文件ID列表
     */
    @NotEmpty(message = "文件ID列表不能为空")
    private List<Long> fileIds;
    
    /**
     * 操作类型（DELETE, MOVE, UPDATE_ACCESS_LEVEL等）
     */
    @NotNull(message = "操作类型不能为空")
    private String operation;
    
    /**
     * 目标分类ID（用于移动操作）
     */
    private Long targetCategoryId;
    
    /**
     * 新的访问级别（用于访问级别更新操作）
     */
    private String newAccessLevel;
    
    /**
     * 新的关联实体类型（用于关联更新操作）
     */
    private String newRelatedEntityType;
    
    /**
     * 新的关联实体ID（用于关联更新操作）
     */
    private Long newRelatedEntityId;
    
    /**
     * 操作说明
     */
    private String reason;
    
    /**
     * 操作类型枚举
     */
    public enum OperationType {
        DELETE("删除"),
        MOVE("移动"),
        UPDATE_ACCESS_LEVEL("更新访问级别"),
        UPDATE_RELATION("更新关联");
        
        private final String description;
        
        OperationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}