package br.com.avaliacao.apimusicmanagement.api.v1.controller;

import br.com.avaliacao.apimusicmanagement.api.v1.dto.response.FaixaResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.mapper.FaixaMapper;
import br.com.avaliacao.apimusicmanagement.config.security.jwt.JwtAuthenticationFilter;
import br.com.avaliacao.apimusicmanagement.config.security.rateLimit.RateLimitFilter;
import br.com.avaliacao.apimusicmanagement.domain.model.entity.Faixa;
import br.com.avaliacao.apimusicmanagement.domain.service.FaixaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FaixaControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
class FaixaControllerV1Test {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    FaixaService faixaService;

    @MockitoBean
    FaixaMapper faixaMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    @Test
    @WithMockUser
    void listar_deveRetornar200() throws Exception {
        Faixa faixa = new Faixa(1, "Intro", 120, false);

        when(faixaService.listarPorAlbum(1L)).thenReturn(List.of(faixa));
        when(faixaMapper.toResponse(faixa)).thenReturn(new FaixaResponse(10L, 1L, 1, "Intro", 120, false, null, null));

        mvc.perform(get("/api/v1/albuns/1/faixas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Intro"));
    }
}
