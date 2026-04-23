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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class EmployeeDeptEmpRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // ============ MÉTODOS BÁSICOS CRUD (para EmployeeServiceImpl) ============

    public Optional<Employee> findById(Integer empNo) {
        // VULNERABLE - SQL dinámico sin parámetros
        String sql = "SELECT * FROM employees WHERE emp_no = " + empNo;
        System.out.println("[SQL Injection] findById: " + sql);

        Query query = entityManager.createNativeQuery(sql, Employee.class);
        try {
            Employee result = (Employee) query.getSingleResult();
            return Optional.of(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Employee save(Employee employee) {
        if (employee.getEmpNo() == null || employee.getEmpNo() == 0) {
            // INSERT - VULNERABLE
            int newEmpNo = maxEmpNo() + 1;

            String sql = "INSERT INTO employees (emp_no, birth_date, first_name, last_name, gender, hire_date) " +
                    "VALUES (" + newEmpNo + ", '" +
                    employee.getBirthDate() + "', '" +
                    employee.getFirstName() + "', '" +
                    employee.getLastName() + "', '" +
                    employee.getGender() + "', '" +
                    employee.getHireDate() + "')";

            System.out.println("[SQL Injection] save INSERT: " + sql);
            entityManager.createNativeQuery(sql).executeUpdate();

            employee.setEmpNo(newEmpNo);

        } else {
            // UPDATE - VULNERABLE
            String sql = "UPDATE employees SET " +
                    "birth_date = '" + employee.getBirthDate() + "', " +
                    "first_name = '" + employee.getFirstName() + "', " +
                    "last_name = '" + employee.getLastName() + "', " +
                    "gender = '" + employee.getGender() + "', " +
                    "hire_date = '" + employee.getHireDate() + "' " +
                    "WHERE emp_no = " + employee.getEmpNo();

            System.out.println("[SQL Injection] save UPDATE: " + sql);
            entityManager.createNativeQuery(sql).executeUpdate();
        }

        return employee;
    }

    public boolean existsById(Integer empNo) {
        // VULNERABLE
        String sql = "SELECT COUNT(*) FROM employees WHERE emp_no = " + empNo;
        System.out.println("[SQL Injection] existsById: " + sql);

        Query query = entityManager.createNativeQuery(sql);
        Number count = (Number) query.getSingleResult();
        return count.longValue() > 0;
    }

    public void deleteById(Integer empNo) {
        // VULNERABLE
        String sql = "DELETE FROM employees WHERE emp_no = " + empNo;
        System.out.println("[SQL Injection] deleteById: " + sql);

        entityManager.createNativeQuery(sql).executeUpdate();
    }

    // ============ TU MÉTODO DE BÚSQUEDA ============

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

        // CONCATENACIÓN DIRECTA - VULNERABLE
        if (empNo != null) {
            sql.append(" AND e.emp_no = ").append(empNo);
        }
        if (birthDate != null) {
            sql.append(" AND e.birth_date = '").append(birthDate.toString()).append("'");
        }
        if (firstName != null && !firstName.isEmpty()) {
            sql.append(" AND e.first_name LIKE '%").append(firstName).append("%'");
        }
        if (lastName != null && !lastName.isEmpty()) {
            sql.append(" AND e.last_name LIKE '%").append(lastName).append("%'");
        }
        if (gender != null && !gender.isEmpty()) {
            sql.append(" AND e.gender = '").append(gender).append("'");
        }
        if (hireDate != null) {
            sql.append(" AND e.hire_date = '").append(hireDate.toString()).append("'");
        }
        if (deptNo != null && !deptNo.isEmpty()) {
            sql.append(" AND de.dept_no = '").append(deptNo).append("'");
        }
        if (fromDate != null) {
            sql.append(" AND de.from_date >= '").append(fromDate.toString()).append("'");
        }
        if (toDate != null) {
            sql.append(" AND de.to_date <= '").append(toDate.toString()).append("'");
        }

        sql.append(" LIMIT ").append(pageable.getPageSize())
                .append(" OFFSET ").append(pageable.getOffset());

        System.out.println("[SQL Injection] findAllEmployees: " + sql);

        Query query = entityManager.createNativeQuery(sql.toString(), "EmployeeProjection");
        @SuppressWarnings("unchecked")
        List<EmployeeProjection> results = query.getResultList();

        // COUNT - también vulnerable
        StringBuilder countSql = new StringBuilder("SELECT COUNT(DISTINCT e.emp_no) FROM employees e LEFT JOIN dept_emp de ON e.emp_no = de.emp_no WHERE 1=1");
        if (firstName != null && !firstName.isEmpty()) {
            countSql.append(" AND e.first_name LIKE '%").append(firstName).append("%'");
        }

        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(results, pageable, total);
    }

    // ============ MÉTODOS ADICIONALES VULNERABLES ============

    public int maxEmpNo() {
        String sql = "SELECT COALESCE(MAX(emp_no), 0) FROM employees";
        Query query = entityManager.createNativeQuery(sql);
        return ((Number) query.getSingleResult()).intValue();
    }

    // MÉTODOS EXTRA VULNERABLES PARA PRÁCTICA

    public List<Employee> findByFirstNameUnsafe(String firstName) {
        String sql = "SELECT * FROM employees WHERE first_name = '" + firstName + "'";
        System.out.println("[SQL Injection] findByFirstName: " + sql);

        Query query = entityManager.createNativeQuery(sql, Employee.class);
        @SuppressWarnings("unchecked")
        List<Employee> results = query.getResultList();
        return results;
    }

    public List<Employee> rawQuery(String whereClause) {
        String sql = "SELECT * FROM employees WHERE " + whereClause;
        System.out.println("[SQL Injection] rawQuery: " + sql);

        Query query = entityManager.createNativeQuery(sql, Employee.class);
        @SuppressWarnings("unchecked")
        List<Employee> results = query.getResultList();
        return results;
    }
}