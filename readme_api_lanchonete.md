# Sistema de Gestão de Pedidos - API REST

## Descrição e Objetivo
API REST desenvolvida para a disciplina de Desenvolvimento Backend com Spring Boot do Centro Universitário Santo Agostinho (UNI FSA). O sistema tem como objetivo gerir os clientes, os produtos e os pedidos de uma lanchonete, aplicando persistência de dados, organização em camadas (Controller, Service, Repository), tratamento centralizado de erros e regras de negócio complexas (como o cálculo automático do valor dos pedidos e o controlo de estados).

## Tecnologias Utilizadas
* **Java 17** (Utilização de Records para DTOs)
* **Spring Boot 3.3.5**
* **Spring Data JPA**
* **Hibernate Validator** (Validação de dados)
* **H2 Database** (Banco de dados em memória)
* **Springdoc OpenAPI** (Swagger UI)
* **Maven**

## Instruções para Configurar e Executar
1. Clone este repositório:
   ```bash
   git clone <LINK_DO_SEU_REPOSITORIO>
   ```
2. Aceda à pasta do projeto:
   ```bash
   cd lanchonete-api
   ```
3. Execute a aplicação usando o Maven Wrapper:
   * No Windows: `.\mvnw spring-boot:run`
   * No Linux/Mac: `./mvnw spring-boot:run`
4. A API estará disponível e a escutar na porta 8080: `http://localhost:8080`.

## Configuração do Banco de Dados
A aplicação utiliza o banco de dados em memória **H2**, que é reiniciado e estruturado automaticamente a cada execução (`spring.jpa.hibernate.ddl-auto=update`).
* **URL de Acesso ao Console:** `http://localhost:8080/h2-console`
* **JDBC URL:** `jdbc:h2:mem:lanchonete`
* **Username:** `sa`
* **Password:** *(deixar em branco)*

## Documentação Swagger
A documentação interativa da API, que permite consultar e testar todos os endpoints, parâmetros e códigos HTTP, está disponível em:
* **Endereço do Swagger UI:** `http://localhost:8080/swagger-ui.html`

## Exemplos de Utilização dos Principais Endpoints

### Clientes
* **POST /clientes**: Cadastra um novo cliente (Regra: não permite e-mails duplicados).
  ```json
  { "nome": "João Silva", "email": "joao@email.com", "telefone": "86999999999" }
  ```
* **GET /clientes?nome=joao**: Lista clientes filtrados por uma parte do nome (ignorando maiúsculas e minúsculas).
* **DELETE /clientes/{id}**: Remove um cliente (Regra: falha com 409 se o cliente já possuir pedidos).

### Produtos
* **POST /produtos**: Adiciona um novo produto ao cardápio.
  ```json
  { "nome": "X-Bacon", "preco": 25.50 }
  ```

### Pedidos
* **POST /pedidos**: Regista um novo pedido. O sistema calcula o `valorTotal` de forma automática com base no preço dos produtos no momento da criação.
  ```json
  {
    "clienteId": 1,
    "itens": [
      { "produtoId": 1, "quantidade": 2 }
    ]
  }
  ```
* **PUT /pedidos/{id}/status**: Altera o status do pedido respeitando uma máquina de estados estrita (`PENDENTE` -> `PREPARANDO` -> `ENTREGUE` ou `CANCELADO`).
  ```json
  { "status": "PREPARANDO" }
  ```
* **GET /pedidos?status=PENDENTE&dataInicio=2026-11-01**: Lista os pedidos com base em filtros combináveis.