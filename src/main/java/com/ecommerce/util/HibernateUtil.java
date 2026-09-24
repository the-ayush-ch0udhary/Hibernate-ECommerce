package com.ecommerce.util;

import com.ecommerce.entity.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to configure and manage the Hibernate SessionFactory singleton.
 */
public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;
    private static String activeConfigFile = "hibernate.cfg.xml";

    static {
        try {
            sessionFactory = buildSessionFactory(activeConfigFile);
        } catch (Throwable ex) {
            logger.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static SessionFactory buildSessionFactory(String configFile) {
        try {
            logger.info("Initializing Hibernate SessionFactory with configuration: {}", configFile);
            Configuration configuration = new Configuration().configure(configFile);
            
            // Explicitly register entity classes to ensure persisters are registered
            configuration.addAnnotatedClass(Category.class);
            configuration.addAnnotatedClass(Product.class);
            configuration.addAnnotatedClass(Users.class);
            configuration.addAnnotatedClass(Orders.class);
            configuration.addAnnotatedClass(OrderDetails.class);

            return configuration.buildSessionFactory();
        } catch (Exception ex) {
            logger.error("Failed to build SessionFactory for {}", configFile, ex);
            throw new RuntimeException("SessionFactory initialization error: " + ex.getMessage(), ex);
        }
    }

    /**
     * Retrieves the default SessionFactory instance.
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Allows rebuilding the SessionFactory with a different configuration (e.g. for MySQL or Tests).
     */
    public static synchronized void reconfigure(String configFile) {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        activeConfigFile = configFile;
        sessionFactory = buildSessionFactory(configFile);
    }

    /**
     * Safely closes all SessionFactory resources.
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            logger.info("Closing Hibernate SessionFactory.");
            sessionFactory.close();
        }
    }
}
