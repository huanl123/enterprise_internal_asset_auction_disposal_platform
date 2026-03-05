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

/**
 * 资产控制器
 */
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    /**
     * 分页查询资产
     */
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

    /**
     * 根据ID获取资产
     */
    @GetMapping("/{id}")
    public Result<AssetDTO> getById(@PathVariable Long id) {
        return Result.success(assetService.getAssetDTOById(id));
    }

    /**
     * 获取资产历史记录
     */
    @GetMapping("/{id}/history")
    public Result<java.util.List<AssetHistory>> getHistory(@PathVariable Long id) {
        return Result.success(assetService.getAssetHistory(id));
    }

    /**
     * 创建资产（资产专员）
     */
    @PostMapping
    public Result<Asset> create(@RequestBody Asset asset, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        assetService.createAsset(asset, operatorId);
        return Result.success("创建成功", asset);
    }

    /**
     * 更新资产（资产专员）
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Asset asset) {
        asset.setId(id);
        assetService.updateAsset(asset);
        return Result.success("更新成功", null);
    }

    /**
     * 删除资产（资产专员）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return Result.success("删除成功", null);
    }

    /**
     * 重新计算资产价值（资产专员）
     */
    @PostMapping("/{id}/recalculate")
    public Result<Void> recalculate(@PathVariable Long id) {
        assetService.recalculateValue(id);
        return Result.success("重新计算成功，已进入待审核状态", null);
    }
}