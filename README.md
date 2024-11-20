# Como correr o projeto
1. run main.java
2. run chatClient.java
   
# Login

| email                              | password | role    |
|------------------------------------|----------|---------|
| coord_regional@protecao.gov.pt     | senha123 | HIGH    |
| coord_local1@protecao.gov.pt       | senha456 | MEDIUM  |
| operador1@protecao.gov.pt          | senha321 | LOW     |
| agente_emergencia2@protecao.gov.pt | senha222 | REGULAR |


# Grupos default que cada role pertence
| role    | grupo        |
|---------|--------------|
| HIGH    | GRUPO-HIGH, GRUPO-MEDIUM, GRUPO-LOW, GRUPO-REGULAR, GRUPO-GERAL |
| MEDIUM  | GRUPO-MEDIUM, GRUPO-LOW, GRUPO-REGULAR, GRUPO-GERAL |
| LOW     | GRUPO-LOW, GRUPO-REGULAR, GRUPO-GERAL |
| REGULAR | GRUPO-REGULAR, GRUPO-GERAL |
