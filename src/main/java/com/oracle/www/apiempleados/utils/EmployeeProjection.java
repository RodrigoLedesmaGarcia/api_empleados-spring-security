package com.oracle.www.apiempleados.utils;

import java.time.LocalDate;

public interface EmployeeProjection {

    public abstract Integer getEmpNo();

    public abstract LocalDate getBirthDate();

    public abstract String getFirstName();

    public abstract String getLastName();

    public abstract String  getGender();

    public abstract LocalDate getHireDate();

    public abstract String getDeptNo();

    public abstract LocalDate getFromDate();
}
