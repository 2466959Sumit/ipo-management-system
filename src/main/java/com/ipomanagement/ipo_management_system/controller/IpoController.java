package com.ipomanagement.ipo_management_system.controller;

import com.ipomanagement.ipo_management_system.domain.entity.Ipo;
import com.ipomanagement.ipo_management_system.repository.IpoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class IpoController {

    private final IpoRepository ipoRepository;

    public IpoController(IpoRepository ipoRepository) {
        this.ipoRepository = ipoRepository;
    }

    @GetMapping({"/", "/ipos"})
    public String list(Model model) {
        model.addAttribute("ipos", ipoRepository.findAllWithCompany());
        return "ipo/list";
    }

    @GetMapping("/ipos/{ipoId}")
    public String details(@PathVariable Long ipoId, Model model) {
        Ipo ipo = ipoRepository.findByIdWithCompany(ipoId).orElseThrow();
        model.addAttribute("ipo", ipo);
        return "ipo/details";
    }
}