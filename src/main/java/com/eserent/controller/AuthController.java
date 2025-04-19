package com.eserent.controller;

import com.eserent.entity.User;
import com.eserent.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

/**
 * Controller for authentication operations.
 * Handles login, registration, and logout.
 */
@RequiredArgsConstructor
@Controller
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        // If user is already authenticated, redirect to home
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // If user is already authenticated, redirect to home
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("user", new User());
        model.addAttribute("roles", new User.Role[]{User.Role.ROLE_CUSTOMER, User.Role.ROLE_LANDLORD});
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                              BindingResult bindingResult,
                              @RequestParam(required = false) User.Role role,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", new User.Role[]{User.Role.ROLE_CUSTOMER, User.Role.ROLE_LANDLORD});
            return "register";
        }
        
        try {
            // Set role from form submission
            if (role != null) {
                user.setRole(role);
            } else {
                user.setRole(User.Role.ROLE_CUSTOMER); // Default role
            }
            
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", new User.Role[]{User.Role.ROLE_CUSTOMER, User.Role.ROLE_LANDLORD});
            return "register";
        }
    }
}
