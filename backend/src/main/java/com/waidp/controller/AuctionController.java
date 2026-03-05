package com.waidp.controller;

import com.waidp.common.PageResult;
import com.waidp.common.Result;
import com.waidp.dto.AuctionDTO;
import com.waidp.entity.Auction;
import com.waidp.service.AuctionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拍卖控制器
 */
@RestController
@RequestMapping("/api/auction")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    /**
     * 分页查询拍卖
     */
    @GetMapping
    public Result<PageResult<AuctionDTO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean myAuctions,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        if (myAuctions != null && myAuctions) {
            // 查询我的拍卖
            Long userId = (Long) request.getAttribute("userId");
            List<AuctionDTO> myAuctionDTOs = auctionService.getMyAuctionsDTO(userId, status);

            int total = myAuctionDTOs == null ? 0 : myAuctionDTOs.size();
            int fromIndex = Math.max(0, (page - 1) * pageSize);
            int toIndex = Math.min(total, fromIndex + pageSize);
            List<AuctionDTO> pageList = (total == 0 || fromIndex >= total)
                    ? java.util.Collections.emptyList()
                    : myAuctionDTOs.subList(fromIndex, toIndex);

            PageResult<AuctionDTO> result = new PageResult<>(pageList, (long) total);
            return Result.success(result);
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<AuctionDTO> auctionPage = auctionService.getAuctionsDTO(name, status, pageable);
        return Result.success(PageResult.of(auctionPage));
    }

    /**
     * 根据ID获取拍卖
     */
    @GetMapping("/{id}")
    public Result<AuctionDTO> getById(@PathVariable Long id) {
        return Result.success(auctionService.getAuctionDTOById(id));
    }

    /**
     * 创建拍卖（资产专员）
     */
    @PostMapping
    public Result<Void> create(@RequestBody Auction auction) {
        auctionService.createAuction(auction);
        return Result.success("创建成功", null);
    }

    /**
     * 删除拍卖（资产专员，只能删除未开始的拍卖）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        auctionService.deleteAuction(id);
        return Result.success("删除成功", null);
    }

    /**
     * 参与竞价
     */
    @PostMapping("/{id}/bid")
    public Result<Void> bid(@PathVariable Long id, @Valid @RequestBody BidRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        auctionService.bid(id, userId, request.price());
        return Result.success("竞价成功", null);
    }

    /**
     * 一键出价
     */
    @PostMapping("/{id}/quick-bid")
    public Result<Void> quickBid(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        auctionService.quickBid(id, userId);
        return Result.success("竞价成功", null);
    }

    /**
     * 确认成交（中标者）
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        auctionService.confirmTransaction(id, userId);
        return Result.success("确认成功", null);
    }

    /**
     * 获取拍卖的竞价记录
     */
    @GetMapping("/{id}/bids")
    public Result<List<BidView>> getBids(@PathVariable Long id) {
        List<com.waidp.entity.Bid> bids = auctionService.getBidsByAuctionId(id);

        Long highestId = bids.stream()
                .filter(bid -> bid.getPrice() != null)
                .max(Comparator.comparing(com.waidp.entity.Bid::getPrice, BigDecimal::compareTo)
                        .thenComparing(com.waidp.entity.Bid::getBidTime, Comparator.nullsLast(LocalDateTime::compareTo)))
                .map(com.waidp.entity.Bid::getId)
                .orElse(null);

        List<BidView> views = bids.stream().map(bid -> {
            String bidderName = null;
            try {
                if (bid.getBidder() != null) {
                    bidderName = bid.getBidder().getName();
                }
            } catch (Exception ignored) {
            }
            Boolean isHighest = highestId != null && highestId.equals(bid.getId());
            return new BidView(
                    bid.getId(),
                    bid.getBidderId(),
                    bidderName,
                    bid.getPrice(),
                    bid.getBidTime(),
                    isHighest
            );
        }).collect(Collectors.toList());
        return Result.success(views);
    }
}

/**
 * 竞价请求
 */
record BidRequest(@NotNull(message = "出价金额不能为空") BigDecimal price) {
}

/**
 * 竞价展示
 */
record BidView(Long id, Long bidderId, String bidderName, BigDecimal price, LocalDateTime bidTime, Boolean isHighest) {
}
