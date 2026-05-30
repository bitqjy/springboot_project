package com.controller;

import com.annotation.IgnoreAuth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class AdminRedirectController {

    @IgnoreAuth
    @RequestMapping({
            "/admin",
            "/admin/",
            "/admin/index.html",
            "/admin/dist",
            "/admin/dist/",
            "/admin/admin",
            "/admin/admin/",
            "/admin/admin/index.html",
            "/admin/admin/dist",
            "/admin/admin/dist/",
            "/admin/admin/dist/index.html"
    })
    public void redirectToAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/dist/index.html");
    }
}
