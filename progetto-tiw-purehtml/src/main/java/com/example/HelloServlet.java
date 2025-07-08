package com.example;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import org.thymeleaf.web.IWebExchange;

public class HelloServlet extends HttpServlet {
    private TemplateEngine engine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        // 1. Build the web application
        application = JakartaServletWebApplication.buildApplication(getServletContext());
        
        // 2. Configure the template resolver
        WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(application);
        resolver.setPrefix("/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");

        // 3. Configure the template engine
        engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Create the web exchange and context
        IWebExchange webExchange = application.buildExchange(req, resp);
        WebContext ctx = new WebContext(webExchange);
        ctx.setVariable("name", "Thymeleaf with Tomcat");

        resp.setContentType("text/html;charset=UTF-8");
        engine.process("home", ctx, resp.getWriter());
    }
}