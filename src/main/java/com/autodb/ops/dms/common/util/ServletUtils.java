package com.autodb.ops.dms.common.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * servlet工具类<br/>
 *
 * @author dongjs
 * @since 2012-11-12
 */
public class ServletUtils {
    /**
     * 判断请求是否是ajax请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return requestedWith != null && "XMLHttpRequest".equals(requestedWith);
    }

    /**
     * 判断是否为搜索引擎
     */
    public static boolean isRobot(HttpServletRequest request) {
        String ua = request.getHeader("user-agent");
        return !StringUtils.isBlank(ua) && (ua.contains("Baiduspider")
                || ua.contains("Googlebot") || ua.contains("sogou")
                || ua.contains("sina") || ua.contains("iaskspider")
                || ua.contains("ia_archiver") || ua.contains("Sosospider")
                || ua.contains("YoudaoBot") || ua.contains("yahoo")
                || ua.contains("yodao") || ua.contains("MSNBot")
                || ua.contains("spider") || ua.contains("Twiceler")
                || ua.contains("Sosoimagespider") || ua.contains("naver.com/robots")
                || ua.contains("Nutch") || ua.contains("spider"));
    }

    /**
     * 获取IP地址
     *
     * @param request request current HTTP request
     */
    public static String getRemoteAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Determine the session id of the given request, if any.
     *
     * @param request current HTTP request
     * @return the session id, or <code>null</code> if none
     */
    public static String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null ? session.getId() : null);
    }

    /**
     * Check the given request for a session attribute of the given name.
     * Returns null if there is no session or if the session has no such attribute.
     * Does not create a new session if none has existed before!
     *
     * @param request current HTTP request
     * @param name    the name of the session attribute
     * @return the value of the session attribute, or <code>null</code> if not found
     */
    public static Object getSessionAttribute(HttpServletRequest request, String name) {
        HttpSession session = request.getSession(false);
        return (session != null ? session.getAttribute(name) : null);
    }

    /**
     * Set the session attribute with the given name to the given value.
     * Removes the session attribute if value is null, if a session existed at all.
     * Does not create a new session if not necessary!
     *
     * @param request current HTTP request
     * @param name    the name of the session attribute
     * @param value   the value of the session attribute
     */
    public static void setSessionAttribute(HttpServletRequest request, String name, Object value) {
        if (value != null) {
            request.getSession().setAttribute(name, value);
        } else {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(name);
            }
        }
    }

    /**
     * Retrieve the first cookie with the given name. Note that multiple
     * cookies can have the same name but different paths or domains.
     *
     * @param request current servlet request
     * @param name    cookie name
     * @return the first cookie with the given name, or <code>null</code> if none is found
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * 根据cookie名称返回cookie的值
     *
     * @param request http请求
     * @param name    cookie名称
     * @return 返回cookie值, 值不存在则返回null
     */
    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie cookie = getCookie(request, name);
        if (cookie == null) {
            return null;
        }
        return cookie.getValue();
    }

    /**
     * 是否是有效的Redirect地址
     */
    public static boolean isValidRedirectUrl(String url) {
        return url != null && url.startsWith("/") || isAbsoluteUrl(url);
    }

    /**
     * 是否是绝对地址
     */
    public static boolean isAbsoluteUrl(String url) {
        final Pattern ABSOLUTE_URL = Pattern.compile("\\A[a-z.+-]+://.*", Pattern.CASE_INSENSITIVE);
        return ABSOLUTE_URL.matcher(url).matches();
    }

    /**
     * 转发到指定页面<br/>
     * /user/index.jsp
     *
     * @param pageUrl 转发的页面
     */
    public static void forward(HttpServletRequest request, HttpServletResponse response, String pageUrl) throws IOException, ServletException {
        if (!response.isCommitted()) {
            if (StringUtils.isNotBlank(pageUrl)) {
                // forward to page.
                request.getRequestDispatcher(pageUrl).forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
            }
        }
    }

    /**
     * 重定向到指定页面<br/>
     * /user/index.jsp
     *
     * @param pageUrl 重定向的页面
     * @throws IOException
     */
    public static void redirect(HttpServletRequest request, HttpServletResponse response, String pageUrl) throws IOException {
        if (!response.isCommitted()) {
            if (StringUtils.isNotBlank(pageUrl)) {
                // redirect to page.
                response.sendRedirect(request.getContextPath() + pageUrl);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
            }
        }
    }
}