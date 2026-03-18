package com.waidp.controller;

import com.waidp.common.PageResult;
import com.waidp.common.Result;
import com.waidp.dto.AssetDTO;
import com.waidp.entity.Asset;
import com.waidp.entity.AssetHistory;
import com.waidp.service.AssetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资产管理控制器
 * 提供资产管理的所有接口，包括：
 * 资产列表查询（支持分页、多条件筛选）、资产详情查询、
 * 资产创建、更新、删除、资产价值重新计算（根据折旧规则）、
 * 资产历史记录查询等。
 *
 * 资产状态流转：待审核 → 待拍卖 → 拍卖中 → 待处置 → 已处置 / 流拍 → 待拍卖
 */
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public Result<PageResult<AssetDTO>> list(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<AssetDTO> assetPage = assetService.getAssetsDTO(code, name, status, departmentId, pageable);
        return Result.success(PageResult.of(assetPage));
    }

    @GetMapping("/{id}")
    public Result<AssetDTO> getById(@PathVariable Long id) {
        return Result.success(assetService.getAssetDTOById(id));
    }

    @GetMapping("/{id}/history")
    public Result<List<AssetHistory>> getHistory(@PathVariable Long id) {
        return Result.success(assetService.getAssetHistory(id));
    }

    @PostMapping
    public Result<Asset> create(@RequestBody Asset asset, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        assetService.createAsset(asset, operatorId);
        return Result.success("创建成功", asset);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Asset asset) {
        asset.setId(id);
        assetService.updateAsset(asset);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/recalculate")
    public Result<Void> recalculate(@PathVariable Long id) {
        assetService.recalculateValue(id);
        return Result.success("重新计算成功，已进入待审核状态", null);
    }
}

