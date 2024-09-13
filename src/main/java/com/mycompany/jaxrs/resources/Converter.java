///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
package com.mycompany.jaxrs.resources;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;

import javax.ws.rs.core.Context;

@Path("converter")
public class Converter {

    static JmxConverter mbean;
    static boolean registered;

    static JmsConnection jmsConnection;

    static Session session;
    static Queue htmlq;
    static Queue pdfq;
    static Queue replyq;
        private static final Logger LOGGER = Logger.getLogger(ConvertHtml.class.getName());

    static {
        try {

            session = JmsConnection.getInstance().getSession();
            htmlq = session.createQueue("htmlq");
            pdfq = session.createQueue("pdfq");
            replyq = session.createQueue("replyq");

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @POST
    @Path("/convert/{type}")
    public Response sendActivemq(@FormParam("data") String data,@Context HttpServletRequest request, @PathParam("type") String action) throws JMSException, IOException {

        MessageProducer producer = session.createProducer("html".equals(action) ? htmlq : pdfq);

        UUID uuid = UUID.randomUUID();
        MapMessage requestMessage = session.createMapMessage();
        requestMessage.setString("action", action);
        requestMessage.setString("data", data);
        requestMessage.setJMSCorrelationID(uuid.toString());
        requestMessage.setJMSReplyTo(replyq);

        producer.send(requestMessage);

        System.out.println("Mesaj gönderildi: ");
        session.commit();

        try {
            MessageConsumer consumer = JmsConnection.getInstance().getNewSession().createConsumer(replyq, "JMSCorrelationID = '" + uuid.toString() + "'");
            Message message = consumer.receive();

            if (message instanceof MapMessage) {
                MapMessage mapMessage = (MapMessage) message;
                String responseAction = mapMessage.getString("action");
                byte[] responseData = mapMessage.getBytes("data");
                String correlationId = mapMessage.getJMSCorrelationID();

                System.out.println("Received MapMessage:");
                System.out.println("Action: " + responseAction);
                System.out.println("Data: " + new String(responseData,"UTF-8"));
                System.out.println("Correlation ID: " + correlationId);
                return Response.status(200).entity(responseData).type("html".equals(action) ? "text/html" : "application/pdf").build();

            }
        } catch (JMSException e) {
            LOGGER.log(Level.SEVERE, "JMSException", e);
            session.rollback();
        }
        return Response.status(Response.Status.OK).entity("Geçersiz veri türü").build();

    }
        
    @POST
    @Path("/convertWithXslt/{type}")
    public Response sendActivemq(@FormParam("data") String data,@FormParam("xslt") String xslt, @PathParam("type") String action) throws JMSException, IOException {

        MessageProducer producer = session.createProducer("html".equals(action) ? htmlq : pdfq);

        UUID uuid = UUID.randomUUID();
        MapMessage requestMessage = session.createMapMessage();
        requestMessage.setString("action", action);
        requestMessage.setString("data", data);
        requestMessage.setString("xslt", xslt);
        requestMessage.setJMSCorrelationID(uuid.toString());
        requestMessage.setJMSReplyTo(replyq);

        producer.send(requestMessage);

        System.out.println("Mesaj gönderildi: ");
        session.commit();

        try {
            MessageConsumer consumer = JmsConnection.getInstance().getNewSession().createConsumer(replyq, "JMSCorrelationID = '" + uuid.toString() + "'");
            Message message = consumer.receive();

            if (message instanceof MapMessage) {
                MapMessage mapMessage = (MapMessage) message;
                String responseAction = mapMessage.getString("action");
                byte[] responseData = mapMessage.getBytes("data");
                String correlationId = mapMessage.getJMSCorrelationID();

                System.out.println("Received MapMessage:");
                System.out.println("Action: " + responseAction);
                System.out.println("Data: " + new String(responseData,"UTF-8"));
                System.out.println("Correlation ID: " + correlationId);
                return Response.status(200).entity(responseData).type("html".equals(action) ? "text/html" : "application/pdf").build();

            }
        } catch (JMSException e) {
            LOGGER.log(Level.SEVERE, "JMSException", e);
            session.rollback();
        }
        return Response.status(Response.Status.OK).entity("Geçersiz veri türü").build();

    }

}
