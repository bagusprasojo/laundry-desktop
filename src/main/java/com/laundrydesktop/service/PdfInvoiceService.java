package com.laundrydesktop.service;

import com.laundrydesktop.model.Order;
import com.laundrydesktop.repo.BusinessProfileRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PdfInvoiceService {
    private final BusinessProfileRepository businessProfileRepository = new BusinessProfileRepository();

    public Path generate(Order order) {
        try {
            Path outputDir = Paths.get(System.getProperty("user.home"), ".laundry-desktop", "invoices");
            Files.createDirectories(outputDir);
            Path pdfPath = outputDir.resolve(order.invoiceNo() + ".pdf");
            var profile = businessProfileRepository.get();

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfPath.toFile()));
            document.open();
            document.add(new Paragraph(profile.businessName().isBlank() ? "NOTA LAUNDRY" : profile.businessName()));
            if (!profile.address().isBlank()) {
                document.add(new Paragraph("Alamat: " + profile.address()));
            }
            if (!profile.ownerName().isBlank()) {
                document.add(new Paragraph("Penanggung Jawab: " + profile.ownerName()));
            }
            if (!profile.phone().isBlank()) {
                document.add(new Paragraph("No. Telp: " + profile.phone()));
            }
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Invoice: " + order.invoiceNo()));
            document.add(new Paragraph("Tanggal: " + order.orderDate()));
            document.add(new Paragraph("Pelanggan: " + order.customerName()));
            document.add(new Paragraph("Layanan: " + order.serviceName()));
            document.add(new Paragraph("Kecepatan: " + order.speedName()));
            document.add(new Paragraph("Satuan: " + order.unitName()));
            document.add(new Paragraph("Qty: " + order.quantity()));
            document.add(new Paragraph("Harga Satuan: Rp" + order.unitPrice()));
            document.add(new Paragraph("Total: Rp" + order.totalPrice()));
            document.add(new Paragraph("Pembayaran: " + order.paymentMethod()));
            document.add(new Paragraph("Status Bayar: " + order.paymentStatus()));
            document.add(new Paragraph("Status Order: " + order.orderStatus()));
            document.add(new Paragraph("Estimasi Selesai: " + order.estimateDone()));
            document.close();
            return pdfPath;
        } catch (Exception e) {
            throw new RuntimeException("Gagal membuat PDF nota", e);
        }
    }
}
