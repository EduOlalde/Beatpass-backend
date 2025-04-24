package com.daw2edudiego.beatpasstfg.service;

import com.daw2edudiego.beatpasstfg.dto.EntradaAsignadaDTO;
import com.daw2edudiego.beatpasstfg.exception.*; // Importar todas las excepciones personalizadas
import java.util.List;
import java.util.Optional;

/**
 * Interfaz que define la lógica de negocio y las operaciones relacionadas con
 * la gestión de Entradas Asignadas (entradas individuales). Incluye operaciones
 * clave como la nominación y cancelación de entradas.
 *
 * @author Eduardo Olalde
 */
public interface EntradaAsignadaService {

    /**
     * Asigna (nomina) una entrada específica a un asistente. Busca al asistente
     * por email y lo crea si no existe. Verifica que la entrada exista,
     * pertenezca al festival correcto (verificando la propiedad del promotor
     * sobre el festival), y esté en estado ACTIVA y sin nominar.
     * <p>
     * La operación es transaccional.
     * </p>
     *
     * @param idEntradaAsignada ID de la entrada individual a nominar.
     * @param emailAsistente Email del asistente a quien nominar la entrada
     * (obligatorio).
     * @param nombreAsistente Nombre del asistente (obligatorio, especialmente
     * si se crea nuevo).
     * @param telefonoAsistente Teléfono del asistente (opcional).
     * @param idPromotor ID del usuario (promotor) que realiza la acción para
     * verificación de permisos.
     * @throws EntradaAsignadaNotFoundException si la entrada con
     * idEntradaAsignada no existe.
     * @throws UsuarioNotFoundException si el promotor con idPromotor no existe.
     * @throws SecurityException si el promotor no es el dueño del festival
     * asociado a la entrada.
     * @throws IllegalStateException si la entrada no está en estado ACTIVA o ya
     * está nominada.
     * @throws IllegalArgumentException si idEntradaAsignada, emailAsistente o
     * idPromotor son nulos/vacíos, o si falta el nombre al intentar crear un
     * nuevo asistente.
     * @throws RuntimeException si ocurre un error inesperado durante el
     * proceso.
     */
    void nominarEntrada(Integer idEntradaAsignada, String emailAsistente, String nombreAsistente, String telefonoAsistente, Integer idPromotor);

    /**
     * Obtiene una lista de todas las entradas asignadas para un festival
     * específico. Verifica que el festival exista y que el usuario (promotor)
     * que realiza la solicitud sea el propietario del festival.
     *
     * @param idFestival El ID del festival cuyas entradas asignadas se quieren
     * obtener.
     * @param idPromotor El ID del usuario (promotor) que realiza la consulta.
     * @return Una lista de {@link EntradaAsignadaDTO} para el festival
     * especificado.
     * @throws FestivalNotFoundException si el festival no se encuentra.
     * @throws UsuarioNotFoundException si el promotor no se encuentra.
     * @throws SecurityException si el promotor no tiene permisos sobre el
     * festival.
     * @throws IllegalArgumentException si idFestival o idPromotor son nulos.
     * @throws RuntimeException si ocurre un error inesperado.
     */
    List<EntradaAsignadaDTO> obtenerEntradasAsignadasPorFestival(Integer idFestival, Integer idPromotor);

    /**
     * Cancela una entrada asignada específica. Solo se pueden cancelar entradas
     * que estén en estado ACTIVA. Verifica que la entrada exista y que el
     * usuario (promotor) que realiza la acción sea el propietario del festival
     * asociado. Como efecto secundario, incrementa en 1 el stock del tipo de
     * entrada original de donde provino esta entrada asignada.
     * <p>
     * La operación es transaccional e incluye bloqueo pesimista sobre el tipo
     * de entrada original para asegurar la consistencia del stock.
     * </p>
     *
     * @param idEntradaAsignada ID de la entrada individual a cancelar.
     * @param idPromotor ID del usuario (promotor) que realiza la acción.
     * @throws EntradaAsignadaNotFoundException si la entrada con
     * idEntradaAsignada no existe.
     * @throws UsuarioNotFoundException si el promotor con idPromotor no existe.
     * @throws SecurityException si el promotor no es el dueño del festival
     * asociado a la entrada.
     * @throws IllegalStateException si la entrada no está en estado ACTIVA.
     * @throws IllegalArgumentException si idEntradaAsignada o idPromotor son
     * nulos.
     * @throws RuntimeException si ocurre un error inesperado durante el proceso
     * (ej: error al actualizar stock).
     */
    void cancelarEntrada(Integer idEntradaAsignada, Integer idPromotor);

    /**
     * Obtiene los detalles completos (DTO) de una entrada asignada específica
     * por su ID. Verifica que la entrada exista y que el usuario (promotor) que
     * realiza la solicitud sea el propietario del festival asociado. Útil para
     * mostrar detalles o preparar ediciones/acciones.
     *
     * @param idEntradaAsignada El ID de la entrada asignada a buscar.
     * @param idPromotor El ID del usuario (promotor) que realiza la consulta.
     * @return Un {@link Optional} conteniendo el {@link EntradaAsignadaDTO} si
     * se encuentra y el promotor tiene permisos, de lo contrario, un Optional
     * vacío.
     * @throws UsuarioNotFoundException si el promotor no existe (aunque la
     * verificación de propiedad ya lo implicaría).
     * @throws IllegalArgumentException si idEntradaAsignada o idPromotor son
     * nulos.
     * @throws RuntimeException si ocurre un error inesperado.
     * @throws SecurityException si el promotor no tiene permisos (implícito si
     * devuelve Optional vacío por esta causa).
     * @throws EntradaAsignadaNotFoundException si la entrada no existe
     * (implícito si devuelve Optional vacío por esta causa).
     */
    Optional<EntradaAsignadaDTO> obtenerEntradaAsignadaPorId(Integer idEntradaAsignada, Integer idPromotor);

    // Podrían añadirse otros métodos como:
    // Optional<EntradaAsignadaDTO> validarEntradaPorQr(String codigoQr, Integer idOperadorAcceso);
    // void asociarPulseraAEntrada(Integer idEntradaAsignada, String codigoUidPulsera, Integer idOperador);
}
