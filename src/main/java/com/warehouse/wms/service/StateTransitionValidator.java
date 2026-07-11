package com.warehouse.wms.service;

import com.warehouse.wms.entity.ReturnOrder.ReturnStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StateTransitionValidator {

    private static final Map<ReturnStatus, List<ReturnStatus>> RETURNS_TRANSITIONS = new EnumMap<>(ReturnStatus.class);

    static {
        RETURNS_TRANSITIONS.put(ReturnStatus.RETURN_REQUESTED, Arrays.asList(ReturnStatus.AWAITING_PICKUP, ReturnStatus.CLOSED));
        RETURNS_TRANSITIONS.put(ReturnStatus.AWAITING_PICKUP, Arrays.asList(ReturnStatus.IN_TRANSIT, ReturnStatus.CLOSED));
        RETURNS_TRANSITIONS.put(ReturnStatus.IN_TRANSIT, Arrays.asList(ReturnStatus.RECEIVED, ReturnStatus.CLOSED));
        RETURNS_TRANSITIONS.put(ReturnStatus.RECEIVED, Arrays.asList(ReturnStatus.INSPECTING, ReturnStatus.CLOSED));
        RETURNS_TRANSITIONS.put(ReturnStatus.INSPECTING, Arrays.asList(ReturnStatus.RESTOCKED, ReturnStatus.SCRAPPED, ReturnStatus.REJECTED));
        RETURNS_TRANSITIONS.put(ReturnStatus.RESTOCKED, Arrays.asList(ReturnStatus.REFUND_TRIGGERED, ReturnStatus.CLOSED));
        RETURNS_TRANSITIONS.put(ReturnStatus.SCRAPPED, Arrays.asList(ReturnStatus.REFUND_TRIGGERED, ReturnStatus.CLOSED));
        RETURNS_TRANSITIONS.put(ReturnStatus.REJECTED, Arrays.asList(ReturnStatus.CLOSED));
        RETURNS_TRANSITIONS.put(ReturnStatus.REFUND_TRIGGERED, Arrays.asList(ReturnStatus.CLOSED));
        RETURNS_TRANSITIONS.put(ReturnStatus.CLOSED, Collections.emptyList());
    }

    public boolean isValidTransition(ReturnStatus from, ReturnStatus to) {
        if (from == to) return true;
        List<ReturnStatus> allowed = RETURNS_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public List<ReturnStatus> getAllowedNextStates(ReturnStatus current) {
        return RETURNS_TRANSITIONS.getOrDefault(current, Collections.emptyList());
    }
}
