package com.warehouse.wms.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.dto.SupplierDTO;
import com.warehouse.wms.entity.Supplier;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierService {
    
    private final SupplierRepository supplierRepository;
    
    public Page<SupplierDTO> getAllSuppliers(Pageable pageable) {
        Page<Supplier> suppliers = supplierRepository.findAll(pageable);
        return suppliers.map(this::convertToDTO);
    }
    
    public SupplierDTO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        return convertToDTO(supplier);
    }
    
    @Transactional
    public SupplierDTO createSupplier(SupplierDTO supplierDTO) {
        if (supplierRepository.existsByCode(supplierDTO.getCode())) {
            throw new IllegalArgumentException("Supplier code already exists");
        }
        
        Supplier supplier = new Supplier();
        supplier.setCode(supplierDTO.getCode());
        supplier.setName(supplierDTO.getName());
        supplier.setContactPerson(supplierDTO.getContactPerson());
        supplier.setEmail(supplierDTO.getEmail());
        supplier.setPhone(supplierDTO.getPhone());
        supplier.setAddress(supplierDTO.getAddress());
        supplier.setGstNumber(supplierDTO.getGstNumber());
        supplier.setIsActive(true);
        
        supplier = supplierRepository.save(supplier);
        log.info("Supplier created: {}", supplier.getName());
        
        return convertToDTO(supplier);
    }
    
    // ✅ ADD THIS - Update Supplier
    @Transactional
    public SupplierDTO updateSupplier(Long id, SupplierDTO supplierDTO) {
        Supplier existingSupplier = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        
        // Check if code is being changed and if new code already exists
        if (!existingSupplier.getCode().equals(supplierDTO.getCode()) && 
            supplierRepository.existsByCode(supplierDTO.getCode())) {
            throw new IllegalArgumentException("Supplier code already exists");
        }
        
        existingSupplier.setCode(supplierDTO.getCode());
        existingSupplier.setName(supplierDTO.getName());
        existingSupplier.setContactPerson(supplierDTO.getContactPerson());
        existingSupplier.setEmail(supplierDTO.getEmail());
        existingSupplier.setPhone(supplierDTO.getPhone());
        existingSupplier.setAddress(supplierDTO.getAddress());
        existingSupplier.setGstNumber(supplierDTO.getGstNumber());
        existingSupplier.setIsActive(supplierDTO.getIsActive());
        
        existingSupplier = supplierRepository.save(existingSupplier);
        log.info("Supplier updated: {}", existingSupplier.getName());
        
        return convertToDTO(existingSupplier);
    }
    
    // ✅ ADD THIS - Delete Supplier (Soft Delete)
    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        
        // Soft delete - just set isActive to false
        supplier.setIsActive(false);
        supplierRepository.save(supplier);
        log.info("Supplier deactivated: {}", supplier.getName());
    }
    
    private SupplierDTO convertToDTO(Supplier supplier) {
        return SupplierDTO.builder()
            .id(supplier.getId())
            .code(supplier.getCode())
            .name(supplier.getName())
            .contactPerson(supplier.getContactPerson())
            .email(supplier.getEmail())
            .phone(supplier.getPhone())
            .address(supplier.getAddress())
            .gstNumber(supplier.getGstNumber())
            .isActive(supplier.getIsActive())
            .build();
    }
}