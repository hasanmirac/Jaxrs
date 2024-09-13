/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.jaxrs.resources;

/**
 *
 * @author hasang
 */
public interface JmxConverterMBean {

    int getHtmlHitCount();

    int getPdfHitCount();

    void reportHtmlHitCount();

    void reportPdfHitCount();

    void reportHtmlDuration(long duration);

    void reportPdfDuration(long duration);

    void reportHtmlInputBytes(long size);

    void reportHtmlOutputBytes(long size);
    
    double getHtmlInputByteSize();

    double getPdfInputByteSize();

    void reportPdfInputBytes(long size);

    void reportPdfOutputBytes(long size);
    
    double getHtmlOutputByteSize();

    double getPdfOutputByteSize();

    void setHtmlHitCount(int htmlHitCount);

    void setPdfHitCount(int pdfHitCount);
    
    void reportRequestHitCount();
    
    int getRequestHitCount();
    
    double getreportHtmlDuration();

    double getreportPdfDuration();

}
