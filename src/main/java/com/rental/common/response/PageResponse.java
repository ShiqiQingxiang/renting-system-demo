package com.rental.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 分页响应格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int currentPage;        // 当前页码
    private int pageSize;          // 每页大小
    private long totalElements;    // 总元素数
    private int totalPages;        // 总页数
    private boolean hasNext;       // 是否有下一页
    private boolean hasPrevious;   // 是否有上一页
    private boolean first;         // 是否第一页
    private boolean last;          // 是否最后一页
    private boolean empty;         // 是否为空

    // 兼容旧的字段名
    public int getPage() {
        return currentPage;
    }

    public void setPage(int page) {
        this.currentPage = page;
    }

    public int getSize() {
        return pageSize;
    }

    public void setSize(int size) {
        this.pageSize = size;
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return PageResponse.<T>builder()
                .content(content)
                .currentPage(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .empty(content == null || content.isEmpty())
                .build();
    }
}
