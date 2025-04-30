package com.example.enhancedsearch.dto;

import java.sql.Array;
import java.sql.SQLException;
import java.sql.Struct;

import org.springframework.stereotype.Component;

import java.sql.Connection;

@Component
public class StructAndArrayDummy {
    public Struct createStruct(String name, Connection conn, Object[] attributes) throws SQLException {
        return conn.createStruct(name, attributes);
    }

    public Array createArray(String name, Connection conn, Struct[] sortArray) throws SQLException {
        return conn.createArrayOf(name, sortArray);
    }
}
