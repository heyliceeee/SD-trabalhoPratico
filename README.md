## Como correr o projeto
1. run main.java
2. run chatClient.java

# Comandos
| comando                         |                                                                                           | exemplo                                                                          |
|---------------------------------|-------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| **SEND** [email] [messagem]     | enviar mensagens privadas                                                                 | SEND coord_local1@protecao.gov.pt Boa Tarde Coordenador Local como se encontra?  |
| **JOIN** [grupo]                | juntar (criar) grupo                                                                      | JOIN Tempestade-Kirk                                                             |
| **LEAVE** [grupo]               | sair grupo                                                                                | LEAVE Tempestade-Kirk                                                            |
| **GROUPMSG** [grupo] [messagem] | enviar mensagens em grupos                                                                | GROUPMSG Tempestade-Kirk Bom Dia.                                                |
| **APPROVE** EVACUATE            | aprovar (somente users com a role HIGH, excluindo quem pediu EVACUATE)                    |                                                                                  |
| **APPROVE** ACTIVATE            | aprovar (somente users com a role HIGH ou MEDIUM, excluindo quem pediu ACTIVATE)          |                                                                                  |
| **APPROVE** DISTRIBUTE          | aprovar (somente users com a role HIGH ou MEDIUM ou LOW, excluindo quem pediu DISTRIBUTE) |                                                                                  |

🛑Nota: Para que a funcionalidade de guardar mensagens de grupo funcione para os utilizadores que estiverem offline e as possam consultar mais tarde, é necessário executar o projeto da seguinte forma: iniciar várias instâncias de `ChatClient.java`, uma para cada utilizador que irá fazer login. Depois do login, podes encerrar uma instância, mas o `Main.java` deve permanecer sempre em execução.
   
# Login

| email                              | password | role    |
|------------------------------------|----------|---------|
| coord_regional@protecao.gov.pt     | senha123 | HIGH    |
| coord_local1@protecao.gov.pt       | senha456 | MEDIUM  |
| operador1@protecao.gov.pt          | senha321 | LOW     |
| agente_emergencia2@protecao.gov.pt | senha222 | REGULAR |


# Grupos default que cada role pertence
| role    | grupo                                                           |
|---------|-----------------------------------------------------------------|
| HIGH    | GRUPO-HIGH, GRUPO-MEDIUM, GRUPO-LOW, GRUPO-REGULAR, GRUPO-GERAL |
| MEDIUM  | GRUPO-MEDIUM, GRUPO-LOW, GRUPO-REGULAR, GRUPO-GERAL             |
| LOW     | GRUPO-LOW, GRUPO-REGULAR, GRUPO-GERAL                           |
| REGULAR | GRUPO-REGULAR, GRUPO-GERAL                                      |
