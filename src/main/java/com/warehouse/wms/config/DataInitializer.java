package com.warehouse.wms.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.GoodsReceipt;
import com.warehouse.wms.entity.GoodsReceiptLine;
import com.warehouse.wms.entity.Inventory;
import com.warehouse.wms.entity.Permission;
import com.warehouse.wms.entity.PurchaseOrder;
import com.warehouse.wms.entity.PurchaseOrderLine;
import com.warehouse.wms.entity.PurchaseOrderStatus;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.entity.RackCompartment;
import com.warehouse.wms.entity.Role;
import com.warehouse.wms.entity.Sku;
import com.warehouse.wms.entity.SkuDimension;
import com.warehouse.wms.entity.Trolley;
import com.warehouse.wms.entity.User;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.repository.AisleRepository;
import com.warehouse.wms.repository.BinRepository;
import com.warehouse.wms.repository.GoodsReceiptLineRepository;
import com.warehouse.wms.repository.GoodsReceiptRepository;
import com.warehouse.wms.repository.InventoryRepository;
import com.warehouse.wms.repository.PurchaseOrderLineRepository;
import com.warehouse.wms.repository.PurchaseOrderRepository;
import com.warehouse.wms.repository.PutawayTaskRepository;
import com.warehouse.wms.repository.RackCompartmentRepository;
import com.warehouse.wms.repository.RackRepository;
import com.warehouse.wms.repository.RoleRepository;
import com.warehouse.wms.repository.ShipmentRecordRepository;
import com.warehouse.wms.repository.SkuDimensionRepository;
import com.warehouse.wms.repository.SkuRepository;
import com.warehouse.wms.repository.TrolleyRepository;
import com.warehouse.wms.repository.UserRepository;
import com.warehouse.wms.repository.WarehouseRepository;
import com.warehouse.wms.repository.ZoneRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Value("${app.db.seed.users:true}")
    private boolean seedUsersEnabled;

    @Value("${app.db.seed.demo:true}")
    private boolean seedDemoEnabled;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final WarehouseRepository warehouseRepository;
    private final ZoneRepository zoneRepository;
    private final AisleRepository aisleRepository;
    private final RackRepository rackRepository;
    private final BinRepository binRepository;
    private final SkuRepository skuRepository;
    private final SkuDimensionRepository skuDimensionRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final ShipmentRecordRepository shipmentRecordRepository;
    private final RackCompartmentRepository rackCompartmentRepository;
    private final TrolleyRepository trolleyRepository;
    private final PutawayTaskRepository putawayTaskRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final InventoryRepository inventoryRepository;
    private final JdbcTemplate jdbc;

    public DataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            WarehouseRepository warehouseRepository,
            ZoneRepository zoneRepository,
            AisleRepository aisleRepository,
            RackRepository rackRepository,
            BinRepository binRepository,
            SkuRepository skuRepository,
            SkuDimensionRepository skuDimensionRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderLineRepository purchaseOrderLineRepository,
            ShipmentRecordRepository shipmentRecordRepository,
            RackCompartmentRepository rackCompartmentRepository,
            TrolleyRepository trolleyRepository,
            PutawayTaskRepository putawayTaskRepository,
            GoodsReceiptRepository goodsReceiptRepository,
            GoodsReceiptLineRepository goodsReceiptLineRepository,
            InventoryRepository inventoryRepository,
            JdbcTemplate jdbc) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.warehouseRepository = warehouseRepository;
        this.zoneRepository = zoneRepository;
        this.aisleRepository = aisleRepository;
        this.rackRepository = rackRepository;
        this.binRepository = binRepository;
        this.skuRepository = skuRepository;
        this.skuDimensionRepository = skuDimensionRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.shipmentRecordRepository = shipmentRecordRepository;
        this.rackCompartmentRepository = rackCompartmentRepository;
        this.trolleyRepository = trolleyRepository;
        this.putawayTaskRepository = putawayTaskRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
        this.inventoryRepository = inventoryRepository;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (seedUsersEnabled) {
            seedUsers();
        } else {
            if (roleRepository.count() == 0) {
                seedDefaultRoles();
            }
        }
        if (seedDemoEnabled) {
            clearDemoTables();
            seedWarehouseStructure();
            seedRackCompartments();
            seedTrolleys();
            seedSkus();
            seedPurchaseOrders();
            seedGoodsReceipts();
            seedInventory();
        }
    }

    private void clearDemoTables() {
        System.out.println("[DataInitializer] Clearing existing demo tables...");
        jdbc.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbc.execute("TRUNCATE TABLE wms_goods_receipt_line");
        jdbc.execute("TRUNCATE TABLE wms_goods_receipt");
        jdbc.execute("TRUNCATE TABLE wms_pick_task");
        jdbc.execute("TRUNCATE TABLE wms_putaway_task");
        jdbc.execute("TRUNCATE TABLE wms_shipment_record");
        jdbc.execute("TRUNCATE TABLE wms_sales_order_line");
        jdbc.execute("TRUNCATE TABLE wms_sales_order");
        jdbc.execute("TRUNCATE TABLE wms_purchase_order_line");
        jdbc.execute("TRUNCATE TABLE wms_purchase_order");
        jdbc.execute("TRUNCATE TABLE wms_inventory");
        jdbc.execute("TRUNCATE TABLE wms_bin");
        jdbc.execute("TRUNCATE TABLE wms_rack_compartment");
        jdbc.execute("TRUNCATE TABLE wms_rack");
        jdbc.execute("TRUNCATE TABLE wms_aisle");
        jdbc.execute("TRUNCATE TABLE wms_zone");
        jdbc.execute("TRUNCATE TABLE wms_warehouse");
        jdbc.execute("TRUNCATE TABLE wms_trolley");
        jdbc.execute("TRUNCATE TABLE wms_sku_dimension");
        jdbc.execute("TRUNCATE TABLE wms_sku");
        jdbc.execute("TRUNCATE TABLE wms_movement_log");
        jdbc.execute("SET FOREIGN_KEY_CHECKS = 1");
        System.out.println("[DataInitializer] Demo tables cleared successfully.");
    }

    // ── Users ─────────────────────────────────────────────────────────────────
    private void seedUsers() {
        seedDefaultRoles();
        repairInvalidUserRoleIds();
        seedUser("superadmin", "12345678", "SUPER_ADMIN");
        seedUser("admin",      "12345678", "ADMIN");
        seedUser("manager",    "12345678", "MANAGER");
        seedUser("worker",     "12345678", "WORKER");
        seedUser("demo",       "12345678", "SUPER_ADMIN");
    }

    private void repairInvalidUserRoleIds() {
        jdbc.update("""
            UPDATE wms__user u
            JOIN wms_role r ON r.name = 'WORKER'
            LEFT JOIN wms_role existing ON existing.id = u.role_id
            SET u.role_id = r.id
            WHERE existing.id IS NULL
            """);

        jdbc.update("UPDATE wms__user u JOIN wms_role r ON r.name = 'SUPER_ADMIN' SET u.role_id = r.id WHERE u.username = 'superadmin'");
        jdbc.update("UPDATE wms__user u JOIN wms_role r ON r.name = 'ADMIN' SET u.role_id = r.id WHERE u.username = 'admin'");
        jdbc.update("UPDATE wms__user u JOIN wms_role r ON r.name = 'MANAGER' SET u.role_id = r.id WHERE u.username = 'manager'");
        jdbc.update("UPDATE wms__user u JOIN wms_role r ON r.name = 'WORKER' SET u.role_id = r.id WHERE u.username = 'worker'");
        jdbc.update("UPDATE wms__user u JOIN wms_role r ON r.name = 'SUPER_ADMIN' SET u.role_id = r.id WHERE u.username = 'demo'");
    }

    private void seedDefaultRoles() {
        upsertRole("SUPER_ADMIN", EnumSet.allOf(Permission.class));
        upsertRole("ADMIN", EnumSet.of(
                Permission.DASHBOARD_VIEW,
                Permission.PURCHASE_VIEW,
                Permission.INBOUND_VIEW, Permission.INBOUND_RECEIVE,
                Permission.INVENTORY_VIEW, Permission.INVENTORY_ADJUST,
                Permission.PUTAWAY_VIEW, Permission.PUTAWAY_EXECUTE,
                Permission.PICKING_VIEW, Permission.PICKING_EXECUTE,
                Permission.PACKING_VIEW, Permission.PACKING_EXECUTE,
                Permission.SHIPPING_VIEW, Permission.SHIPPING_CONFIRM,
                Permission.ORDERS_VIEW, Permission.ORDERS_CREATE,
                Permission.TROLLEYS_VIEW, Permission.TROLLEYS_CREATE, Permission.TROLLEYS_ASSIGN,
                Permission.LABELS_VIEW, Permission.LABELS_PRINT,
                Permission.REPORTS_VIEW, Permission.REPORTS_EXPORT,
                Permission.MASTER_VIEW, Permission.MASTER_MANAGE,
                Permission.USERS_VIEW
        ));
        upsertRole("MANAGER", EnumSet.of(
                Permission.DASHBOARD_VIEW,
                Permission.PURCHASE_VIEW,
                Permission.INBOUND_VIEW, Permission.INBOUND_RECEIVE,
                Permission.INVENTORY_VIEW, Permission.INVENTORY_ADJUST,
                Permission.PUTAWAY_VIEW, Permission.PUTAWAY_EXECUTE,
                Permission.PICKING_VIEW, Permission.PICKING_EXECUTE,
                Permission.PACKING_VIEW, Permission.PACKING_EXECUTE,
                Permission.SHIPPING_VIEW, Permission.SHIPPING_CONFIRM,
                Permission.ORDERS_VIEW, Permission.ORDERS_CREATE,
                Permission.TROLLEYS_VIEW, Permission.TROLLEYS_CREATE, Permission.TROLLEYS_ASSIGN,
                Permission.LABELS_VIEW, Permission.LABELS_PRINT,
                Permission.REPORTS_VIEW, Permission.REPORTS_EXPORT,
                Permission.MASTER_VIEW
        ));
        upsertRole("WORKER", EnumSet.of(
                Permission.DASHBOARD_VIEW,
                Permission.PURCHASE_VIEW,
                Permission.INBOUND_VIEW,
                Permission.INVENTORY_VIEW,
                Permission.PUTAWAY_VIEW, Permission.PUTAWAY_EXECUTE,
                Permission.PICKING_VIEW, Permission.PICKING_EXECUTE,
                Permission.PACKING_VIEW, Permission.PACKING_EXECUTE,
                Permission.TROLLEYS_VIEW, Permission.TROLLEYS_CREATE, Permission.TROLLEYS_ASSIGN,
                Permission.LABELS_VIEW, Permission.LABELS_PRINT
        ));
    }

    private void upsertRole(String roleName, EnumSet<Permission> permissions) {
        Role role = roleRepository.findByNameIgnoreCase(roleName).orElseGet(Role::new);
        role.setName(roleName);
        role.setPermissions(EnumSet.copyOf(permissions));
        roleRepository.save(role);
    }

    private void seedUser(String username, String password, String roleName) {
        User u = userRepository.findByUsername(username).orElseGet(User::new);
        Role role = roleRepository.findByName(roleName).orElseThrow();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setRole(role);
        userRepository.save(u);
    }

    // ── Warehouse Structure ────────────────────────────────────────────────────
    private void seedWarehouseStructure() {
        Warehouse wh = new Warehouse();
        wh.setName("Main Warehouse");
        wh.setLocation("40 Industrial Ave, Chicago IL");
        wh = warehouseRepository.save(wh);

        Aisle[] aisles = new Aisle[4];
        String[][] zoneAisles = {
            {"Zone A – Ambient",      "A1", "A2"},
            {"Zone B – Refrigerated", "B1", "B2"}
        };
        int aisleIdx = 0;
        for (String[] za : zoneAisles) {
            Zone zone = new Zone();
            zone.setName(za[0]);
            zone.setWarehouse(wh);
            zone = zoneRepository.save(zone);
            for (int i = 1; i < za.length; i++) {
                Aisle aisle = new Aisle();
                aisle.setAisleId(za[i]);
                aisle.setZone(zone);
                aisles[aisleIdx++] = aisleRepository.save(aisle);
            }
        }

        String[] rackLabels = {"A1-R1","A1-R2","A2-R1","A2-R2","B1-R1","B1-R2","B2-R1","B2-R2"};
        int rIdx = 0;
        for (Aisle aisle : aisles) {
            for (int r = 1; r <= 2; r++) {
                Rack rack = new Rack();
                rack.setRackId(rackLabels[rIdx]);
                rack.setAisle(aisle);
                rack = rackRepository.save(rack);
                for (int b = 1; b <= 5; b++) {
                    Bin bin = new Bin();
                    bin.setBarcode(String.format("BIN-%s-%02d", rackLabels[rIdx], b));
                    bin.setRack(rack);
                    bin.setLengthCm(BigDecimal.valueOf(60));
                    bin.setWidthCm(BigDecimal.valueOf(40));
                    bin.setHeightCm(BigDecimal.valueOf(30));
                    bin.setMaxWeightG(BigDecimal.valueOf(25000));
                    bin.setOccupiedVolumeCm3(BigDecimal.ZERO);
                    bin.setOccupiedWeightG(BigDecimal.ZERO);
                    bin.setStatus(Bin.BinStatus.AVAILABLE);
                    binRepository.save(bin);
                }
                rIdx++;
            }
        }
        System.out.println("[DataInitializer] Seeded warehouse: 1 warehouse, 2 zones, 4 aisles, 8 racks, 40 bins");
    }

    private void seedRackCompartments() {
        List<Rack> racks = rackRepository.findAll();
        for (Rack rack : racks) {
            for (int i = 1; i <= 3; i++) {
                RackCompartment comp = new RackCompartment();
                comp.setRack(rack);
                comp.setCompartmentId(String.format("COMP-%s-%02d", rack.getRackId(), i));
                rackCompartmentRepository.save(comp);
            }
        }
        System.out.println("[DataInitializer] Seeded 24 rack compartments");
    }

    private void seedTrolleys() {
        Map<String, String> trolleyAssignments = Map.of(
            "TROLLEY-01", "A1-R1",
            "TROLLEY-02", "A2-R1"
        );

        trolleyAssignments.forEach((trolleyId, rackId) -> {
            Trolley trolley = new Trolley();
            trolley.setTrolleyIdentifier(trolleyId);
            trolley = trolleyRepository.save(trolley);

            for (int i = 1; i <= 3; i++) {
                String compartmentId = String.format("COMP-%s-%02d", rackId, i);
                RackCompartment comp = rackCompartmentRepository
                    .findByCompartmentId(compartmentId)
                    .orElseThrow(() -> new RuntimeException("Compartment not found: " + compartmentId));
                comp.setTrolley(trolley);
                rackCompartmentRepository.save(comp);
            }
        });

        Trolley t3 = new Trolley();
        t3.setTrolleyIdentifier("TROLLEY-03");
        t3 = trolleyRepository.save(t3);

        System.out.println("[DataInitializer] Seeded 3 trolleys");
    }

    // ── SKUs ──────────────────────────────────────────────────────────────────
    private static final Object[][] SKU_DATA = {
        {"SKU-001", "Laptop 15\"",        38, 26,  3, 2100},
        {"SKU-002", "Wireless Mouse",     12,  8,  4,  120},
        {"SKU-003", "USB-C Hub 7-port",   12,  8,  2,  180},
        {"SKU-004", "Mechanical Keyboard",44, 15,  4,  850},
        {"SKU-005", "Monitor 27\"",       65, 39,  6, 5200},
        {"SKU-006", "Webcam 1080p",       14, 10,  8,  280},
        {"SKU-007", "Headset USB",        20, 18,  8,  320},
        {"SKU-008", "Laptop Stand",       26, 22,  4,  380},
        {"SKU-009", "External SSD 1TB",   14,  8,  1,  130},
        {"SKU-010", "Docking Station",    22, 18, 10,  920},
    };

    private void seedSkus() {
        for (Object[] row : SKU_DATA) {
            Sku sku = new Sku();
            sku.setSkuCode((String) row[0]);
            sku.setDescription((String) row[1]);
            sku = skuRepository.save(sku);

            SkuDimension dim = new SkuDimension();
            dim.setSku(sku);
            dim.setLengthCm(BigDecimal.valueOf((int) row[2]));
            dim.setWidthCm(BigDecimal.valueOf((int) row[3]));
            dim.setHeightCm(BigDecimal.valueOf((int) row[4]));
            dim.setWeightG(BigDecimal.valueOf((int) row[5]));
            skuDimensionRepository.save(dim);
        }
        System.out.println("[DataInitializer] Seeded 10 SKUs with dimensions");
    }

    // ── Purchase Orders ────────────────────────────────────────────────────────
    private void seedPurchaseOrders() {
        List<Sku> skus = skuRepository.findAll();
        if (skus.size() < 10) return;

        PurchaseOrder po1 = new PurchaseOrder();
        po1.setPoNumber("PO-2026-001");
        po1.setSupplierName("TechSupply Co.");
        po1.setExpectedArrivalDate(LocalDate.now().minusDays(5));
        po1.setStatus(PurchaseOrderStatus.ACCEPTED);
        po1 = purchaseOrderRepository.save(po1);

        int[][] po1Lines = {{0,50},{1,200},{2,100},{3,80},{4,20}};
        for (int[] l : po1Lines) savePOLine(po1, skus.get(l[0]), l[1]);

        PurchaseOrder po2 = new PurchaseOrder();
        po2.setPoNumber("PO-2026-002");
        po2.setSupplierName("Global Parts Ltd.");
        po2.setExpectedArrivalDate(LocalDate.now().minusDays(3));
        po2.setStatus(PurchaseOrderStatus.ACCEPTED);
        po2 = purchaseOrderRepository.save(po2);

        int[][] po2Lines = {{5,150},{6,100},{7,80},{8,200},{9,60}};
        for (int[] l : po2Lines) savePOLine(po2, skus.get(l[0]), l[1]);

        PurchaseOrder po3 = new PurchaseOrder();
        po3.setPoNumber("PO-2026-003");
        po3.setSupplierName("FutureTech Inc.");
        po3.setExpectedArrivalDate(LocalDate.now().plusDays(5));
        po3.setStatus(PurchaseOrderStatus.ACCEPTED);
        po3 = purchaseOrderRepository.save(po3);

        int[][] po3Lines = {{0,10},{2,30},{4,5}};
        for (int[] l : po3Lines) savePOLine(po3, skus.get(l[0]), l[1]);

        System.out.println("[DataInitializer] Seeded 3 purchase orders with lines");
    }

    private void savePOLine(PurchaseOrder po, Sku sku, int qty) {
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setPurchaseOrder(po);
        line.setSkuId(sku);
        line.setQuantity(qty);
        line = purchaseOrderLineRepository.save(line);
        if (po.getLines() == null) {
            po.setLines(new ArrayList<>());
        }
        po.getLines().add(line);
    }

    private void seedGoodsReceipts() {
        List<PurchaseOrder> pos = purchaseOrderRepository.findAll();
        List<Sku> skus = skuRepository.findAll();
        if (pos.isEmpty() || skus.size() < 10) return;

        PurchaseOrder po1 = pos.stream().filter(p -> "PO-2026-001".equals(p.getPoNumber())).findFirst().orElseThrow();
        GoodsReceipt gr1 = new GoodsReceipt();
        gr1.setGrnNo("GRN-2026-001");
        gr1.setPurchaseOrder(po1);
        gr1 = goodsReceiptRepository.save(gr1);

        for (PurchaseOrderLine line : po1.getLines()) {
            GoodsReceiptLine grLine = new GoodsReceiptLine();
            grLine.setGoodsReceipt(gr1);
            grLine.setSkuId(line.getSkuId());
            grLine.setQuantityReceived(line.getQuantity());
            grLine.setBatchNo("BATCH-2026-001");
            grLine = goodsReceiptLineRepository.save(grLine);
            if (gr1.getLines() == null) gr1.setLines(new ArrayList<>());
            gr1.getLines().add(grLine);
        }

        PurchaseOrder po2 = pos.stream().filter(p -> "PO-2026-002".equals(p.getPoNumber())).findFirst().orElseThrow();
        GoodsReceipt gr2 = new GoodsReceipt();
        gr2.setGrnNo("GRN-2026-002");
        gr2.setPurchaseOrder(po2);
        gr2 = goodsReceiptRepository.save(gr2);

        for (int i = 0; i < 3; i++) {
            PurchaseOrderLine line = po2.getLines().get(i);
            GoodsReceiptLine grLine = new GoodsReceiptLine();
            grLine.setGoodsReceipt(gr2);
            grLine.setSkuId(line.getSkuId());
            grLine.setQuantityReceived(line.getQuantity());
            grLine.setBatchNo("BATCH-2026-002");
            grLine = goodsReceiptLineRepository.save(grLine);
            if (gr2.getLines() == null) gr2.setLines(new ArrayList<>());
            gr2.getLines().add(grLine);
        }

        System.out.println("[DataInitializer] Seeded goods receipts");
    }

    // ── Inventory ─────────────────────────────────────────────────────────────
    private void seedInventory() {
        List<Sku> skus = skuRepository.findAll();
        List<Bin> bins = binRepository.findAll();
        if (skus.isEmpty() || bins.isEmpty()) return;

        int binIdx = 0;
        for (int i = 0; i < 60; i++) {
            Sku sku = skus.get(i % skus.size());
            Bin bin = bins.get(binIdx % 20);
            if (i > 0 && i % 3 == 0) binIdx++;

            Inventory inv = new Inventory();
            inv.setSku(sku);
            inv.setBin(bin);
            inv.setQuantity(1);
            inv.setState(Inventory.InventoryState.AVAILABLE);
            inv.setBatchNo(String.format("BATCH-2026-%03d", (i / 10) + 1));
            inv.setSerialNo("SN-AVAIL-" + (i + 1));
            inventoryRepository.save(inv);
        }

        jdbc.update("UPDATE wms_bin SET occupied_volume_cm3 = 14400 WHERE id IN "
                + "(SELECT sub.id FROM (SELECT id FROM wms_bin ORDER BY id LIMIT 20) sub)");

        for (int i = 0; i < 20; i++) {
            int daysAgo = (i % 6) + 1;
            Inventory inv = new Inventory();
            inv.setSku(skus.get(i % skus.size()));
            inv.setQuantity(1);
            inv.setState(Inventory.InventoryState.SHIPPED);
            inv.setBatchNo(String.format("BATCH-SHIP-%03d", i + 1));
            inv.setSerialNo("SN-SHIP-" + (i + 1));
            Inventory saved = inventoryRepository.save(inv);

            LocalDateTime ts = LocalDateTime.now().minusDays(daysAgo).withHour(11).withMinute(0).withSecond(0).withNano(0);
            jdbc.update("UPDATE wms_inventory SET created_at = ?, updated_at = ? WHERE id = ?",
                    ts, ts, saved.getId());
        }

        System.out.println("[DataInitializer] Seeded 60 AVAILABLE + 20 SHIPPED inventory items");
    }
}