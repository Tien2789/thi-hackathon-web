package com.ontop.wms.service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.springframework.stereotype.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PdfService {

    public byte[] generatePdfFromHtml(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage());
            return null;
        }
    }

    public String generateInboundHtml(Object inbound) {
        // Simple HTML template for 01-VT
        return "<html><body><h1>Phiếu Nhập Kho (Mẫu 01-VT)</h1><p>Đang triển khai mẫu chi tiết...</p></body></html>";
    }

    public String generateOutboundHtml(Object outbound) {
        // Simple HTML template for 02-VT
        return "<html><body><h1>Phiếu Xuất Kho (Mẫu 02-VT)</h1><p>Đang triển khai mẫu chi tiết...</p></body></html>";
    }
}
