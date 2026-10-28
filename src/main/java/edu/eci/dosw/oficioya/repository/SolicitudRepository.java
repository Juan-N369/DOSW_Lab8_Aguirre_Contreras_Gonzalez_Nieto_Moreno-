package edu.eci.dosw.oficioya.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import edu.eci.dosw.oficioya.entity.*;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByContratanteId(Long contratanteId);

    List<Solicitud> findByTrabajadorId(Long trabajadorId);

    Optional<Solicitud> findByIdAndContratanteId(Long id, Long contratanteId);

    Optional<Solicitud> findByIdAndTrabajadorId(Long id, Long trabajadorId);

    // La consulta incluye al participante: una solicitud ajena y una inexistente dan el mismo resultado vacío
    @Query("select s from Solicitud s where s.id = :id "
            + "and (s.contratante.id = :usuarioId or s.trabajador.id = :usuarioId)")
    Optional<Solicitud> findByIdAndParticipante(@Param("id") Long id, @Param("usuarioId") Long usuarioId);
}
