/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.mycompany.jaxrs.resources;


import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;


@WebServlet(name = "JmsConsumerServlet", urlPatterns = {"/JmsConsumerServlet"}, loadOnStartup = 1)
public class JmsConsumerServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    private Connection connection;
    private Session session;
    private MessageConsumer htmlConsumer;
    private MessageConsumer pdfConsumer;
    private JmsConnection jmsConnection;
    private int htmlConsumerCount;
    private int pdfConsumerCount;
    private ConvertHtml convertHtml;
    private ConvertPdf convertPdf;

    @Override
    public void init() throws ServletException {
       
        super.init();
 
        if (!Converter.registered) {
            Converter.registered = true;
            try {
                Converter.mbean = new JmxConverter();
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

                ObjectName helloName = null;

                helloName = new ObjectName("tr.com.cs:project=Jaxrs");
                mbs.registerMBean(Converter.mbean, helloName);

            } catch (Throwable t) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, t);
            }
        }
        try {
            session = JmsConnection.getInstance().getSession();

            Queue htmlq = session.createQueue("htmlq");
            Queue pdfq = session.createQueue("pdfq");

            htmlConsumerCount = Integer.parseInt(System.getProperty("html_consumer_count", "1"));
            pdfConsumerCount = Integer.parseInt(System.getProperty("pdf_consumer_count", "1"));

            for (int i = 1; i <= htmlConsumerCount; i++) {

                convertHtml = new ConvertHtml();
            }

            for (int i = 1; i <= pdfConsumerCount; i++) {
                convertPdf = new ConvertPdf();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
   
    private static void unregisterMBean() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName("tr.com.cs:project=Jaxrs");
            mbs.unregisterMBean(name);
        } catch (MalformedObjectNameException | MBeanRegistrationException | InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {

        try {
            if (htmlConsumer != null) {
                htmlConsumer.close();
            }
            if (pdfConsumer != null) {
                pdfConsumer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
            if (Converter.mbean != null) {
                unregisterMBean();
            }
           
        } catch (JMSException e) {
            e.printStackTrace();
        }

        super.destroy();
    }

}
