package ca.humanbeans.sourbeans;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class SourBeansApplicationTests {
	@Autowired DataSource dataSource;
	@Autowired EntityManager entityManager;

	@Test
	void connectsToTestDatabaseAndInitializesSchema() throws Exception {
		try (var connection = dataSource.getConnection()) {
			assertEquals("sourbeans_test", connection.getCatalog());
			assertTrue(connection.isValid(5));
			for (var table : new String[] {"projects", "issues"}) {
				try (var tables = connection.getMetaData().getTables(connection.getCatalog(), null, table, new String[] {"TABLE"})) {
					assertTrue(tables.next(), "Missing schema table: " + table);
				}
			}
		}
		assertEquals(1, ((Number) entityManager.createNativeQuery("SELECT 1").getSingleResult()).intValue());
	}

}
