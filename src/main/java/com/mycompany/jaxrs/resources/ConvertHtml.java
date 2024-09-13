/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.jaxrs.resources;

import static com.mycompany.jaxrs.resources.Converter.mbean;
import static com.mycompany.jaxrs.resources.Converter.replyq;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author hasang
 */
public class ConvertHtml implements MessageListener {

    private JmsConnection jmsConnection;
    // private JmsConsumerServlet servlet;
    private Queue htmlq;
    private Session session;

    public ConvertHtml() throws JMSException {
        session = JmsConnection.getInstance().getSession();
        htmlq = session.createQueue("htmlq");
        session.createConsumer(htmlq).setMessageListener(this);

    }

    public byte[] getToHtml(String data, String xslt) {
        // xslt
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {

            DocumentBuilder builder = factory.newDocumentBuilder();
            //   InputSource is = new InputSource(new ByteArrayInputStream(Base64.getMimeDecoder().decode(data)));

            InputSource is = new InputSource(new ByteArrayInputStream(Base64.getMimeDecoder().decode(data)));

            Document doc = builder.parse(is);

            byte[] xsltBytes = null;
            
            if(xslt == null || xslt.isEmpty()){
                NodeList nodes = doc.getElementsByTagNameNS("urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", "EmbeddedDocumentBinaryObject");               

                if (nodes.getLength() > 0) {
                    xslt = nodes.item(0).getTextContent();
                    xsltBytes = Base64.getMimeDecoder().decode(xslt);                    
                }
            }else{
                xsltBytes = Base64.getMimeDecoder().decode(xslt);
            }
            
            if (xsltBytes != null) {

                TransformerFactory transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
                // SAXTransformerFactory transformerFactory=(SAXTransformerFactory) SAXTransformerFactory.newInstance();

                StreamSource xsltProgram = new StreamSource(new ByteArrayInputStream(xsltBytes));
                Transformer transformer = transformerFactory.newTransformer(xsltProgram);
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                DOMSource source = new DOMSource(doc);
                ByteArrayOutputStream htmlBytes = new ByteArrayOutputStream();
                StreamResult result = new StreamResult(htmlBytes);
                transformer.transform(source, result);
                return htmlBytes.toByteArray();

            }

        } catch (Exception t) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, t);

        }
        return new byte[0];

    }

    public void convertHtml(Message message) throws JMSException {
        // @FormParam("data") String data
        //, @Context HttpServletRequest request
        long start, finish;
        start = System.currentTimeMillis();
        MapMessage mapMessage = (MapMessage) message;
        String data = mapMessage.getString("data");
        String xslt = mapMessage.getString("xslt");
        byte[] htmlContent ;
        if(xslt == null) {
            htmlContent = getToHtml(data, null);
        } else {
            
            htmlContent = getToHtml(data, xslt);
        }
        
        finish = System.currentTimeMillis();
        mbean.reportHtmlHitCount();
        mbean.reportHtmlDuration(finish - start);
        mbean.reportHtmlInputBytes(data.length());
        mbean.reportHtmlOutputBytes(htmlContent.length);

        Logger.getLogger(ConvertHtml.class.getName()).log(Level.INFO, "Html çevrim süresi = " + (finish - start) + "ms");
        Logger.getLogger(ConvertHtml.class.getName()).log(Level.INFO, "Html ortalama çevrim süresi =" + mbean.getreportHtmlDuration() + "ms");
        Logger.getLogger(ConvertHtml.class.getName()).log(Level.INFO, "Html gelen istek sayısı =" + mbean.getHtmlHitCount());
        Logger.getLogger(ConvertHtml.class.getName()).log(Level.INFO, "Html Input data size =" + mbean.getHtmlInputByteSize());
        Logger.getLogger(ConvertHtml.class.getName()).log(Level.INFO, "Html Output data size =" + mbean.getHtmlOutputByteSize());
        if (htmlContent.length > 0) {

//            return Response.status(200).entity(htmlContent).build();
            // producer üret replyq ya gönder
            //            requestMessage.setString("data", htmlContent.toString());
            try {
                MessageProducer producer = session.createProducer(replyq);

                MapMessage requestMessage = session.createMapMessage();

                requestMessage.setBytes("data", htmlContent);
                requestMessage.setJMSCorrelationID(mapMessage.getJMSCorrelationID());

                requestMessage.setJMSReplyTo(replyq);

                producer.send(requestMessage);
                session.commit();

                //producer.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onMessage(Message message) {
        try {
            convertHtml(message);
        } catch (JMSException ex) {
            Logger.getLogger(ConvertHtml.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
