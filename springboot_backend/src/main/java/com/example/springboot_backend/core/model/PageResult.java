package com.example.springboot_backend.core.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PageResult<T> {
  private List<T> records;

  @JsonProperty("total_num")
  private long totalNum;

  @JsonProperty("page_size")
  private long pageSize;

  @JsonProperty("current_page")
  private long currentPage;

  @JsonProperty("total_pages")
  private long totalPages;

  public static <T> PageResult<T> of(Page<T> page) {
    PageResult<T> result = new PageResult<>();
    result.records = page.getRecords();
    result.totalNum = page.getTotal();
    result.pageSize = page.getSize();
    result.currentPage = page.getCurrent();
    result.totalPages = page.getPages();
    return result;
  }

  public List<T> getRecords() {
    return records;
  }

  public void setRecords(List<T> records) {
    this.records = records;
  }

  public long getTotalNum() {
    return totalNum;
  }

  public void setTotalNum(long totalNum) {
    this.totalNum = totalNum;
  }

  public long getPageSize() {
    return pageSize;
  }

  public void setPageSize(long pageSize) {
    this.pageSize = pageSize;
  }

  public long getCurrentPage() {
    return currentPage;
  }

  public void setCurrentPage(long currentPage) {
    this.currentPage = currentPage;
  }

  public long getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(long totalPages) {
    this.totalPages = totalPages;
  }
}
