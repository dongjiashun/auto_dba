package com.autodb.ops.dms.web.controller;

import com.autodb.ops.dms.common.AppContext;
import com.autodb.ops.dms.common.data.pagination.Orders;
import com.autodb.ops.dms.common.data.pagination.Page;
import com.autodb.ops.dms.common.data.pagination.Pagination;
import com.autodb.ops.dms.entity.user.User;
import com.autodb.ops.dms.web.exception.PageNotFoundException;
import com.autodb.ops.dms.web.exception.RedirectException;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller super class
 * @author dongjs
 * @since 2015/10/23
 */
public class SuperController {
    private static Logger log = LoggerFactory.getLogger(SuperController.class);

    @Autowired
    protected VelocityEngine velocityEngine;

    /** JSR-349 spring mvc抛出的异常 */
    @ExceptionHandler(ValidationException.class)
    public ModelAndView handleValidationException(ValidationException e, HttpServletResponse response) throws IOException {
        // 400
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        return new ModelAndView();
    }

    /**
     * 在controller未捕获的NumberFormatException将404
     */
    @ExceptionHandler(NumberFormatException.class)
    public ModelAndView handleNumberFormatException(NumberFormatException e, HttpServletResponse response) throws IOException {
        // 404
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return new ModelAndView();
    }

    /** page not found will 404 **/
    @ExceptionHandler(PageNotFoundException.class)
    public ModelAndView handlePageNotFoundException(PageNotFoundException e, HttpServletResponse response) throws IOException {
        // 404
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return new ModelAndView();
    }

    /** redirect to url **/
    @ExceptionHandler(RedirectException.class)
    public ModelAndView handleRedirectException(RedirectException e, HttpServletResponse response) throws IOException {
        // redirect
        ModelAndView mav = new ModelAndView();
        mav.setView(new RedirectView(e.getUrl()));
        return mav;
    }

    protected User getUser() {
        return AppContext.getCurrentUser();
    }

    /** merge template to string **/
    public String mergeTemplate(String template, Map<String, Object> contextMap) {
        StringWriter html = new StringWriter();
        VelocityContext context = new VelocityContext(contextMap);

        velocityEngine.mergeTemplate(template, "UTF-8", context, html);
        return html.toString();
    }

    /** merge template to string **/
    public String mergeTemplate(String template, String contextName, Object contextObj) {
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put(contextName, contextObj);
        return mergeTemplate(template, contextMap);
    }

    /** limit <= 500 **/
    public <T> Page<T> getPage(HttpServletRequest request) {
        String offsetStr = request.getParameter("offset");
        String limitStr = request.getParameter("limit");
        String sort = request.getParameter("sort");
        String order = request.getParameter("order");
        String search = request.getParameter("search");

        // pagination
        Pagination pagination = new Pagination();
        try {
            if (StringUtils.isNoneEmpty(offsetStr) && StringUtils.isNoneEmpty(limitStr)) {
                int offset = Integer.parseInt(offsetStr);
                int limit = Integer.parseInt(limitStr);

                if (limit > 500) {
                    log.info("data query pagination limit {}", limit);
                }
                limit = limit > 500 ? 500 : limit;

                pagination.setCurrentPage(offset / limit + 1);
                pagination.setPageSize(limit);
            }
        } catch (NumberFormatException e) {
            // ignore
        }

        // orders
        Orders orders = null;
        if (StringUtils.isNoneEmpty(order)) {
            orders = StringUtils.isNoneEmpty(sort)
                    ? new Orders(sort, "asc".equalsIgnoreCase(order))
                    : new Orders("asc".equalsIgnoreCase(order));
        }

        return StringUtils.isNotBlank(search)
                ? new Page<>(pagination, orders, search)
                : new Page<>(pagination, orders);
    }
}
