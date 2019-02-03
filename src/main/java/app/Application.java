package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Spring Boot Application - to query OC Transpo Cancellations database in Azure.
 * Uses JDBC to access Azure SQL DB.
 * Presents a REST API in the frontend.
 * 
 * @author ramdarbha
 */
@SpringBootApplication
//@ComponentScan(basePackageClasses = ApiV1Controller.class)
public class Application implements CommandLineRunner {

	private static final String Version_ms = "v1.0 3Feb19";
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String args[]) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {

        log.info("OC Transpo Cancellations App " + Version_ms);

        //jdbcTemplate.execute("DROP TABLE customers IF EXISTS");
    }
}

