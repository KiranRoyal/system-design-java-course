package course.orders;

import org.springframework.data.jpa.repository.JpaRepository;

interface Events extends JpaRepository<OutboxEvent, String> {}
