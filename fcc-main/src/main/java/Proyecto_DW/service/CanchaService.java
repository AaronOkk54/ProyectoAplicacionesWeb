package Proyecto_DW.service;

import Proyecto_DW.domain.Cancha;
import Proyecto_DW.repository.CanchaRepository;
import Proyecto_DW.repository.ReservaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CanchaService
 * Servicio para la gestión de canchas de fútbol
 */
@Service
public class CanchaService {

    private final CanchaRepository canchaRepository;
    private final ReservaRepository reservaRepository;

    public CanchaService(CanchaRepository canchaRepository, ReservaRepository reservaRepository) {
        this.canchaRepository = canchaRepository;
        this.reservaRepository = reservaRepository;
    }

    /**
     * Obtener todas las canchas
     */
    @Transactional(readOnly = true)
    public List<Cancha> getCanchas(boolean soloDisponibles) {
        if (soloDisponibles) {
            return canchaRepository.findCanchasDisponibles();
        }
        return canchaRepository.findAll();
    }

    /**
     * Obtener una cancha por ID
     */
    @Transactional(readOnly = true)
    public Optional<Cancha> getCancha(Integer idCancha) {
        return canchaRepository.findById(idCancha);
    }

    /**
     * Buscar canchas por nombre
     */
    @Transactional(readOnly = true)
    public List<Cancha> buscarPorNombre(String nombre) {
        return canchaRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Buscar canchas por ubicación
     */
    @Transactional(readOnly = true)
    public List<Cancha> buscarPorUbicacion(String ubicacion) {
        return canchaRepository.findByUbicacionContainingIgnoreCase(ubicacion);
    }

    /**
     * Buscar canchas por estado
     */
    @Transactional(readOnly = true)
    public List<Cancha> buscarPorEstado(String estado) {
        return canchaRepository.findByEstado(estado);
    }

    /**
     * Obtener canchas de un vendedor específico
     */
    @Transactional(readOnly = true)
    public List<Cancha> getCanchasByVendedor(Integer idUsuario) {
        return canchaRepository.findByVendedorIdUsuario(idUsuario);
    }

    /**
     * Guardar una nueva cancha
     */
    @Transactional
    public Cancha save(Cancha cancha) {
        // Validar datos requeridos
        if (cancha.getNombre() == null || cancha.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la cancha no puede estar vacío.");
        }

        if (cancha.getUbicacion() == null || cancha.getUbicacion().trim().isEmpty()) {
            throw new IllegalArgumentException("La ubicación no puede estar vacía.");
        }

        if (cancha.getPrecioHora() == null || cancha.getPrecioHora().signum() <= 0) {
            throw new IllegalArgumentException("El precio por hora debe ser mayor a cero.");
        }

        // Establecer estado por defecto si no está definido
        if (cancha.getEstado() == null) {
            cancha.setEstado("DISPONIBLE");
        }

        return canchaRepository.save(cancha);
    }

    /**
     * Actualizar una cancha existente
     */
    @Transactional
    public Cancha update(Cancha cancha) {
        if (!canchaRepository.existsById(cancha.getIdCancha())) {
            throw new IllegalArgumentException("La cancha con ID " + cancha.getIdCancha() + " no existe.");
        }

        // Validar datos requeridos
        if (cancha.getNombre() == null || cancha.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la cancha no puede estar vacío.");
        }

        return canchaRepository.save(cancha);
    }

    /**
     * Cambiar el estado de una cancha
     */
    @Transactional
    public void cambiarEstado(Integer idCancha, String nuevoEstado) {
        Optional<Cancha> canchaOpt = canchaRepository.findById(idCancha);
        if (canchaOpt.isEmpty()) {
            throw new IllegalArgumentException("La cancha con ID " + idCancha + " no existe.");
        }

        // Validar estado válido
        if (!nuevoEstado.equals("DISPONIBLE") && !nuevoEstado.equals("MANTENIMIENTO") && !nuevoEstado.equals("CLAUSURADA")) {
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado);
        }

        Cancha cancha = canchaOpt.get();
        cancha.setEstado(nuevoEstado);
        canchaRepository.save(cancha);
    }

    /**
     * Eliminar una cancha
     */
    @Transactional
    public void delete(Integer idCancha) {
        // Verifica si la cancha existe
        if (!canchaRepository.existsById(idCancha)) {
            throw new IllegalArgumentException("La cancha con ID " + idCancha + " no existe.");
        }

        // Verificar si tiene reservas asociadas
        long countReservas = reservaRepository.findByCancha(canchaRepository.findById(idCancha).get()).size();
        if (countReservas > 0) {
            throw new IllegalStateException("No se puede eliminar la cancha. Tiene reservas asociadas.");
        }

        try {
            canchaRepository.deleteById(idCancha);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar la cancha. Tiene datos asociados.", e);
        }
    }
}
