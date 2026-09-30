package com.foodeats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {

    @GetMapping({"/", "/index"})
    public String customerPortal() {
        return "forward:/index.html";
    }

    @GetMapping({"/gateway", "/portal", "/sprints"})
    public String gatewayPortal() {
        return "forward:/gateway.html";
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
