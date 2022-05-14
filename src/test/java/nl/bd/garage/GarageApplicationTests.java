package nl.bd.garage;

import nl.bd.garage.controllers.CostItemController;
import nl.bd.garage.controllers.CustomerController;
import nl.bd.garage.controllers.EmployeeController;
import nl.bd.garage.controllers.RepairController;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class GarageApplicationTests {
	@Autowired
	private CostItemController costItemController;

	@Autowired
	private CustomerController customerController;

	@Autowired
	private EmployeeController employeeController;

	@Autowired
	private RepairController repairController;

	@Test
	void contextLoads() {
		assertThat(this.costItemController).isNotNull();
		assertThat(this.customerController).isNotNull();
		assertThat(this.employeeController).isNotNull();
		assertThat(this.repairController).isNotNull();
	}
}
