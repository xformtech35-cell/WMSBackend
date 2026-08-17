// ====== FILE: src/main/java/com/warehouse/wms/mapper/QRCodeMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.QRCodeGenerateRequest;
import com.warehouse.wms.dto.response.QRCodeResponse;
import com.warehouse.wms.entity.QRCode;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {RockMapper.class}
)
public interface QRCodeMapper {

    QRCode toEntity(QRCodeGenerateRequest request);

    @Mapping(source = "status", target = "status")
    @Mapping(source = "rock", target = "rock")
    @Mapping(source = "taskAssinged", target = "isTaskAssinged")  
    QRCodeResponse toResponse(QRCode qrCode);
    

    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    void updateEntity(
        @MappingTarget QRCode qrCode,
        QRCodeGenerateRequest request
    );
}