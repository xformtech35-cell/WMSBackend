// ====== FILE: src/main/java/com/warehouse/wms/service/LevelService.java ======
package com.warehouse.wms.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.warehouse.wms.dto.request.LevelRequest;
import com.warehouse.wms.dto.response.LevelResponse;

public interface LevelService {

    // ====== Create ======
    LevelResponse createLevel(LevelRequest request);

    // ====== Read ======
    LevelResponse getLevelById(Long id);

    LevelResponse getLevelByLevelId(String levelId);

    Page<LevelResponse> getAllLevels(Pageable pageable, String search, Long rackId);

    List<LevelResponse> getLevelsByRack(Long rackId);

    List<LevelResponse> getActiveLevelsByRack(Long rackId);

    List<LevelResponse> getLevelsByRackOrdered(Long rackId);

    // ====== Update ======
    LevelResponse updateLevel(Long id, LevelRequest request);

    LevelResponse toggleLevelStatus(Long id, Boolean isActive);

    // ====== Delete ======
    void deleteLevel(Long id);

    void deleteLevelByLevelId(String levelId);
}