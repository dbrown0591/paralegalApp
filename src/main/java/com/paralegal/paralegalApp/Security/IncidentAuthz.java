// src/main/java/com/paralegal/paralegalApp/Security/IncidentAuthz.java
package com.paralegal.paralegalApp.Security;

import com.paralegal.paralegalApp.Repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("incidentAuthz")
@RequiredArgsConstructor
public class IncidentAuthz {

    private final IncidentRepository incidentRepository;

    private boolean isOwner(Authentication auth, Long incidentId) {
        if (auth == null || incidentId == null) return false;
        var email = auth.getName(); // your JWT username = email
        return incidentRepository.existsByIdAndReportedBy(incidentId, email);
    }

    public boolean canView(Authentication auth, Long id)   { return isOwner(auth, id); }
    public boolean canEdit(Authentication auth, Long id)   { return isOwner(auth, id); }
    public boolean canDelete(Authentication auth, Long id) { return isOwner(auth, id); }
}
