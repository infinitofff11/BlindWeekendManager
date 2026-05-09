package com.blindweekend.manager.common;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页响应数据
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页码 */
    private long pageNum;

    /** 每页大小 */
    private long pageSize;

    /** 总记录数 */
    private long total;

    /** 总页数 */
    private long pages;

    /** 当前页数据列表 */
    private List<T> list;

    public PageResult() {}

    public PageResult(long pageNum, long pageSize, long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = (total + pageSize - 1) / pageSize;
        this.list = list;
    }

    /**
     * 从PageInfo构建
     */
    public static <T> PageResult<T> of(com.github.pagehelper.PageInfo<T> pageInfo) {
        return new PageResult<>(
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getTotal(),
                pageInfo.getList()
        );
    }
}
