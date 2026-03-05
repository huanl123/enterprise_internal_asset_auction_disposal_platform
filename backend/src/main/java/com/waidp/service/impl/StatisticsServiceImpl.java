package com.waidp.service.impl;

import com.waidp.dto.AssetSummary;
import com.waidp.dto.BiddingStatistics;
import com.waidp.dto.DashboardStatistics;
import com.waidp.dto.DepartmentDisposalStatistics;
import com.waidp.dto.DisposalStatistics;
import com.waidp.entity.Asset;
import com.waidp.entity.Auction;
import com.waidp.entity.Bid;
import com.waidp.entity.Department;
import com.waidp.entity.Transaction;
import com.waidp.entity.User;
import com.waidp.repository.AssetRepository;
import com.waidp.repository.AuctionRepository;
import com.waidp.repository.BidRepository;
import com.waidp.repository.DepartmentRepository;
import com.waidp.repository.TransactionRepository;
import com.waidp.repository.UserRepository;
import com.waidp.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final AssetRepository assetRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public List<BiddingStatistics> getBiddingStatistics(Long userId, String startDate, String endDate, String period,
                                                        Long currentUserId, String currentRole) {
        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return Collections.emptyList();
        }

        Long scopedDepartmentId = resolveScopedDepartmentId(currentUserId, currentRole);
        if (scopedDepartmentId != null && !Objects.equals(scopedDepartmentId, targetUser.getDepartmentId())) {
            return Collections.emptyList();
        }

        LocalDateTime[] range = resolveRange(startDate, endDate, period);
        return List.of(buildBiddingStatistics(targetUser, range[0], range[1]));
    }

    @Override
    public List<BiddingStatistics> getAllBiddingStatistics(String startDate, String endDate, String period, String role,
                                                           Long departmentId, Long currentUserId, String currentRole) {
        LocalDateTime[] range = resolveRange(startDate, endDate, period);
        Long effectiveDepartmentId = resolveEffectiveDepartmentId(currentUserId, currentRole, departmentId);

        return userRepository.findAll().stream()
                .filter(user -> !isAdminRole(user.getRole()))
                .filter(user -> matchesRoleFilter(user, role))
                .filter(user -> effectiveDepartmentId == null || Objects.equals(user.getDepartmentId(), effectiveDepartmentId))
                .map(user -> buildBiddingStatistics(user, range[0], range[1]))
                .collect(Collectors.toList());
    }

    @Override
    public DisposalStatistics getDisposalStatistics(String startDate, String endDate, String period,
                                                    Long departmentId, Long currentUserId, String currentRole) {
        LocalDateTime[] range = resolveRange(startDate, endDate, period);
        Long effectiveDepartmentId = resolveEffectiveDepartmentId(currentUserId, currentRole, departmentId);

        List<Auction> endedAuctions = auctionRepository.findByStatus("ended").stream()
                .filter(auction -> filterByTimeRange(auction.getEndTime(), range[0], range[1]))
                .collect(Collectors.toList());

        Map<Long, Asset> assetMap = loadAssetMap(endedAuctions);
        Map<Long, User> winnerMap = loadWinnerMap(endedAuctions);

        List<Auction> scopedAuctions = endedAuctions.stream()
                .filter(auction -> matchDepartmentScope(auction, assetMap, winnerMap, effectiveDepartmentId))
                .collect(Collectors.toList());

        long totalAssets = scopedAuctions.size();
        long succeededCount = scopedAuctions.stream().filter(this::isAuctionSuccessful).count();
        long failedCount = totalAssets - succeededCount;

        BigDecimal totalAmount = scopedAuctions.stream()
                .filter(this::isAuctionSuccessful)
                .map(Auction::getFinalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double failureRate = totalAssets > 0 ? ((double) failedCount / totalAssets) * 100 : 0.0;
        return new DisposalStatistics(totalAssets, totalAmount, failureRate, succeededCount, failedCount);
    }

    @Override
    public List<DepartmentDisposalStatistics> getDepartmentDisposalStatistics(String startDate, String endDate, String period,
                                                                              Long departmentId, Long currentUserId, String currentRole) {
        LocalDateTime[] range = resolveRange(startDate, endDate, period);
        Long effectiveDepartmentId = resolveEffectiveDepartmentId(currentUserId, currentRole, departmentId);

        List<Auction> endedAuctions = auctionRepository.findByStatus("ended").stream()
                .filter(auction -> filterByTimeRange(auction.getEndTime(), range[0], range[1]))
                .collect(Collectors.toList());

        Map<Long, Asset> assetMap = loadAssetMap(endedAuctions);
        Map<Long, User> winnerMap = loadWinnerMap(endedAuctions);

        Set<Long> departmentIds = endedAuctions.stream()
                .map(auction -> resolveAuctionDepartmentId(auction, assetMap, winnerMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Department> departmentMap = departmentRepository.findAllById(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, department -> department));

        Map<Long, DepartmentStat> statMap = new LinkedHashMap<>();
        for (Auction auction : endedAuctions) {
            Long relatedDepartmentId = resolveAuctionDepartmentId(auction, assetMap, winnerMap);
            if (relatedDepartmentId == null) {
                continue;
            }
            if (effectiveDepartmentId != null && !Objects.equals(effectiveDepartmentId, relatedDepartmentId)) {
                continue;
            }

            DepartmentStat stat = statMap.computeIfAbsent(relatedDepartmentId, key -> new DepartmentStat());
            if (isAuctionSuccessful(auction)) {
                stat.successCount++;
                if (auction.getFinalPrice() != null) {
                    stat.totalAmount = stat.totalAmount.add(auction.getFinalPrice());
                }
            } else {
                stat.failedCount++;
            }
        }

        return statMap.entrySet().stream()
                .map(entry -> {
                    Long deptId = entry.getKey();
                    DepartmentStat stat = entry.getValue();
                    long totalCount = stat.successCount + stat.failedCount;
                    double failureRate = totalCount > 0 ? ((double) stat.failedCount / totalCount) * 100 : 0.0;
                    String departmentName = departmentMap.get(deptId) != null
                            ? departmentMap.get(deptId).getName()
                            : "未知部门";
                    return new DepartmentDisposalStatistics(
                            deptId,
                            departmentName,
                            totalCount,
                            stat.failedCount,
                            stat.totalAmount,
                            failureRate
                    );
                })
                .sorted(Comparator.comparing(DepartmentDisposalStatistics::departmentId))
                .collect(Collectors.toList());
    }

    @Override
    public DashboardStatistics getDashboardStatistics(Long currentUserId, String currentRole) {
        Long scopedDepartmentId = resolveScopedDepartmentId(currentUserId, currentRole);

        List<Asset> assets = assetRepository.findAll().stream()
                .filter(asset -> scopedDepartmentId == null || Objects.equals(asset.getDepartmentId(), scopedDepartmentId))
                .collect(Collectors.toList());

        long totalAssets = assets.size();
        long pendingAssets = assets.stream()
                .filter(asset -> statusEquals(asset.getStatus(), "待审核", "PENDING"))
                .count();
        long auctioningAssets = assets.stream()
                .filter(asset -> statusEquals(asset.getStatus(), "拍卖中"))
                .count();

        List<Auction> allAuctions = auctionRepository.findAll();
        Map<Long, Asset> auctionAssetMap = loadAssetMap(allAuctions);
        List<Auction> scopedAuctions = allAuctions.stream()
                .filter(auction -> scopedDepartmentId == null
                        || Objects.equals(resolveAuctionDepartmentId(auction, auctionAssetMap, Collections.emptyMap()), scopedDepartmentId))
                .collect(Collectors.toList());

        List<Auction> endedAuctions = scopedAuctions.stream()
                .filter(auction -> "ended".equalsIgnoreCase(auction.getStatus()))
                .collect(Collectors.toList());
        List<Auction> successfulAuctions = endedAuctions.stream()
                .filter(this::isAuctionSuccessful)
                .collect(Collectors.toList());

        long disposedAssets = successfulAuctions.size();
        long totalAuctions = scopedAuctions.size();
        long inProgressAuctions = scopedAuctions.stream()
                .filter(auction -> "in_progress".equalsIgnoreCase(auction.getStatus()))
                .count();

        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(tx -> {
                    if (scopedDepartmentId == null) {
                        return true;
                    }
                    if (tx.getAssetId() == null) {
                        return false;
                    }
                    Asset txAsset = auctionAssetMap.get(tx.getAssetId());
                    if (txAsset == null) {
                        txAsset = assetRepository.findById(tx.getAssetId()).orElse(null);
                    }
                    return txAsset != null && Objects.equals(txAsset.getDepartmentId(), scopedDepartmentId);
                })
                .collect(Collectors.toList());
        long totalTransactions = transactions.size();

        BigDecimal totalAmount = successfulAuctions.stream()
                .map(Auction::getFinalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double failRate = endedAuctions.isEmpty()
                ? 0.0
                : ((double) (endedAuctions.size() - successfulAuctions.size()) / endedAuctions.size()) * 100;

        List<DashboardStatistics.TrendPoint> monthlyTrend = buildTrendData(successfulAuctions, "month", null);
        List<DepartmentDisposalStatistics> departmentCompare = getDepartmentDisposalStatistics(
                null, null, null, null, currentUserId, currentRole
        );
        List<DashboardStatistics.RecentDisposal> recentDisposals = buildRecentDisposals(successfulAuctions);

        return new DashboardStatistics(
                totalAssets,
                pendingAssets,
                auctioningAssets,
                disposedAssets,
                totalAuctions,
                inProgressAuctions,
                totalTransactions,
                totalAmount,
                failRate,
                recentDisposals,
                monthlyTrend,
                departmentCompare
        );
    }

    @Override
    public List<DashboardStatistics.TrendPoint> getDealTrend(String period, String month, Long currentUserId, String currentRole) {
        Long scopedDepartmentId = resolveScopedDepartmentId(currentUserId, currentRole);

        List<Auction> endedAuctions = auctionRepository.findByStatus("ended");
        Map<Long, Asset> assetMap = loadAssetMap(endedAuctions);
        Map<Long, User> winnerMap = loadWinnerMap(endedAuctions);

        List<Auction> successfulAuctions = endedAuctions.stream()
                .filter(this::isAuctionSuccessful)
                .filter(auction -> matchDepartmentScope(auction, assetMap, winnerMap, scopedDepartmentId))
                .collect(Collectors.toList());

        return buildTrendData(successfulAuctions, period, month);
    }

    private BiddingStatistics buildBiddingStatistics(User user, LocalDateTime startTime, LocalDateTime endTime) {
        Long userId = user.getId();

        List<Bid> userBids = bidRepository.findByBidderIdAndBidTimeRange(userId, startTime, endTime);
        Set<Long> auctionIds = userBids.stream()
                .map(Bid::getAuctionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<Auction> auctions = auctionIds.isEmpty() ? Collections.emptyList() : auctionRepository.findAllById(auctionIds);

        List<Auction> wonAuctions = auctions.stream()
                .filter(auction -> Objects.equals(auction.getWinnerId(), userId))
                .filter(auction -> "ended".equalsIgnoreCase(auction.getStatus()))
                .filter(auction -> filterByTimeRange(auction.getEndTime(), startTime, endTime))
                .filter(this::isAuctionSuccessful)
                .collect(Collectors.toList());

        long breachCount = countBreachTransactions(userId, startTime, endTime);
        BigDecimal totalAmount = wonAuctions.stream()
                .map(Auction::getFinalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AssetSummary> assets = wonAuctions.stream()
                .map(auction -> {
                    Asset asset = auction.getAsset();
                    Long assetId = asset != null ? asset.getId() : null;
                    String assetName = asset != null ? asset.getName() : auction.getName();
                    String dateText = auction.getEndTime() != null ? auction.getEndTime().toLocalDate().toString() : "";
                    return new AssetSummary(assetId, assetName, auction.getFinalPrice(), dateText);
                })
                .collect(Collectors.toList());

        String displayName = user.getName() != null ? user.getName() : user.getRealName();
        String departmentName = user.getDepartment() != null ? user.getDepartment().getName() : "";

        return new BiddingStatistics(
                userId,
                user.getUsername(),
                displayName,
                departmentName,
                (long) auctionIds.size(),
                (long) wonAuctions.size(),
                breachCount,
                totalAmount,
                assets
        );
    }

    private Map<Long, Asset> loadAssetMap(List<Auction> auctions) {
        Set<Long> assetIds = auctions.stream()
                .map(Auction::getAssetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (assetIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return assetRepository.findAllById(assetIds).stream()
                .collect(Collectors.toMap(Asset::getId, asset -> asset));
    }

    private Map<Long, User> loadWinnerMap(List<Auction> auctions) {
        Set<Long> winnerIds = auctions.stream()
                .map(Auction::getWinnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (winnerIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userRepository.findAllById(winnerIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    private Long resolveAuctionDepartmentId(Auction auction, Map<Long, Asset> assetMap, Map<Long, User> winnerMap) {
        if (auction == null) {
            return null;
        }
        User winner = auction.getWinnerId() != null ? winnerMap.get(auction.getWinnerId()) : null;
        if (winner != null && winner.getDepartmentId() != null) {
            return winner.getDepartmentId();
        }
        Asset asset = auction.getAssetId() != null ? assetMap.get(auction.getAssetId()) : null;
        if (asset != null) {
            return asset.getDepartmentId();
        }
        return null;
    }

    private boolean matchDepartmentScope(Auction auction, Map<Long, Asset> assetMap, Map<Long, User> winnerMap, Long departmentId) {
        if (departmentId == null) {
            return true;
        }
        Long relatedDepartmentId = resolveAuctionDepartmentId(auction, assetMap, winnerMap);
        return Objects.equals(departmentId, relatedDepartmentId);
    }

    private boolean isAuctionSuccessful(Auction auction) {
        if (auction == null || auction.getWinnerId() == null || auction.getFinalPrice() == null) {
            return false;
        }
        if (Boolean.TRUE.equals(auction.getHasReservePrice()) && auction.getReservePrice() != null) {
            return auction.getFinalPrice().compareTo(auction.getReservePrice()) >= 0;
        }
        return true;
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
                YearMonth item = current.minusMonths(i);
                amountByKey.put(item.format(monthFormatter), BigDecimal.ZERO);
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
            String key = "day".equals(mode)
                    ? auctionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    : YearMonth.from(auctionDate).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            if (amountByKey.containsKey(key)) {
                amountByKey.put(key, amountByKey.get(key).add(auction.getFinalPrice()));
            }
        }

        List<DashboardStatistics.TrendPoint> points = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : amountByKey.entrySet()) {
            points.add(new DashboardStatistics.TrendPoint(entry.getKey(), entry.getValue()));
        }
        return points;
    }

    private List<DashboardStatistics.RecentDisposal> buildRecentDisposals(List<Auction> successfulAuctions) {
        Set<Long> assetIds = successfulAuctions.stream()
                .map(Auction::getAssetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Asset> assetMap = assetIds.isEmpty()
                ? Collections.emptyMap()
                : assetRepository.findAllById(assetIds).stream()
                .collect(Collectors.toMap(Asset::getId, asset -> asset));

        Set<Long> winnerIds = successfulAuctions.stream()
                .map(Auction::getWinnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> winnerMap = winnerIds.isEmpty()
                ? Collections.emptyMap()
                : userRepository.findAllById(winnerIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Set<Long> departmentIds = winnerMap.values().stream()
                .map(User::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Department> departmentMap = departmentIds.isEmpty()
                ? Collections.emptyMap()
                : departmentRepository.findAllById(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, department -> department));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return successfulAuctions.stream()
                .sorted(Comparator.comparing(Auction::getEndTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(10)
                .map(auction -> {
                    Asset asset = assetMap.get(auction.getAssetId());
                    User winner = winnerMap.get(auction.getWinnerId());
                    String departmentName = "";
                    if (winner != null && winner.getDepartmentId() != null) {
                        Department department = departmentMap.get(winner.getDepartmentId());
                        if (department != null) {
                            departmentName = department.getName();
                        }
                    }
                    return new DashboardStatistics.RecentDisposal(
                            asset != null ? asset.getCode() : "",
                            asset != null ? asset.getName() : auction.getName(),
                            winner != null ? (winner.getName() != null ? winner.getName() : winner.getUsername()) : "",
                            auction.getFinalPrice(),
                            auction.getEndTime() != null ? auction.getEndTime().format(formatter) : "",
                            departmentName
                    );
                })
                .collect(Collectors.toList());
    }

    private LocalDateTime[] resolveRange(String startDate, String endDate, String period) {
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);
        if (start != null || end != null) {
            return new LocalDateTime[]{start, end};
        }
        if (period == null || period.isBlank()) {
            return new LocalDateTime[]{null, null};
        }

        LocalDate today = LocalDate.now();
        switch (period.trim().toLowerCase()) {
            case "month" -> {
                LocalDate monthStart = today.withDayOfMonth(1);
                return new LocalDateTime[]{monthStart.atStartOfDay(), today.atTime(23, 59, 59)};
            }
            case "quarter" -> {
                int monthValue = today.getMonthValue();
                int quarterStartMonth = ((monthValue - 1) / 3) * 3 + 1;
                LocalDate quarterStart = LocalDate.of(today.getYear(), quarterStartMonth, 1);
                return new LocalDateTime[]{quarterStart.atStartOfDay(), today.atTime(23, 59, 59)};
            }
            case "year" -> {
                LocalDate yearStart = LocalDate.of(today.getYear(), 1, 1);
                return new LocalDateTime[]{yearStart.atStartOfDay(), today.atTime(23, 59, 59)};
            }
            default -> {
                return new LocalDateTime[]{null, null};
            }
        }
    }

    private LocalDateTime parseStartDate(String startDate) {
        if (startDate == null || startDate.isBlank()) {
            return null;
        }
        return LocalDate.parse(startDate).atStartOfDay();
    }

    private LocalDateTime parseEndDate(String endDate) {
        if (endDate == null || endDate.isBlank()) {
            return null;
        }
        return LocalDate.parse(endDate).atTime(23, 59, 59);
    }

    private boolean filterByTimeRange(LocalDateTime value, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null && endTime == null) {
            return true;
        }
        if (value == null) {
            return false;
        }
        if (startTime != null && value.isBefore(startTime)) {
            return false;
        }
        if (endTime != null && value.isAfter(endTime)) {
            return false;
        }
        return true;
    }

    private long countBreachTransactions(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Transaction> transactions = transactionRepository.findByWinnerIdOrderByCreateTimeDesc(userId);
        return transactions.stream()
                .filter(this::isBreachTransaction)
                .filter(tx -> filterByTimeRange(resolveBreachTime(tx), startTime, endTime))
                .count();
    }

    private boolean isBreachTransaction(Transaction tx) {
        String confirmStatus = normalizeText(tx.getConfirmStatus());
        String paymentStatus = normalizeText(tx.getPaymentStatus());
        return "expired".equals(confirmStatus) || "rejected".equals(paymentStatus);
    }

    private LocalDateTime resolveBreachTime(Transaction tx) {
        String confirmStatus = normalizeText(tx.getConfirmStatus());
        String paymentStatus = normalizeText(tx.getPaymentStatus());

        if ("expired".equals(confirmStatus)) {
            if (tx.getUpdateTime() != null) {
                return tx.getUpdateTime();
            }
            if (tx.getConfirmDeadline() != null) {
                return tx.getConfirmDeadline();
            }
            return tx.getCreateTime();
        }
        if ("rejected".equals(paymentStatus)) {
            return tx.getUpdateTime() != null ? tx.getUpdateTime() : tx.getCreateTime();
        }
        return tx.getUpdateTime() != null ? tx.getUpdateTime() : tx.getCreateTime();
    }

    private Long resolveEffectiveDepartmentId(Long currentUserId, String currentRole, Long requestedDepartmentId) {
        Long scopedDepartmentId = resolveScopedDepartmentId(currentUserId, currentRole);
        return scopedDepartmentId != null ? scopedDepartmentId : requestedDepartmentId;
    }

    private Long resolveScopedDepartmentId(Long currentUserId, String currentRole) {
        if (!isDepartmentScopedRole(currentRole) || currentUserId == null) {
            return null;
        }
        return userRepository.findById(currentUserId)
                .map(User::getDepartmentId)
                .orElse(null);
    }

    private boolean isDepartmentScopedRole(String role) {
        String normalized = normalizeRole(role);
        return "ASSET_SPECIALIST".equals(normalized)
                || "FINANCE_SPECIALIST".equals(normalized)
                || "资产专员".equals(normalized)
                || "财务专员".equals(normalized);
    }

    private boolean isAdminRole(String role) {
        String normalized = normalizeRole(role);
        return "ADMIN".equals(normalized)
                || "SYSTEM_ADMIN".equals(normalized)
                || "系统管理员".equals(normalized);
    }

    private boolean matchesRoleFilter(User user, String roleFilter) {
        if (roleFilter == null || roleFilter.isBlank()) {
            return true;
        }
        return normalizeRole(user.getRole()).equals(normalizeRole(roleFilter));
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String normalized = role.trim();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return normalized.toUpperCase();
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean statusEquals(String actual, String... expectedValues) {
        if (actual == null) {
            return false;
        }
        String normalized = actual.trim().toLowerCase();
        for (String expected : expectedValues) {
            if (expected != null && normalized.equals(expected.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static class DepartmentStat {
        private long successCount;
        private long failedCount;
        private BigDecimal totalAmount = BigDecimal.ZERO;
    }
}
