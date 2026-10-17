package mx.tecnm.orizaba.tp.biblioteca.eventos;

/**
 * Observador funcional de eventos. La UI se suscribe con una lambda, una referencia a método
 * o una clase anónima.
 * <p><strong>Importante:</strong> el monitor notifica desde un hilo de fondo; la UI debe
 * reenviar la actualización a su propio hilo ({@code Platform.runLater},
 * {@code SwingUtilities.invokeLater}, SSE/WebSocket, etc.).</p>
 */
@FunctionalInterface
public interface OyenteEvento {

    void notificar(Evento evento);

    /**
     * Se crea la lambda que primero atiende la notificscion de this (El oyente actual) y posteriormente del parametro siguiente
     *
     * */

    /** Composición secuencial de oyentes (función de orden superior). */
    default OyenteEvento luego(OyenteEvento siguiente) {
        // TODO E11: Devolver una lambda que notifique primero a this y después a siguiente.
        return evento ->{ this.notificar(evento);
        siguiente.notificar(evento);};
    }
}
