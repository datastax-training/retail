package playlist.controller;

import playlist.model.StatisticsDAO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class StatisticsServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


    List<StatisticsDAO> statistics = StatisticsDAO.getStatistics();

    request.setAttribute("statistics", statistics);
    getServletContext().getRequestDispatcher("/statistics.jsp").forward(request,response);

  }
}
