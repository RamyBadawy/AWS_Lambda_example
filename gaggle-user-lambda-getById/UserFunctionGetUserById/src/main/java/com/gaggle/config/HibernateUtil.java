package com.gaggle.config;



import static org.hibernate.cfg.AvailableSettings.DEFAULT_SCHEMA;
import static org.hibernate.cfg.AvailableSettings.DIALECT;
import static org.hibernate.cfg.AvailableSettings.DRIVER;
import static org.hibernate.cfg.AvailableSettings.PASS;
import static org.hibernate.cfg.AvailableSettings.SHOW_SQL;
import static org.hibernate.cfg.AvailableSettings.URL;
import static org.hibernate.cfg.AvailableSettings.USER;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.gaggle.model.User;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    private static SessionFactory buildSessionFactory() {
    	 Configuration configuration = new Configuration();

 	    String jdbcUrl = String.format(
 	        "jdbc:mysql://%s/%s",
 	        System.getenv("DB_URL"),
 	        System.getenv("DB_NAME"));

 	    configuration
 	        .addAnnotatedClass(User.class)
 	        .setProperty(URL, jdbcUrl + "?serverTimezone=UTC&useUnicode=true&useSSL=false&autoReconnect=true&failOverReadOnly=false&maxReconnects=10")
 	        .setProperty(USER, System.getenv("DB_USER"))
 	        .setProperty(PASS, System.getenv("DB_PASSWORD"))
 	        .setProperty(DIALECT, "org.hibernate.dialect.MySQL57Dialect")
 	        .setProperty("storage_engine", "org.hibernate.dialect.storage_engine")
 	        .setProperty(DEFAULT_SCHEMA, "user")
 	        .setProperty(DRIVER, "com.mysql.cj.jdbc.Driver")
 	        .setProperty(SHOW_SQL, "true")
 	        .setProperty("hibernate.hikari.connectionTimeout", "30000")
 	        .setProperty("hibernate.hikari.idleTimeout", "60000")
 	        .setProperty("hibernate.hikari.minimumIdle", "1")
 	        .setProperty("hibernate.hikari.maximumPoolSize", "2")
 	        .setProperty("hibernate.hikari.maxLifetime", "1800000");

 	    ServiceRegistry serviceRegistry =
 	        new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();

 	    try {
 	      return new MetadataSources(serviceRegistry)
 	              .addAnnotatedClass(User.class)
 	              .buildMetadata()
 	              .buildSessionFactory();
 	    } catch (HibernateException e) {
 	      System.err.println("Initial SessionFactory creation failed." + e);
 	      throw new ExceptionInInitializerError(e);
 	    }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }
}
