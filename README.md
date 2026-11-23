# Oficina Mecânica — API de manutenção

## Problema e solução

Oficinas que registram serviços em papel ou mensagens têm dificuldade para consultar o histórico de cada veículo, acompanhar o andamento e calcular o orçamento. Esta API centraliza clientes, veículos e ordens de serviço, preservando o histórico das manutenções concluídas. O escopo é exclusivamente backend.

## Tecnologias e execução

Java 21, Spring Boot 4.1.0, Maven Wrapper, Spring Data JPA, Bean Validation, PostgreSQL e Swagger/OpenAPI (springdoc 3.1.0). H2 é usado apenas nos testes automatizados.

1. Inicie o PostgreSQL e crie um banco separado: `CREATE DATABASE oficina;`.
2. Abra esta pasta no IntelliJ como projeto Maven, ou use o PowerShell.
3. As configurações locais padrão são banco `oficina`, usuário `postgres`, senha `postgres`, porta 5432. Ajuste via variáveis de ambiente, se necessário:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/oficina"
$env:DB_USER="postgres"
$env:DB_PASSWORD="sua-senha"
.\mvnw.cmd spring-boot:run
```

A API usa a porta **8081**, ajustável por `PORT`. Não é necessário instalar Maven separadamente. No IntelliJ, execute `OficinaApplication`.

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs
- Base da API: http://localhost:8081/api
- Testes: `.\mvnw.cmd test`
- Gerar JAR: `.\mvnw.cmd package`
- Rodar JAR: `java -jar target/oficina-api-1.0.0.jar`

As tabelas são criadas/atualizadas pelo Hibernate. Os dados do PostgreSQL persistem após reiniciar. Os testes usam banco H2 isolado e não alteram os dados locais.

## Organização

- controller: endpoints e códigos HTTP.
- service: regras de negócio e transações.
- repository: consultas e persistência JPA.
- model: entidades Cliente, Veiculo, OrdemServico e enum StatusOrdem.
- dto: contratos JSON de entrada e saída.
- exception: respostas de erro padronizadas.
- config: documentação OpenAPI com exemplos.

## Entidades e relacionamentos

Cliente (id, nome, cpf, email, telefone) **1 → N** Veiculo (id, placa, marca, modelo, ano, clienteId) **1 → N** OrdemServico (id, veiculoId, descricaoProblema, servicoRealizado, quilometragem, valorPecas, valorMaoObra, valorTotal, status, dataAbertura, dataConclusao).

IDs são gerados pelo banco. As chaves estrangeiras são obrigatórias. Não existe exclusão em cascata. Email e placa são únicos; email é normalizado para minúsculas e placa para maiúsculas.

## Regras de negócio

1. Um veículo só pode ter uma ordem ABERTA ou EM_ANDAMENTO. A criação bloqueia a linha do veículo durante a transação, inclusive para requisições simultâneas.
2. O total é calculado pelo servidor: valorPecas + valorMaoObra. Valores negativos ou mais de duas casas decimais são rejeitados.
3. Fluxo: ABERTA → EM_ANDAMENTO → CONCLUIDA. ABERTA e EM_ANDAMENTO também podem ir para CANCELADA. Para concluir, é obrigatório informar servicoRealizado.
4. Ordens CONCLUIDAS ou CANCELADAS não podem ser editadas ou reabertas.
5. Apenas ordens CANCELADAS podem ser excluídas. Ordens concluídas permanecem no histórico.
6. Clientes com veículos e veículos com ordens não podem ser excluídos. Veículo com histórico não pode trocar de cliente. Não é permitido trocar o veículo de uma ordem.

## Endpoints (16 operações)

| Método | Rota | Finalidade |
|---|---|---|
| POST | /api/clientes | Cadastrar cliente |
| GET | /api/clientes | Listar clientes |
| GET | /api/clientes/{id} | Consultar cliente |
| PUT | /api/clientes/{id} | Atualizar cliente |
| DELETE | /api/clientes/{id} | Excluir cliente sem veículos |
| POST | /api/veiculos | Cadastrar veículo de um cliente |
| GET | /api/veiculos | Listar; filtro opcional clienteId |
| GET | /api/veiculos/{id} | Consultar veículo |
| PUT | /api/veiculos/{id} | Atualizar veículo |
| DELETE | /api/veiculos/{id} | Excluir veículo sem ordens |
| POST | /api/ordens-servico | Abrir manutenção |
| GET | /api/ordens-servico | Listar; filtros opcionais veiculoId e status |
| GET | /api/ordens-servico/{id} | Consultar ordem |
| PUT | /api/ordens-servico/{id} | Atualizar descrição, custos e serviço |
| PUT | /api/ordens-servico/{id}/status | Iniciar, concluir ou cancelar |
| DELETE | /api/ordens-servico/{id} | Excluir ordem cancelada |

Filtros de exemplo: `/api/veiculos?clienteId=1`, `/api/ordens-servico?veiculoId=1&status=CONCLUIDA`.
Listas vazias retornam 200 com []. Relações inexistentes retornam 404.
PUT substitui os campos editáveis: envie o corpo completo. Não envie id, valorTotal, dataAbertura, dataConclusao ou status no corpo de cadastro/edição da ordem. Status tem rota própria.

## Exemplo de uso (Swagger ou Postman)

Use Content-Type: application/json. Substitua os IDs abaixo pelos retornados em cada cadastro.

### 1. POST /api/clientes

```json
{"nome":"Ana Souza","cpf":"52998224725","email":"ana@example.com","telefone":"11987654321"}
```

Retorno 201 com id, nome, cpf, email e telefone. O cabeçalho Location informa a rota de consulta.

### 2. POST /api/veiculos

```json
{"placa":"ABC1D23","marca":"Fiat","modelo":"Uno","ano":2018,"clienteId":1}
```

Retorno 201 com id e os campos cadastrados. Placa sem hífen: formato antigo ABC1234 ou Mercosul ABC1D23.

### 3. POST /api/ordens-servico

```json
{"veiculoId":1,"descricaoProblema":"Ruído nos freios","quilometragem":45000,"valorPecas":150.00,"valorMaoObra":200.00}
```

Exemplo de resposta 201:

```json
{"id":1,"veiculoId":1,"descricaoProblema":"Ruído nos freios","servicoRealizado":null,"quilometragem":45000,"valorPecas":150.00,"valorMaoObra":200.00,"valorTotal":350.00,"status":"ABERTA","dataAbertura":"2026-11-22T10:00:00","dataConclusao":null}
```

### 4. PUT /api/ordens-servico/1/status

Para iniciar:

```json
{"status":"EM_ANDAMENTO"}
```

Para concluir:

```json
{"status":"CONCLUIDA","servicoRealizado":"Troca das pastilhas e teste dos freios"}
```

Retorno 200 com a ordem e status atualizado. dataConclusao é preenchida ao concluir.
Para testar DELETE, crie outra ordem, cancele com `{"status":"CANCELADA"}` e exclua pelo ID dessa nova ordem.

## Validações e retornos

Nomes, marca, modelo e descrição não podem estar vazios. Email válido, telefone de 10 ou 11 dígitos, ano entre 1886 e 2100, quilometragem não negativa, IDs de vínculo positivos. Custos obrigatórios, não negativos e até 10 dígitos inteiros e 2 decimais. Datas usam o horário local do servidor; valores em reais.

- 200: consulta/atualização concluída.
- 201: cadastro criado.
- 204: exclusão concluída, sem corpo.
- 400: campos inválidos, JSON inválido ou enum desconhecido.
- 404: registro ou vínculo inexistente.
- 409: violação de regra, duplicidade ou vínculo que impede exclusão.

Exemplo de erro 409:

```json
{"data":"2026-11-22T10:00:00","status":409,"mensagem":"Veiculo ja possui uma ordem aberta ou em andamento","caminho":"/api/ordens-servico","campos":{}}
```

Erros de validação também incluem os campos e respectivas mensagens em `campos`.

## Conferência dos requisitos

Três entidades relacionadas; CRUD de clientes, veículos e ordens (com restrições de negócio); 16 operações HTTP; JSON; validações; regras de manutenção e cálculo; PostgreSQL com chaves estrangeiras e unicidade; Swagger com parâmetros, schemas, exemplos e retornos.

Escopo acadêmico/local, sem frontend e sem autenticação. Para publicação real, será necessário adicionar autenticação, permissões, configuração de segredos e migrações versionadas. O springdoc usado é compatível com Spring Boot 4.1 conforme a [documentação oficial](https://springdoc.org/index.html).

## Verificacao realizada

Os quatro testes de integracao JUnit passaram. Com a API conectada ao PostgreSQL, o roteiro HTTP executou 34 verificacoes, incluindo os 16 endpoints, concorrencia, validacao, calculo, filtros e integridade. Os registros temporarios desse roteiro foram excluidos.

Para repetir a verificacao local, com a API ligada e Node.js 18+ instalado:

```powershell
node scripts/test-api.mjs
```

A especificacao OpenAPI exportada esta em docs/openapi.json; tambem pode ser importada no Postman. A documentacao ao vivo em /v3/api-docs acompanha o codigo atual.

## CPF do cliente

CPF e obrigatorio no POST e no PUT de clientes. Envie 11 digitos, sem pontos ou hifen. O servidor valida os digitos verificadores e rejeita CPF duplicado com HTTP 409. CPF ausente, vazio ou invalido retorna HTTP 400.

Cadastros anteriores a inclusao do campo permanecem com cpf nulo ate serem atualizados com o CPF correto; nenhum CPF foi inventado para esses registros. A coluna permite nulos apenas para preservar esse legado; novas gravacoes pela API exigem CPF.
