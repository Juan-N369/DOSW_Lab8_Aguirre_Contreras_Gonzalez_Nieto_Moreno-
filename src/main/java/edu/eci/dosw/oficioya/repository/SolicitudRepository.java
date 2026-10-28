package edu.eci.dosw.oficioya.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import edu.eci.dosw.oficioya.entity.*;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByContratanteId(Long contratanteId);

    List<Solicitud> findByTrabajadorId(Long trabajadorId);

    Optional<Solicitud> findByIdAndContratanteId(Long id, Long contratanteId);

    Optional<Solicitud> findByIdAndTrabajadorId(Long id, Long trabajadorId);
}
