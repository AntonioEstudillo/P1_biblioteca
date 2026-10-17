package mx.tecnm.orizaba.tp.biblioteca.persistencia;

import mx.tecnm.orizaba.tp.biblioteca.modelo.Socio;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Acervo;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Prestamo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Respaldo binario mediante serialización de objetos (complementa a SQLite).
 * Escribe y lee en el mismo orden (véase {@code java_4_modelo/t7_persistencia/a_serializacion/Gusano_07}).
 */
public final class Respaldo {

    public static final String ENCABEZADO = "Respaldo Biblioteca v1";

    private Respaldo() {
    }

    /** Contenido recuperado de un respaldo. */
    public record Contenido(String encabezado, LocalDateTime fecha, Acervo catalogo,
                            List<Socio> socios, List<Prestamo> registros) {
    }

    public static void exportar(Path archivo, Acervo catalogo, Collection<Socio> socios,
                                Collection<Prestamo> registros) throws IOException {
        // TODO E14: ObjectOutputStream(BufferedOutputStream(Files.newOutputStream)): encabezado, fecha, catálogo, listas.
        throw new UnsupportedOperationException("TODO E14");
    }

    @SuppressWarnings("unchecked")
    public static Contenido importar(Path archivo) throws IOException, ClassNotFoundException {
        // TODO E14: Leer en el mismo orden; validar el encabezado (IOException si no coincide).
        throw new UnsupportedOperationException("TODO E14");
    }
}
