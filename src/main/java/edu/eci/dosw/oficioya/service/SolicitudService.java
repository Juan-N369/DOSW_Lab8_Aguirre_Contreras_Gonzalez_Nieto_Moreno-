package edu.eci.dosw.oficioya.service;

import edu.eci.dosw.oficioya.dto.*;
import edu.eci.dosw.oficioya.entity.*;
import edu.eci.dosw.oficioya.exception.*;
import edu.eci.dosw.oficioya.mapper.SolicitudMapper;
import edu.eci.dosw.oficioya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class SolicitudService {

    private static final String NO_ENCONTRADA = "Solicitud no encontrada";

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudMapper mapper;

    public SolicitudResponse crear(SolicitudRequest req, String correoAutenticado) {
        Usuario contratante = actual(correoAutenticado);
        Usuario trabajador = usuarioRepository.findByIdAndRol(req.trabajadorId(), Rol.TRABAJADOR)
                .orElseThrow(() -> new RecursoNoEncontradoException("Trabajador no encontrado"));

        Solicitud s = new Solicitud();
        s.setDescripcion(req.descripcion());
        s.setEstado(EstadoSolicitud.PENDIENTE);
        s.setContratante(contratante);
        s.setTrabajador(trabajador);
        return mapper.toResponse(solicitudRepository.save(s));
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar(String correoAutenticado) {
        Usuario u = actual(correoAutenticado);
        List<Solicitud> propias = u.getRol() == Rol.CONTRATANTE
                ? solicitudRepository.findByContratanteId(u.getId())
                : solicitudRepository.findByTrabajadorId(u.getId());
        return propias.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponse obtener(Long id, String correoAutenticado) {
        Usuario u = actual(correoAutenticado);
        return solicitudRepository.findByIdAndParticipante(id, u.getId())   // ajena o inexistente: mismo 404
                .map(mapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));
    }

    public SolicitudResponse aceptar(Long id, String correoAutenticado) {
        return responder(id, correoAutenticado, EstadoSolicitud.ACEPTADA);
    }

    public SolicitudResponse rechazar(Long id, String correoAutenticado) {
        return responder(id, correoAutenticado, EstadoSolicitud.RECHAZADA);
    }

    public SolicitudResponse cancelar(Long id, String correoAutenticado) {
        Usuario contratante = actual(correoAutenticado);
        Solicitud s = solicitudRepository.findByIdAndContratanteId(id, contratante.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));
        if (s.getEstado() != EstadoSolicitud.PENDIENTE && s.getEstado() != EstadoSolicitud.ACEPTADA) {
            throw new EstadoInvalidoException("Solo se puede cancelar una solicitud pendiente o aceptada");
        }
        s.setEstado(EstadoSolicitud.CANCELADA);
        return mapper.toResponse(solicitudRepository.save(s));
    }

    // Solo el trabajador asignado responde, y solo si la solicitud está pendiente
    private SolicitudResponse responder(Long id, String correoAutenticado, EstadoSolicitud nuevo) {
        Usuario trabajador = actual(correoAutenticado);
        Solicitud s = solicitudRepository.findByIdAndTrabajadorId(id, trabajador.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException(NO_ENCONTRADA));
        if (s.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new EstadoInvalidoException("Solo se puede responder una solicitud pendiente");
        }
        s.setEstado(nuevo);
        return mapper.toResponse(solicitudRepository.save(s));
    }

    private Usuario actual(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
