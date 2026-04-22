package com.oracle.www.apiempleados.service;

import com.oracle.www.apiempleados.entity.DeptEmp;
import com.oracle.www.apiempleados.entity.DeptEmpId;
import com.oracle.www.apiempleados.entity.Employee;
import com.oracle.www.apiempleados.entity.EmployeeCreateAndUpdateRequest;
import com.oracle.www.apiempleados.repository.DeptEmpRepository;
import com.oracle.www.apiempleados.repository.EmployeeDeptEmpRepository;
import com.oracle.www.apiempleados.repository.EmployeeRepository;
import com.oracle.www.apiempleados.utils.EmployeeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeDeptEmpRepository repository;

    private final DeptEmpRepository deptEmpRepository;

    public EmployeeServiceImpl(EmployeeDeptEmpRepository repository, DeptEmpRepository deptEmpRepository) {
        this.repository = repository;
        this.deptEmpRepository = deptEmpRepository;
    }

    // helper para quitar esapacios de una cadena
    private String quitarEscacios(String string){
        return (string == null) ? null : string.trim();
    }

    /*
    Buscar empleados con filtros
     */
    @Override
    public Page<EmployeeProjection> findAllEmployees(Integer empNo, LocalDate birthDate, String firstName, String lastName, String gender, LocalDate hireDate, String deptNo, LocalDate fromDate, LocalDate toDate, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return  repository.findAllEmployees(
                empNo == null ? null : empNo.intValue(),
                birthDate,
                quitarEscacios(firstName),
                quitarEscacios(lastName),
                quitarEscacios(gender),
                hireDate,
                quitarEscacios(deptNo),
                fromDate,
                toDate,
                pageable
        );
    }

    /*
    Buscar empleado especifico por id, para editarlo o borrarlo
     */
    @Override
    public Optional<Employee> findById(Integer empNo) {
        return repository.findById(empNo);
    }

    @Override
    @Transactional
    public void crearEmpleado(EmployeeCreateAndUpdateRequest request) {

        int nextEmpNo = repository.maxEmpNo() + 1;

        LocalDate toDate = request.getToDate() != null
                ? request.getToDate()
                : LocalDate.of(9999, 1, 1);

        Employee employee = new Employee();
        employee.setEmpNo(nextEmpNo);
        employee.setBirthDate(request.getBirthDate());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setGender(request.getGender());
        employee.setHireDate(request.getHireDate());

        DeptEmp deptEmp = new DeptEmp();
        deptEmp.setId(new DeptEmpId(nextEmpNo, request.getDeptNo()));
        deptEmp.setEmployee(employee);
        deptEmp.setFromDate(request.getFromDate());
        deptEmp.setToDate(toDate);

        employee.getDepartaments().add(deptEmp);

        repository.save(employee);
    }

    /*
    Editar empleado ya existente
     */

    @Override
    @Transactional
    public void editarEmpleado(EmployeeCreateAndUpdateRequest request) {

        if (request == null) {
            throw new RuntimeException("La solicitud no puede ser nula");
        }

        if (request.getEmpNo() == null) {
            throw new RuntimeException("El empNo es obligatorio para editar el empleado");
        }

        if (request.getDeptNo() == null || request.getDeptNo().isBlank()) {
            throw new RuntimeException("El deptNo es obligatorio para editar el empleado");
        }

        Employee employee = repository.findById(request.getEmpNo())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado: " + request.getEmpNo()));

        employee.setBirthDate(request.getBirthDate());
        employee.setFirstName(quitarEscacios(request.getFirstName()));
        employee.setLastName(quitarEscacios(request.getLastName()));
        employee.setGender(quitarEscacios(request.getGender()));
        employee.setHireDate(request.getHireDate());

        LocalDate toDate = request.getToDate() != null
                ? request.getToDate()
                : LocalDate.of(9999, 1, 1);

        if (employee.getDepartaments() != null) {
            employee.getDepartaments().clear();
        }

        DeptEmp deptEmp = new DeptEmp();
        deptEmp.setId(new DeptEmpId(employee.getEmpNo(), quitarEscacios(request.getDeptNo())));
        deptEmp.setEmployee(employee);
        deptEmp.setFromDate(request.getFromDate());
        deptEmp.setToDate(toDate);

        employee.getDepartaments().add(deptEmp);

        repository.save(employee);
    }


    /*
    Eliminar empleado
     */
    @Override
    public void eliminarEmpleado(Integer empNo) {

        if(!repository.existsById(empNo)){
            throw new RuntimeException("Empleado no encontrado: "+ empNo);
        }

        // eliminar relaciones (tabl dept_emp)
        deptEmpRepository.deleteByEmpNo(empNo);

        // elimiinar empleado
        repository.deleteById(empNo);

    }
}
