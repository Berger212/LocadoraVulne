# Locadora de Video Games - API Segura vs Vulnerável

Aplicação para fins acadêmicos em Java com Spring Boot que expõe uma API REST para gerenciar usuários, clientes, jogos e locações de uma locadora de video games. 

## Arquitetura

- **Ferramentas**: Java, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, H2 (memória).
- **Camadas**: `controller` (exposição REST) → `service` (regras) → `repository` (JPA) → banco H2.
- **DTOs**: separados para requests/responses, evitando expor entidades diretamente.
- **Segurança**: filtro próprio (`TokenAuthenticationFilter`) + `TokenService` ciente do modo; `AppSecurityProperties` centraliza `app.security.mode`.
- **Utilidades**: `InputSanitizer` (JSoup) e `DataProtectionService` (AES) mostram contraste entre dados sanitizados/cifrados x sem proteção.

## Modelagem

| Entidade | Campos principais | 
| --- | --- | 
| `Usuario` | `username`, `senha`, `role` (`ADMIN`/`ATENDENTE`) | 
| `Cliente` | `nome`, `email`, `documento`, `telefone` |
| `Jogo` | `titulo`, `genero`, `precoDiaria`, `disponivel` | 
| `Locacao` | `cliente`, `jogo`, `dataLocacao`, `valorTotal`, `status` | 

## Endpoints

| Método | Caminho | Função |
| --- | --- | --- |
| POST | `/usuarios` | Cria usuários com perfis. |
| POST | `/login` | Autentica e emite token. |
| GET | `/jogos` | Lista jogos. |
| POST | `/jogos` | Cadastra jogo. |
| PUT | `/jogos/{id}` | Atualiza jogo. |
| DELETE | `/jogos/{id}` | Remove jogo. |
| GET | `/clientes` | Lista clientes. |
| POST | `/clientes` | Cadastra cliente . |
| POST | `/locacoes` | Registra locação, bloqueando disponibilidade. |
| GET | `/locacoes` | Lista locações. |
| PUT | `/locacoes/{id}/devolucao` | Registra devolução e calcula multa. |

### Fluxo geral

1. Usuário cadastrado (`POST /usuarios`).
2. Login (`POST /login`) retorna token (Seguro: UUID+expiração / Inseguro: `username::ROLE`).
3. Token enviado via `Authorization: Bearer <token>`. É possível burlar com `X-Bypass-Auth`.
4. CRUD de jogos, clientes e locações seguem validações do serviço.

## Vulnerabilidades

| # | Vulnerabilidade | Onde/Código | Impacto | Exploração | 
|---|-----------------|-------------|---------|------------|
|1|SQL Injection em login|`AuthService.login` usa concatenação de SQL|Bypass de autenticação|`username=admin' OR '1'='1`|
|2|Cadastro sem sanitização (XSS)|`UsuarioService.criarUsuario` não usa `InputSanitizer`|Armazena `<script>`|Enviar `<script>alert(1)</script>` no nome|
|3|Senha em texto puro|`SecurityConfig.passwordEncoder` retorna `NoOpPasswordEncoder`|Vazamento total do banco|Dump da tabela `usuarios`|
|4|Token sem expiração|`TokenService.emitToken` retorna `username::ROLE`|Session hijacking|Reuso infinito do token|
|5|Controle de acesso fraco|`JogoService.remover` não verifica role|Usuário comum deleta jogos|`DELETE /jogos/1` como atendente|
|6|Endpoint sem validação|`JogoService.validarJogo` nãoestá sendo executado.|Persistência de dados maliciosos|JSON com `<img onerror>`|
|7|Stack trace exposto|`GlobalExceptionHandler` adiciona `stackTrace`|Divulga detalhes internos|Qualquer erro revela classe/linha|
|8|Dados sensíveis sem criptografia|`DataProtectionService.protect` retorna claro|Documento do cliente vazado|Dump tabela `clientes`|
|9|Falha na validação de ID|`JogoService.atualizar` aceita `new Jogo()`|Manipulação de registros|`PUT /jogos/9999` cria objeto órfão|
|10|Dependência vulnerável|`pom.xml` inclui `commons-collections:3.2.1`|Passível de RCE conhecido|Ataques via gadget chains|

## Como executar

```bash
# Requisitos: JDK 17+ e Maven
backend (API)
./mvnw spring-boot:run ou mvn spring-boot:run

frontend (Aplicação Web)
npm run dev
```

A aplicação sobe em `http://localhost:8080`.

## Interface Web

A fim de alguns testes foi criada uma interface web em `src/main/resources/static/index.html` onde é gerado formulários para todos os endpoints.

1. Acesse `http://localhost:8080/` após subir o Spring Boot.
2. Na seção **Autenticação & Segurança**:
3. Nas seções **Jogos**, **Clientes** e **Locações** há formulários para POST/PUT/DELETE e botões de listagem que renderizam o JSON retornado em `<pre>` ao lado.

Os arquivos de suporte estão em `src/main/resources/static/assets/styles.css` (estilos) e `.../assets/app.js` (fetch + integração com a API).

### Exemplos de requisições (cURL)

```bash
# Criar usuário ADMIN 
curl -X POST http://localhost:8080/usuarios   -H 'Content-Type: application/json'   -d '{"username":"admin","senha":"SenhaForte123","nome":"Administrador","role":"ADMIN","email":"admin@locadora.dev"}'

# Login
TOKEN=$(curl -s -X POST http://localhost:8080/login   -H 'Content-Type: application/json'   -d '{"username":"admin","senha":"SenhaForte123"}')

# Criar jogo
curl -X POST http://localhost:8080/jogos   -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json'   -d '{"titulo":"Zelda","genero":"Aventura","precoDiaria":12.5,"descricao":"Clássico"}'
```

## Execução de testes de segurança

Endpoints Vulneráveis :
- SQL Injection detectada em `AuthService`.
- Uso de `NoOpPasswordEncoder` e armazenamento de senha em texto.
- Dependência vulnerável (SCA) `commons-collections:3.2.1`.
- `/login`: SQL Injection.
- `/usuarios`: XSS persistente.
- `/jogos` (POST/PUT): falta de validação.
- `/jogos/{id}` (DELETE): acesso sem autorização.
- `/clientes`: dados sensíveis sem criptografia (respostas mostram documento claro).
