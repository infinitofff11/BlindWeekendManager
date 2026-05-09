package com.blindweekend.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blindweekend.manager.common.BusinessException;
import com.blindweekend.manager.common.PageResult;
import com.blindweekend.manager.entity.CheckIn;
import com.blindweekend.manager.entity.Review;
import com.blindweekend.manager.mapper.CheckInMapper;
import com.blindweekend.manager.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 评价管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final CheckInMapper checkInMapper;

    /**
     * 分页查询打卡记录列表
     */
    public PageResult<CheckIn> queryCheckIns(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CheckIn::getCheckInTime);

        Page<CheckIn> page = new Page<>(pageNum != null ? pageNum : 1,
                                         pageSize != null ? pageSize : 10);
        Page<CheckIn> result = checkInMapper.selectPage(page, wrapper);

        return new PageResult<>(result.getCurrent(), result.getSize(),
                                result.getTotal(), result.getRecords());
    }

    /**
     * 分页查询评价列表
     */
    public PageResult<Review> queryReviews(Integer pageNum, Integer pageSize,
                                           Integer status) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        }

        wrapper.orderByDesc(Review::getCreatedAt);

        Page<Review> page = new Page<>(pageNum != null ? pageNum : 1,
                                       pageSize != null ? pageSize : 10);
        Page<Review> result = reviewMapper.selectPage(page, wrapper);

        return new PageResult<>(result.getCurrent(), result.getSize(),
                                result.getTotal(), result.getRecords());
    }

    /**
     * 屏蔽/恢复评价
     */
    public void toggleReviewStatus(Long id, Integer status) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        review.setStatus(status);
        reviewMapper.updateById(review);
        log.info("更新评价状态: id={}, status={}", id, status);
    }

    /**
     * 删除评价
     */
    public void deleteReview(Long id) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        reviewMapper.deleteById(id);
        log.info("删除评价: id={}", id);
    }
}
