package br.com.avaliacao.apimusicmanagement.api.v1.controller;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.GeneroResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.mapper.GeneroMapper;
import br.com.avaliacao.apimusicmanagement.config.security.jwt.JwtAuthenticationFilter;
import br.com.avaliacao.apimusicmanagement.config.security.rateLimit.RateLimitFilter;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Genero;
import br.com.avaliacao.apimusicmanagement.domain.service.GeneroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GeneroControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
class GeneroControllerV1Test {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GeneroService generoService;

    @MockitoBean
    private GeneroMapper generoMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    @Test
    @WithMockUser
    void listar_deveRetornar200() throws Exception {
        var genero = new Genero("Rock");

        when(generoService.listar()).thenReturn(List.of(genero));
        when(generoMapper.toResponse(genero)).thenReturn(new GeneroResponse(1L, "Rock", null, null));

        mvc.perform(get("/api/v1/generos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Rock"));
    }
}
