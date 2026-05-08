package com.oracle.www.apiempleados.controller;

import com.oracle.www.apiempleados.dto.PageResponse;
import com.oracle.www.apiempleados.entity.DeptEmp;
import com.oracle.www.apiempleados.entity.Employee;
import com.oracle.www.apiempleados.entity.EmployeeCreateAndUpdateRequest;
import com.oracle.www.apiempleados.service.EmployeeServiceImpl;
import com.oracle.www.apiempleados.utils.EmployeeProjection;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
@RestController
@RequestMapping("/employee")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

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
    public ResponseEntity<PageResponse<EmployeeProjection>> buscar(
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
        logger.debug("Buscando empleados con filtros: empNo={}, firstName={}, lastName={}, deptNo={}, page={}, size={}",
                empNo, firstName, lastName, deptNo, page, size);

        firstName = quitarEspacios(firstName);
        lastName = quitarEspacios(lastName);
        deptNo = quitarEspacios(deptNo);
        gender = quitarEspacios(gender);

        Page<EmployeeProjection> result = service.findAllEmployees(
                empNo, birthDate, firstName, lastName, gender, hireDate,
                deptNo, fromDate, toDate, page, size
        );

        PageResponse<EmployeeProjection> response = new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );

        logger.info("Búsqueda completada. Total encontrados: {}", result.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/nuevo")
    public ResponseEntity<?> crearEmpleado(
            @Valid @RequestBody EmployeeCreateAndUpdateRequest request
    ) {
        logger.info("Creando empleado con empNo={}", request.getEmpNo());

        service.crearEmpleado(request);

        logger.info("Empleado creado correctamente. empNo={}", request.getEmpNo());

        return ResponseEntity.ok(Map.of(
                "message", "Empleado creado correctamente"
        ));
    }

    @GetMapping("/editar/{empNo}")
    public ResponseEntity<EmployeeCreateAndUpdateRequest> obtenerEmpleado(
            @PathVariable Integer empNo
    ) {
        logger.debug("Consultando empleado para edición. empNo={}", empNo);

        Employee employee = service.findById(empNo)
                .orElseThrow(() -> {
                    logger.warn("Empleado no encontrado. empNo={}", empNo);
                    return new RuntimeException(
                            "Empleado con N° de empleado: " + empNo + " no encontrado"
                    );
                });

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

            logger.debug("Departamento encontrado para empleado empNo={}, deptNo={}", empNo, deptEmp.getId().getDeptNo());
        }

        logger.info("Empleado cargado para edición. empNo={}", empNo);

        return ResponseEntity.ok(request);
    }

    @PutMapping("/editar")
    public ResponseEntity<?> editarEmpleado(
            @Valid @RequestBody EmployeeCreateAndUpdateRequest request
    ) {
        logger.info("Editando empleado. empNo={}", request.getEmpNo());

        service.editarEmpleado(request);

        logger.info("Empleado editado correctamente. empNo={}", request.getEmpNo());

        return ResponseEntity.ok(Map.of(
                "message", "Empleado editado correctamente"
        ));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/eliminar/{empNo}")
    public ResponseEntity<?> eliminarEmpleado(@PathVariable Integer empNo) {
        logger.warn("Solicitud para eliminar empleado. empNo={}", empNo);

        service.eliminarEmpleado(empNo);

        logger.warn("Empleado eliminado correctamente. empNo={}", empNo);

        return ResponseEntity.ok(Map.of(
                "message", "Empleado eliminado correctamente"
        ));
    }
}