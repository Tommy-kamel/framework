package com.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import com.registry.AnnotatedRouteRegistry;

public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Initialize the registry
        AnnotatedRouteRegistry registry = new AnnotatedRouteRegistry();
        registry.scanAndRegister("com.example");
        sce.getServletContext().setAttribute("routeRegistry", registry);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup
    }
}