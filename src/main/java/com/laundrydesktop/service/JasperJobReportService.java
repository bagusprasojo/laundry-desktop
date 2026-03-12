package com.laundrydesktop.service;

import com.laundrydesktop.model.Order;
import com.laundrydesktop.repo.BusinessProfileRepository;
import com.laundrydesktop.repo.OrderRepository;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JasperJobReportService {
    private final OrderRepository orderRepository = new OrderRepository();
    private final BusinessProfileRepository businessProfileRepository = new BusinessProfileRepository();

    public void previewJobListReport(LocalDate startDate, LocalDate endDate) {
        JasperPrint jasperPrint = buildJasperPrint(startDate, endDate);
        JasperViewer.viewReport(jasperPrint, false);
    }

    public Path exportJobListPdf(LocalDate startDate, LocalDate endDate) {
        JasperPrint jasperPrint = buildJasperPrint(startDate, endDate);
        try {
            Path outputDir = Paths.get(System.getProperty("user.home"), ".laundry-desktop", "reports");
            Files.createDirectories(outputDir);
            String fileName = "laporan-pekerjaan-" + startDate + "_sd_" + endDate + ".pdf";
            Path outputFile = outputDir.resolve(fileName);

            JasperExportManager.exportReportToPdfFile(jasperPrint, outputFile.toString());
            return outputFile;
        } catch (Exception e) {
            throw new RuntimeException("Gagal export laporan Jasper", e);
        }
    }

    private JasperPrint buildJasperPrint(LocalDate startDate, LocalDate endDate) {
        try {
            List<Order> orders = orderRepository.findByDateRange(startDate, endDate);
            Collection<Map<String, ?>> rows = orders.stream()
                    .map(this::toRowMap)
                    .toList();
            JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(rows);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Laporan Daftar Pekerjaan");
            parameters.put("periodLabel", startDate + " s.d " + endDate);
            parameters.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            var profile = businessProfileRepository.get();
            parameters.put("businessName", profile.businessName());
            parameters.put("businessAddress", profile.address());
            parameters.put("businessOwner", profile.ownerName());
            parameters.put("businessPhone", profile.phone());

            JasperReport report = compileTemplate();
            return JasperFillManager.fillReport(report, parameters, dataSource);
        } catch (Exception e) {
            throw new RuntimeException("Gagal membuat laporan Jasper", e);
        }
    }

    private JasperReport compileTemplate() throws JRException {
        InputStream inputStream = getClass().getResourceAsStream("/reports/job-list.jrxml");
        if (inputStream == null) {
            throw new IllegalStateException("Template laporan /reports/job-list.jrxml tidak ditemukan.");
        }
        return JasperCompileManager.compileReport(inputStream);
    }

    private Map<String, ?> toRowMap(Order order) {
        Map<String, Object> row = new HashMap<>();
        row.put("invoiceNo", order.invoiceNo());
        row.put("orderDate", order.orderDate());
        row.put("customerName", order.customerName());
        row.put("serviceName", order.serviceName());
        row.put("speedName", order.speedName());
        row.put("unitName", order.unitName());
        row.put("quantity", order.quantity());
        row.put("totalPrice", order.totalPrice());
        row.put("orderStatus", order.orderStatus());
        row.put("paymentStatus", order.paymentStatus());
        return row;
    }
}
