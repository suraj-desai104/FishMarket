package com.market.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/admin")
@RestController
public class AdminController {
	
	@GetMapping
	public String adminData() {
	    return "Only admin can view this!";
	}

}
