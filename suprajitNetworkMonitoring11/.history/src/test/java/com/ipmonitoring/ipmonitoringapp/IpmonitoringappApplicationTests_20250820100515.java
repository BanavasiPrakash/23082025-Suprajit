import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockMvc
class IpmonitoringappApplicationTests {

    @Test
    @WithMockUser   // simulates an authenticated user for tests
    void contextLoads() {
    }
}

