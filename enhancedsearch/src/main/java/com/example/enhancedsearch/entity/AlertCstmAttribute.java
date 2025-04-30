package com.example.enhancedsearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "alert_cstm_attribute", schema = "cads")
@Getter
@Setter
public class AlertCstmAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_id", nullable = false)
    private AlertMst alertMst;

    // Maps to "Transaction Message" in UI via PP_MESSAGE field
    @Column(name = "PP_MESSAGE")
    private String ppMessage;

    public void setAlertMst(AlertMst alertMst) {
        this.alertMst = alertMst;
    }

    // Add other custom attribute fields if necessary

    // Consider overriding equals() and hashCode()
}
