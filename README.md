# 🎵 API Music Management – Projeto Full Stack Sênior

## 📊 Informações do Candidato

* **Nome:** Carlos Henrique Teixeira Carneiro
* **E-mail:** [chtcarneiro@gmail.com](mailto:chtcarneiro@gmail.com)
* **Vaga:** Desenvolvedor Full Stack Sênior
* **Tecnologias:** Java, Spring Boot, React, PostgreSQL, MinIO

---

## 📌 Visão Geral do Projeto

Este projeto é uma **solução Full Stack** para gerenciamento de artistas, álbuns, faixas e gêneros, com **upload e gerenciamento de capas**. O foco está em **boas práticas, escalabilidade, segurança e clareza arquitetural**, atendendo aos critérios do edital do processo seletivo.

---

## 🏗️ Arquitetura

### Back-end

O back-end foi desenvolvido em **Spring Boot** com **arquitetura em camadas** (controller, service, repository e model). Aspectos transversais como **segurança (JWT, CORS e rate limit)**, **OpenAPI**, **infraestrutura Docker** e **armazenamento de arquivos no MinIO (S3)** ficam isolados em módulos próprios. Também há **notificações em tempo real via WebSocket (STOMP)** para eventos de criação de álbuns e **integração com o endpoint externo de regionais**, com sincronização na inicialização e endpoint manual.

### Front-end

O front-end (React + TypeScript) consome a API REST de forma autenticada, com **renovação automática de JWT**, organização modular por domínio e integração **WebSocket** para exibir notificações de novos álbuns. A UI usa **PrimeReact/PrimeFlex** e o build é servido via **Nginx** dentro do `docker-compose`.

---

## 🧩 Tecnologias Utilizadas

### Back-end

* **Java 21 + Spring Boot 4.0.1**
* **Segurança:** JWT (Access + Refresh), CORS, Rate Limit
* **Banco:** PostgreSQL
* **Migração:** Flyway
* **Storage:** MinIO (S3)
* **Documentação:** OpenAPI / Swagger
* **Observabilidade:** Actuator
* **Mensageria:** WebSocket + STOMP
* **Containerização:** Docker e Docker Compose

### Front-end

* **React + TypeScript (Vite)**
* **PrimeReact + PrimeFlex (UI)**
* **RxJS (BehaviorSubject)**
* **STOMP Client (@stomp/stompjs)**

---

## 🗂️ Modelo de Domínio

* **Entidades principais:** Artista, Álbum, Faixa, Gênero
* **Capas de Álbum:** entidade AlbumCapa (1:N com Álbum)
* **Foto de Artista:** entidade ArtistaFoto (1:1 com Artista)
* **Regionais:** tabela Regional (com sincronização externa)
* **Autenticação:** tabela de Refresh Tokens

### Relacionamentos

* Artista → Álbum (1:N)
* Álbum → Faixa (1:N)
* Álbum ↔ Gênero (N:N)
* Álbum → AlbumCapa (1:N)
* Artista → ArtistaFoto (1:1)
* Artista ↔ Regional (N:N)

---

## 🧬 Migrações de Banco (Flyway)

* **V1__init.sql:** criação das tabelas base
* **V2__music_domain.sql:** evolução do domínio musical
* **V3__auth_refresh_token.sql:** persistência de refresh tokens
* **V4__album_capas.sql:** estrutura de capas de álbuns
* **V5__seed_data.sql:** carga inicial de dados
* **V6__regionais.sql:** estrutura da tabela de regionais
* **V7__artista_regionais.sql:** relação N:N artista ↔ regional
* **V8__artista_foto.sql:** foto do artista (1:1)

---

## 🔐 Segurança e Autenticação

* **JWT** com:
  * Access Token com expiração de 5 minutos
  * Refresh Token persistido em banco
* **CORS** configurado por allowlist
* **Rate limit:** 10 requisições por minuto por usuário

---

## 🔌 Endpoints da API (v1)

### Artistas

* GET `/api/v1/artistas?nome=&regionalId=&page=&size=&sort=`
* GET `/api/v1/artistas/{id}`
* POST `/api/v1/artistas`
* PUT `/api/v1/artistas/{id}`
* DELETE `/api/v1/artistas/{id}`
* POST `/api/v1/artistas/{artistaId}/foto`
* GET `/api/v1/artistas/{artistaId}/foto`
* DELETE `/api/v1/artistas/{artistaId}/foto`

### Gêneros

* GET `/api/v1/generos`
* GET `/api/v1/generos/{id}`
* POST `/api/v1/generos`
* PUT `/api/v1/generos/{id}`
* DELETE `/api/v1/generos/{id}`

### Álbuns

* GET `/api/v1/albuns?nome=&page=&size=&sort=`
* GET `/api/v1/albuns/artista?artistaId=&page=&size=&sort=`
* GET `/api/v1/albuns/{id}`
* POST `/api/v1/albuns`
* PUT `/api/v1/albuns/{id}`
* DELETE `/api/v1/albuns/{id}`

### Faixas

* GET `/api/v1/albuns/{albumId}/faixas`
* POST `/api/v1/albuns/{albumId}/faixas`
* PUT `/api/v1/albuns/{albumId}/faixas/{id}`
* DELETE `/api/v1/albuns/{albumId}/faixas/{id}`

### Capas de Álbum (MinIO)

* POST `/api/v1/albuns/{albumId}/capas`
* GET `/api/v1/albuns/{albumId}/capas`
* DELETE `/api/v1/albuns/{albumId}/capas/{capaId}`

### Autenticação

* POST `/api/v1/auth/login`
* POST `/api/v1/auth/refresh`
* POST `/api/v1/auth/logout`

### Regionais (integração externa)

* GET `/api/v1/regionais?ativo=`
* POST `/api/v1/regionais/sync`

---

## 🔔 WebSocket (Notificações)

* **Endpoint STOMP:** `/ws`
* **Tópico:** `/topic/albuns`
* **Evento:** notificação de novo álbum criado

---

## ✅ Testes

* **Testes unitários (Service):** JUnit 5 + Mockito
* **Testes de Controller:** WebMvcTest + MockMvc

```bash
mvn test
```

---

## 🚀 Execução com Docker

### Pré-requisitos

* Docker
* Docker Compose

### Subir a aplicação

```bash
docker compose up --build
```

### Serviços disponíveis

* API: `http://localhost:8083`
* Swagger UI: `http://localhost:8083/swagger`
* Health Check: `http://localhost:8083/actuator/health`
* MinIO Console: `http://localhost:9001`
* Front (Nginx): `http://localhost:5173`

### Configuração do Front-end

O front-end é buildado no container e utiliza a variável `VITE_API_BASE_URL` (arquivo `musicManagement/.env`) para apontar para a API. Caso altere a porta da API no `.env` raiz, ajuste também essa variável e refaça o build.

---

## 🧪 Dados Iniciais (Seed)

A migration de seed insere artistas e álbuns iniciais para testes manuais, facilitando a validação das funcionalidades principais.

---

## 📝 Observações Finais

Para este projeto foi usado as bibliotecas do Prime (PrimeReact, PrimeFlex e PrimeIcons) no lugar do Tailwind, por se tratar de uma biblioteca que dispõe de componentes pre estilizados com funcionalidades de UI já integradas, permitindo um foco maior na logica da aplicação.