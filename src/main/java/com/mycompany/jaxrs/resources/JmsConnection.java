package com.mycompany.jaxrs.resources;


import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;


public class JmsConnection {

    private static ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();

    private static Connection connection = null;
    private static Session session = null;
    private static JmsConnection instance;

    static {
        try {
            instance = new JmsConnection();
        } catch (JMSException ex) {
            Logger.getLogger(JmsConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JmsConnection() throws JMSException {

        if (connection == null) {

            connectionFactory.setUserName(System.getProperty("activemq.broker.username", "admin"));
            connectionFactory.setBrokerURL(System.getProperty("activemq.broker.url", "tcp://localhost:61616"));
            connectionFactory.setPassword(System.getProperty("activemq.broker.password", "admin"));
            connection = connectionFactory.createConnection();
            connection.start();

        }
    }

    public synchronized Session getSession() throws JMSException {

        if (session == null) {
            try {
                session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        return session;
    }

    public static JmsConnection getInstance() {
        return instance;
    }

    public synchronized Session getNewSession() throws JMSException {

        return connection.createSession(true, Session.AUTO_ACKNOWLEDGE);

    }

}