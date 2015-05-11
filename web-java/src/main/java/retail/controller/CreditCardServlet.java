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

public class CreditCardServlet extends JinjaServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();
        Map<String, Object> context = Maps.newHashMap();

        String credit_card_str = request.getParameter("cc_no");

        List<ReceiptDAO> receipts = null;
        if (credit_card_str != null) {
            long credit_card_no = Long.parseLong(credit_card_str);

            receipts = ReceiptDAO.getReceiptsByCreditCard(credit_card_no);
        }
        context.put("receipts", receipts);

        byte[] renderedTemplate = render("/credit_card_search.jinja2", context);
        out.write(renderedTemplate);

    }
}
