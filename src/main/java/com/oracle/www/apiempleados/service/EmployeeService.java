package com.oracle.www.apiempleados.service;

import com.oracle.www.apiempleados.entity.Employee;
import com.oracle.www.apiempleados.entity.EmployeeCreateAndUpdateRequest;
import com.oracle.www.apiempleados.utils.EmployeeProjection;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.Optional;


public interface EmployeeService {

    Page<EmployeeProjection> findAllEmployees(
            Integer empNo,
            LocalDate birthDate,
            String firstName,
            String lastName,
            String gender,
            LocalDate hireDate,
            String deptNo,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    );

    Optional<Employee> findById(Integer empNo);

    void crearEmpleado(EmployeeCreateAndUpdateRequest request);

    void editarEmpleado(EmployeeCreateAndUpdateRequest request);

    void eliminarEmpleado(Integer empNo);



}
