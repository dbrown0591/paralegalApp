package com.paralegal.paralegalApp.Model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paralegal.paralegalApp.Enum.IncidentStatus;
import com.paralegal.paralegalApp.Enum.SeverityLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incident")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "ReportedBy is required")
    @JsonProperty("reportedBy")
    @JsonAlias({"reported_by"})
    private String reportedBy;  //User

    @PastOrPresent
    private LocalDateTime createdAt;

    @NotBlank(message = "Incident type is required")
    @JsonProperty("incidentType")
    @JsonAlias({"incident_type"})
    private String incidentType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JsonProperty("severityLevel")
    @JsonAlias({"severity_level"})
    private SeverityLevel severityLevel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JsonProperty("status")
    @JsonAlias({"incident_status"})
    private IncidentStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "incident_id")
    private List<Evidence> evidence = new ArrayList<>();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @NotBlank(message = "Location is required")
    @JsonProperty("location")
    @JsonAlias({"location"})
    private String location;

    @Size(max = 500, message = "Description must be 500 characters or less")
    @JsonProperty("description")
    @JsonAlias({"description"})
    private String description;
    @PrePersist
    void prePersist(){
        if(createdAt == null) createdAt = LocalDateTime.now();
        if(status == null) status = IncidentStatus.OPEN;
        if(severityLevel == null) severityLevel = SeverityLevel.MEDIUM;
    }


}
