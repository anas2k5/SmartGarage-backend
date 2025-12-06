package com.smartgarage.backend.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smartgarage.backend.exception.ResourceNotFoundException;
import com.smartgarage.backend.model.Booking;
import com.smartgarage.backend.model.Invoice;
import com.smartgarage.backend.model.Payment;
import com.smartgarage.backend.repository.BookingRepository;
import com.smartgarage.backend.repository.InvoiceRepository;
import com.smartgarage.backend.repository.PaymentRepository;
import com.smartgarage.backend.service.InvoicePdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public byte[] generateInvoicePdf(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        Invoice invoice = invoiceRepository.findByBooking(booking)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for booking " + bookingId));

        Payment payment = paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking " + bookingId));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Smart Garage - Invoice", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);

            // Invoice Info
            Font bold = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 12);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            infoTable.addCell(makeCell("Invoice Number:", bold));
            infoTable.addCell(makeCell(invoice.getInvoiceNumber(), normal));

            infoTable.addCell(makeCell("Invoice Date:", bold));
            infoTable.addCell(makeCell(invoice.getInvoiceDate() != null ? dtf.format(invoice.getInvoiceDate()) : "-", normal));

            infoTable.addCell(makeCell("Booking ID:", bold));
            infoTable.addCell(makeCell(String.valueOf(booking.getId()), normal));

            infoTable.addCell(makeCell("Payment Txn ID:", bold));
            infoTable.addCell(makeCell(payment.getTransactionId(), normal));

            document.add(infoTable);

            document.add(Chunk.NEWLINE);

            // Customer & Garage Info
            PdfPTable partiesTable = new PdfPTable(2);
            partiesTable.setWidthPercentage(100);

            String customerBlock = "Customer:\n"
                    + "ID: " + (booking.getCustomer() != null ? booking.getCustomer().getId() : "-") + "\n"
                    + "Email: " + (booking.getCustomer() != null ? booking.getCustomer().getEmail() : "-");

            String garageBlock = "Garage:\n"
                    + "Name: " + (booking.getGarage() != null ? booking.getGarage().getName() : "-") + "\n"
                    + "ID: " + (booking.getGarage() != null ? booking.getGarage().getId() : "-");

            partiesTable.addCell(makeCell(customerBlock, normal));
            partiesTable.addCell(makeCell(garageBlock, normal));

            document.add(partiesTable);

            document.add(Chunk.NEWLINE);

            // Booking / Service details
            PdfPTable serviceTable = new PdfPTable(2);
            serviceTable.setWidthPercentage(100);

            serviceTable.addCell(makeCell("Service Type:", bold));
            serviceTable.addCell(makeCell(booking.getServiceType(), normal));

            serviceTable.addCell(makeCell("Booking Time:", bold));
            serviceTable.addCell(makeCell(booking.getBookingTime() != null ? dtf.format(booking.getBookingTime()) : "-", normal));

            serviceTable.addCell(makeCell("Vehicle ID:", bold));
            serviceTable.addCell(makeCell(booking.getVehicle() != null ? String.valueOf(booking.getVehicle().getId()) : "-", normal));

            serviceTable.addCell(makeCell("Mechanic ID:", bold));
            serviceTable.addCell(makeCell(booking.getMechanic() != null ? String.valueOf(booking.getMechanic().getId()) : "-", normal));

            document.add(serviceTable);

            document.add(Chunk.NEWLINE);

            // Amount section
            PdfPTable amountTable = new PdfPTable(2);
            amountTable.setWidthPercentage(50);
            amountTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            amountTable.addCell(makeCell("Estimated Cost:", bold));
            amountTable.addCell(makeCell(booking.getEstimatedCost() != null ? "₹ " + booking.getEstimatedCost() : "-", normal));

            amountTable.addCell(makeCell("Final Amount:", bold));
            amountTable.addCell(makeCell("₹ " + invoice.getTotalAmount(), normal));

            document.add(amountTable);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            Paragraph footer = new Paragraph("Thank you for servicing with Smart Garage!", normal);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate invoice PDF", ex);
        }
    }

    private PdfPCell makeCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }
}
