package com.bookingservice.service;

import com.bookingservice.dto.dashboard.*;
import com.bookingservice.projections.ConversionAgg;
import com.bookingservice.projections.PaymentBreakdownRow;
import com.bookingservice.projections.SummaryAgg;
import com.bookingservice.repository.BookingRepository;
import com.bookingservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BookingRepository bookingRepo;
    private final EventRepository eventRepo;

    private record Range(Instant start, Instant end) {
    }

    private Range normalizeRange(LocalDate from, LocalDate to) {
        // inclusive from 00:00:00Z, exclusive to+1 00:00:00Z
        ZoneId z = ZoneOffset.UTC;
        Instant start = from.atStartOfDay(z).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(z).toInstant();
        return new Range(start, end);
    }

    public SummaryDto summary(LocalDate from, LocalDate to) {
        Range r = normalizeRange(from, to);
        SummaryAgg s = bookingRepo.sumPaid(r.start, r.end);
        ConversionAgg c = bookingRepo.conversion(r.start, r.end);
        long abandoned = bookingRepo.abandoned(r.start, r.end);

        BigDecimal gross = s.getGrossRevenue() != null ? s.getGrossRevenue() : BigDecimal.ZERO;
        long ordersPaid = s.getOrdersPaid() != null ? s.getOrdersPaid() : 0L;
        long tickets = s.getTicketsSold() != null ? s.getTicketsSold() : 0L;

        BigDecimal aov = ordersPaid == 0 ? BigDecimal.ZERO :
            gross.divide(BigDecimal.valueOf(ordersPaid), 2, RoundingMode.HALF_UP);
        BigDecimal avgQty = ordersPaid == 0 ? BigDecimal.ZERO :
            BigDecimal.valueOf(tickets).divide(BigDecimal.valueOf(ordersPaid), 2, RoundingMode.HALF_UP);

        long sessions = (c.getSessionsTotal() != null) ? c.getSessionsTotal() : 0L;
        long paid = (c.getPaid() != null) ? c.getPaid() : 0L;

        double conversion = sessions == 0 ? 0.0 : (paid * 100.0 / sessions);
        double abandonedRate = sessions == 0 ? 0.0 : (abandoned * 100.0 / sessions);

        return new SummaryDto(
            gross,
            null,           // netRevenue nếu chưa tính phí/refund
            tickets,
            ordersPaid,
            aov,
            avgQty,
            round1(conversion),
            round1(abandonedRate),
            null            // refunds
        );
    }

    public List<TimeseriesPointDto> timeseries(LocalDate from, LocalDate to) {
        Range r = normalizeRange(from, to);
        return bookingRepo.timeseries(r.start, r.end).stream().map(row -> {
            LocalDate d = row.getDay()
                .atZone(ZoneOffset.UTC)     // normalize to a zone
                .toLocalDate();
            BigDecimal revenue = row.getRevenue() != null ? row.getRevenue() : BigDecimal.ZERO;
            long tickets = row.getTickets() != null ? row.getTickets() : 0L;
            long ordersPaid = row.getOrders_paid() != null ? row.getOrders_paid() : 0L;
            return new TimeseriesPointDto(d, revenue, tickets, ordersPaid);
        }).toList();
    }

    public PaymentBreakdownDto paymentBreakdown(LocalDate from, LocalDate to) {
        Range r = normalizeRange(from, to);
        PaymentBreakdownRow pb = bookingRepo.paymentBreakdown(r.start, r.end);
        return new PaymentBreakdownDto(
            safeLong(pb.getPaid()),
            safeLong(pb.getPending()),
            safeLong(pb.getCanceled())
        );
    }

    public List<TopEventDto> topEvents(LocalDate from, LocalDate to, int limit) {
        Range r = normalizeRange(from, to);
        return bookingRepo.topEvents(r.start, r.end, limit).stream()
            .map(t -> new TopEventDto(
                t.getEventId(),
                t.getTitle(),
                t.getRevenue() != null ? t.getRevenue() : BigDecimal.ZERO,
                safeLong(t.getTickets())
            )).toList();
    }

    public List<OccupancyItemDto> occupancy(LocalDate from, LocalDate to) {
        Range r = normalizeRange(from, to);
        return eventRepo.occupancy(r.start, r.end).stream()
            .map(o -> new OccupancyItemDto(
                o.getEventId(),
                o.getTitle(),
                safeInt(o.getTotalSeats()),
                safeInt(o.getAvailableSeats()),
                round1(safeDouble(o.getOccupancyPct()))
            )).toList();
    }

    private static long safeLong(Long v) {
        return v == null ? 0L : v;
    }

    private static int safeInt(Integer v) {
        return v == null ? 0 : v;
    }

    private static double safeDouble(Double v) {
        return v == null ? 0.0 : v;
    }

    private static double round1(double d) {
        return Math.round(d * 10.0) / 10.0;
    }
}
