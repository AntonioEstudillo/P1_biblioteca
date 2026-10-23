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
import java.util.Objects;

/**
 * Respaldo binario mediante serialización de objetos (complementa a SQLite).
 * Escribe y lee en el mismo orden (véase {@code java_4_modelo/t7_persistencia/a_serializacion/Gusano_07}).
 */
public final class Respaldo {

    public static final String ENCABEZADO = "Respaldo Biblioteca v1";

    private Respaldo() {
    }


    /**
     * Se crea el flujo de salida
     * Escribimos los campos requeridos en el archivo.
     * Registro y socio se escribe como arrayList para escribir los datos que estan dentro de ellos.
     *
     * */
    /** Contenido recuperado de un respaldo. */
    public record Contenido(String encabezado, LocalDateTime fecha, Acervo catalogo,
                            List<Socio> socios, List<Prestamo> registros) {
    }

    public static void exportar(Path archivo, Acervo catalogo, Collection<Socio> socios,
                                Collection<Prestamo> registros) throws IOException {
        // TODO E14: ObjectOutputStream(BufferedOutputStream(Files.newOutputStream)): encabezado, fecha, catálogo, listas.
        try (ObjectOutputStream salida = new ObjectOutputStream( new BufferedOutputStream(Files.newOutputStream(archivo)))){
            salida.writeUTF(ENCABEZADO);

            salida.writeObject(LocalDateTime.now());

            salida.writeObject(catalogo);

            salida.writeObject(new ArrayList<>(socios));
            salida.writeObject(new ArrayList<>(registros));
        }
    }
    /**
     * Crea el flujo de entrada
     * Leemos el encabeza y comprobamos que sea valido
     *Recuperamos los objetos mediante Upcasting
     * retornamos un objeto de Contenido con los datos recuperados
     * */
    @SuppressWarnings("unchecked")
    public static Contenido importar(Path archivo) throws IOException, ClassNotFoundException {
        // TODO E14: Leer en el mismo orden; validar el encabezado (IOException si no coincide).
        try (ObjectInputStream entrada = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(archivo)))){
            String encabezado = entrada.readUTF();

            if (!ENCABEZADO.equals(encabezado)) {
                throw new IOException(
                        "Formato de respaldo no reconocido"
                );
            }

            LocalDateTime fecha = (LocalDateTime) entrada.readObject();

            Acervo catalogo = (Acervo) entrada.readObject();

            List<Socio> socios = (List<Socio>) entrada.readObject();

            List<Prestamo> registros = (List<Prestamo>) entrada.readObject();

            return new Contenido(encabezado,fecha, catalogo, socios,registros);
        }
    }
}
