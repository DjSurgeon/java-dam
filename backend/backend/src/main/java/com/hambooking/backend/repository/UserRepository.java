package com.hambooking.backend.repository;

import com.hambooking.backend.model.entity.User;
import com.hambooking.backend.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad {@link User}.
 *
 * Extiende {@link JpaRepository} y hereda automáticamente:
 *   - save()       → INSERT / UPDATE
 *   - findById()   → SELECT WHERE id = ?
 *   - findAll()    → SELECT *
 *   - deleteById() → DELETE WHERE id = ?
 *   - count()      → SELECT COUNT(*)
 *   - existsById() → SELECT COUNT(*) > 0
 *
 * Los métodos declarados aquí son "query methods": Spring Data JPA
 * parsea el nombre del método y genera la SQL automáticamente en
 * tiempo de arranque. Si hay un typo, la aplicación no arranca.
 *
 * Casos de uso principales:
 *   - Login:          findByEmailAndIsActiveTrue()
 *   - Registro:       existsByEmail() / existsByDni()
 *   - Panel admin:    findByRole()
 *   - Perfil:         findByDni()
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =========================================================================
    // BÚSQUEDAS POR CAMPO ÚNICO
    // =========================================================================

    /**
     * Busca un usuario por email.
     *
     * SQL generada: SELECT * FROM users WHERE email = ?
     *
     * Uso: Autenticación (login), recuperación de cuenta.
     *
     * @param email Email del usuario
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por email SOLO si está activo.
     *
     * SQL generada: SELECT * FROM users WHERE email = ? AND is_active = true
     *
     * Uso: Login — los usuarios desactivados no pueden autenticarse.
     * Es preferible a findByEmail() en el flujo de autenticación.
     *
     * @param email Email del usuario
     * @return Optional con el usuario activo si existe
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * Busca un usuario por DNI.
     *
     * SQL generada: SELECT * FROM users WHERE dni = ?
     *
     * Uso: Verificar duplicados antes de registrar un nuevo usuario.
     *
     * @param dni DNI en formato español (ej: 12345678A)
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<User> findByDni(String dni);

    // =========================================================================
    // BÚSQUEDAS POR ROL
    // =========================================================================

    /**
     * Devuelve todos los usuarios con un rol concreto.
     *
     * SQL generada: SELECT * FROM users WHERE role = ?
     *
     * Uso: Listar todos los clientes (panel admin), verificar si
     * existe un admin registrado durante el setup inicial.
     *
     * @param role Rol a filtrar (Role.ADMIN o Role.CLIENT)
     * @return Lista de usuarios con ese rol (puede estar vacía)
     */
    List<User> findByRole(Role role);

    /**
     * Devuelve todos los usuarios activos con un rol concreto.
     *
     * SQL generada: SELECT * FROM users WHERE role = ? AND is_active = true
     *
     * Uso: Listar solo clientes activos en el panel de administración.
     *
     * @param role Rol a filtrar
     * @return Lista de usuarios activos con ese rol
     */
    List<User> findByRoleAndIsActiveTrue(Role role);

    // =========================================================================
    // VERIFICACIONES DE EXISTENCIA (para validaciones de unicidad)
    // =========================================================================

    /**
     * Comprueba si ya existe un usuario registrado con ese email.
     *
     * SQL generada: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     *
     * Uso: Validar antes de registrar un nuevo usuario que el email
     * no esté ya en uso. Más eficiente que findByEmail() porque
     * no carga el objeto completo — solo comprueba existencia.
     *
     * @param email Email a verificar
     * @return true si ya existe un usuario con ese email
     */
    boolean existsByEmail(String email);

    /**
     * Comprueba si ya existe un usuario registrado con ese DNI.
     *
     * SQL generada: SELECT COUNT(*) > 0 FROM users WHERE dni = ?
     *
     * Uso: Validar unicidad del DNI al registrar un nuevo usuario.
     *
     * @param dni DNI a verificar
     * @return true si ya existe un usuario con ese DNI
     */
    boolean existsByDni(String dni);
}