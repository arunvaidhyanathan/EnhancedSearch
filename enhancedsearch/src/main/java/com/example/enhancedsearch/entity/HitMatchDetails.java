package com.example.enhancedsearch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "hit_match_details", schema = "cads")
@Getter
@Setter
public class HitMatchDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_id", nullable = false)
    private AlertMst alertMst;

    // Add other fields relevant to hit_match_details searching if needed
    // Example:
    // @Column(name = "MATCH_FIELD")
    // private String matchField;
    //
    // @Column(name = "MATCH_VALUE")
    // private String matchValue;

    public void setAlertMst(AlertMst alertMst) {
        this.alertMst = alertMst;
    }

    // Consider overriding equals() and hashCode()
}
