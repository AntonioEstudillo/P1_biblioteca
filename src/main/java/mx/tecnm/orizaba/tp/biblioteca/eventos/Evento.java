package mx.tecnm.orizaba.tp.biblioteca.eventos;

import java.time.LocalDateTime;
import java.util.Objects;

/** Evento inmutable del dominio. */
public record Evento(TipoEvento tipo, String mensaje, LocalDateTime instante) {

    public Evento {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(mensaje, "mensaje");
        Objects.requireNonNull(instante, "instante");
    }

    /** Fábrica estática (útil como referencia a método estático). */
    public static Evento de(TipoEvento tipo, String mensaje) {
        return new Evento(tipo, mensaje, LocalDateTime.now());
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", tipo, mensaje);
    }
}
