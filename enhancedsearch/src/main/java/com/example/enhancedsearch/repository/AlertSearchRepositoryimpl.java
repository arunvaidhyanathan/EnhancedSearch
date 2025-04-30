package com.example.enhancedsearch.repository;

import java.sql.SQLException;
import java.sql.Struct;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.example.enhancedsearch.dto.SearchCriteria;
import com.example.enhancedsearch.dto.StructAndArrayDummy;
import com.example.enhancedsearch.model.AppFieldMaster;
import com.example.enhancedsearch.model.CurrencyBuDto;
import com.example.enhancedsearch.model.EnhancedSearchField;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


import com.example.enhancedsearch.model.FilterCriteria;
import com.example.enhancedsearch.model.UserBuCriteria;

import javax.sql.DataSource;

import java.sql.Array;
import java.sql.Connection; 

public class AlertSearchRepositoryimpl implements AlertSearchRepository {

    private final JdbcTemplate jdbcTemplate;

    private StructAndArrayDummy structAndArrayDummy;

    private static final String WFS_FILTER_TAB_TYPE = "WFS_FILTER_TAB_TYPE";
    private static final String WFS_FILTER_REC_TYPE = "WFS_FILTER_REC_TYPE";
    private static final String WFS_SORT_TAB_TYPE = "WFS_SORT_TAB_TYPE";
    private static final String WFS_SORT_REC_TYPE = "WFS_SORT_REC_TYPE";

    @Autowired
    public AlertSearchRepositoryimpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AppFieldMaster> loadAppFieldMaster() {
        String sql = "Select field_name,description, date_type, operators, field_size,table_name, column_name, query, argument, created_by, created_date, column_width, text_align, gui_identifier"
+ "from (Select ap.* rank over (partition by table_name, column_name order by gc.field_name nulls last)rnk"
+ "from ARR Field Met ap (Select distinct field_name from grid_column)gc where ap.field_name = gc.field_name and rnk =1";
return jdbcTemplate.query(sql,new BeanPropertyRowMapper<AppFieldMaster> (AppFieldMaster.class));
}

    @Override
    public List<AppFieldMaster> loadEnhancedSearchFieldMaster() {
        String sql = "Select field_name, description, data_type, operators, field_size, table_name, column_name, argument, created_by, created_date, column_width, text_align, gui_identifier"
        + "from (Select ap ank l over (partition bu table name column name order by as field name mulls last link"
        + "From ENHANCED_SEARCH_FIELD_MST ap(Select distinct field name from grid column) gc where ap.field_name = gc.field_name and rnk =1";
        return jdbcTemplate.query(sql,new BeanPropertyRowMapper<AppFieldMaster>(AppFieldMaster.class));
        }

@Override 
public List<AppFieldMaster> loadEnhancedSearchFieldMasterForFilteredFields(){
String sql = "Select field_name, description, data_type, operators, field_size, table_name, column_name, argument, created_by, created_date, column_width, text_align, gui_identifier"
+ "from (Select ap*, rank() over (partition by table name, column name order by ge field name nulls last link."
+ "from ENHANCED_SEARCH_FIELD_MST ap(Select distinct field name from grid column) gc where ap.field_name = gc.field_name and rnk =1";
return jdbcTemplate.query(sql,new BeanPropertyRowMapper<AppFieldMaster>(AppFieldMaster.class));
    
}

@Override 
public List<Map<String, Object>> getAlertsByCriteria(String smUserld, SearchCriteria critera) 
{   
    List<Map<String, Object>> data = null;
    return data;
}

@Override 
public List<CurrencyBuDto> getAvaliableCurrencyOfUserData (String userld) {
        String sql="select distinct currency currency from BU_CURRENCY_MAP cbu"
        + "where cbu.business_unit in (select ubu.bu_identifier from user_bu_map ubu where ubu.user_identifier = :userld)."
        + "order by 1";

    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("userld", userld);
    return namedJdbcTemplate.query(sql, parameters, new BeanPropertyRowMapper<CurrencyBuDto>(CurrencyBuDto.class));
}

@Override 
public List<CurrencyBuDto> getBusOfUserData(String userld) {
    String sql="select ubu.bu_identifier from user_bu_map ubu where ubu.user_identifier = :userld order by 1";
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("userld", userld);
    return namedJdbcTemplate.query(sql, parameters, new BeanPropertyRowMapper<CurrencyBuDto>(CurrencyBuDto.class));
}

@Override
public List<Map<String, Object>> getSortableListData() {
    String sql = "select field_name, gui_identifier from ENHANCED_SEARCH_FIELD_MST where is_sortable = 'Y'";
    return jdbcTemplate.queryForList(sql);
}

@Override
public List<UserBuCriteria> getUserBuData(List<String> userlds) {
    String sql="select user_identifier userld bu_identifier business_unit created_date created_date created_by created_by from user_bu_map where user_identifier in (:userlds)";
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("userlds", userlds);
    return namedJdbcTemplate.query(sql, parameters, new BeanPropertyRowMapper<UserBuCriteria>(UserBuCriteria.class));
}

@Override 
public List<EnhancedSearchField> loadEnhancedSearchFieldMasterData() {
    String sql="select e.field_group, e.field_name, e.field_type, e.display_order, g.gui_identifier, e.is_sortable, e.collection_name, g.label, e.regex_enabled from CADS.enhanced_search_fields e,"+ 
"cads.gui_label g where e.gui_identifier=g.gui_identifier and e.is_active='Y' group by e.field_group, e.field_name, e.field_type, e.display_order, g.gui_identifier, e.is_sortable, "+
"e.collection_name, g.label, e.regex_enabled order by e.display_order";
    return jdbcTemplate.query(sql, new BeanPropertyRowMapper<EnhancedSearchField>(EnhancedSearchField.class));
}

/*private OracleConnection getOracleConnection() throws SQLException {
    DataSource ds = jdbcTemplate.getDataSource();
    Connection connection = DataSourceUtils.getConnection(ds);
    if (!connection.isWrapperFor(OracleConnection.class)) {
        throw new RuntimeException("Exception while creating connection to DB");
    }
    return connection.unwrap(OracleConnection.class);
}

private Array copyToFilterArray (SearchCriteria criteria, OracleConnection oracleConnection) throws SQLException {
    if(criteria.getFilterList() != null && !criteria.getFilterList().isEmpty()) {
        Struct[] filterArray = new Struct[criteria.getFilterList().size()];
        int j=0;
        for(FilterCriteria filterCriteria : criteria.getFilterList()) {
            //StructDescriptor structDescriptor = structDescriptorDummy.createDescriptor(WFS_FILTER_REC_TYPE, oracleConnection);
            if(filterCriteria.getValue() != null && !filterCriteria.getValue().isEmpty()) {
                Object[] attributes = {
                    filterCriteria.getProperty(),
                    filterCriteria.getValue(),
                    filterCriteria.getOperator().toOperator().toUpperCase()
                };
                filterArray[j++] = structAndArrayDummy.createStruct(WFS_FILTER_REC_TYPE, oracleConnection, attributes);
            }
        }
        //ArrayDescriptor filterArraydesc = arrayDescriptorDummy.createDescriptor(WFS_FILTER_TAB_TYPE, oracleConnection);
        return structAndArrayDummy.createArray(WFS_FILTER_TAB_TYPE, oracleConnection, filterArray);
    }
    return null;
}*/
}
