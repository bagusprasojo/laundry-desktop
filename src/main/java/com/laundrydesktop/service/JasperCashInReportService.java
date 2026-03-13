package com.laundrydesktop.service;

import com.laundrydesktop.model.CashInEntry;
import com.laundrydesktop.repo.BusinessProfileRepository;
import com.laundrydesktop.repo.PaymentTransactionRepository;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JasperCashInReportService {
    private final PaymentTransactionRepository paymentTransactionRepository = new PaymentTransactionRepository();
    private final BusinessProfileRepository businessProfileRepository = new BusinessProfileRepository();

    public void previewCashInReport(LocalDate startDate, LocalDate endDate) {
        JasperPrint jasperPrint = buildJasperPrint(startDate, endDate);
        JasperViewer.viewReport(jasperPrint, false);
    }

    private JasperPrint buildJasperPrint(LocalDate startDate, LocalDate endDate) {
        try {
            List<CashInEntry> entries = paymentTransactionRepository.findByDateRange(startDate, endDate);
            Collection<Map<String, ?>> rows = new ArrayList<>();
            for (CashInEntry entry : entries) {
                rows.add(toRowMap(entry));
            }
            JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(rows);

            int totalCashIn = paymentTransactionRepository.sumByDateRange(startDate, endDate);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("reportTitle", "Laporan Kas Masuk");
            parameters.put("periodLabel", startDate + " s.d " + endDate);
            parameters.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            parameters.put("totalCashIn", totalCashIn);

            var profile = businessProfileRepository.get();
            parameters.put("businessName", profile.businessName());
            parameters.put("businessAddress", profile.address());
            parameters.put("businessPhone", profile.phone());

            JasperReport report = compileTemplate();
            return JasperFillManager.fillReport(report, parameters, dataSource);
        } catch (Exception e) {
            throw new RuntimeException("Gagal membuat laporan kas masuk", e);
        }
    }

    private JasperReport compileTemplate() throws JRException {
        InputStream inputStream = getClass().getResourceAsStream("/reports/cash-in-period.jrxml");
        if (inputStream == null) {
            throw new IllegalStateException("Template laporan /reports/cash-in-period.jrxml tidak ditemukan.");
        }
        return JasperCompileManager.compileReport(inputStream);
    }

    private Map<String, ?> toRowMap(CashInEntry entry) {
        Map<String, Object> row = new HashMap<>();
        row.put("paymentDate", entry.paymentDate());
        row.put("invoiceNo", entry.invoiceNo());
        row.put("customerName", entry.customerName());
        row.put("paymentMethod", entry.paymentMethod());
        row.put("amount", entry.amount());
        row.put("note", entry.note());
        return row;
    }
}
