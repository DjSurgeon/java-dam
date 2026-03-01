package com.hambooking.backend.repository;

import com.hambooking.backend.model.entity.Carver;
import com.hambooking.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad {@link Carver}.
 *
 * Carver tiene una particularidad respecto a UserRepository:
 * sus búsquedas más útiles no son por campos propios (specialty,
 * experienceYears) sino por su relación con User — porque en el
 * dominio de negocio preguntamos "¿existe un cortador para este
 * usuario?" más que "dame todos los cortadores de especialidad X".
 *
 * Métodos heredados de JpaRepository (gratis):
 *   save(), findById(), findAll(), deleteById(), count(), existsById()
 */
@Repository
public interface CarverRepository extends JpaRepository<Carver, Long> {

    // =========================================================================
    // BÚSQUEDAS POR RELACIÓN CON USER
    // =========================================================================

    /**
     * Busca el perfil de cortador asociado a un User.
     *
     * SQL generada: SELECT * FROM carvers WHERE user_id = ?
     *
     * Uso principal: Cuando el admin edita un cortador y solo tiene
     * el objeto User en contexto, sin conocer el ID del Carver.
     *
     * @param user El usuario cuyo perfil de cortador se busca
     * @return Optional con el Carver si existe, vacío si ese usuario
     *         no tiene perfil de cortador
     */
    Optional<Carver> findByUser(User user);

    /**
     * Comprueba si un User ya tiene perfil de cortador asignado.
     *
     * SQL generada: SELECT COUNT(*) > 0 FROM carvers WHERE user_id = ?
     *
     * Uso: Validar antes de crear un nuevo Carver que ese User
     * no tenga ya uno (restricción de unicidad del negocio).
     *
     * @param user El usuario a comprobar
     * @return true si ese usuario ya es cortador
     */
    boolean existsByUser(User user);

    // =========================================================================
    // BÚSQUEDAS POR ESTADO
    // =========================================================================

    /**
     * Devuelve todos los cortadores activos.
     *
     * SQL generada: SELECT * FROM carvers WHERE is_active = true
     *
     * Uso: Pantalla de selección de cortador al crear una reserva.
     * Solo se muestran los cortadores disponibles (activos).
     *
     * @return Lista de cortadores activos (puede estar vacía)
     */
    List<Carver> findByIsActiveTrue();

    /**
     * Devuelve todos los cortadores con un estado concreto.
     *
     * SQL generada: SELECT * FROM carvers WHERE is_active = ?
     *
     * Uso: Panel de administración — listar activos o inactivos
     * según el filtro seleccionado.
     *
     * @param isActive true para activos, false para inactivos
     * @return Lista de cortadores con ese estado
     */
    List<Carver> findByIsActive(Boolean isActive);

    // =========================================================================
    // BÚSQUEDAS POR ESPECIALIDAD
    // =========================================================================

    /**
     * Devuelve los cortadores que tienen una especialidad concreta.
     *
     * SQL generada: SELECT * FROM carvers WHERE specialty = ?
     *
     * Uso: Filtrar cortadores por tipo de corte en el panel admin.
     * Los valores posibles de specialty vienen del dominio de negocio:
     * "Jamón", "Paleta", "Embutidos", "Todos".
     *
     * @param specialty Especialidad a buscar
     * @return Lista de cortadores con esa especialidad
     */
    List<Carver> findBySpecialty(String specialty);

    /**
     * Devuelve los cortadores activos con una especialidad concreta.
     *
     * SQL generada: SELECT * FROM carvers WHERE specialty = ? AND is_active = true
     *
     * Uso: Al crear una reserva, filtrar por tipo de servicio y
     * mostrar solo los cortadores disponibles para ese tipo.
     *
     * @param specialty Especialidad a filtrar
     * @return Lista de cortadores activos con esa especialidad
     */
    List<Carver> findBySpecialtyAndIsActiveTrue(String specialty);
}