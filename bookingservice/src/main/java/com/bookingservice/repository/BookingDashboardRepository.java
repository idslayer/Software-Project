//// BookingDashboardRepository.java
//package com.bookingservice.repository;
//
//import com.bookingservice.projections.*;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.Repository;
//import org.springframework.data.repository.query.Param;
//
//import java.time.Instant;
//import java.util.List;
//
//public interface BookingDashboardRepository extends Repository<Object, Long> {
//
//    // ---- Summary pieces ----
//    @Query(value = """
//        SELECT
//          COALESCE(SUM(b.total_price),0) AS grossRevenue,
//          COALESCE(SUM(b.number_of_tickets),0) AS ticketsSold,
//          COUNT(*) AS ordersPaid
//        FROM bookings b
//        WHERE b.status = 'CONFIRMED'
//          AND b.created_at >= :start AND b.created_at < :end
//        """, nativeQuery = true)
//    SummaryAgg sumPaid(@Param("start") Instant start, @Param("end") Instant end);
//
//    @Query(value = """
//        SELECT
//          COUNT(*) FILTER (WHERE b.status IN ('PENDING','CONFIRMED')) AS sessionsTotal,
//          COUNT(*) FILTER (WHERE b.status = 'CONFIRMED') AS paid
//        FROM bookings b
//        WHERE b.created_at >= :start AND b.created_at < :end
//        """, nativeQuery = true)
//    ConversionAgg conversion(@Param("start") Instant start, @Param("end") Instant end);
//
//    // pending quá 2 giờ (ngưỡng có thể đổi)
//    @Query(value = """
//        SELECT COUNT(*) AS abandoned
//        FROM bookings b
//        WHERE b.status = 'PENDING'
//          AND b.created_at < now() - INTERVAL '2 hours'
//          AND b.created_at >= :start AND b.created_at < :end
//        """, nativeQuery = true)
//    long abandoned(@Param("start") Instant start, @Param("end") Instant end);
//
//    // ---- Time series (PAID) ----
//    @Query(value = """
//        SELECT
//          date_trunc('day', b.created_at) AS day,
//          COALESCE(SUM(b.total_price),0) AS revenue,
//          COALESCE(SUM(b.number_of_tickets),0) AS tickets,
//          COUNT(*) AS orders_paid
//        FROM bookings b
//        WHERE b.status = 'CONFIRMED'
//          AND b.created_at >= :start AND b.created_at < :end
//        GROUP BY 1
//        ORDER BY 1
//        """, nativeQuery = true)
//    List<TimeseriesRow> timeseries(@Param("start") Instant start, @Param("end") Instant end);
//
//    // ---- Payment breakdown ----
//    @Query(value = """
//        SELECT
//          COUNT(*) FILTER (WHERE b.status = 'CONFIRMED') AS paid,
//          COUNT(*) FILTER (WHERE b.status = 'PENDING') AS pending,
//          COUNT(*) FILTER (WHERE b.status = 'CANCELED') AS canceled
//        FROM bookings b
//        WHERE b.created_at >= :start AND b.created_at < :end
//        """, nativeQuery = true)
//    PaymentBreakdownRow paymentBreakdown(@Param("start") Instant start, @Param("end") Instant end);
//
//    // ---- Top events by revenue (PAID) ----
//    @Query(value = """
//        SELECT
//          b.event_id AS eventId,
//          e.title AS title,
//          COALESCE(SUM(b.total_price),0) AS revenue,
//          COALESCE(SUM(b.number_of_tickets),0) AS tickets
//        FROM bookings b
//        JOIN events e ON e.id = b.event_id
//        WHERE b.status = 'CONFIRMED'
//          AND b.created_at >= :start AND b.created_at < :end
//        GROUP BY b.event_id, e.title
//        ORDER BY revenue DESC
//        LIMIT :limit
//        """, nativeQuery = true)
//    List<TopEventRow> topEvents(@Param("start") Instant start, @Param("end") Instant end, @Param("limit") int limit);
//}
