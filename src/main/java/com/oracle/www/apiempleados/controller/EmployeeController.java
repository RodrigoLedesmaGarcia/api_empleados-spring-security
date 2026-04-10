
package com.oracle.www.apiempleados.controller;

import com.oracle.www.apiempleados.entity.DeptEmp;
import com.oracle.www.apiempleados.entity.Employee;
import com.oracle.www.apiempleados.entity.EmployeeCreateAndUpdateRequest;
import com.oracle.www.apiempleados.service.EmployeeServiceImpl;
import com.oracle.www.apiempleados.utils.EmployeeProjection;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    private final EmployeeServiceImpl service;

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
    @GetMapping("/buscar")
    public String buscar(
            @RequestParam(required = false) Integer empNo,
            @RequestParam(required = false) @DateTimeFormat( iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @DateTimeFormat( iso = DateTimeFormat.ISO.DATE) LocalDate hireDate,
            @RequestParam(required = false) String deptNo,
            @RequestParam(required = false) @DateTimeFormat( iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat( iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
            ){

        firstName= quitarEspacios(firstName);
        lastName = quitarEspacios(lastName);
        deptNo = quitarEspacios(deptNo);
        gender = quitarEspacios(gender);

        Page<EmployeeProjection> result = service.findAllEmployees(
                empNo, birthDate, firstName, lastName, gender, hireDate, deptNo, fromDate, toDate, page, size
        );

        model.addAttribute("results", result);

        if(empNo != null && result.isEmpty()){
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
    public String fromNuevo (Model model){
        if(!model.containsAttribute("employee")){
            model.addAttribute("employee", new EmployeeCreateAndUpdateRequest());
        }
        return "new-employee";
    }

    @PostMapping("/nuevo")
    public String crearEmpleado(@Valid @ModelAttribute("employee") EmployeeCreateAndUpdateRequest request, BindingResult result){


        if(result.hasErrors()){
            return "new-employee";
        }

        /*
        if (request.getToDate() == null) {
            request.setToDate(LocalDate.of(9999, 1, 1));
        }
         */

        service.crearEmpleado(request);
        return "redirect:/employee/nuevo?ok";
    }



    // --- 𝔼𝕕𝕚𝕥𝕒𝕣 𝕖𝕞𝕡𝕝𝕖𝕒𝕕𝕠 𝕪𝕒 𝕖𝕩𝕚𝕤𝕥𝕖𝕟𝕥𝕖 ---
    @GetMapping("/editar/{empNo}")
    public String EditarFormulario(@PathVariable Integer empNo, Model model){

        Employee employee = service.findById(empNo).orElseThrow( () -> new RuntimeException("Empleado con en N° de empleado: "+empNo+" no encontrado"));

        EmployeeCreateAndUpdateRequest request = new EmployeeCreateAndUpdateRequest();
        request.setEmpNo(employee.getEmpNo());
        request.setBirthDate(employee.getBirthDate());
        request.setFirstName(employee.getFirstName());
        request.setLastName(request.getLastName());
        request.setGender(request.getGender());
        request.setHireDate(request.getHireDate());

        if(employee.getDepartaments() != null && !employee.getDepartaments().isEmpty()){

            DeptEmp deptEmp = employee.getDepartaments().get(0);
            request.setDeptNo(deptEmp.getId().getDeptNo());
            request.setFromDate(deptEmp.getFromDate());
            request.setToDate(deptEmp.getToDate());
        }

        model.addAttribute("employee", request);
        return "form-editar";
    }

    @PostMapping("/editar")
    public String editarEmpleado(@Valid @ModelAttribute("employee") EmployeeCreateAndUpdateRequest request, BindingResult result){

        if(result.hasErrors()){
            return "form-editar";
        }

        service.editarEmpleado(request);
        return "redirect:/employee/inicio?editado=true";
    }



    // --- 𝔼𝕝𝕚𝕞𝕚𝕟𝕒𝕣 𝕖𝕞𝕡𝕝𝕖𝕒𝕕𝕠 ---
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/eliminar/{empNo}")
    private String eliminarEmpleado(@PathVariable Integer empNo){
        service.eliminarEmpleado(empNo);
        return "redirect:/employee/inicio?eliminado";
    }


} // fin de la clase

