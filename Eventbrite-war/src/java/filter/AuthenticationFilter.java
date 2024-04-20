/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Filter.java to edit this template
 */
package filter;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import managedbean.CustomerManagedBean;

/**
 *
 * @author timothy
 */
public class AuthenticationFilter implements Filter {
    
    @Inject
    private CustomerManagedBean customerManagedBean;
    
    public AuthenticationFilter() {
    }    

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request1 = (HttpServletRequest) request;
        if (customerManagedBean == null || customerManagedBean.getCurrCustomer() == null) {
            ((HttpServletResponse) response).sendRedirect(request1.getContextPath() + "/index.xhtml");
        } else {
            chain.doFilter(request1, response);
        }
    }

    @Override
    public void destroy() {        
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {        
        
    }
}
