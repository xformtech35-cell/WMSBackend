package com.warehouse.wms.config;

import com.warehouse.wms.entity.*;
import com.warehouse.wms.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final InventoryRepository inventoryRepository;
    private final ShipmentRecordRepository shipmentRecordRepository;
    private final RackCompartmentRepository rackCompartmentRepository;
    private final TrolleyRepository trolleyRepository;
    private final PickTaskRepository pickTaskRepository;
    private final PutawayTaskRepository putawayTaskRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
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
            SalesOrderRepository salesOrderRepository,
            SalesOrderLineRepository salesOrderLineRepository,
            InventoryRepository inventoryRepository,
            ShipmentRecordRepository shipmentRecordRepository,
            RackCompartmentRepository rackCompartmentRepository,
            TrolleyRepository trolleyRepository,
            PickTaskRepository pickTaskRepository,
            PutawayTaskRepository putawayTaskRepository,
            GoodsReceiptRepository goodsReceiptRepository,
            GoodsReceiptLineRepository goodsReceiptLineRepository,
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
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.inventoryRepository = inventoryRepository;
        this.shipmentRecordRepository = shipmentRecordRepository;
        this.rackCompartmentRepository = rackCompartmentRepository;
        this.trolleyRepository = trolleyRepository;
        this.pickTaskRepository = pickTaskRepository;
        this.putawayTaskRepository = putawayTaskRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
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
            seedSalesOrdersAndShipments();
            seedInventory();
            seedPutawayTasks();
            seedPickTasks();
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
                aisle.setAisleNumber(za[i]);
                aisle.setZone(zone);
                aisles[aisleIdx++] = aisleRepository.save(aisle);
            }
        }

        String[] rackLabels = {"A1-R1","A1-R2","A2-R1","A2-R2","B1-R1","B1-R2","B2-R1","B2-R2"};
        int rIdx = 0;
        for (Aisle aisle : aisles) {
            for (int r = 1; r <= 2; r++) {
                Rack rack = new Rack();
                rack.setRackIdentifier(rackLabels[rIdx]);
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
                comp.setCompartmentIdentifier(String.format("COMP-%s-%02d", rack.getRackIdentifier(), i));
                rackCompartmentRepository.save(comp);
            }
        }
        System.out.println("[DataInitializer] Seeded 24 rack compartments");
    }

    private void seedTrolleys() {
        Trolley t1 = new Trolley();
        t1.setTrolleyIdentifier("TROLLEY-01");
        t1 = trolleyRepository.save(t1);

        for (int i = 1; i <= 3; i++) {
            RackCompartment comp = rackCompartmentRepository.findByCompartmentIdentifier(String.format("COMP-A1-R1-%02d", i)).orElseThrow();
            comp.setTrolley(t1);
            rackCompartmentRepository.save(comp);
        }

        Trolley t2 = new Trolley();
        t2.setTrolleyIdentifier("TROLLEY-02");
        t2 = trolleyRepository.save(t2);

        for (int i = 1; i <= 3; i++) {
            RackCompartment comp = rackCompartmentRepository.findByCompartmentIdentifier(String.format("COMP-A2-R1-%02d", i)).orElseThrow();
            comp.setTrolley(t2);
            rackCompartmentRepository.save(comp);
        }

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
        po1.setSupplier("TechSupply Co.");
        po1.setExpectedArrivalDate(LocalDate.now().minusDays(5));
        po1.setStatus("RECEIVED");
        po1 = purchaseOrderRepository.save(po1);

        int[][] po1Lines = {{0,50},{1,200},{2,100},{3,80},{4,20}};
        for (int[] l : po1Lines) savePOLine(po1, skus.get(l[0]), l[1]);

        PurchaseOrder po2 = new PurchaseOrder();
        po2.setPoNumber("PO-2026-002");
        po2.setSupplier("Global Parts Ltd.");
        po2.setExpectedArrivalDate(LocalDate.now().minusDays(3));
        po2.setStatus("PARTIALLY_RECEIVED");
        po2 = purchaseOrderRepository.save(po2);

        int[][] po2Lines = {{5,150},{6,100},{7,80},{8,200},{9,60}};
        for (int[] l : po2Lines) savePOLine(po2, skus.get(l[0]), l[1]);

        PurchaseOrder po3 = new PurchaseOrder();
        po3.setPoNumber("PO-2026-003");
        po3.setSupplier("FutureTech Inc.");
        po3.setExpectedArrivalDate(LocalDate.now().plusDays(5));
        po3.setStatus("OPEN");
        po3 = purchaseOrderRepository.save(po3);

        int[][] po3Lines = {{0,10},{2,30},{4,5}};
        for (int[] l : po3Lines) savePOLine(po3, skus.get(l[0]), l[1]);

        System.out.println("[DataInitializer] Seeded 3 purchase orders with lines");
    }

    private void savePOLine(PurchaseOrder po, Sku sku, int qty) {
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setPurchaseOrder(po);
        line.setSku(sku);
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
            grLine.setSku(line.getSku());
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
            grLine.setSku(line.getSku());
            grLine.setQuantityReceived(line.getQuantity());
            grLine.setBatchNo("BATCH-2026-002");
            grLine = goodsReceiptLineRepository.save(grLine);
            if (gr2.getLines() == null) gr2.setLines(new ArrayList<>());
            gr2.getLines().add(grLine);
        }

        System.out.println("[DataInitializer] Seeded goods receipts");
    }

    // ── Sales Orders & Shipments ───────────────────────────────────────────────
    private void seedSalesOrdersAndShipments() {
        List<Sku> skus = skuRepository.findAll();
        if (skus.size() < 10) return;

        String[][] orderData = {
            {"SO-2026-001", "Acme Corporation",    "SHIPPED",  "6"},
            {"SO-2026-002", "GlobalTech Ltd",      "SHIPPED",  "5"},
            {"SO-2026-003", "Metro Electronics",   "SHIPPED",  "3"},
            {"SO-2026-004", "DataCenter Pro",      "PACKING",  "0"},
            {"SO-2026-005", "StartupHub Inc",      "PICKING",  "0"},
            {"SO-2026-006", "TechVentures",        "PENDING",  "0"},
            {"SO-2026-007", "CloudBase Ltd",       "PENDING",  "0"},
            {"SO-2026-008", "NetSystems Ltd",      "PENDING",  "0"},
        };

        int[][] soLines = {
            {0,2},{1,5},  {2,3},{4,1},  {3,2},{5,4},  {6,3},{7,2},
            {0,1},{8,5},  {1,10},{2,7}, {4,2},{9,3},  {3,4},{6,2},
        };

        SalesOrder[] savedOrders = new SalesOrder[8];
        for (int i = 0; i < orderData.length; i++) {
            String[] d = orderData[i];
            int days = Integer.parseInt(d[3]);
            SalesOrder so = new SalesOrder();
            so.setSoNumber(d[0]);
            so.setCustomerName(d[1]);
            so.setOrderDate(LocalDate.now().minusDays(days));
            so.setStatus(d[2]);
            savedOrders[i] = salesOrderRepository.save(so);

            SalesOrderLine l1 = new SalesOrderLine();
            l1.setSalesOrder(savedOrders[i]);
            l1.setSku(skus.get(soLines[i * 2][0]));
            l1.setQuantity(soLines[i * 2][1]);
            l1 = salesOrderLineRepository.save(l1);

            SalesOrderLine l2 = new SalesOrderLine();
            l2.setSalesOrder(savedOrders[i]);
            l2.setSku(skus.get(soLines[i * 2 + 1][0]));
            l2.setQuantity(soLines[i * 2 + 1][1]);
            l2 = salesOrderLineRepository.save(l2);

            if (savedOrders[i].getLines() == null) {
                savedOrders[i].setLines(new ArrayList<>());
            }
            savedOrders[i].getLines().add(l1);
            savedOrders[i].getLines().add(l2);
        }

        for (int i = 0; i < 3; i++) {
            int days = Integer.parseInt(orderData[i][3]);
            LocalDateTime ts = LocalDateTime.now().minusDays(days).withHour(9).withMinute(0).withSecond(0).withNano(0);
            jdbc.update("UPDATE wms_sales_order SET created_at = ? WHERE id = ?", ts, savedOrders[i].getId());
        }

        String[] couriers = {"DHL", "FedEx", "UPS"};
        for (int i = 0; i < 3; i++) {
            int days = Integer.parseInt(orderData[i][3]);
            ShipmentRecord sr = new ShipmentRecord();
            sr.setSalesOrder(savedOrders[i]);
            sr.setAwbNumber(String.format("AWB%04d%04d", i + 1, savedOrders[i].getId()));
            sr.setCourierName(couriers[i]);
            ShipmentRecord saved = shipmentRecordRepository.save(sr);
            LocalDateTime shipTs = LocalDateTime.now().minusDays(days).withHour(14).withMinute(0).withSecond(0).withNano(0);
            jdbc.update("UPDATE wms_shipment_record SET created_at = ? WHERE id = ?", shipTs, saved.getId());
        }

        System.out.println("[DataInitializer] Seeded 8 sales orders + 3 shipment records");
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

    private void seedPutawayTasks() {
        Warehouse wh = warehouseRepository.findAll().get(0);
        List<Bin> bins = binRepository.findAll();
        GoodsReceipt gr2 = goodsReceiptRepository.findByGrnNo("GRN-2026-002").orElseThrow();
        List<GoodsReceiptLine> grLines = gr2.getLines();

        int taskCount = 0;
        for (GoodsReceiptLine grLine : grLines) {
            Inventory inv = new Inventory();
            inv.setSku(grLine.getSku());
            inv.setQuantity(grLine.getQuantityReceived());
            inv.setState(Inventory.InventoryState.IN_PUTAWAY);
            inv.setBatchNo(grLine.getBatchNo());
            inv.setSerialNo("SN-PUTAWAY-" + grLine.getId());
            inv.setGoodsReceiptLine(grLine);
            inv = inventoryRepository.save(inv);

            PutawayTask task = new PutawayTask();
            task.setInventory(inv);
            task.setSuggestedBin(bins.get(30 + taskCount));
            task.setStatus(PutawayTask.PutawayTaskStatus.PENDING);
            task.setPriority(1);
            task.setWarehouse(wh);
            putawayTaskRepository.save(task);
            taskCount++;
        }
        System.out.println("[DataInitializer] Seeded " + taskCount + " pending putaway tasks");
    }

    private void seedPickTasks() {
        SalesOrder so4 = salesOrderRepository.findBySoNumber("SO-2026-004").orElseThrow();
        SalesOrder so5 = salesOrderRepository.findBySoNumber("SO-2026-005").orElseThrow();
        Trolley trolley1 = trolleyRepository.findByTrolleyIdentifier("TROLLEY-01").orElseThrow();
        List<RackCompartment> compartments = rackCompartmentRepository.findByTrolleyId(trolley1.getId());

        compartments.get(0).setSalesOrder(so4);
        rackCompartmentRepository.save(compartments.get(0));

        int compIdx = 0;
        for (SalesOrderLine line : so4.getLines()) {
            Inventory inv = inventoryRepository.findAll().stream()
                    .filter(i -> i.getSku().getId().equals(line.getSku().getId()) && i.getState() == Inventory.InventoryState.AVAILABLE)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No available inventory for SKU " + line.getSku().getSkuCode()));

            inv.setState(Inventory.InventoryState.PICKED);
            inv.setSerialNo("SN-PICKED-" + line.getId());
            inventoryRepository.save(inv);

            PickTask task = new PickTask();
            task.setSalesOrderLine(line);
            task.setInventory(inv);
            task.setBinBarcode(inv.getBin().getBarcode());
            task.setSkuCode(line.getSku().getSkuCode());
            task.setQuantityToPick(line.getQuantity());
            task.setStatus("COMPLETED");
            task.setTrolley(trolley1);
            task.setRackCompartment(compartments.get(compIdx));
            pickTaskRepository.save(task);
        }

        compartments.get(1).setSalesOrder(so5);
        rackCompartmentRepository.save(compartments.get(1));

        boolean first = true;
        for (SalesOrderLine line : so5.getLines()) {
            Inventory inv = inventoryRepository.findAll().stream()
                    .filter(i -> i.getSku().getId().equals(line.getSku().getId()) && i.getState() == Inventory.InventoryState.AVAILABLE)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No available inventory for SKU " + line.getSku().getSkuCode()));

            PickTask task = new PickTask();
            task.setSalesOrderLine(line);
            task.setInventory(inv);
            task.setBinBarcode(inv.getBin().getBarcode());
            task.setSkuCode(line.getSku().getSkuCode());
            task.setQuantityToPick(line.getQuantity());

            if (first) {
                inv.setState(Inventory.InventoryState.PICKED);
                inv.setSerialNo("SN-PICKED-" + line.getId());
                inventoryRepository.save(inv);

                task.setStatus("COMPLETED");
                task.setTrolley(trolley1);
                task.setRackCompartment(compartments.get(1));
                first = false;
            } else {
                inv.setState(Inventory.InventoryState.RESERVED);
                inventoryRepository.save(inv);
                task.setStatus("PENDING");
            }
            pickTaskRepository.save(task);
        }

        System.out.println("[DataInitializer] Seeded pick tasks");
    }
}
