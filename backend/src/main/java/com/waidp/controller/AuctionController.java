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
 * 提供拍卖活动的所有接口，包括：
 * 拍卖列表查询（支持分页、筛选、我的拍卖）、拍卖详情查询、
 * 拍卖创建与删除（资产专员权限）、竞拍出价（一键出价、手动出价）、
 * 撤价功能、成交确认（中标者）、竞拍记录查询等。
 */
@RestController
@RequestMapping("/api/auction")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    /**
     * 鍒嗛〉鏌ヨ鎷嶅崠
     */
    @GetMapping
    public Result<PageResult<AuctionDTO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean myAuctions,
            @RequestParam(required = false) String confirmStatus,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        if (myAuctions != null && myAuctions) {
            // 鏌ヨ鎴戠殑鎷嶅崠
            Long userId = (Long) request.getAttribute("userId");
            List<AuctionDTO> myAuctionDTOs = auctionService.getMyAuctionsDTO(userId, status, confirmStatus);

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
     * 鏍规嵁ID鑾峰彇鎷嶅崠
     */
    @GetMapping("/{id}")
    public Result<AuctionDTO> getById(@PathVariable Long id) {
        return Result.success(auctionService.getAuctionDTOById(id));
    }

    /**
     * 鍒涘缓鎷嶅崠锛堣祫浜т笓鍛橈級
     */
    @PostMapping
    public Result<Void> create(@RequestBody Auction auction) {
        auctionService.createAuction(auction);
        return Result.success("鍒涘缓鎴愬姛", null);
    }

    /**
     * 鍒犻櫎鎷嶅崠锛堣祫浜т笓鍛橈紝鍙兘鍒犻櫎鏈紑濮嬬殑鎷嶅崠锛?
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        auctionService.deleteAuction(id);
        return Result.success("鍒犻櫎鎴愬姛", null);
    }

    /**
     * 鍙備笌绔炰环
     */
    @PostMapping("/{id}/bid")
    public Result<Void> bid(@PathVariable Long id, @Valid @RequestBody BidRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        auctionService.bid(id, userId, request.price());
        return Result.success("绔炰环鎴愬姛", null);
    }

    /**
     * 涓€閿嚭浠?
     */
    @PostMapping("/{id}/quick-bid")
    public Result<Void> quickBid(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        auctionService.quickBid(id, userId);
        return Result.success("绔炰环鎴愬姛", null);
    }

    /**
     * 鎾ゅ洖褰撳墠鏈€楂樺嚭浠凤紙缁撴潫鍓?12 灏忔椂澶栵級
     */
    @PostMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        auctionService.withdrawHighestBid(id, userId);
        return Result.success("鎾ゅ洖鎴愬姛", null);
    }

    /**
     * 纭鎴愪氦锛堜腑鏍囪€咃級
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        auctionService.confirmTransaction(id, userId);
        return Result.success("纭鎴愬姛", null);
    }

    /**
     * 鑾峰彇鎷嶅崠鐨勭珵浠疯褰?
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
 * 绔炰环璇锋眰
 */
record BidRequest(@NotNull(message = "鍑轰环閲戦涓嶈兘涓虹┖") BigDecimal price) {
}

/**
 * 绔炰环灞曠ず
 */
record BidView(Long id, Long bidderId, String bidderName, BigDecimal price, LocalDateTime bidTime, Boolean isHighest) {
}
