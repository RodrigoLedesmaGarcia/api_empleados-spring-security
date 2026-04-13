package com.oracle.www.apiempleados.entity;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class EmployeeCreateAndUpdateRequest {

    @Column(name = "emp_no", nullable = false)
    private Integer empNo;

    @Column(name = "birth_date", nullable = false)
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE)
    @NotNull(message = "el campo de fecha de nacimiento no puede estar vacio")
    private LocalDate birthDate;

    @Column(name = "first_name", nullable = false)
    @NotBlank(message = "el campo de nombre(s) no puede estar vacio")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "el campo de apellido no puede estar vacio")
    private String lastName;

    @Column(name = "gender", nullable = false)
    @NotBlank(message = "el campo de genero no puede estar vacio")
    private String gender;

    @Column(name = "hire_date", nullable = false)
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE)
    @NotNull(message = "el campo de fecha de contratacion no puede estar vacio")
    private LocalDate hireDate;

    @Column(name = "dept_no", nullable = false)
    @NotBlank(message = "el campo de departamento no puede estar vacio")
    private String deptNo;


    @Column(name = "from_date", nullable = false)
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE)
    @NotNull(message = "el campo de fecha de inicio no puede estar vacio")
    private LocalDate fromDate;


    @Column(name = "to_date")
    @DateTimeFormat( iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    public EmployeeCreateAndUpdateRequest(){}

    public EmployeeCreateAndUpdateRequest(Integer empNo, LocalDate birthDate, String firstName, String lastName, String gender, LocalDate hireDate, String deptNo, LocalDate fromDate, LocalDate toDate) {
        this.empNo = empNo;
        this.birthDate = birthDate;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.hireDate = hireDate;
        this.deptNo = deptNo;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public Integer getEmpNo() {
        return empNo;
    }

    public void setEmpNo(Integer empNo) {
        this.empNo = empNo;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getDeptNo() {
        return deptNo;
    }

    public void setDeptNo(String deptNo) {
        this.deptNo = deptNo;
    }


    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }
}
