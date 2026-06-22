package com.example.locadora;

import com.example.locadora.config.AppSecurityProperties;
import com.example.locadora.config.SecurityMode;
import com.example.locadora.repository.ClienteRepository;
import com.example.locadora.repository.JogoRepository;
import com.example.locadora.repository.LocacaoRepository;
import com.example.locadora.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.security.mode=SECURE")
@DisplayName("✅ Testes do Modo Seguro (Baseline/Proteção)")
 class SecureModeSmokeTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppSecurityProperties securityProperties;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private LocacaoRepository locacaoRepository;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();
        jogoRepository.deleteAll();
        clienteRepository.deleteAll();
        locacaoRepository.deleteAll();
        securityProperties.setMode(SecurityMode.SECURE);
    }

    @Test
    @DisplayName("Modo SECURE deve estar ativo")
    void shouldBeInSecureMode() {
        assert securityProperties.isSecureMode();
        assert !securityProperties.isInsecureMode();
    }

    @Test
    @DisplayName("Deve rejeitar SQL Injection em modo SECURE (OR 1=1)")
    void shouldRejectSqlInjectionInSecureMode() throws Exception {
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "admin",
                            "senha": "SenhaForte123",
                            "nome": "Admin",
                            "role": "ADMIN",
                            "email": "admin@test.com"
                        }
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "admin' OR '1'='1",
                            "senha": "qualquer_coisa"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("Deve rejeitar XSS - sanitiza HTML tags")
    void shouldRejectXssInSecureMode() throws Exception {
        String xssPayload = "<img src=x onerror=\"alert('XSS')\">";

        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "user1",
                            "senha": "Pass123",
                            "nome": "%s",
                            "role": "ATENDENTE",
                            "email": "user@test.com"
                        }
                        """.formatted(xssPayload.replace("\"", "\\\""))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value(not(containsString("<img"))))
                .andExpect(jsonPath("$.nome").value(not(containsString("onerror"))));
    }

    @Test
    @DisplayName("Deve aceitar login válido em SECURE")
    void shouldAcceptValidLogin() throws Exception {
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "validuser",
                            "senha": "ValidPass123",
                            "nome": "Valid User",
                            "role": "ATENDENTE",
                            "email": "valid@test.com"
                        }
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "validuser",
                            "senha": "ValidPass123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ATENDENTE"));
    }

    @Test
    @DisplayName("Deve rejeitar preço negativo")
    void shouldRejectNegativePriceInSecureMode() throws Exception {
        String token = createAndLoginAdmin();

        mockMvc.perform(post("/jogos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "titulo": "Game",
                            "genero": "Ação",
                            "precoDiaria": -10.00,
                            "descricao": "Game"
                        }
                        """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Não deve expor stack trace")
    void shouldNotExposeStackTraceInSecureMode() throws Exception {
        String token = createAndLoginAdmin();

        mockMvc.perform(get("/jogos/9999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.error").value("Jogo não encontrado"));
    }

    private String createAndLoginAdmin() throws Exception {
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "adminuser",
                            "senha": "SenhaForte123",
                            "nome": "Admin",
                            "role": "ADMIN",
                            "email": "admin@test.com"
                        }
                        """))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "username": "adminuser",
                            "senha": "SenhaForte123"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return new ObjectMapper().readTree(response).get("token").asText();
    }

}
