package course.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

interface Reservations extends JpaRepository<Reservation, String> {}
