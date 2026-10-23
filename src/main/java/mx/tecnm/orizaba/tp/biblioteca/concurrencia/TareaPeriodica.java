package mx.tecnm.orizaba.tp.biblioteca.concurrencia;

/**
 * Tarea que se ejecuta periódicamente en su propio hilo.
 * <ul>
 *   <li>Implementa {@link Runnable} en lugar de extender {@link Thread}
 *       (véase {@code java_4_modelo/t6_concurrencia/EjemploHilos}).</li>
 *   <li>Detención cooperativa: bandera {@code volatile} + {@code interrupt()} + {@code join()}.</li>
 *   <li>Template Method: {@link #run()} es final; la variación está en {@link #ejecutarCiclo()}.</li>
 * </ul>
 */
public abstract class TareaPeriodica implements Runnable {

    private final long periodoMs;
    private volatile boolean activa;
    private Thread hilo;
    private volatile long ciclos;

    protected TareaPeriodica(long periodoMs) {
        if (periodoMs <= 0) {
            throw new IllegalArgumentException("Periodo inválido: " + periodoMs);
        }
        this.periodoMs = periodoMs;
    }

    /** Arranca el hilo (demonio) con el nombre indicado. */

    /**
     * Comprueba si activa es verdadero, si no lanza una exepción
     * se crea un nuevo hilo con los parametros requeridos
     * define el daemon como verdadero
     * inicia el hilo
     * */
    public final synchronized void iniciar(String nombreHilo) {
        // TODO E12: Si ya está activa lanzar IllegalStateException; crear new Thread(this, nombreHilo), marcarlo demonio, activar y start().
        if(activa){
            throw new IllegalStateException();
        }

        hilo = new Thread(this, nombreHilo);

        hilo.setDaemon(true);

        activa = true;
        hilo.start();


    }

    /**
     * Pasamos activa de true a false
     * comprobamos si el hilo existe
     * Si es así, lo interrumpimos y esperamos con join()
     * */

    /** Solicita la detención y espera a que el hilo termine. */
    public final void detener() throws InterruptedException {
        // TODO E12: Bajar la bandera, interrumpir el hilo y esperar con join() (si existe).
        activa = false;

        if(hilo != null){
            hilo.interrupt();
            hilo.join();
        }
    }

    public final boolean activa() {
        return activa;
    }

    public final long ciclos() {
        return ciclos;
    }

    /**
     *Usamos un ciclo while mientras activa sea verdadero
     *dentro del ciclo, utilizamos el método ejecutarCiclo y aumentamos en 1 la variable ciclos
     *Usamos un try/catch para poder ejecutar el método sleep de Thread .
     * */


    @Override
    public final void run() {
        // TODO E12: Mientras activa: ejecutarCiclo(), incrementar ciclos y dormir periodoMs; ante InterruptedException restaurar la interrupción y salir.
        while (activa){
            ejecutarCiclo();
            ciclos++;
            try {
                Thread.sleep(periodoMs);
            } catch (InterruptedException e) {
                //throw new RuntimeException(e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /** Trabajo de un ciclo. */
    protected abstract void ejecutarCiclo();
}
