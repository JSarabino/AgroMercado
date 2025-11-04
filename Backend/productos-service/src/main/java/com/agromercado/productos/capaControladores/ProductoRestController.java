
package com.agromercado.productos.capaControladores;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.agromercado.productos.fachadaServices.DTO.ProductoDTO;
import com.agromercado.productos.fachadaServices.services.IProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/productos")
public class ProductoRestController {

	@Autowired
	private IProductoService productoService;

	/**
	 * GET /productos - Listar todos los productos
	 */
	@GetMapping
	public ResponseEntity<List<ProductoDTO>> listarProductos(
			@RequestHeader(value = "X-Gateway-Passed", required = false) String gatewayHeader
	) {
		if (gatewayHeader == null || !gatewayHeader.equals("true")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		return ResponseEntity.ok(productoService.findAll());
	}

	/**
	 * GET /productos/{id} - Obtener producto por ID
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ProductoDTO> consultarProducto(@PathVariable Integer id) {
		try {
			ProductoDTO producto = productoService.findById(id);
			return ResponseEntity.ok(producto);
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

	/**
	 * GET /api/productos/productor/{idProductor} - Listar productos de un productor
	 */
	@GetMapping("/productor/{idProductor}")
	public ResponseEntity<List<ProductoDTO>> listarProductosPorProductor(@PathVariable String idProductor) {
		List<ProductoDTO> productos = productoService.findByProductorId(idProductor);
		return ResponseEntity.ok(productos);
	}

	/**
	 * GET /api/productos/zona/{zonaId} - Listar productos de una zona
	 */
	@GetMapping("/zona/{zonaId}")
	public ResponseEntity<List<ProductoDTO>> listarProductosPorZona(@PathVariable String zonaId) {
		List<ProductoDTO> productos = productoService.findByZonaId(zonaId);
		return ResponseEntity.ok(productos);
	}

	/**
	 * GET /api/productos/disponibles - Listar productos disponibles
	 */
	@GetMapping("/disponibles")
	public ResponseEntity<List<ProductoDTO>> listarProductosDisponibles() {
		List<ProductoDTO> productos = productoService.findAvailable();
		return ResponseEntity.ok(productos);
	}

	/**
	 * POST /productos - Crear nuevo producto
	 */
	@PostMapping
	public ResponseEntity<?> crearProducto(
			@Valid @RequestBody ProductoDTO productoDTO,
			@RequestHeader(value = "X-User-Id", required = false) String userId
	) {
		try {
			System.out.println("=== DEBUG: Creando producto ===");
			System.out.println("ProductoDTO recibido: " + productoDTO);
			System.out.println("User ID desde header: " + userId);

			// Asignar el ID del productor desde el header si no viene en el DTO
			if (productoDTO.getIdProductor() == null && userId != null) {
				productoDTO.setIdProductor(userId);
				System.out.println("ID del productor asignado desde header: " + userId);
			}

			// Validar que tenga idProductor
			if (productoDTO.getIdProductor() == null || productoDTO.getIdProductor().isBlank()) {
				System.out.println("ERROR: El ID del productor está vacío");
				return ResponseEntity.badRequest().body("{\"message\": \"El ID del productor es obligatorio\"}");
			}

			System.out.println("Llamando a productoService.create...");
			ProductoDTO nuevoProducto = productoService.create(productoDTO);
			System.out.println("Producto creado exitosamente: " + nuevoProducto.getIdProducto());

			return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
		} catch (IllegalArgumentException e) {
			System.out.println("ERROR de validación: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
		} catch (Exception e) {
			System.out.println("ERROR inesperado: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("{\"message\": \"" + e.getMessage() + "\"}");
		}
	}

	/**
	 * PUT /api/productos/{id} - Actualizar producto
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ProductoDTO> actualizarProducto(
			@PathVariable Integer id,
			@Valid @RequestBody ProductoDTO productoDTO
	) {
		try {
			ProductoDTO productoActualizado = productoService.update(id, productoDTO);
			return ResponseEntity.ok(productoActualizado);
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

	/**
	 * DELETE /api/productos/{id} - Eliminar producto
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminarProducto(@PathVariable Integer id) {
		try {
			productoService.delete(id);
			return ResponseEntity.noContent().build();
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	/**
	 * PATCH /api/productos/{id}/disponibilidad - Cambiar disponibilidad
	 */
	@PatchMapping("/{id}/disponibilidad")
	public ResponseEntity<ProductoDTO> toggleDisponibilidad(@PathVariable Integer id) {
		try {
			ProductoDTO producto = productoService.toggleDisponibilidad(id);
			return ResponseEntity.ok(producto);
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}
}
