package com.oracle.www.apiempleados.repository;

import com.oracle.www.apiempleados.entity.Employee;
import com.oracle.www.apiempleados.utils.EmployeeProjection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class EmployeeDeptEmpRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // COMPLETAMENTE VULNERABLE - Concatenación directa pura
    public Page<EmployeeProjection> findAllEmployees(
            Integer empNo,
            LocalDate birthDate,
            String firstName,
            String lastName,
            String gender,
            LocalDate hireDate,
            String deptNo,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {

        // Construcción dinámica vulnerable
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.emp_no as empNo, ");
        sql.append("e.birth_date as birthDate, ");
        sql.append("e.first_name as firstName, ");
        sql.append("e.last_name as lastName, ");
        sql.append("e.gender as gender, ");
        sql.append("e.hire_date as hireDate, ");
        sql.append("de.dept_no as deptNo, ");
        sql.append("de.from_date as fromDate, ");
        sql.append("de.to_date as toDate ");
        sql.append("FROM employees e ");
        sql.append("LEFT JOIN dept_emp de ON e.emp_no = de.emp_no ");
        sql.append("WHERE 1=1 ");

        // Todas las condiciones concatenadas directamente SIN SANITIZACIÓN
        if (empNo != null) {
            sql.append(" AND e.emp_no = ").append(empNo);
        }

        if (birthDate != null) {
            sql.append(" AND e.birth_date = '").append(birthDate).append("'");
        }

        if (firstName != null && !firstName.isEmpty()) {
            // Vulnerable a ' OR '1'='1
            sql.append(" AND e.first_name LIKE '%").append(firstName).append("%'");
        }

        if (lastName != null && !lastName.isEmpty()) {
            sql.append(" AND e.last_name LIKE '%").append(lastName).append("%'");
        }

        if (gender != null && !gender.isEmpty()) {
            sql.append(" AND UPPER(TRIM(e.gender)) = UPPER(TRIM('").append(gender).append("'))");
        }

        if (hireDate != null) {
            sql.append(" AND e.hire_date = '").append(hireDate).append("'");
        }

        if (deptNo != null && !deptNo.isEmpty()) {
            sql.append(" AND de.dept_no = '").append(deptNo).append("'");
        }

        if (fromDate != null) {
            sql.append(" AND de.from_date >= '").append(fromDate).append("'");
        }

        if (toDate != null) {
            sql.append(" AND de.to_date <= '").append(toDate).append("'");
        }

        // Agregar paginación también de forma vulnerable
        sql.append(" LIMIT ").append(pageable.getPageSize());
        sql.append(" OFFSET ").append(pageable.getOffset());

        System.out.println("SQL Ejecutado: " + sql.toString());

        // Ejecución directa sin parámetros
        Query nativeQuery = entityManager.createNativeQuery(sql.toString(), "EmployeeProjection");

        List<EmployeeProjection> results = nativeQuery.getResultList();

        // Count query también vulnerable
        String countSql = "SELECT COUNT(DISTINCT e.emp_no) FROM employees e " +
                "LEFT JOIN dept_emp de ON e.emp_no = de.emp_no WHERE 1=1";

        if (empNo != null) {
            countSql += " AND e.emp_no = " + empNo;
        }
        if (firstName != null && !firstName.isEmpty()) {
            countSql += " AND e.first_name LIKE '%" + firstName + "%'";
        }

        Query countQuery = entityManager.createNativeQuery(countSql);
        Long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(results, pageable, total);
    }

    // Método adicional vulnerable usando String.format
    public List<?> findEmployeesByRawQuery(String whereClause) {
        // EXTREMADAMENTE VULNERABLE - El usuario controla parte del WHERE
        String sql = String.format("SELECT * FROM employees WHERE %s", whereClause);
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    // Método vulnerable con ORDER BY dinámico
    public List<?> findAllOrderedBy(String columnName, String sortOrder) {
        String sql = "SELECT * FROM employees ORDER BY " + columnName + " " + sortOrder;
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    // Método vulnerable con tabla dinámica (sí, se puede hacer SQL Injection en nombres de tabla)
    public List<?> findFromTable(String tableName) {
        String sql = "SELECT * FROM " + tableName; // Vulnerable a UNION-based attacks
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    // Método vulnerable con inyección en LIMIT/OFFSET
    public List<?> findWithPagination(String limit, String offset) {
        String sql = "SELECT * FROM employees LIMIT " + limit + " OFFSET " + offset;
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }

    public int maxEmpNo() {
        Query query = entityManager.createNativeQuery("SELECT COALESCE(MAX(emp_no), 0) FROM employees");
        return ((Number) query.getSingleResult()).intValue();
    }
}