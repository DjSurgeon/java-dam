package com.hambooking.backend.repository;

import com.hambooking.backend.model.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad {@link Service}.
 *
 * Service es la entidad más simple del dominio: no tiene FKs de entrada,
 * solo campos propios (name, durationMinutes, basePrice, isActive).
 * Sus queries son sencillas y orientadas al catálogo de servicios.
 *
 * En el negocio de HamBooking existen 3 servicios predefinidos:
 *   - Corte de Jamón  (120 min, 45.00€)
 *   - Corte de Paleta (60 min,  25.00€)
 *   - Corte de Embutido (30 min, 12.00€)
 *
 * Métodos heredados de JpaRepository (gratis):
 *   save(), findById(), findAll(), deleteById(), count(), existsById()
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    // =========================================================================
    // BÚSQUEDAS POR NOMBRE
    // =========================================================================

    /**
     * Busca un servicio por su nombre exacto.
     *
     * SQL generada: SELECT * FROM services WHERE name = ?
     *
     * Uso: Verificar si ya existe un servicio con ese nombre antes
     * de crear uno nuevo (el nombre tiene constraint UNIQUE en BD).
     *
     * @param name Nombre exacto del servicio
     * @return Optional con el servicio si existe, vacío si no
     */
    Optional<Service> findByName(String name);

    /**
     * Comprueba si ya existe un servicio con ese nombre.
     *
     * SQL generada: SELECT COUNT(*) > 0 FROM services WHERE name = ?
     *
     * Uso: Validación previa al INSERT — más eficiente que findByName()
     * porque no carga el objeto completo, solo comprueba existencia.
     *
     * @param name Nombre a verificar
     * @return true si ya existe un servicio con ese nombre
     */
    boolean existsByName(String name);

    // =========================================================================
    // BÚSQUEDAS POR ESTADO
    // =========================================================================

    /**
     * Devuelve todos los servicios activos del catálogo.
     *
     * SQL generada: SELECT * FROM services WHERE is_active = true
     *
     * Uso: Pantalla de selección de servicio al crear una reserva.
     * Solo se muestran los servicios disponibles (activos).
     *
     * @return Lista de servicios activos (puede estar vacía)
     */
    List<Service> findByIsActiveTrue();

    /**
     * Devuelve todos los servicios con un estado concreto.
     *
     * SQL generada: SELECT * FROM services WHERE is_active = ?
     *
     * Uso: Panel de administración — ver el catálogo completo
     * con posibilidad de filtrar activos o inactivos.
     *
     * @param isActive true para activos, false para inactivos
     * @return Lista de servicios con ese estado
     */
    List<Service> findByIsActive(Boolean isActive);

    // =========================================================================
    // BÚSQUEDAS POR PRECIO
    // =========================================================================

    /**
     * Devuelve los servicios cuyo precio es menor o igual al indicado.
     *
     * SQL generada: SELECT * FROM services WHERE base_price <= ?
     *
     * Uso: Filtro de precio en el catálogo — mostrar servicios
     * asequibles para un presupuesto dado.
     *
     * @param maxPrice Precio máximo (inclusive)
     * @return Lista de servicios dentro del presupuesto, ordenada por precio
     */
    List<Service> findByBasePriceLessThanEqual(BigDecimal maxPrice);

    /**
     * Devuelve los servicios activos cuyo precio es menor o igual al indicado.
     *
     * SQL generada: SELECT * FROM services WHERE base_price <= ? AND is_active = true
     *
     * Uso: Filtro combinado — precio máximo y solo disponibles.
     *
     * @param maxPrice Precio máximo (inclusive)
     * @return Lista de servicios activos dentro del presupuesto
     */
    List<Service> findByBasePriceLessThanEqualAndIsActiveTrue(BigDecimal maxPrice);
}