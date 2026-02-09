package com.ipomanagement.ipo_management_system.service.audit;

import com.ipomanagement.ipo_management_system.domain.entity.AuditTrail;
import com.ipomanagement.ipo_management_system.domain.entity.User;
import com.ipomanagement.ipo_management_system.repository.AuditTrailRepository;
import com.ipomanagement.ipo_management_system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditTrailRepository auditTrailRepository;
    private final UserRepository userRepository;

    public AuditService(AuditTrailRepository auditTrailRepository, UserRepository userRepository) {
        this.auditTrailRepository = auditTrailRepository;
        this.userRepository = userRepository;
    }

    public void log(Long userId, String action, String entityType, Long entityId, String description) {
        AuditTrail a = new AuditTrail();

        if (userId != null) {
            User u = userRepository.findById(userId).orElse(null);
            a.setUser(u);
        }

        a.setAction(action);
        a.setEntityType(entityType);
        a.setEntityId(entityId);
        a.setDescription(description);

        auditTrailRepository.save(a);
    }

    public List<AuditTrail> latest200() {
        return auditTrailRepository.findTop200ByOrderByCreatedAtDesc();
    }
}