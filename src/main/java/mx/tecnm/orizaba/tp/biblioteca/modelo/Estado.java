package mx.tecnm.orizaba.tp.biblioteca.modelo;

/**
 * Ciclo de vida de un(a) préstamo: máquina de estados finita.
 * <pre>ABIERTA → EN_CURSO → CERRADA;  ABIERTA|EN_CURSO → CANCELADA</pre>
 */
public enum Estado {
    ABIERTA("Registrado"), EN_CURSO("Prestado"), CERRADA("Devuelto"), CANCELADA("Cancelado");

    private final String etiqueta;

    Estado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }


    /**
     * Se crea un switch en el que abarca los casos de los posibles estados
     * Indica a que esatdo es posible pasar dependiendo del estado en el que se encuentre
     *
     * */
    /** Indica si la transición hacia {@code destino} es válida (switch como expresión). */
    public boolean permiteTransicionA(Estado destino) {
        // TODO E07: Implementar con switch-expresión las transiciones del diagrama; los estados terminales no permiten ninguna.
        return switch (this){
            case ABIERTA ->
                destino == EN_CURSO || destino == CANCELADA;
            case EN_CURSO ->
                destino == CERRADA || destino == CANCELADA;
            case CERRADA, CANCELADA ->
                false;

        };
    }

    public boolean esTerminal() {
        return this == CERRADA || this == CANCELADA;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
