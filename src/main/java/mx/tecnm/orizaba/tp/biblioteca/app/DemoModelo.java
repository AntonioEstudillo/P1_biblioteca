package mx.tecnm.orizaba.tp.biblioteca.app;

import mx.tecnm.orizaba.tp.biblioteca.eventos.OyenteEvento;
import mx.tecnm.orizaba.tp.biblioteca.eventos.Evento;
import mx.tecnm.orizaba.tp.biblioteca.funcional.Reglas;
import mx.tecnm.orizaba.tp.biblioteca.modelo.*;
import mx.tecnm.orizaba.tp.biblioteca.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.biblioteca.servicio.Biblioteca;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Recorrido de consola por el modelo: sirve como referencia de uso para construir la UI.
 * La UI del estudiante debe ofrecer, como mínimo, lo que aquí se muestra.
 */
public final class DemoModelo {

    public static void main(String[] args) throws Exception {
        Path bd = args.length > 0 ? Path.of(args[0]) : Files.createTempFile("biblioteca", ".db");
        AtomicReference<LocalDate> hoy = new AtomicReference<>(LocalDate.now());

        try (ConexionSQLite cx = new ConexionSQLite(bd.toString());
             Biblioteca g = new Biblioteca(cx, hoy::get)) {

            // Suscripción con clase anónima (la UI usaría Platform.runLater / invokeLater aquí).
            g.suscribir(new OyenteEvento() {
                @Override
                public void notificar(Evento e) {
                    System.out.println("   evento " + e + " [" + Thread.currentThread().getName() + "]");
                }
            });

            System.out.println("== Alta de materiales (referencias a constructor)");
            g.registrar(Material.Libro::new, "Estructuras de datos en Java", 15.0, 420);
            g.registrar(Material.Libro::new, "Object-Oriented Analysis and Design", 25.0, 717);
            g.registrar(Material.Revista::new, "Communications of the ACM", 10.0, 2);
            g.registrar(Material.Revista::new, "IEEE Software", 12.0, 8);
            g.registrar(Material.Tesis::new, "Cortes sobre variables locales en AspectJ", 20.0, "Doctorado");
            g.registrar(Material.Tesis::new, "Ingeniería de contexto con LLM", 18.0, "Maestría");

            System.out.println("== socios");
            g.registrarSocio("Ana López", "ana.lopez@orizaba.tecnm.mx", Nivel.EXTERNO);
            g.registrarSocio("Bruno Díaz", "bruno.diaz@orizaba.tecnm.mx", Nivel.ESTUDIANTE);
            g.registrarSocio("Carla Ruiz", "carla.ruiz@gmail.com", Nivel.DOCENTE);

            System.out.println("== Flujo de préstamo");
            Prestamo t = g.abrir(1);
            g.agregarLinea(t.id(), "LIB-0001", 2);
            g.agregarLinea(t.id(), "TES-0001", 3);
            g.iniciar(t.id());
            t.forEach(System.out::println);
            g.cerrar(t.id());
            Prestamo t2 = g.abrir(2);
            g.agregarLinea(t2.id(), "REV-0001", 1);
            g.iniciar(t2.id());
            g.abrir(3);

            System.out.println("== Reportes");
            System.out.println(g.catalogo().resumen());
            System.out.println("Ingresos por tipo: " + g.ingresosPorTipo());
            System.out.println("Estadísticas: " + g.estadisticas());
            System.out.println("Conteo por estado: " + g.conteoPorEstado());
            g.mejores(3).forEach(System.out::println);
            g.buscar(Reglas.conCapacidad(Digitalizable.class)).stream().map(g::describir).forEach(System.out::println);

            System.out.println("== Monitor en segundo plano (reloj simulado +" + (Biblioteca.PLAZO_DIAS + 1) + " días)");
            g.iniciarMonitor(100);
            hoy.set(hoy.get().plusDays(Biblioteca.PLAZO_DIAS + 1));
            Thread.sleep(400);
            g.detenerMonitor();
            System.out.println("Base de datos: " + bd.toAbsolutePath());
        }
    }
}
