package co.estebanlopez.controller;

import co.estebanlopez.domain.Employee;
import co.estebanlopez.domain.Hours;
import co.estebanlopez.domain.StatusPay;
import co.estebanlopez.repository.EmployeeRepository;
import co.estebanlopez.repository.HoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class HoursController {

    private HoursRepository repository;
    private EmployeeRepository employeeRepository;

    @Autowired
    public void setEmployeeRepository(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    @Autowired
    public void setRepository(HoursRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/hours",method = RequestMethod.POST, consumes = "application/json")
    public void create(@RequestBody Hours hours)
    {
        if (hours.getDate() == null)
        {
            hours.setDate(new Date(System.currentTimeMillis()));
            hours.setStatusPay(StatusPay.Pendiente);
        }
        Employee employee = employeeRepository.findOne(hours.getEmployee().getId());
        employee.getHoursList().add(hours);
        System.out.println("hours = [" + hours + "]");
        employeeRepository.save(employee);
    }

    @RequestMapping(value = "/hours",method = RequestMethod.PUT,consumes = "application/json")
    public void update(@RequestBody Hours hours)
    {
        Hours oldHours = repository.findOne(hours.getId());
        oldHours.setApproved(hours.getApproved());
        oldHours.setStatusPay(hours.getStatusPay());
        oldHours.setTotalPrice(oldHours.getUnitPrice() * hours.getApproved());
        System.out.println("hours = [" + hours + "]");
        repository.save(oldHours);
    }
}

