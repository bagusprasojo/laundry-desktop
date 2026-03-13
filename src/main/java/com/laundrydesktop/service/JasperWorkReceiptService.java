package com.laundrydesktop.service;

import com.laundrydesktop.model.Order;
import com.laundrydesktop.repo.BusinessProfileRepository;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JasperWorkReceiptService {
    private final BusinessProfileRepository businessProfileRepository = new BusinessProfileRepository();

    public void previewReceipt(Order order) {
        try {
            InputStream template = getClass().getResourceAsStream("/reports/work-receipt.jrxml");
            if (template == null) {
                throw new IllegalStateException("Template nota /reports/work-receipt.jrxml tidak ditemukan.");
            }

            JasperReport report = JasperCompileManager.compileReport(template);

            Map<String, Object> params = new HashMap<>();
            var profile = businessProfileRepository.get();
            params.put("businessName", profile.businessName());
            params.put("businessAddress", profile.address());
            params.put("businessPhone", profile.phone());
            params.put("printedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            Map<String, Object> row = new HashMap<>();
            row.put("invoiceNo", order.invoiceNo());
            row.put("orderDate", order.orderDate());
            row.put("customerName", order.customerName());
            row.put("serviceName", order.serviceName());
            row.put("speedName", order.speedName());
            row.put("unitName", order.unitName());
            row.put("quantity", order.quantity());
            row.put("unitPrice", order.unitPrice());
            row.put("totalPrice", order.totalPrice());
            row.put("paidAmount", order.paidAmount());
            row.put("paymentMethod", order.paymentMethod());
            row.put("paymentStatus", order.paymentStatus());
            row.put("orderStatus", order.orderStatus());
            row.put("note", order.note());
            row.put("estimateDone", order.estimateDone());

            JasperPrint print = JasperFillManager.fillReport(report, params, new JRMapCollectionDataSource(List.of(row)));
            JasperViewer.viewReport(print, false);
        } catch (Exception e) {
            throw new RuntimeException("Gagal menampilkan nota Jasper", e);
        }
    }
}
