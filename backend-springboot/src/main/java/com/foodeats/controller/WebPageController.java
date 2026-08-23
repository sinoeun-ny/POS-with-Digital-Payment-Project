package com.foodeats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {

    @GetMapping({"/main", "/portal"})
    public String mainPortal() {
        return "forward:/index.html";
    }

    @GetMapping({"/main/customer", "/customer"})
    public String customerPortal() {
        return "forward:/customer.html";
    }

    @GetMapping({"/main/merchant", "/merchant"})
    public String merchantPortal() {
        return "forward:/merchant.html";
    }

    @GetMapping({"/main/driver", "/driver"})
    public String driverPortal() {
        return "forward:/driver.html";
    }

    @GetMapping({"/main/admin", "/admin"})
    public String adminPortal() {
        return "forward:/admin.html";
    }
}
