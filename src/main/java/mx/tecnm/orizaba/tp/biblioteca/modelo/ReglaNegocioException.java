package mx.tecnm.orizaba.tp.biblioteca.modelo;

import java.io.Serial;

/**
 * Excepción no comprobada que señala la violación de una invariante del dominio.
 * <p>Encapsulamiento (Booch): los objetos protegen su estado rechazando valores inválidos.</p>
 */
public class ReglaNegocioException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
