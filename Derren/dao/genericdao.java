package com.kopdes.kmp.dao;

import java.sql.SQLException;
import java.util.List;

public interface GenericDAO<T, ID> {
    T findById(ID id) throws SQLException;
    List<T> findAll() throws SQLException;
    T save(T entity) throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean delete(ID id) throws SQLException;
}