
package com.pasteleriamilsabores.pasteleria.api.config;

import com.pasteleriamilsabores.pasteleria.api.model.Rol;
import com.pasteleriamilsabores.pasteleria.api.model.Usuario;
import com.pasteleriamilsabores.pasteleria.api.model.Categoria;
import com.pasteleriamilsabores.pasteleria.api.model.Producto;
import com.pasteleriamilsabores.pasteleria.api.repository.UsuarioRepository;
import com.pasteleriamilsabores.pasteleria.api.repository.CategoriaRepository;
import com.pasteleriamilsabores.pasteleria.api.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga los datos de prueba al arrancar la API, solo si las tablas están vacías.
 * Un método por módulo: cada integrante agrega el suyo.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public DatosIniciales(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        cargarUsuarios();
        cargarCategorias();
        // cargarProductos();   -> Catalina (C3-02)
        cargarProductos();
    }

    // ---------- Usuarios (José, S3-05) ----------
    private void cargarUsuarios() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        usuarioRepository.saveAll(List.of(
                new Usuario("111111111", "Administrador", "Mil Sabores", "admin@gmail.com",
                        passwordEncoder.encode("admin123"), null,
                        "Región del Biobío", "Concepción", "Av. San Martín 1234", Rol.ADMINISTRADOR),
                new Usuario("222222222", "Vendedor", "Mil Sabores", "vendedor@gmail.com",
                        passwordEncoder.encode("vend123"), null,
                        "Región del Biobío", "Concepción", "Av. San Martín 1234", Rol.VENDEDOR),
                new Usuario("333333333", "Cliente", "Mil Sabores", "cliente@gmail.com",
                        passwordEncoder.encode("cliente123"), null,
                        "Región Metropolitana de Santiago", "Santiago", "Av. Libertador Bernardo O'Higgins 1000",
                        Rol.CLIENTE)
        ));
        log.info("Usuarios iniciales cargados: 3 (admin, vendedor y cliente)");
    }

    // ---------- Categorías (Catalina, C3-01) ----------
    private void cargarCategorias() {
        if (categoriaRepository.count() > 0) {
            return;
        }
        categoriaRepository.saveAll(List.of(
                new Categoria("cuadradas", "Tortas Cuadradas"),
                new Categoria("circulares", "Tortas Circulares"),
                new Categoria("individuales", "Postres Individuales"),
                new Categoria("sin-azucar", "Sin Azúcar"),
                new Categoria("tradicional", "Tradicional"),
                new Categoria("sin-gluten", "Sin Gluten"),
                new Categoria("veganos", "Veganos"),
                new Categoria("especiales", "Tortas Especiales")
        ));
        log.info("Categorías iniciales cargadas: 8");
    }

    // ---------- Productos (Catalina, C3-02) ----------
    private void cargarProductos() {
        if (productoRepository.count() > 0) {
            return;
        }
        productoRepository.saveAll(List.of(
                new Producto("TC001", "Torta Cuadrada de Chocolate",
                        "Deliciosa torta de chocolate con capas de ganache y un toque de avellanas. Personalizable con mensajes especiales.",
                        45000, 10, 3, "/img/Torta Cuadrada de Chocolate.png", buscarCategoria("cuadradas")),

                new Producto("TC002", "Torta Cuadrada de Frutas",
                        "Una mezcla de frutas frescas y crema chantilly sobre un suave bizcocho de vainilla, ideal para celebraciones.",
                        50000, 2, 5, "/img/Torta Cuadrada de Frutas.png", buscarCategoria("cuadradas")),

                new Producto("TT001", "Torta Circular de Vainilla",
                        "Bizcocho de vainilla clásico relleno con crema pastelera y cubierto con un glaseado dulce, perfecto para cualquier ocasión.",
                        40000, 12, 4, "/img/Torta Circular de Vainilla.png", buscarCategoria("circulares")),

                new Producto("TT002", "Torta Circular de Manjar",
                        "Torta tradicional chilena con manjar y nueces, un deleite para los amantes de los sabores dulces y clásicos.",
                        42000, 4, 4, "/img/Torta Circular de Manjar.png", buscarCategoria("circulares")),

                new Producto("PI001", "Mousse de Chocolate",
                        "Postre individual cremoso y suave, hecho con chocolate de alta calidad, ideal para los amantes del chocolate.",
                        5000, 20, 5, "/img/Mousse de Chocolate.png", buscarCategoria("individuales")),

                new Producto("PI002", "Tiramisú Clásico",
                        "Un postre italiano individual con capas de café, mascarpone y cacao, perfecto para finalizar cualquier comida.",
                        5500, 3, 5, "/img/Tiramisú Clásico.png", buscarCategoria("individuales")),

                new Producto("PSA001", "Torta Sin Azúcar de Naranja",
                        "Torta ligera y deliciosa, endulzada naturalmente, ideal para quienes buscan opciones más saludables.",
                        48000, 7, 3, "/img/Torta Sin Azúcar de Naranja.png", buscarCategoria("sin-azucar")),

                new Producto("PSA002", "Cheesecake Sin Azúcar",
                        "Suave y cremoso, este cheesecake es una opción perfecta para disfrutar sin culpa.",
                        47000, 2, 3, "/img/Cheesecake Sin Azúcar.png", buscarCategoria("sin-azucar")),

                new Producto("PT001", "Empanada de Manzana",
                        "Pastelería tradicional rellena de manzanas especiadas, perfecta para un dulce desayuno o merienda.",
                        3000, 25, 8, "/img/Empanada de Manzana.png", buscarCategoria("tradicional")),

                new Producto("PT002", "Tarta de Santiago",
                        "Tradicional tarta española hecha con almendras, azúcar y huevos, una delicia para los amantes de los postres clásicos.",
                        6000, 8, 3, "/img/Tarta de Santiago.png", buscarCategoria("tradicional")),

                new Producto("PG001", "Brownie Sin Gluten",
                        "Rico y denso, este brownie es perfecto para quienes necesitan evitar el gluten sin sacrificar el sabor.",
                        4000, 4, 5, "/img/Brownie Sin Gluten.png", buscarCategoria("sin-gluten")),

                new Producto("PG002", "Pan Sin Gluten",
                        "Suave y esponjoso, ideal para sándwiches o para acompañar cualquier comida.",
                        3500, 15, 5, "/img/Pan Sin Gluten.png", buscarCategoria("sin-gluten")),

                new Producto("PV001", "Torta Vegana de Chocolate",
                        "Torta de chocolate húmeda y deliciosa, hecha sin productos de origen animal, perfecta para veganos.",
                        50000, 6, 3, "/img/Torta Vegana de Chocolate.png", buscarCategoria("veganos")),

                new Producto("PV002", "Galletas Veganas de Avena",
                        "Crujientes y sabrosas, estas galletas son una excelente opción para un snack saludable y vegano.",
                        4500, 18, 6, "/img/Galletas Veganas de Avena.png", buscarCategoria("veganos")),

                new Producto("TE001", "Torta Especial de Cumpleaños",
                        "Diseñada especialmente para celebraciones, personalizable con decoraciones y mensajes únicos.",
                        55000, 2, 2, "/img/Torta Especial de Cumpleaños.png", buscarCategoria("especiales")),

                new Producto("TE002", "Torta Especial de Boda",
                        "Elegante y deliciosa, esta torta está diseñada para ser el centro de atención en cualquier boda.",
                        60000, 5, 2, "/img/Torta Especial de Boda.png", buscarCategoria("especiales"))
        ));
        log.info("Productos iniciales cargados: 16");
    }

    private Categoria buscarCategoria(String slug) {
        return categoriaRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalStateException("Categoría no encontrada: " + slug));
    }
}
