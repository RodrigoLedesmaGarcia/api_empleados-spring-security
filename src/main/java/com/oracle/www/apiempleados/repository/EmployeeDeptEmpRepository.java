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
                select e.emp_no       as empNo,
                       e.birth_date   as birthDate,
                       e.first_name   as firstName,
                       e.last_name    as lastName,
                       e.gender       as gender,
                       e.hire_date    as  hireDate,
                       de.emp_no      as empNo,
                       de.dept_no     as deptNo,
                       de.from_date   as fromDate,
                       de.to_date     as fromDate
                   from employees e left join dept_emp de on e.emp_no = de.emp_no
                WHERE (:empNo     IS NULL OR e.emp_no = :empNo)
                AND (:birthDate   IS NULL OR e.birth_date = :birthDate)
                AND (:first_name  IS NULL OR e.first_name LIKE CONCAT('%', :firstName, '%'))
                AND (:lastName    IS NULL OR e.last_name LIKE CONCAT('%', :lastName, '%'))
                AND (:gender      IS NULL OR UPPER(TRIM(e.gender)) = UPPER(TRIM(:gender)))
                AND (:hireDate    IS NULL OR e.hire_date = : hireDate)
                AND (:deptNo      IS NULL OR de.dept_no = :deptNo)
                AND (:formDate    IS NULL OR de.form_date  >= :fromDate)
                AND (:toDate      IS NULL OR de.to_date <= : toDate)
                """,

        countQuery = """
                     SELECT COUNT(DISTINCT e.emp_no)
                     FROM employees e 
                     LEFT JOIN dept_emp de ON e.emp_no = de.emp_no
                     WHERE (:empNo     IS NULL OR e.emp_no = :empNo)
                     AND (:birthDate   IS NULL OR e.birth_date = :birthDate)
                     AND (:first_name  IS NULL OR e.first_name LIKE CONCAT('%', :firstName, '%'))
                     AND (:lastName    IS NULL OR e.last_name LIKE CONCAT('%', :lastName, '%'))
                     AND (:gender      IS NULL OR UPPER(TRIM(e.gender)) = UPPER(TRIM(:gender)))
                     AND (:hireDate    IS NULL OR e.hire_date = : hireDate)
                     """,

        nativeQuery = true
)
    Page<EmployeeProjection> findAllEmployees(
            @Param("empNo")         Integer empNo,
            @Param("birthDate")     LocalDate birthDate,
            @Param("firstName")     String firstName,
            @Param("lastName")      String lastName,
            @Param("gender")        String gender,
            @Param("hireDate")      LocalDate hireDate,
            @Param("deptNo")        String deptNo,
            @Param("fromDate")      LocalDate fromDate,
            @Param("toDate")        LocalDate toDate,
            Pageable pageable

            );

    @Query(value = "SELECT COALESCE(MAX(emp_no), 0) FROM employees", nativeQuery = true)
    int maxEmpNo();
}
