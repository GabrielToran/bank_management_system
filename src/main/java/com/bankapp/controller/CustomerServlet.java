package com.bankapp.controller;

import com.bankapp.model.entity.CustomerEntity;
import com.bankapp.model.repository.JpaCustomerRepository;
import com.bankapp.service.CustomerService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/customers")
public class CustomerServlet extends HttpServlet {
    private CustomerService customerService;

    @Override
    public void init() {
        JpaCustomerRepository customerRepository = new JpaCustomerRepository();
        customerService = new CustomerService(customerRepository);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String customerId = request.getParameter("id");
        
        if (customerId != null) {
            CustomerEntity customer = customerService.getCustomerById(customerId);
            request.setAttribute("customer", customer);
            request.getRequestDispatcher("/WEB-INF/views/customer-details.jsp").forward(request, response);
        } else {
            List<CustomerEntity> customers = customerService.getAllCustomers();
            request.setAttribute("customers", customers);
            request.getRequestDispatcher("/WEB-INF/views/customers.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Create new customer logic here
        // Use customerService.createCustomer()
    }

    @Override
    public void destroy() {
        if (customerService != null) {
            // Close repository if needed
        }
    }
}
