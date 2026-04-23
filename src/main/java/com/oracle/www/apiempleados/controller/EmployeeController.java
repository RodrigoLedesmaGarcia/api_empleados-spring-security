package com.oracle.www.apiempleados.controller;

import com.oracle.www.apiempleados.entity.DeptEmp;
import com.oracle.www.apiempleados.entity.Employee;
import com.oracle.www.apiempleados.entity.EmployeeCreateAndUpdateRequest;
import com.oracle.www.apiempleados.service.EmployeeServiceImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/employee")
@Transactional
public class EmployeeController {

    private final EmployeeServiceImpl service;

    // INYECTAMOS EntityManager para hacer queries nativas vulnerables
    @PersistenceContext
    private EntityManager entityManager;

    public EmployeeController(EmployeeServiceImpl service) {
        this.service = service;
    }

    // --- ℍ𝕖𝕝𝕡𝕖𝕣 ---
    private String quitarEspacios(String string){
        if(string == null) return  null;
        string = string.trim();
        return string.isEmpty() ? null : string;
    }

    /*
    ---------------
    𝕧𝕚𝕤𝕥𝕒𝕤 𝕖𝕟 𝕥𝕙𝕪𝕞𝕖𝕝𝕖𝕒𝕗
    ---------------
     */


    // --- 𝕚𝕟𝕚𝕔𝕚𝕠 ---
    @GetMapping("/inicio")
    public String view(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());
        return "employee";
    }


    // --- 𝔹𝕦𝕤𝕔𝕒𝕣 𝕖𝕞𝕡𝕝𝕖𝕒𝕕𝕠 ---
    // VERSIÓN VULNERABLE - Concatenación directa de SQL
    @GetMapping("/buscar")
    public String buscar(
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
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // NO limpiamos los parámetros para permitir inyección

        // CONSTRUCCIÓN VULNERABLE DE SQL
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.emp_no, ");
        sql.append("e.birth_date, ");
        sql.append("e.first_name, ");
        sql.append("e.last_name, ");
        sql.append("e.gender, ");
        sql.append("e.hire_date, ");
        sql.append("de.dept_no, ");
        sql.append("de.from_date, ");
        sql.append("de.to_date ");
        sql.append("FROM employees e ");
        sql.append("LEFT JOIN dept_emp de ON e.emp_no = de.emp_no ");
        sql.append("WHERE 1=1 ");

        // Concatenación directa SIN PARÁMETROS ENLAZADOS
        if (empNo != null) {
            sql.append(" AND e.emp_no = ").append(empNo);
        }

        if (birthDate != null) {
            sql.append(" AND e.birth_date = '").append(birthDate.toString()).append("'");
        }

        // VULNERABLE: firstName puede contener ' OR '1'='1
        if (firstName != null && !firstName.isEmpty()) {
            sql.append(" AND e.first_name LIKE '%").append(firstName).append("%'");
        }

        // VULNERABLE: lastName puede inyectar SQL
        if (lastName != null && !lastName.isEmpty()) {
            sql.append(" AND e.last_name LIKE '%").append(lastName).append("%'");
        }

        // VULNERABLE
        if (gender != null && !gender.isEmpty()) {
            sql.append(" AND e.gender = '").append(gender).append("'");
        }

        // VULNERABLE
        if (hireDate != null) {
            sql.append(" AND e.hire_date = '").append(hireDate.toString()).append("'");
        }

        // VULNERABLE
        if (deptNo != null && !deptNo.isEmpty()) {
            sql.append(" AND de.dept_no = '").append(deptNo).append("'");
        }

        // VULNERABLE
        if (fromDate != null) {
            sql.append(" AND de.from_date >= '").append(fromDate.toString()).append("'");
        }

        // VULNERABLE
        if (toDate != null) {
            sql.append(" AND de.to_date <= '").append(toDate.toString()).append("'");
        }

        sql.append(" LIMIT ").append(size).append(" OFFSET ").append(page * size);

        System.out.println("SQL VULNERABLE: " + sql.toString());

        // EJECUCIÓN VULNERABLE - Sin mapeo, devuelve Object[]
        Query query = entityManager.createNativeQuery(sql.toString());
        List<Object[]> rawResults = query.getResultList();

        // Mapeo manual a EmployeeDTO (crea esta clase si no existe)
        List<EmployeeCreateAndUpdateRequest> results = new ArrayList<>();
        for (Object[] row : rawResults) {
            EmployeeCreateAndUpdateRequest dto = new EmployeeCreateAndUpdateRequest();
            dto.setEmpNo(row[0] != null ? ((Number) row[0]).intValue() : null);
            dto.setBirthDate((LocalDate) row[1]);
            dto.setFirstName((String) row[2]);
            dto.setLastName((String) row[3]);
            dto.setGender((String) row[4]);
            dto.setHireDate((LocalDate) row[5]);          // <-- CORREGIDO
            dto.setDeptNo((String) row[6]);
            dto.setFromDate((LocalDate) row[7]);          // <-- CORREGIDO
            dto.setToDate((LocalDate) row[8]);            // <-- CORREGIDO
            results.add(dto);
        }

        // Count también vulnerable
        StringBuilder countSql = new StringBuilder("SELECT COUNT(DISTINCT e.emp_no) FROM employees e LEFT JOIN dept_emp de ON e.emp_no = de.emp_no WHERE 1=1");
        if (firstName != null && !firstName.isEmpty()) {
            countSql.append(" AND e.first_name LIKE '%").append(firstName).append("%'");
        }
        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Page<EmployeeCreateAndUpdateRequest> result = new PageImpl<>(results,
                org.springframework.data.domain.PageRequest.of(page, size), total);

        model.addAttribute("results", result);

        if (empNo != null && result.isEmpty()) {
            model.addAttribute("error", "usuario inexistente");
        }

        model.addAttribute("empNo", empNo);
        model.addAttribute("birthDate", birthDate);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("gender", gender);
        model.addAttribute("hireDate", hireDate);
        model.addAttribute("deptNo", deptNo);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        return "employee";
    }


    // --- ℂ𝕣𝕖𝕒𝕣 𝕖𝕞𝕡𝕝𝕖𝕒𝕕𝕠 𝕟𝕦𝕖𝕧𝕠 ---
    @GetMapping("/nuevo")
    public String fromNuevo(Model model) {
        if (!model.containsAttribute("employee")) {
            model.addAttribute("employee", new EmployeeCreateAndUpdateRequest());
        }
        return "new-employee";
    }


    // VERSIÓN VULNERABLE - INSERT con concatenación
    @PostMapping("/nuevo")
    public String crearEmpleado(
            @Valid @ModelAttribute("employee") EmployeeCreateAndUpdateRequest request,
            BindingResult result) {

        System.out.println("Entró al POST");
        System.out.println("Gender: " + request.getGender());

        if (result.hasErrors()) {
            System.out.println("Hay errores");
            result.getAllErrors().forEach(e -> System.out.println(e.toString()));
            return "new-employee";
        }

        // OBTENER EL PRÓXIMO EMP_NO VULNERABLE
        String maxSql = "SELECT COALESCE(MAX(emp_no), 0) + 1 FROM employees";
        Query maxQuery = entityManager.createNativeQuery(maxSql);
        Integer newEmpNo = ((Number) maxQuery.getSingleResult()).intValue();

        // SQL INSERT VULNERABLE
        String sql = "INSERT INTO employees (emp_no, birth_date, first_name, last_name, gender, hire_date) " +
                "VALUES (" + newEmpNo + ", '" +
                request.getBirthDate() + "', '" +
                request.getFirstName() + "', '" +
                request.getLastName() + "', '" +
                request.getGender() + "', '" +
                request.getHireDate() + "')";

        System.out.println("SQL INSERT: " + sql);
        entityManager.createNativeQuery(sql).executeUpdate();

        return "redirect:/employee/nuevo?ok";
    }


    // --- 𝔼𝕕𝕚𝕥𝕒𝕣 𝕖𝕞𝕡𝕝𝕖𝕒𝕕𝕠 𝕪𝕒 𝕖𝕩𝕚𝕤𝕥𝕖𝕟𝕥𝕖 ---
    @GetMapping("/editar/{empNo}")
    public String EditarFormulario(@PathVariable Integer empNo, Model model) {

        // Query vulnerable en lugar de findById seguro
        String sql = "SELECT * FROM employees WHERE emp_no = " + empNo;
        System.out.println("SQL SELECT: " + sql);

        Query query = entityManager.createNativeQuery(sql, Employee.class);
        Employee employee;
        try {
            employee = (Employee) query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Empleado con N° de empleado: " + empNo + " no encontrado");
        }

        System.out.println("GET editar");
        System.out.println("employee.hireDate = " + employee.getHireDate());

        EmployeeCreateAndUpdateRequest request = new EmployeeCreateAndUpdateRequest();
        request.setEmpNo(employee.getEmpNo());
        request.setBirthDate(employee.getBirthDate());
        request.setFirstName(employee.getFirstName());
        request.setLastName(employee.getLastName());
        request.setGender(employee.getGender());
        request.setHireDate(employee.getHireDate());

        System.out.println("request.hireDate = " + request.getHireDate());

        if (employee.getDepartaments() != null && !employee.getDepartaments().isEmpty()) {
            DeptEmp deptEmp = employee.getDepartaments().get(0);
            request.setDeptNo(deptEmp.getId().getDeptNo());
            request.setFromDate(deptEmp.getFromDate());
            request.setToDate(deptEmp.getToDate());
        }

        model.addAttribute("employee", request);
        return "form-editar";
    }


    // VERSIÓN VULNERABLE - UPDATE con concatenación
    @PostMapping("/editar")
    public String editarEmpleado(
            @Valid @ModelAttribute("employee") EmployeeCreateAndUpdateRequest request,
            BindingResult result) {

        System.out.println("Entró al POST editar");

        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.toString()));
            return "form-editar";
        }

        // UPDATE VULNERABLE
        String sql = "UPDATE employees SET " +
                "first_name = '" + request.getFirstName() + "', " +
                "last_name = '" + request.getLastName() + "', " +
                "gender = '" + request.getGender() + "', " +
                "birth_date = '" + request.getBirthDate() + "', " +
                "hire_date = '" + request.getHireDate() + "' " +
                "WHERE emp_no = " + request.getEmpNo();

        System.out.println("SQL UPDATE: " + sql);
        entityManager.createNativeQuery(sql).executeUpdate();

        // UPDATE de dept_emp también vulnerable
        if (request.getDeptNo() != null) {
            String deptSql = "UPDATE dept_emp SET " +
                    "dept_no = '" + request.getDeptNo() + "', " +
                    "from_date = '" + request.getFromDate() + "', " +
                    "to_date = '" + request.getToDate() + "' " +
                    "WHERE emp_no = " + request.getEmpNo();
            System.out.println("SQL DEPT_UPDATE: " + deptSql);
            entityManager.createNativeQuery(deptSql).executeUpdate();
        }

        return "redirect:/employee/inicio?editado=true";
    }


    // --- 𝔼𝕝𝕚𝕞𝕚𝕟𝕒𝕣 𝕖𝕞𝕡𝕝𝕖𝕒𝕕𝕠 ---
    @PreAuthorize("hasRole('ROLE_ADMIN')")

    // VERSIÓN VULNERABLE - DELETE con concatenación
    @PostMapping("/eliminar/{empNo}")
    public String eliminarEmpleado(@PathVariable Integer empNo) {
        String sql = "DELETE FROM employees WHERE emp_no = " + empNo;
        String deptSql = "DELETE FROM dept_emp WHERE emp_no = " + empNo;

        System.out.println("SQL DELETE: " + sql);
        System.out.println("SQL DEPT_DELETE: " + deptSql);

        entityManager.createNativeQuery(deptSql).executeUpdate();
        entityManager.createNativeQuery(sql).executeUpdate();

        return "redirect:/employee/inicio?eliminado";
    }


    // ============================================
    // ENDPOINTS EXTRA VULNERABLES PARA PRÁCTICA
    // ============================================

    // EXTREMADAMENTE VULNERABLE - Acepta ORDER BY dinámico
    @GetMapping("/orderBy")
    @ResponseBody
    public List<?> orderBy(@RequestParam String column, @RequestParam String direction) {
        String sql = "SELECT * FROM employees ORDER BY " + column + " " + direction;
        System.out.println("SQL ORDER: " + sql);
        return entityManager.createNativeQuery(sql, Employee.class).getResultList();
    }

    // VULNERABLE - Raw query directo
    @GetMapping("/raw")
    @ResponseBody
    public List<?> rawQuery(@RequestParam String where) {
        String sql = "SELECT * FROM employees WHERE " + where;
        System.out.println("SQL RAW: " + sql);
        return entityManager.createNativeQuery(sql, Employee.class).getResultList();
    }

    // VULNERABLE - Búsqueda por nombre directo
    @GetMapping("/nombre/{name}")
    @ResponseBody
    public List<?> porNombre(@PathVariable String name) {
        String sql = "SELECT * FROM employees WHERE first_name = '" + name + "'";
        return entityManager.createNativeQuery(sql, Employee.class).getResultList();
    }

    // VULNERABLE - Stacked queries permitidas
    @GetMapping("/busquedaAvanzada")
    @ResponseBody
    public List<?> busquedaAvanzada(@RequestParam String condicion) {
        // Esto permite múltiples sentencias si la DB lo soporta
        String sql = "SELECT * FROM employees WHERE " + condicion + ";";
        System.out.println("SQL AVANZADO: " + sql);
        return entityManager.createNativeQuery(sql, Employee.class).getResultList();
    }


} // fin de la clase