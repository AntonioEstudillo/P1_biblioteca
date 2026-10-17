package mx.tecnm.orizaba.tp.biblioteca.app;

import mx.tecnm.orizaba.tp.biblioteca.eventos.Evento;
import mx.tecnm.orizaba.tp.biblioteca.eventos.TipoEvento;
import mx.tecnm.orizaba.tp.biblioteca.funcional.FabricaElemento;
import mx.tecnm.orizaba.tp.biblioteca.funcional.Regla;
import mx.tecnm.orizaba.tp.biblioteca.funcional.Reglas;
import mx.tecnm.orizaba.tp.biblioteca.modelo.*;
import mx.tecnm.orizaba.tp.biblioteca.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.biblioteca.persistencia.Respaldo;
import mx.tecnm.orizaba.tp.biblioteca.servicio.Biblioteca;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Pruebas de aceptación del modelo (sin dependencias externas). Deben pasar al 100 %
 * antes de comenzar la UI. Ejecutar: {@code java ... mx.tecnm.orizaba.tp.biblioteca.app.PruebasModelo}
 */
public final class PruebasModelo {

    private static int aprobadas;
    private static final List<String> fallidas = new ArrayList<>();

    @FunctionalInterface
    private interface Caso {
        void ejecutar() throws Exception;
    }

