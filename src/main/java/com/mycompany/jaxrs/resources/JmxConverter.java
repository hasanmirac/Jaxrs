/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.jaxrs.resources;


public class JmxConverter implements JmxConverterMBean {

    public int pdfHitCount;
    public int htmlHitCount;
    public double pdfOutputSize;
    public double htmlOutputSize;
    public double pdfInputSize;
    public double htmlInputSize;
    public double pdfDuration;
    public double htmlDuration;

    @Override
    public void reportPdfHitCount() {
        pdfHitCount++;
    }

    @Override
    public void reportHtmlHitCount() {
        htmlHitCount++;
    }

    @Override
    public int getPdfHitCount() {
        return pdfHitCount;
    }

    @Override
    public int getHtmlHitCount() {
        return htmlHitCount;
    }

    @Override
    public void setPdfHitCount(int pdfHitCount) {
        this.pdfHitCount = pdfHitCount;
    }

    @Override
    public void setHtmlHitCount(int htmlHitCount) {
        this.htmlHitCount = htmlHitCount;
    }

    @Override
    public void reportHtmlInputBytes(long size) {
        this.htmlInputSize = size;
    }

    @Override
    public void reportPdfInputBytes(long size) {
        this.pdfInputSize = size;
    }

    @Override
    public void reportHtmlOutputBytes(long size) {
        this.htmlOutputSize = size;
    }

    @Override
    public void reportPdfOutputBytes(long size) {
        this.pdfOutputSize = size;
    }

    @Override
    public void reportHtmlDuration(long duration) {
        htmlDuration = (1.0 / htmlHitCount) *(duration - htmlDuration);

    }

    @Override
    public void reportPdfDuration(long duration) {
        pdfDuration = (1.0 / pdfHitCount) * (duration - pdfDuration);
    }

    @Override
    public double getHtmlInputByteSize() {
        return htmlInputSize;
    }

    @Override
    public double getPdfInputByteSize() {
        return pdfInputSize;
    }

    @Override
    public double getHtmlOutputByteSize() {
        return htmlOutputSize;
    }

    @Override
    public double getPdfOutputByteSize() {
        return pdfOutputSize;
    }

    @Override
    public double getreportHtmlDuration() {
        return htmlDuration;
    }

    @Override
    public double getreportPdfDuration() {
        return pdfDuration;
    }

    @Override
    public void reportRequestHitCount() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int getRequestHitCount() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    

}
