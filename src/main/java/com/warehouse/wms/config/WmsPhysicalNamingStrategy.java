package com.warehouse.wms.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class WmsPhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        Identifier basic = super.toPhysicalTableName(logicalName, jdbcEnvironment);
        if (basic == null) {
            return null;
        }
        String name = basic.getText();
        
        // Map _user or user table to wms_user
        if ("_user".equals(name) || "user".equals(name)) {
            return Identifier.toIdentifier("wms_user");
        }
        
        if (!name.startsWith("wms_")) {
            return Identifier.toIdentifier("wms_" + name);
        }
        return basic;
    }
}
