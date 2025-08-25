//// EventDashboardRepository.java
//package com.bookingservice.repository;
//
//import com.bookingservice.projections.OccupancyRow;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.Repository;
//import org.springframework.data.repository.query.Param;
//
//import java.time.Instant;
//import java.util.List;
//
//public interface EventDashboardRepository extends Repository<Object, Long> {
//
//    @Query(value = """
//        SELECT
//          e.id AS eventId,
//          e.title AS title,
//          e.total_seats AS totalSeats,
//          e.available_seats AS availableSeats,
//          CASE WHEN e.total_seats > 0
//               THEN ((e.total_seats - e.available_seats) * 100.0 / e.total_seats)
//               ELSE 0 END AS occupancyPct
//        FROM events e
//        WHERE e.start_time >= :start AND e.start_time < :end
//        ORDER BY occupancyPct DESC, e.title ASC
//        """, nativeQuery = true)
//    List<OccupancyRow> occupancy(@Param("start") Instant start, @Param("end") Instant end);
//}
