package com.event.repository;

import java.time.LocalDate;
import java.util.Collection;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.event.entity.Event;
import com.event.enums.Status;

public interface EventRepository extends JpaRepository<Event, Long> {
	@Modifying
	@Query("update Event e set e.availableSeats=e.availableSeats-:seats where e.eventId=:eventId and e.status not in :closed and e.availableSeats>=:seats")
	int reserveSeats(@Param("eventId") Long eventId, @Param("seats") Integer seats,
			@Param("closed") Collection<Status> closed);

	@Modifying
	@Query("update Event e set e.availableSeats=e.availableSeats+:seats where e.eventId=:eventId and e.availableSeats+:seats<=e.totalSeats")
	int releaseSeats(@Param("eventId") Long eventId, @Param("seats") Integer seats);

	@Query("select e from Event e where (:city is null or lower(e.venueCity)=lower(:city)) "
			+ "and (:category is null or lower(e.eventCategory) like lower(concat('%',:category,'%'))) "
			+ "and (:date is null or e.eventDate=:date) and (:minPrice is null or e.ticketPrice>=:minPrice) "
			+ "and (:maxPrice is null or e.ticketPrice<=:maxPrice) and (:availableOnly=false or e.availableSeats>0)")
	Page<Event> search(@Param("city") String city, @Param("category") String category, @Param("date") LocalDate date,
			@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice,
			@Param("availableOnly") boolean availableOnly, Pageable pageable);
}