    private static void prueba(String nombre, Caso caso) {
        try {
            caso.ejecutar();
            aprobadas++;
            System.out.println("  [OK]    " + nombre);
        } catch (Throwable t) {
            fallidas.add(nombre);
            System.out.println("  [FALLA] " + nombre + " -> " + t);
        }
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static void cerca(double esperado, double real) {
        verificar(Math.abs(esperado - real) < 0.01, "se esperaba " + esperado + " y se obtuvo " + real);
    }

    private static void lanza(Class<? extends Throwable> tipo, Caso caso) {
        try {
            caso.ejecutar();
        } catch (Throwable t) {
            verificar(tipo.isInstance(t), "se esperaba " + tipo.getSimpleName() + " y se obtuvo " + t);
            return;
        }
        throw new AssertionError("se esperaba " + tipo.getSimpleName());
    }

    public static void main(String[] args) throws Exception {
        Path tmp = Files.createTempDirectory("pruebas-biblioteca");
        System.out.println("Pruebas del modelo – Biblioteca");

        prueba("T01 Signatura: formato, parse y validación", () -> {
            Signatura c = Signatura.parse("ABC-0042");
            verificar(c.toString().equals("ABC-0042"), "toString");
            verificar(c.compareTo(new Signatura("ABC", 43)) < 0, "orden");
            lanza(IllegalArgumentException.class, () -> new Signatura("ab", 1));
            lanza(IllegalArgumentException.class, () -> Signatura.parse("ABC_1"));
        });

        prueba("T02 Invariantes de Material (encapsulamiento)", () -> {
            lanza(ReglaNegocioException.class, () -> new Material.Libro("x", -1, 420));
            lanza(ReglaNegocioException.class, () -> new Material.Libro(" ", 10, 420));
            lanza(ReglaNegocioException.class, () -> new Material.Revista("x", 10, -1));
            lanza(ReglaNegocioException.class, () -> new Material.Libro("x", 10, 420).tarifa(0));
        });

        prueba("T03 Polimorfismo: tarifas por subtipo (Template Method)", () -> {
            Prestable[] v = {
                new Material.Libro("a", 15.0, 320),
                new Material.Revista("b", 10.0, 8),
                new Material.Tesis("c", 20.0, "Maestría") };
            cerca(60.0, v[0].tarifa(4));
            cerca(10.0, v[1].tarifa(2));
            cerca(40.0, v[2].tarifa(5));
            cerca(Math.round(60.0 * 1.16 * 100.0) / 100.0, v[0].tarifaConImpuesto(4));
        });

        prueba("T04 Clasificación múltiple y colisión de métodos default", () -> {
            Object s1 = new Material.Libro("a", 1, 420);
            Object s2 = new Material.Revista("b", 1, 8);
            Material.Tesis s3 = new Material.Tesis("c", 1, "Maestría");
            verificar(s1 instanceof Digitalizable && !(s1 instanceof Restringido), "Libro sólo Digitalizable");
            verificar(s2 instanceof Restringido && !(s2 instanceof Digitalizable), "Revista sólo Restringido");
            verificar(s3 instanceof Digitalizable && s3 instanceof Restringido, "Tesis ambas");
            String d = s3.descripcionCorta();
            verificar(d.contains("Repositorio digital") && d.contains("Nivel de acceso"), "colisión resuelta: " + d);
        });

        prueba("T05 Retorno covariante en copiar()", () -> {
            Material.Libro a = new Material.Libro("a", 5, 420);
            Material.Libro b = a.copiar();   // sin conversión: retorno covariante
            verificar(!a.equals(b) && a.nombre().equals(b.nombre()), "copia con clave nueva");
        });

        prueba("T06 Máquina de estados de Prestamo", () -> {
            Socio x = new Socio(900, "Prueba", "prueba@ito.mx");
            Prestamo t = new Prestamo(900, x, LocalDate.now());
            lanza(ReglaNegocioException.class, t::iniciar);
            t.agregar(new Material.Libro("a", 10, 420), 1);
            t.iniciar();
            lanza(ReglaNegocioException.class, () -> t.agregar(new Material.Libro("b", 1, 420), 1));
            t.cerrar();
            lanza(ReglaNegocioException.class, t::cancelar);
            verificar(t.estado() == Estado.CERRADA, "estado final");
        });

        prueba("T07 Composición: la línea usa el nivel de la envolvente", () -> {
            Nivel n = Nivel.values()[Nivel.values().length - 1];
            Socio x = new Socio(901, "Prueba", "p2@ito.mx", n);
            Prestamo t = new Prestamo(901, x, LocalDate.now());
            Prestamo.Linea l = t.agregar(new Material.Libro("a", 15.0, 320), 4);
            cerca(n.aplicar(60.0), l.importe());
            verificar(l.contenedor() == t, "Prestamo.this");
            lanza(ReglaNegocioException.class, () -> t.agregar(l.elemento(), 2));
        });

        prueba("T08 Agregación, iterador oculto, clase anónima y clase local", () -> {
            Material a = new Material.Libro("Alfa", 300, 420);
            Material b = new Material.Revista("Beta", 100, 8);
            Acervo cat = new Acervo(a, b);
            lanza(ReglaNegocioException.class, () -> cat.agregar(a));
            Iterator<Material> it = cat.iterator();
            verificar(!it.getClass().getSimpleName().isEmpty() && it.hasNext(), "iterador");
            lanza(UnsupportedOperationException.class, it::remove);
            verificar(cat.ordenados(Acervo.porTarifa(1)).get(0) == b, "comparador anónimo");
            verificar(cat.resumen().startsWith("2 "), "clase local: " + cat.resumen());
            verificar(cat.deTipo(Material.Libro.class).size() == 1, "genéricos acotados");
        });

        prueba("T09 Interfaz funcional propia y composición de reglas", () -> {
            Material a = new Material.Libro("Alfa", 300, 420);
            Material b = new Material.Tesis("Gamma", 100, "Maestría");
            Acervo cat = new Acervo(a, b);
            Regla<Material> r = Reglas.precioHasta(200).y(Reglas.conCapacidad(Restringido.class));
            verificar(cat.filtrar(r).equals(List.of(b)), "y");
            verificar(cat.filtrar(r.negar()).equals(List.of(a)), "negar");
            verificar(cat.filtrar(Reglas.nombreContiene("ALF").o(r)).size() == 2, "o");
            FabricaElemento<Material.Libro, Integer> f = Material.Libro::new;
            verificar(f.crear("Delta", 1, 420).tipo().equals("Libro"), "referencia a constructor");
        });

        prueba("T10 Serialización: transient y Externalizable", () -> {
            Material a = new Material.Libro("Alfa", 10, 420);
            a.tarifa(1);
            Socio x = new Socio(7, "Ada", "ada@ito.mx", Nivel.values()[1]);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
                oos.writeObject(a);
                oos.writeObject(x);
            }
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                Material a2 = (Material) ois.readObject();
                Socio x2 = (Socio) ois.readObject();
                verificar(a2.equals(a) && a2.cotizaciones() == 0 && a.cotizaciones() == 1, "transient");
                verificar(x2.correo().equals("ada@ito.mx") && x2.nivel() == Nivel.values()[1], "externalizable");
            }
        });

        Path bd = tmp.resolve("pruebas.db");
        AtomicReference<LocalDate> hoy = new AtomicReference<>(LocalDate.now());
        int[] ids = new int[2];

