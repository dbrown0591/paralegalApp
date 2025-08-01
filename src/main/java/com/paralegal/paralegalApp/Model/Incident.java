package com.paralegal.paralegalApp.Model;

import com.paralegal.paralegalApp.Enum.IncidentStatus;
import com.paralegal.paralegalApp.Enum.SeverityLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table()
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ID;

    private String reportedBy;  //User

    private LocalDateTime createdAt;

    private String incidentType;

    @Enumerated(EnumType.STRING)
    private SeverityLevel severityLevel;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    private List<Evidence> evidenceList;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL)
    private List<Comment> comments;

    private String location;

    private String description;
}
