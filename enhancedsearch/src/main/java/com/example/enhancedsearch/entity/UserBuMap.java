package com.example.enhancedsearch.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user_bu_map", schema = "cads")
@Getter
@Setter
public class UserBuMap {

    @Id
    @Column(name = "user_identifier")
    private String userIdentifier;

    @Column(name = "bu_identifier")
    private String buIdentifier;

    @Column(name = "valid_from")
    private Date validFrom;

    @Column(name = "valid_to")
    private Date validTo;

    @Column(name = "username")
    private String username;

    @Column(name = "modifiedby")
    private String modifiedby;

    @Column(name = "modifiedtime")
    private Date modifiedtime;
}