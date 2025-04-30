package com.example.enhancedsearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "alert_det", schema = "cads")
@Getter
@Setter
public class AlertDet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // optional=false enforces FK constraint
    @JoinColumn(name = "alert_id", nullable = false) // FK column name
    private AlertMst alertMst;

    @Column(name = "FIELD_NAME_ALIAS", nullable = false)
    private String fieldNameAlias; // e.g., "BENEFICIARY_NAME"

    @Column(name = "FIELD_VALUE")
    private String fieldValue; // The value associated with the alias

    public void setAlertMst(AlertMst alertMst) {
        this.alertMst = alertMst;
    }

    // Consider overriding equals() and hashCode()
}
