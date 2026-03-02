package com.waidp.service.impl;

import com.waidp.dto.*;
import com.waidp.entity.*;
import com.waidp.repository.*;
import com.waidp.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 缁熻鏈嶅姟瀹炵幇
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final AssetRepository assetRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AssetHistoryRepository assetHistoryRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public List<BiddingStatistics> getBiddingStatistics(Long userId, String startDate, String endDate, String period) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }

        LocalDateTime[] range = resolveRange(startDate, endDate, period);
        LocalDateTime startTime = range[0];
        LocalDateTime endTime = range[1];

        List<Bid> userBids = bidRepository.findByBidderIdAndBidTimeRange(userId, startTime, endTime);
        Set<Long> auctionIds = userBids.stream()
                .map(Bid::getAuctionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Auction> auctions = auctionRepository.findAllById(auctionIds);

        // 获取中标记录（以拍卖结束为准）
        List<Auction> wonAuctions = auctions.stream()
                .filter(a -> a.getWinnerId() != null && a.getWinnerId().equals(userId))
                .filter(a -> "ended".equals(a.getStatus()))
                .filter(a -> filterByTimeRange(a.getEndTime(), startTime, endTime))
                .collect(Collectors.toList());

        // 鑾峰彇杩濈害娆℃暟锛堜粠璧勪骇鍘嗗彶涓煡璇級
        long defaultCount = assetHistoryRepository.findByAssetIdInAndOperationContains(
                wonAuctions.stream().map(Auction::getAssetId).collect(Collectors.toList()),
                "杩濈害"
        ).size();

        // 鑾峰彇鎴愪氦璧勪骇鏄庣粏
        List<AssetSummary> assets = wonAuctions.stream()
                .map(a -> {
                    Asset asset = a.getAsset();
                    return new AssetSummary(
                            asset != null ? asset.getId() : null,
                            asset != null ? asset.getName() : a.getName(),
                            a.getFinalPrice(),
                            a.getEndTime() != null ? a.getEndTime().toLocalDate().toString() : ""
                    );
                })
                .collect(Collectors.toList());

        // 计算总成交金额
        BigDecimal totalAmount = wonAuctions.stream()
                .map(Auction::getFinalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BiddingStatistics stats = new BiddingStatistics(
                userId,
                user.getUsername(),
                user.getName(),
                user.getDepartment() != null ? user.getDepartment().getName() : "",
                (long) auctionIds.size(),
                (long) wonAuctions.size(),
                defaultCount,
                totalAmount,
                assets
        );

        return List.of(stats);
    }

    @Override
    public List<BiddingStatistics> getAllBiddingStatistics(String startDate, String endDate, String period, String role) {
        // 获取所有用户
        List<User> users;
        if (role != null && !role.isEmpty()) {
            users = userRepository.findByRole(role);
        } else {
            users = userRepository.findAll();
        }

        List<BiddingStatistics> statistics = new ArrayList<>();

        for (User user : users) {
            statistics.addAll(getBiddingStatistics(user.getId(), startDate, endDate, period));
        }

        return statistics;
    }

    @Override
    public DisposalStatistics getDisposalStatistics(String startDate, String endDate, String period) {
        LocalDateTime[] range = resolveRange(startDate, endDate, period);
        LocalDateTime startTime = range[0];
        LocalDateTime endTime = range[1];

        List<Auction> allEndedAuctions = auctionRepository.findByStatus("ended").stream()
                .filter(a -> filterByTimeRange(a.getEndTime(), startTime, endTime))
                .collect(Collectors.toList());

        long succeededCount = allEndedAuctions.stream()
                .filter(this::isAuctionSuccessful)
                .count();
        long failedCount = allEndedAuctions.size() - succeededCount;
        long totalAssets = allEndedAuctions.size();

        BigDecimal totalAmount = allEndedAuctions.stream()
                .filter(this::isAuctionSuccessful)
                .map(Auction::getFinalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double failureRate = totalAssets > 0 ? (double) failedCount / totalAssets * 100 : 0.0;

        return new DisposalStatistics(totalAssets, totalAmount, failureRate, succeededCount, failedCount);
    }

    @Override
    public List<DepartmentDisposalStatistics> getDepartmentDisposalStatistics(String startDate, String endDate, String period) {
        LocalDateTime[] range = resolveRange(startDate, endDate, period);
        LocalDateTime startTime = range[0];
        LocalDateTime endTime = range[1];

        // 浣跨敤涓庢€讳綋缁熻鐩稿悓鐨勯€昏緫锛氳幏鍙栨墍鏈夌粨鏉熺殑鎷嶅崠
        List<Auction> deptEndedAuctions = auctionRepository.findByStatus("ended").stream()
                .filter(a -> filterByTimeRange(a.getEndTime(), startTime, endTime))
                .collect(Collectors.toList());

        // 中标者部门优先，若无中标者则回退到资产所属部门
        Set<Long> assetIds = deptEndedAuctions.stream()
                .map(Auction::getAssetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Asset> assetMap = assetRepository.findAllById(assetIds).stream()
                .collect(Collectors.toMap(Asset::getId, a -> a));

        Set<Long> winnerIds = deptEndedAuctions.stream()
                .map(Auction::getWinnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> winnerMap = userRepository.findAllById(winnerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Set<Long> departmentIds = new HashSet<>();
        winnerMap.values().stream()
                .map(User::getDepartmentId)
                .filter(Objects::nonNull)
                .forEach(departmentIds::add);
        assetMap.values().stream()
                .map(Asset::getDepartmentId)
                .filter(Objects::nonNull)
                .forEach(departmentIds::add);

        Map<Long, Department> departmentMap = departmentRepository.findAllById(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, d -> d));

        Map<Long, DepartmentStat> statMap = new LinkedHashMap<>();
        for (Auction auction : deptEndedAuctions) {
            Long departmentId = null;
            User winner = auction.getWinnerId() != null ? winnerMap.get(auction.getWinnerId()) : null;
            if (winner != null && winner.getDepartmentId() != null) {
                departmentId = winner.getDepartmentId();
            } else {
                Asset asset = assetMap.get(auction.getAssetId());
                if (asset != null && asset.getDepartmentId() != null) {
                    departmentId = asset.getDepartmentId();
                }
            }

            if (departmentId == null) {
                continue;
            }

            DepartmentStat stat = statMap.computeIfAbsent(departmentId, key -> new DepartmentStat());
            if (isAuctionSuccessful(auction)) {
                stat.successCount++;
                if (auction.getFinalPrice() != null) {
                    stat.totalAmount = stat.totalAmount.add(auction.getFinalPrice());
                }
            } else {
                stat.failedCount++;
            }
        }

        List<DepartmentDisposalStatistics> statistics = new ArrayList<>();
        for (Map.Entry<Long, DepartmentStat> entry : statMap.entrySet()) {
            Long departmentId = entry.getKey();
            DepartmentStat stat = entry.getValue();

            long totalAssets = stat.successCount + stat.failedCount;
            double failureRate = totalAssets > 0 ? (double) stat.failedCount / totalAssets * 100 : 0.0;
            String departmentName = departmentMap.get(departmentId) != null
                    ? departmentMap.get(departmentId).getName()
                    : "鏈煡閮ㄩ棬";

            statistics.add(new DepartmentDisposalStatistics(
                    departmentId,
                    departmentName,
                    totalAssets,
                    stat.failedCount,
                    stat.totalAmount,
                    failureRate
            ));
        }

        return statistics.stream()
                .sorted(Comparator.comparing(DepartmentDisposalStatistics::departmentId))
                .collect(Collectors.toList());
    }

    @Override
    public DashboardStatistics getDashboardStatistics() {
        long totalAssets = assetRepository.count();
        long pendingAssets = assetRepository.findByStatus("待审核", org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long auctioningAssets = assetRepository.findByStatus("拍卖中", org.springframework.data.domain.Pageable.unpaged()).getTotalElements();

        List<Auction> endedAuctions = auctionRepository.findByStatus("ended");
        List<Auction> successfulAuctions = endedAuctions.stream()
                .filter(this::isAuctionSuccessful)
                .collect(Collectors.toList());

        long disposedAssets = successfulAuctions.size();
        long totalAuctions = auctionRepository.count();
        long inProgressAuctions = auctionRepository.findByStatus("in_progress",
                org.springframework.data.domain.Pageable.unpaged()).getTotalElements();

        long totalTransactions = transactionRepository.count();
        BigDecimal totalAmount = successfulAuctions.stream()
                .map(Auction::getFinalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double failRate = endedAuctions.isEmpty() ? 0.0
                : ((double) (endedAuctions.size() - successfulAuctions.size()) / endedAuctions.size()) * 100;

        List<DashboardStatistics.TrendPoint> monthlyTrend = buildMonthlyTrend(successfulAuctions);
        List<DepartmentDisposalStatistics> departmentCompare = getDepartmentDisposalStatistics(null, null, null);
        List<DashboardStatistics.RecentDisposal> recentDisposals = buildRecentDisposals(successfulAuctions);

        return new DashboardStatistics(
                totalAssets, pendingAssets, auctioningAssets, disposedAssets,
                totalAuctions, inProgressAuctions, totalTransactions, totalAmount,
                failRate, recentDisposals, monthlyTrend, departmentCompare
        );
    }

    @Override
    public List<DashboardStatistics.TrendPoint> getDealTrend(String period, String month) {
        List<Auction> endedAuctions = auctionRepository.findByStatus("ended");
        List<Auction> successfulAuctions = endedAuctions.stream()
                .filter(this::isAuctionSuccessful)
                .collect(Collectors.toList());
        return buildTrendData(successfulAuctions, period, month);
    }

    private boolean isAuctionSuccessful(Auction auction) {
        return auction != null && auction.getWinnerId() != null && auction.getFinalPrice() != null;
    }

    private List<DashboardStatistics.TrendPoint> buildMonthlyTrend(List<Auction> successfulAuctions) {
        return buildTrendData(successfulAuctions, "month", null);
    }

    private List<DashboardStatistics.TrendPoint> buildTrendData(List<Auction> successfulAuctions, String period, String month) {
        String mode = period == null ? "month" : period.trim().toLowerCase();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(5).withDayOfMonth(1);
        LocalDate endDate = today;

        Map<String, BigDecimal> amountByKey = new LinkedHashMap<>();
        if ("day".equals(mode)) {
            YearMonth targetMonth = null;
            if (month != null && !month.isBlank()) {
                try {
                    targetMonth = YearMonth.parse(month.trim(), DateTimeFormatter.ofPattern("yyyy-MM"));
                } catch (Exception ignored) {
                    targetMonth = null;
                }
            }
            if (targetMonth != null) {
                startDate = targetMonth.atDay(1);
                endDate = targetMonth.atEndOfMonth();
                if (endDate.isAfter(today)) {
                    endDate = today;
                }
            }
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                amountByKey.put(date.format(dayFormatter), BigDecimal.ZERO);
            }
        } else {
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
            YearMonth current = YearMonth.from(today);
            for (int i = 5; i >= 0; i--) {
                YearMonth monthItem = current.minusMonths(i);
                amountByKey.put(monthItem.format(monthFormatter), BigDecimal.ZERO);
            }
        }

        for (Auction auction : successfulAuctions) {
            if (auction.getEndTime() == null || auction.getFinalPrice() == null) {
                continue;
            }
            LocalDate auctionDate = auction.getEndTime().toLocalDate();
            if (auctionDate.isBefore(startDate) || auctionDate.isAfter(endDate)) {
                continue;
            }
            String key;
            if ("day".equals(mode)) {
                key = auctionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                key = YearMonth.from(auctionDate).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            }
            if (amountByKey.containsKey(key)) {
                amountByKey.put(key, amountByKey.get(key).add(auction.getFinalPrice()));
            }
        }

        List<DashboardStatistics.TrendPoint> trendPoints = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : amountByKey.entrySet()) {
            trendPoints.add(new DashboardStatistics.TrendPoint(entry.getKey(), entry.getValue()));
        }
        return trendPoints;
    }

    private List<DashboardStatistics.RecentDisposal> buildRecentDisposals(List<Auction> successfulAuctions) {
        Set<Long> assetIds = successfulAuctions.stream()
                .map(Auction::getAssetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Asset> assetMap = assetRepository.findAllById(assetIds).stream()
                .collect(Collectors.toMap(Asset::getId, a -> a));

        Set<Long> winnerIds = successfulAuctions.stream()
                .map(Auction::getWinnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> winnerMap = userRepository.findAllById(winnerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Set<Long> departmentIds = winnerMap.values().stream()
                .map(User::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Department> departmentMap = departmentRepository.findAllById(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, d -> d));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return successfulAuctions.stream()
                .sorted(Comparator.comparing(Auction::getEndTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(10)
                .map(auction -> {
                    Asset asset = assetMap.get(auction.getAssetId());
                    String assetCode = asset != null ? asset.getCode() : "";
                    String assetName = asset != null ? asset.getName() : auction.getName();
                    String departmentName = "";
                    User winner = winnerMap.get(auction.getWinnerId());
                    if (winner != null && winner.getDepartmentId() != null && departmentMap.get(winner.getDepartmentId()) != null) {
                        departmentName = departmentMap.get(winner.getDepartmentId()).getName();
                    }
                    String winnerName = winner != null ? (winner.getName() != null ? winner.getName() : winner.getUsername()) : "";
                    String disposalTime = auction.getEndTime() != null ? auction.getEndTime().format(formatter) : "";
                    return new DashboardStatistics.RecentDisposal(
                            assetCode,
                            assetName,
                            winnerName,
                            auction.getFinalPrice(),
                            disposalTime,
                            departmentName
                    );
                })
                .collect(Collectors.toList());
    }

    private static class DepartmentStat {
        private long successCount;
        private long failedCount;
        private BigDecimal totalAmount = BigDecimal.ZERO;
    }

    private LocalDateTime[] resolveRange(String startDate, String endDate, String period) {
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);
        if (start != null || end != null) {
            return new LocalDateTime[] { start, end };
        }
        if (period == null || period.isBlank()) {
            return new LocalDateTime[] { null, null };
        }
        LocalDate today = LocalDate.now();
        switch (period.trim().toLowerCase()) {
            case "month" -> {
                LocalDate monthStart = today.withDayOfMonth(1);
                return new LocalDateTime[] { monthStart.atStartOfDay(), today.atTime(23, 59, 59) };
            }
            case "quarter" -> {
                int currentMonth = today.getMonthValue();
                int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
                LocalDate quarterStart = LocalDate.of(today.getYear(), quarterStartMonth, 1);
                return new LocalDateTime[] { quarterStart.atStartOfDay(), today.atTime(23, 59, 59) };
            }
            case "year" -> {
                LocalDate yearStart = LocalDate.of(today.getYear(), 1, 1);
                return new LocalDateTime[] { yearStart.atStartOfDay(), today.atTime(23, 59, 59) };
            }
            default -> {
                return new LocalDateTime[] { null, null };
            }
        }
    }

    /**
     * 瑙ｆ瀽寮€濮嬫棩鏈?     */
    private LocalDateTime parseStartDate(String startDate) {
        if (startDate == null || startDate.isEmpty()) {
            return null;
        }
        return LocalDate.parse(startDate).atStartOfDay();
    }

    /**
     * 瑙ｆ瀽缁撴潫鏃ユ湡
     */
    private LocalDateTime parseEndDate(String endDate) {
        if (endDate == null || endDate.isEmpty()) {
            return null;
        }
        return LocalDate.parse(endDate).atTime(23, 59, 59);
    }


    /**
     * 妫€鏌ユ椂闂存槸鍚﹀湪鑼冨洿鍐?     */
    private boolean filterByTimeRange(LocalDateTime dateTime, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null && endTime == null) {
            return true;
        }
        if (dateTime == null) {
            return false;
        }
        if (startTime != null && dateTime.isBefore(startTime)) {
            return false;
        }
        if (endTime != null && dateTime.isAfter(endTime)) {
            return false;
        }
        return true;
    }
}


