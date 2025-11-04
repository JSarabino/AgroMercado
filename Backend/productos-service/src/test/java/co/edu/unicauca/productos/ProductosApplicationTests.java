package co.edu.unicauca.productos;

import com.agromercado.productos.ProductosApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ProductosApplication.class)
@ActiveProfiles("test")
class ProductosApplicationTests {

	@Test
	void contextLoads() {
		// Test básico que verifica que el contexto de Spring se carga correctamente
	}

}
