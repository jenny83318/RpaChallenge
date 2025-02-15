package com.example.practice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.practice.service.RPAService;

@RestController
@RequestMapping("/rpa")
public class RPAController {

	@Autowired
	private RPAService rpaService;
	
    @GetMapping("/start")
    public String startChallenge() {
    	rpaService.startChallenge();
        return "RPA Challenge started!";
    }
}
