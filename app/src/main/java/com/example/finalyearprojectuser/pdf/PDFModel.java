package com.example.finalyearprojectuser.pdf;

public class PDFModel {

    //PDF Model class
    private String pdfName;
    private String pdfUrl;

    public PDFModel(String pdfName, String pdfUrl) {
        this.pdfName = pdfName;
        this.pdfUrl = pdfUrl;
    }

    //Gettle and Settle
    public String getPdfName() {
        return pdfName;
    }

    public void setPdfName(String pdfName) {
        this.pdfName = pdfName;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }
}
