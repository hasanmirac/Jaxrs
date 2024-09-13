/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.jaxrs.resources;

import static com.mycompany.jaxrs.resources.Converter.mbean;
import static com.mycompany.jaxrs.resources.Converter.replyq;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

/**
 *
 * @author hasang
 */
public class ConvertPdf extends ConvertHtml {

    private JmsConnection jmsConnection;

    private Queue pdfq;
    private Session session;

    public ConvertPdf() throws JMSException {
        session = JmsConnection.getInstance().getSession();
        pdfq = session.createQueue("pdfq");
        session.createConsumer(pdfq).setMessageListener(this);

    }

    public void convertPdf(Message message) throws JMSException {
        //@FormParam("data") String data, @Context HttpServletRequest request) throws JMSException 
        long start, finish;
        start = System.currentTimeMillis();
        MapMessage mapMessage = (MapMessage) message;
        String data = mapMessage.getString("data");
        String xslt = mapMessage.getString("xslt");

        byte[] htmlContent;
        if (xslt == null) {
            htmlContent = getToHtml(data, null);
        } else {

            htmlContent = getToHtml(data, xslt);
        }
        UUID uuid = UUID.randomUUID();

        try (FileOutputStream fos = new FileOutputStream("/tmp/" + uuid.toString() + ".html");) {
            fos.write(htmlContent);
        } catch (IOException ex) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            //return Response.status(415).entity(ex.getLocalizedMessage()).build();
        }
        finish = System.currentTimeMillis();
        mbean.reportHtmlHitCount();
        mbean.reportHtmlDuration(finish - start);
        mbean.reportHtmlInputBytes(data.length());
        mbean.reportHtmlOutputBytes(htmlContent.length);

        Logger.getLogger(ConvertPdf.class.getName()).log(Level.INFO, "Html çevrim süresi = " + (finish - start) + "ms");
        Logger.getLogger(ConvertPdf.class.getName()).log(Level.INFO, "Html ortalama çevrim süresi =" + mbean.getreportHtmlDuration() + "ms");
        Logger.getLogger(ConvertPdf.class.getName()).log(Level.INFO, "Html gelen istek sayısı =" + mbean.getHtmlHitCount());
        Logger.getLogger(ConvertPdf.class.getName()).log(Level.INFO, "Html Input data size =" + mbean.getHtmlInputByteSize());
        Logger.getLogger(ConvertPdf.class.getName()).log(Level.INFO, "Html Output data size =" + mbean.getHtmlOutputByteSize());

        try {

            start = System.currentTimeMillis();
            String phantomjs = System.getProperty("phantomjs.bin", "phantomjs");
            String rasterize = System.getProperty("rasterize.js", "/usr/local/share/phantomjs-1.9.7-linux-x86_64/examples/rasterize.js");

            String[] cmd = {phantomjs, rasterize, "/tmp/" + uuid.toString() + ".html", "/tmp/" + uuid.toString() + ".pdf", "A4", "1.0"};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            try (FileInputStream fis = new FileInputStream("/tmp/" + uuid.toString() + ".pdf");) {

                byte[] buffer = new byte[64000];
                int okundu;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                do {
                    okundu = fis.read(buffer);
                    if (okundu > 0) {
                        baos.write(buffer, 0, okundu);
                    }
                } while (okundu > 0);
                finish = System.currentTimeMillis();
                mbean.reportPdfHitCount();
                mbean.reportPdfDuration(finish - start);
                mbean.reportPdfInputBytes(htmlContent.length);
                mbean.reportPdfOutputBytes(baos.size());
                Logger.getLogger(ConvertPdf.class.getName()).log(Level.INFO, "Pdf çevrim süresi = " + (finish - start) + "ms");
                Logger.getLogger(ConvertPdf.class.getName()).log(Level.INFO, "Pdf ortalama çevrim süresi =" + mbean.getreportPdfDuration() + "ms");
                Logger.getLogger(ConvertPdf.class.getName()).log(Level.INFO, "Pdf gelen istek sayısı =" + mbean.getPdfHitCount());
                Logger.getLogger(ConvertPdf.class.getName()).log(Level.INFO, "Pdf input data size =" + mbean.getPdfInputByteSize());
                Logger.getLogger(ConvertPdf.class.getName()).log(Level.INFO, "Pdf output data size =" + mbean.getPdfOutputByteSize());

//                URL url = new URL("http://localhost:8080/istatistik/rest/test/deneme/?inputByteSize=" + mbean.getPdfInputSize() + "&outputByteSize=" + mbean.getPdfOutputSize() + "&duration=" + mbean
//                        .getPdfDuration() + "&subject=pdf");
//                String jsonInput = "{\"inputByteSize\":" + mbean.getPdfInputSize() + "outputByteSize\":" + mbean.getPdfOutputSize() + "duration\":" + mbean.getPdfDuration() + "subject\": \"pdf\"}";
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Content-Type", "application/json");
//                connection.setDoOutput(true);
//
//                try (OutputStream os = connection.getOutputStream()) {
//                    byte[] input = jsonInput.getBytes("utf-8");
//                    os.write(input, 0, input.length);
//                }

                //return Response.status(200).entity(baos.toByteArray()).build();
                try {
                    MessageProducer producer = session.createProducer(replyq);

                    MapMessage requestMessage = session.createMapMessage();

                    requestMessage.setBytes("data", baos.toByteArray());
                    requestMessage.setJMSCorrelationID(mapMessage.getJMSCorrelationID());

                    requestMessage.setJMSReplyTo(replyq);

                    producer.send(requestMessage);
                    session.commit();

                } catch (JMSException e) {
                    e.printStackTrace();
                }

            }

        } catch (IOException | InterruptedException t) {
            Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, t);

        } finally {
            new File("/tmp/" + uuid.toString() + ".html").delete();
            new File("/tmp/" + uuid.toString() + ".pdf").delete();
        }

    }

    @Override
    public void onMessage(Message message) {
        try {
            convertPdf(message);
        } catch (JMSException ex) {
            Logger.getLogger(ConvertPdf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
