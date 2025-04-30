package com.example.enhancedsearch.entity;

import jakarta.persistence.*;
import lombok.Getter; // Add Getter
import lombok.Setter; // Add Setter

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "alert_mst", schema = "cads")
@Getter // Add Getter
@Setter // Add Setter
public class AlertMst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ALERT_BUSINESS_ID", unique = true) // Assuming this maps to "Alert ID"
    private String alertBusinessId;

    @Column(name = "SOURCE_SYSTEM") // Maps to "Application"
    private String sourceSystem;

    @Column(name = "RECEIVER")
    private String receiver;

    @Column(name = "CREATED_DATE") // Maps to "Creation Date/Time"
    private LocalDateTime createdDate;

    @Column(name = "VALUE_DATE")
    private LocalDateTime valueDate; // Assuming LocalDateTime, adjust if only Date

    @Column(name = "ASSIGNED_TO")
    private String assignedTo;

    @Column(name = "STEP")
    private String step;

    @Column(name = "MATCH_ACCURACY") // Maps to "Match Accuracy %"
    private BigDecimal matchAccuracy; // Use BigDecimal for percentages/precision

    @OneToMany(mappedBy = "alertMst", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AlertDet> alertDets = new HashSet<>(); // Corrected variable name

    @OneToMany(mappedBy = "alertMst", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AlertCstmAttribute> alertCstmAttributes = new HashSet<>();

    @OneToMany(mappedBy = "alertMst", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HitMatchDetails> hitMatchDetails = new HashSet<>();

    // --- Helper methods for bidirectional relationships ---

    // Helper methods will now work because AlertDet etc. will have setAlertMst
    public void addAlertDet(AlertDet alertDet) {
        alertDets.add(alertDet);
        alertDet.setAlertMst(this);
    }

    public void removeAlertDet(AlertDet alertDet) {
        alertDets.remove(alertDet);
        alertDet.setAlertMst(null);
    }

    public void addAlertCstmAttribute(AlertCstmAttribute attribute) {
        alertCstmAttributes.add(attribute);
        attribute.setAlertMst(this);
    }

    public void removeAlertCstmAttribute(AlertCstmAttribute attribute) {
        alertCstmAttributes.remove(attribute);
        attribute.setAlertMst(null);
    }

     public void addHitMatchDetails(HitMatchDetails details) {
        hitMatchDetails.add(details);
        details.setAlertMst(this);
    }

    public void removeHitMatchDetails(HitMatchDetails details) {
        hitMatchDetails.remove(details);
        details.setAlertMst(null);
    }

    // Add other fields from alert_mst as needed
}