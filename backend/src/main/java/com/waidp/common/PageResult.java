package com.waidp.common;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> {
    private List<T> list;
    private Long total;

    public PageResult() {
    }

    public PageResult(List<T> list, Long total) {
        this.list = list;
        this.total = total;
    }

    /**
     * 从 Spring Data Page 创建 PageResult
     */
    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page.getContent(), page.getTotalElements());
    }

    /**
     * 从 List 和总数创建 PageResult
     */
    public static <T> PageResult<T> of(List<T> list, long total) {
        return new PageResult<>(list, total);
    }
}
