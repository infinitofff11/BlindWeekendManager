package com.blindweekend.manager.controller;

import com.blindweekend.manager.common.PageResult;
import com.blindweekend.manager.common.Result;
import com.blindweekend.manager.entity.CheckIn;
import com.blindweekend.manager.entity.Review;
import com.blindweekend.manager.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 评价管理接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 分页查询打卡记录
     */
    @GetMapping("/checkins")
    public Result<PageResult<CheckIn>> listCheckIns(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reviewService.queryCheckIns(pageNum, pageSize));
    }

    /**
     * 分页查询评价列表
     */
    @GetMapping
    public Result<PageResult<Review>> listReviews(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(reviewService.queryReviews(pageNum, pageSize, status));
    }

    /**
     * 屏蔽/恢复评价
     */
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id,
                                      @RequestParam Integer status) {
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("状态值只能为0或1");
        }
        reviewService.toggleReviewStatus(id, status);
        String msg = status == 1 ? "已恢复" : "已屏蔽";
        return Result.success(msg, null);
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return Result.success("删除成功", null);
    }
}
