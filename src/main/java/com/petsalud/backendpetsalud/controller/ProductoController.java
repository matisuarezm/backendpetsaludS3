package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Producto;
import com.petsalud.backendpetsalud.service.ProductoService;
import com.petsalud.backendpetsalud.validation.Patrones;          // ← NUEVO
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;                         // ← NUEVO
import jakarta.validation.constraints.Min;                         // ← NUEVO
import jakarta.validation.constraints.Pattern;                     // ← NUEVO
import jakarta.validation.constraints.Size;                        // ← NUEVO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @PostMapping
    public ResponseEntity<Producto> agregarProducto(@Valid @RequestBody Producto producto) {
        Producto nuevoProducto = productoService.agregarProducto(producto);
        return ResponseEntity.ok(nuevoProducto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        return productoService.obtenerProductoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id,
                                                       @Valid @RequestBody Producto producto) { // ← CAMBIO: faltaba @Valid
        Producto productoActualizado = productoService.actualizarProducto(id, producto);
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        List<Producto> productos = productoService.listarProductos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/categoria")
    public ResponseEntity<List<Producto>> listarProductosPorCategoria(
            // ← NUEVO: la categoría solo puede tener letras (bloquea x' OR 1=1--)
            @RequestParam @Size(max = 50)
            @Pattern(regexp = Patrones.NOMBRE, message = "Solo puede contener letras") String categoria) {
        List<Producto> productos = productoService.listarProductosPorCategoria(categoria);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Producto>> listarProductosConStockBajo(
            // ← NUEVO: rango permitido
            @RequestParam @Min(value = 0, message = "No puede ser negativo")
            @Max(value = 100000, message = "Fuera de rango") Integer cantidad) {
        List<Producto> productos = productoService.listarProductosConStockBajo(cantidad);
        return ResponseEntity.ok(productos);
    }
}