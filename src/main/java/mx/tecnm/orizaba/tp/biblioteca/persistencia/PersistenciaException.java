package mx.tecnm.orizaba.tp.biblioteca.persistencia;

import java.io.Serial;

/** Envuelve los errores de acceso a datos en una excepción no comprobada del dominio. */
public class PersistenciaException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
