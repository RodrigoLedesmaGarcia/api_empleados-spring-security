package com.oracle.www.apiempleados.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "deptEmp")
public class DeptEmp {

    @EmbeddedId
    private DeptEmpId id;

    @ManyToOne
    @MapsId("empNo")
    @JoinColumn(name = "emp_no")
    private Employee employee;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    public DeptEmp(){}

    public DeptEmp(DeptEmpId id, LocalDate fromDate, LocalDate toDate) {
        this.id = id;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public DeptEmpId getId() {
        return id;
    }

    public void setId(DeptEmpId id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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

