package retail.controller;

import com.google.common.collect.Maps;
import retail.helpers.jinjahelper.JinjaServlet;
import retail.model.ReceiptDAO;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

public class ReceiptServlet extends JinjaServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();
        Map<String, Object> context = Maps.newHashMap();

        String receipt_id_str = request.getParameter("receipt_id");

        List<ReceiptDAO> scans = null;
        if (receipt_id_str != null) {
            long receipt_id = Long.parseLong(receipt_id_str);

            scans = ReceiptDAO.getReceiptById(receipt_id);
        }
        context.put("scans", scans);

        byte[] renderedTemplate = render("/receipt_detail.jinja2", context);
        out.write(renderedTemplate);

    }
}
