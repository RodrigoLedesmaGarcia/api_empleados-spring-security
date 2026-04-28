package com.oracle.www.apiempleados.repository;

import com.oracle.www.apiempleados.entity.Employee;
import com.oracle.www.apiempleados.utils.EmployeeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface EmployeeDeptEmpRepository extends JpaRepository<Employee, Integer> {

    @Query(
            value = """
                SELECT e.emp_no       AS empNo,
                       e.birth_date   AS birthDate,
                       e.first_name   AS firstName,
                       e.last_name    AS lastName,
                       e.gender       AS gender,
                       e.hire_date    AS hireDate,
                       de.dept_no     AS deptNo,
                       de.from_date   AS fromDate,
                       de.to_date     AS toDate
                FROM employees e
                LEFT JOIN dept_emp de 
                       ON e.emp_no = de.emp_no
                      AND de.to_date = '9999-01-01'
                WHERE (:empNo IS NULL OR e.emp_no = :empNo)
                  AND (:birthDate IS NULL OR e.birth_date = :birthDate)
                  AND (:firstName IS NULL OR e.first_name LIKE CONCAT('%', :firstName, '%'))
                  AND (:lastName IS NULL OR e.last_name LIKE CONCAT('%', :lastName, '%'))
                  AND (:gender IS NULL OR UPPER(TRIM(e.gender)) = UPPER(TRIM(:gender)))
                  AND (:hireDate IS NULL OR e.hire_date = :hireDate)
                  AND (:deptNo IS NULL OR de.dept_no = :deptNo)
                  AND (:fromDate IS NULL OR de.from_date >= :fromDate)
                  AND (:toDate IS NULL OR de.to_date <= :toDate)
                """,

            countQuery = """
                     SELECT COUNT(DISTINCT e.emp_no)
                     FROM employees e
                     LEFT JOIN dept_emp de 
                            ON e.emp_no = de.emp_no
                           AND de.to_date = '9999-01-01'
                     WHERE (:empNo IS NULL OR e.emp_no = :empNo)
                       AND (:birthDate IS NULL OR e.birth_date = :birthDate)
                       AND (:firstName IS NULL OR e.first_name LIKE CONCAT('%', :firstName, '%'))
                       AND (:lastName IS NULL OR e.last_name LIKE CONCAT('%', :lastName, '%'))
                       AND (:gender IS NULL OR UPPER(TRIM(e.gender)) = UPPER(TRIM(:gender)))
                       AND (:hireDate IS NULL OR e.hire_date = :hireDate)
                       AND (:deptNo IS NULL OR de.dept_no = :deptNo)
                       AND (:fromDate IS NULL OR de.from_date >= :fromDate)
                       AND (:toDate IS NULL OR de.to_date <= :toDate)
                     """,

            nativeQuery = true
    )
    Page<EmployeeProjection> findAllEmployees(
            @Param("empNo") Integer empNo,
            @Param("birthDate") LocalDate birthDate,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("gender") String gender,
            @Param("hireDate") LocalDate hireDate,
            @Param("deptNo") String deptNo,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Query(value = "SELECT COALESCE(MAX(emp_no), 0) FROM employees", nativeQuery = true)
    int maxEmpNo();
}
