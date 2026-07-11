package com.warehouse.wms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.wms.entity.AuditLog;
import com.warehouse.wms.entity.User;
import com.warehouse.wms.repository.AuditLogRepository;
import com.warehouse.wms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(com.warehouse.wms.config.AuditLogged)", returning = "result")
    public void logAudit(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            AuditLogged annotation = method.getAnnotation(AuditLogged.class);

            String module = annotation.module();
            String action = annotation.action();
            
            Long userId = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String username = auth.getName();
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    userId = user.getId();
                }
            }
            if (userId == null) {
                userId = 1L; // default fallback
            }

            String newValueJson = null;
            String entityType = "Unknown";
            Long entityId = null;

            if (result != null) {
                try {
                    newValueJson = objectMapper.writeValueAsString(result);
                } catch (Exception ignored) {
                }
                entityType = result.getClass().getSimpleName();
                
                try {
                    Method getIdMethod = result.getClass().getMethod("getId");
                    Object idObj = getIdMethod.invoke(result);
                    if (idObj instanceof Long) {
                        entityId = (Long) idObj;
                    }
                } catch (Exception ignored) {
                }
            }

            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setModule(module.isEmpty() ? entityType : module);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setAction(action.isEmpty() ? method.getName() : action);
            auditLog.setNewValueJson(newValueJson);
            auditLog.setOldValueJson(null);
            auditLog.setTimestamp(LocalDateTime.now());

            auditLogRepository.save(auditLog);
            log.info("[AuditLogAspect] Saved audit log for action={} on entityType={} id={}", 
                    auditLog.getAction(), entityType, entityId);

        } catch (Exception e) {
            log.error("[AuditLogAspect] Error creating audit log", e);
        }
    }
}
