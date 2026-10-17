package mx.tecnm.orizaba.tp.biblioteca.modelo;

import mx.tecnm.orizaba.tp.biblioteca.funcional.Regla;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Acervo: <strong>agregación</strong> de materiales (las partes existen con independencia del
 * todo; véase {@code java_4_modelo/t4_jerarquia/t2_agregacion/Jerarquia02}).
 * Reúne en un solo lugar tres variantes de tipos internos:
 * clase interna privada ({@link Iterador}), clase anónima ({@link #porTarifa(int)}) y clase local
 * ({@link #resumen()}).
 */
public class Acervo implements Iterable<Material>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<Signatura, Material> elementos = new LinkedHashMap<>();

    /** Constructor con número variable de argumentos. */
    public Acervo(Material... iniciales) {
        agregar(iniciales);
    }


    /**
     * El método recibe una cantidad variable de argumentos y se recorren con un cliclo for
     * el método putIfAbsent evita las claves duplicadas, si no existe devuelve null, si existe devuelve la clave anterior
     * */
    public final void agregar(Material... nuevos) {
        // TODO E09: Recorrer los argumentos; rechazar nulos y claves duplicadas (ReglaNegocioException); usar putIfAbsent.
        for (Material material : nuevos){
            if(material == null){
                throw new ReglaNegocioException("No se puden agregar materiales nulos");
            }
            Material existe = elementos.putIfAbsent(material.clave(), material);

            if (existe != null){
                throw new ReglaNegocioException("Clave ya existe");
            }
        }
    }

    public Optional<Material> buscar(Signatura clave) {
        return Optional.ofNullable(elementos.get(clave));
    }

    public boolean retirar(Signatura clave) {
        return elementos.remove(clave) != null;
    }

    public int tamanio() {
        return elementos.size();
    }


    /**
     * Regla<? super Material> regla = generico con límite superior
     * Con el método de stream filtramos los valores de Material
     * Con la referencia al método accedemos al método de la interfaz que permite hacer ña evalucación
     * toList() para retornar una lista
     * */
    /** Filtrado con la interfaz funcional propia {@link Regla}. */
    public List<Material> filtrar(Regla<? super Material> regla) {
        // TODO E09: Stream de los valores filtrado con regla::evaluar y recolectado con toList().
        return elementos.values().stream().filter(regla::evaluar).toList();
    }

    /**
     *<E extends Material> = Limete inferior
     * Recupera el valor de los elementos
     * filtra si es la instancia que se espera
     * Map::cast se asegura de que el tipo sea del tipo esperado
     * se retorna en fornato de lista
     * */
    /** Filtrado por subtipo con genéricos acotados: {@code deTipo(Material.Libro.class)}. */
    public <E extends Material> List<E> deTipo(Class<E> tipo) {
        // TODO E09: Usar filter(tipo::isInstance) y map(tipo::cast).
        return elementos.values().stream().filter(tipo::isInstance).map(tipo::cast).toList();
    }

    public List<Material> ordenados(Comparator<? super Material> criterio) {
        return elementos.values().stream().sorted(criterio).toList();
    }


    /**
     * Utilizamos una clase anonima para implementar Comparator
     * compara 2 tipos de materiales (libro, tesis, o revista)
     * Devuelve el resultado entre la comparación de la taria, dependiendo de las unidades capturadas por el método
     * */
    /**
     * Comparador implementado con una <strong>clase anónima</strong> que captura la variable
     * {@code unidades} (efectivamente final; véase {@code Parcel09}).
     */
    public static Comparator<Material> porTarifa(final int unidades) {
        // TODO E10: Devolver new Comparator<>() { compare(a, b) → Double.compare(a.tarifa(unidades), b.tarifa(unidades)) }.
        return new Comparator<Material>() {
            @Override
            public int compare(Material o1, Material o2) {
                return Double.compare(o1.tarifa(unidades), o2.tarifa(unidades));
            }
        };
    }

    /**
     * Se declara la clase local Acumulador con las variables solicitadas
     * Se agrega el método vacio agregae, donde se reraliza la operaciones de acumulación y se declaran el minimo y el maximo
     * Se crea un objeto de la clase interna y se recorre el catalogo con un ciclo for
     *Se da formato a la salida
     * */


    /** Resumen calculado con una <strong>clase local</strong> (véase {@code Parcel05}). */
    public String resumen() {
        // TODO E10: Declarar dentro del método la clase local Acumulador (cuenta, suma, mínimo, máximo) y recorrer el catálogo.
       class Acumulador {
           int cuenta;
           double suma;
           double minimo = Double.POSITIVE_INFINITY;
           double maximo = Double.NEGATIVE_INFINITY;

        void agregar (Material material){
            double precio = material.precioBase();
            cuenta++;
            suma =+ precio;
            minimo = Math.min(minimo, precio);
            maximo = Math.max(maximo, precio);
        }

       }

        Acumulador acumulador = new Acumulador();

        for (Material material : elementos.values()) {
            acumulador.agregar(material);
        }

        double minimo = acumulador.cuenta == 0
                ? 0.0
                : acumulador.minimo;

        double maximo = acumulador.cuenta == 0
                ? 0.0
                : acumulador.maximo;

        return "%d elementos, suma=%.2f, mínimo=%.2f, máximo=%.2f"
                .formatted(
                        acumulador.cuenta,
                        acumulador.suma,
                        minimo,
                        maximo
                );
    }

    @Override
    public Iterator<Material> iterator() {
        return new Iterador();
    }

    /**
     * Clase interna privada que implementa una interfaz pública: el cliente sólo conoce
     * {@code Iterator<Material>} y la implementación queda oculta (véase {@code Parcel04}).
     * Itera sobre una instantánea, de modo que el catálogo no puede modificarse a través de ella.
     */
    private class Iterador implements Iterator<Material> {
        private final List<Material> instantanea = new ArrayList<>(elementos.values());
        private int cursor;

        @Override
        public boolean hasNext() {
            return cursor < instantanea.size();
        }


        /**
         * Compara siexiste el siguiente con el método hasNext()
         *
         * Retorna el cursor aumentando en 1
         * */
        @Override
        public Material next() {
            // TODO E10: Lanzar NoSuchElementException si no hay siguiente; devolver el elemento y avanzar el cursor.
           if(!hasNext()){
               throw new NoSuchElementException();
           }

           return instantanea.get(cursor++);
        }
    }
}