        prueba("T11 Servicio + SQLite: alta, flujo y reportes", () -> {
            try (ConexionSQLite cx = new ConexionSQLite(bd.toString())) {
                Biblioteca g = new Biblioteca(cx, hoy::get);
                List<Evento> eventos = new CopyOnWriteArrayList<>();
                g.suscribir(eventos::add);
                Material e1 = g.registrar(Material.Libro::new, "Uno", 15.0, 320);
                Material e3 = g.registrar(Material.Tesis::new, "Tres", 20.0, "Maestría");
                Socio a = g.registrarSocio("Grace Hopper", "grace@ito.mx", Nivel.values()[0]);
                lanza(ReglaNegocioException.class, () -> g.registrarSocio("Otra", "GRACE@ito.mx", Nivel.values()[0]));
                Prestamo t = g.abrir(a.id());
                g.agregarLinea(t.id(), e1.clave().toString(), 4);
                g.agregarLinea(t.id(), e3.clave().toString(), 5);
                g.iniciar(t.id());
                g.cerrar(t.id());
                ids[0] = t.id();
                cerca(60.0 + 40.0, g.estadisticas().getSum());
                verificar(g.ingresosPorTipo().size() == 2, "ingresos por tipo");
                verificar(g.mejores(1).equals(List.of(a)), "mejores");
                verificar(g.masDemandado().isPresent(), "más demandado");
                verificar(g.describir(e1).contains("Libro"), "describir");
                verificar(g.proyectar(Material::nombre).containsAll(List.of("Uno", "Tres")), "proyectar");
                verificar(eventos.stream().anyMatch(ev -> ev.tipo() == TipoEvento.ALTA), "eventos");
                lanza(ReglaNegocioException.class, () -> g.retirar(e1.clave()));
                ids[1] = g.abrir(a.id()).id();
            }
        });

        prueba("T12 Persistencia: la información sobrevive al reinicio", () -> {
            try (ConexionSQLite cx = new ConexionSQLite(bd.toString())) {
                Biblioteca g = new Biblioteca(cx, hoy::get);
                verificar(g.catalogo().tamanio() == 2, "catálogo");
                verificar(g.socios().size() == 1, "socios");
                Prestamo t = g.registro(ids[0]);
                verificar(t.estado() == Estado.CERRADA && t.lineas().size() == 2, "registro reconstruido");
                cerca(60.0 + 40.0, t.total());
            }
        });

        prueba("T13 Monitor: reglas, hilo y detención", () -> {
            try (ConexionSQLite cx = new ConexionSQLite(bd.toString())) {
                Biblioteca g = new Biblioteca(cx, hoy::get);
                List<Evento> eventos = new CopyOnWriteArrayList<>();
                g.suscribir(eventos::add);
                Prestamo t = g.abrir(g.socios().get(0).id());
                g.agregarLinea(t.id(), g.catalogo().iterator().next().clave().toString(), 1);
                g.iniciar(t.id());
                hoy.set(hoy.get().plusDays(Biblioteca.PLAZO_DIAS + 1));
                verificar(g.revisar() == 2, "cancelación + alerta");
                verificar(g.registro(ids[1]).estado() == Estado.CANCELADA, "abierta sin líneas cancelada");
                verificar(g.revisar() == 0, "la alerta no se repite");
                g.iniciarMonitor(20);
                Thread.sleep(120);
                verificar(g.monitorActivo(), "hilo activo");
                g.detenerMonitor();
                verificar(!g.monitorActivo(), "hilo detenido");
                long alertas = eventos.stream().filter(ev -> ev.tipo() == TipoEvento.ALERTA).count();
                verificar(alertas == 1, "alertas: " + alertas);
            }
        });

        prueba("T14 Respaldo binario (exportar/importar)", () -> {
            Path archivo = tmp.resolve("respaldo.bin");
            try (ConexionSQLite cx = new ConexionSQLite(bd.toString())) {
                Biblioteca g = new Biblioteca(cx, hoy::get);
                g.exportarRespaldo(archivo);
                Respaldo.Contenido c = Respaldo.importar(archivo);
                verificar(c.catalogo().tamanio() == g.catalogo().tamanio(), "catálogo");
                verificar(c.registros().size() == g.registros().size(), "registros");
                Prestamo.Linea l = c.registros().get(0).lineas().get(0);
                verificar(l.contenedor() == c.registros().get(0), "clase interna serializada con su envolvente");
            }
        });

        System.out.printf("%nResultado: %d aprobadas, %d fallidas%n", aprobadas, fallidas.size());
        if (!fallidas.isEmpty()) {
            System.exit(1);
        }
    }
}
