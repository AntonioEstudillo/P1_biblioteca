package mx.tecnm.orizaba.tp.biblioteca.modelo;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Socio del sistema. Implementa {@link Externalizable}: el propio objeto decide qué y cómo se
 * escribe (véase {@code java_4_modelo/t7_persistencia/b_externalizacion/Cosas2}); por ello requiere un
 * constructor público sin argumentos.
 */
public class Socio implements Externalizable {

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private int id;
    private String nombre;
    private String correo;
    private Nivel nivel;

    /** Requerido por la externalización: se invoca antes de readExternal. */
    public Socio() {
    }
    /**
     * Valida que el id sea menor que 0
     * asigna el id
     * asigna nomnre, correo y nivel através de setters
     * */
    public Socio(int id, String nombre, String correo, Nivel nivel) {
        // TODO E06: Validar id > 0 y asignar nombre, correo y nivel mediante sus setters.
       if(id < 0){
           throw new ReglaNegocioException("Id menor a 0 es invalidado");
       }

       this.id = id;
       setNombre(nombre);
       setCorreo(correo);
       setNivel(nivel);
    }

    /** Encadenamiento de constructores: nivel por omisión. */
    public Socio(int id, String nombre, String correo) {
        this(id, nombre, correo, Nivel.values()[0]);
    }

    public int id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public String correo() {
        return correo;
    }

    public Nivel nivel() {
        return nivel;
    }

    public final void setNombre(String nombre) {
        // TODO E06: Rechazar nombre nulo o en blanco.
        /**
         * Comprueba que el el nnombre nos ea nulo y que no sea un espacio en blanco
         * recorta espacios en blanco en el nombre antes de asignarlo
         * */
        if (nombre == null || nombre.isBlank()){
            throw new ReglaNegocioException("Nombre no puede estar vacio");
        }

        this.nombre = nombre.trim();
    }
    /**
     * Comprueba que el cporreo no sea vacio y compara el formtao con el patron dado
     * pasa el correo a minusculas*/
    public final void setCorreo(String correo) {
        // TODO E06: Validar el correo con el patrón CORREO y guardarlo en minúsculas.
        if(correo == null || !CORREO.matcher(correo).matches()){
            throw new ReglaNegocioException("Correo invalido");
        }

        this.correo = correo.toLowerCase();
    }

    public final void setNivel(Nivel nivel) {
        this.nivel = Objects.requireNonNull(nivel, "nivel");
    }

    @Override
    public void writeExternal(ObjectOutput salida) throws IOException {
        // TODO E14: Escribir id, nombre, correo y el nombre del nivel (writeInt / writeUTF).
        throw new UnsupportedOperationException("TODO E14");
    }

    @Override
    public void readExternal(ObjectInput entrada) throws IOException {
        // TODO E14: Leer en el mismo orden en que se escribió y reconstruir el nivel con Nivel.valueOf.
        throw new UnsupportedOperationException("TODO E14");
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Socio otro && id == otro.id);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("%s #%d %s <%s> (%s)", "Socio", id, nombre, correo, nivel);
    }
}
