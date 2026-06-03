package com.example.ms_proveedor;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

class MsProveedorApplicationTests {

	@Test
	void applicationClassExists() {
		MsProveedorApplication application = new MsProveedorApplication();
		assertNotNull(application);
	}

	@Test
	void main_DelegatesToSpringApplicationRun() {
		try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
			String[] args = {"--spring.profiles.active=test"};

			MsProveedorApplication.main(args);

			springApplication.verify(() -> SpringApplication.run(MsProveedorApplication.class, args));
		}
	}

}
