package com.example.java_community_site.controllers;

import java.util.Arrays;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {


    @GetMapping("/")
    public String home(@RequestParam(name = "showDetails", required = false) boolean showDetails,
        Model model) {
            // to see this section: http://localhost:8080/?showDetails=true
            model.addAttribute("features", Arrays.asList(
                "Working in projects with experienced mentors",
                "How to deal with feedback",
                "Learn some best coding practicies"
            ));
            model.addAttribute("showDetails", showDetails);
        return "index";
    }
}
