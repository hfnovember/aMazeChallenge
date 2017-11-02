package org.inspirecenter.amazechallenge;

import com.googlecode.objectify.ObjectifyService;
import org.inspirecenter.amazechallenge.data.Parameter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class OfyHelper implements ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {
        // This will be invoked as part of a warm-up request, or the first user request if no warm-up request.
        ObjectifyService.register(Parameter.class);
        // todo add more?
    }

    public void contextDestroyed(ServletContextEvent event) {
        // App Engine does not currently invoke this method.
    }
}