package com.bookingservice.controller;

import com.bookingservice.dto.dashboard.*;
import com.bookingservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService service;

    private record Range(LocalDate from, LocalDate to) {}

    private Range normalize(LocalDate from, LocalDate to) {
        // mặc định 30 ngày gần nhất nếu thiếu tham số
        LocalDate today = LocalDate.now();
        if (from == null && to == null) {
            return new Range(today.minusDays(29), today);
        } else if (from == null) {
            return new Range(to.minusDays(29), to);
        } else if (to == null) {
            return new Range(from, from.plusDays(29));
        }
        return new Range(from, to);
    }

    @GetMapping("/summary")
    public SummaryDto summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Range r = normalize(from, to);
        return service.summary(r.from, r.to);
    }

    @GetMapping("/timeseries")
    public List<TimeseriesPointDto> timeseries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Range r = normalize(from, to);
        return service.timeseries(r.from, r.to);
    }

    @GetMapping("/payment-breakdown")
    public PaymentBreakdownDto paymentBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Range r = normalize(from, to);
        return service.paymentBreakdown(r.from, r.to);
    }

    @GetMapping("/top-events")
    public List<TopEventDto> topEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit) {
        Range r = normalize(from, to);
        return service.topEvents(r.from, r.to, Math.max(1, Math.min(limit, 50)));
    }

    @GetMapping("/occupancy")
    public List<OccupancyItemDto> occupancy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Range r = normalize(from, to);
        return service.occupancy(r.from, r.to);
    }
}
