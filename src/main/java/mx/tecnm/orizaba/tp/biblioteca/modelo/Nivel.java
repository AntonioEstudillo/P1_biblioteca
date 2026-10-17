package mx.tecnm.orizaba.tp.biblioteca.modelo;

/**
 * Nivel del socio; cada constante encapsula su etiqueta y su descuento.
 * <p>Tipificación fuerte: un {@code enum} es un tipo cerrado con estado y comportamiento.</p>
 */
public enum Nivel {
    EXTERNO("Externo", 0.0),
    ESTUDIANTE("Estudiante", 0.1),
    DOCENTE("Docente", 0.25);

    private final String etiqueta;
    private final double descuento;

    Nivel(String etiqueta, double descuento) {
        this.etiqueta = etiqueta;
        this.descuento = descuento;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public double descuento() {
        return descuento;
    }

    /**
     * Realiza la operacion para clacular el descuento
     * Redondea a 2 decimales con Math (al redondear al entero más cercano se multiplica primero por 100
     *  para correr los decimales, posterior se divide entre 100 para ajustar el punto.)
     * */
    /** Aplica el descuento del nivel a un monto, redondeando a 2 decimales. */
    public double aplicar(double monto) {
        // TODO E06: Restar el porcentaje de descuento al monto y redondear a 2 decimales.
        double descuentoAplicado = monto * descuento;

        double total = monto - descuentoAplicado;

        return Math.round(total*100)/100;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
