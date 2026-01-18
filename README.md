## 📊 Informações do Candidato

- **Nome:** Carlos Henrique Teixeira Carneiro
- **E-mail:** chtcarneiro@gmail.com
- **Vaga:** Desenvolvedor Full Stack Sênior
- **Tecnologias:** Java, Spring Boot, React, PostgreSQL

---

# 🎵 API Music Management

API back-end desenvolvida em **Spring Boot 4.0.1** para gerenciamento de artistas e álbuns musicais.

No estado atual, o projeto contempla:
- Estrutura base da aplicação
- Configuração de segurança
- Documentação via Swagger
- Health checks
- Persistência preparada via PostgreSQL
- Execução containerizada com Docker

> ⚠️ As funcionalidades de domínio (artistas, álbuns, imagens, autenticação JWT, WebSocket, etc.) serão implementadas nas próximas etapas do desenvolvimento.

---

## 🏗️ Arquitetura (Estado Atual)

- **Back-end:** Spring Boot 4.0.1 (Java 21)
- **Segurança:** Spring Security (HTTP Basic temporário)
- **Banco de Dados:** PostgreSQL
- **Migração de Banco:** Flyway (configurado, sem migrations ainda)
- **Documentação:** OpenAPI / Swagger
- **Observabilidade:** Spring Boot Actuator
- **Containerização:** Docker + Docker Compose

---

## 🚀 Execução via Docker (Recomendado)

### Pré-requisitos
- Docker
- Docker Compose

### Subir a aplicação

```bash
# Clonar o repositório
git clone <https://github.com/CarlosHTC/SELETIVO_CONJUNTO-001-2026.git>
cd SELETIVO_CONJUNTO-001-2026

# Subir API + PostgreSQL
docker compose up --build

# Após a inicialização:

API: http://localhost:8083

Swagger: http://localhost:8083/swagger

Health Check: http://localhost:8083/actuator/health