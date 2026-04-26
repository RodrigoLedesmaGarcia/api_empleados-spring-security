package com.oracle.www.apiempleados.controller;

import com.oracle.www.apiempleados.entity.DeptEmp;
import com.oracle.www.apiempleados.entity.Employee;
import com.oracle.www.apiempleados.entity.EmployeeCreateAndUpdateRequest;
import com.oracle.www.apiempleados.service.EmployeeServiceImpl;
import com.oracle.www.apiempleados.utils.EmployeeProjection;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    private final EmployeeServiceImpl service;

    public EmployeeController(EmployeeServiceImpl service) {
        this.service = service;
    }

    private String quitarEspacios(String string) {
        if (string == null) return null;
        string = string.trim();
        return string.isEmpty() ? null : string;
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<EmployeeProjection>> buscar(
            @RequestParam(required = false) Integer empNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDate,
            @RequestParam(required = false) String deptNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        firstName = quitarEspacios(firstName);
        lastName = quitarEspacios(lastName);
        deptNo = quitarEspacios(deptNo);
        gender = quitarEspacios(gender);

        Page<EmployeeProjection> result = service.findAllEmployees(
                empNo, birthDate, firstName, lastName, gender, hireDate,
                deptNo, fromDate, toDate, page, size
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/nuevo")
    public ResponseEntity<?> crearEmpleado(
            @Valid @RequestBody EmployeeCreateAndUpdateRequest request
    ) {
        service.crearEmpleado(request);

        return ResponseEntity.ok(Map.of(
                "message", "Empleado creado correctamente"
        ));
    }

    @GetMapping("/editar/{empNo}")
    public ResponseEntity<EmployeeCreateAndUpdateRequest> obtenerEmpleado(
            @PathVariable Integer empNo
    ) {
        Employee employee = service.findById(empNo)
                .orElseThrow(() -> new RuntimeException(
                        "Empleado con N° de empleado: " + empNo + " no encontrado"
                ));

        EmployeeCreateAndUpdateRequest request = new EmployeeCreateAndUpdateRequest();
        request.setEmpNo(employee.getEmpNo());
        request.setBirthDate(employee.getBirthDate());
        request.setFirstName(employee.getFirstName());
        request.setLastName(employee.getLastName());
        request.setGender(employee.getGender());
        request.setHireDate(employee.getHireDate());

        if (employee.getDepartaments() != null && !employee.getDepartaments().isEmpty()) {
            DeptEmp deptEmp = employee.getDepartaments().get(0);
            request.setDeptNo(deptEmp.getId().getDeptNo());
            request.setFromDate(deptEmp.getFromDate());
            request.setToDate(deptEmp.getToDate());
        }

        return ResponseEntity.ok(request);
    }

    @PutMapping("/editar")
    public ResponseEntity<?> editarEmpleado(
            @Valid @RequestBody EmployeeCreateAndUpdateRequest request
    ) {
        service.editarEmpleado(request);

        return ResponseEntity.ok(Map.of(
                "message", "Empleado editado correctamente"
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/eliminar/{empNo}")
    public ResponseEntity<?> eliminarEmpleado(@PathVariable Integer empNo) {
        service.eliminarEmpleado(empNo);

        return ResponseEntity.ok(Map.of(
                "message", "Empleado eliminado correctamente"
        ));
    }
}
