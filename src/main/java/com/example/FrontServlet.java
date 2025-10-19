package com.example;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServlet extends HttpServlet {

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean ressourceContext = getServletContext().getResource(path)!=null;
        if(!ressourceContext){
            response.getWriter().println(path);
        }
        else{
            if(path.endsWith(".jsp")){
                RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("jsp");
                dispatcher.forward(request, response);
            }
            else{
                response.getWriter().println(path);
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        service(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        service(request, response);
    }
}