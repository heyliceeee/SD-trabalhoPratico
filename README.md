# Sistema de Comunicação Distribuído

Este projeto implementa um sistema de comunicação distribuído em **Java** utilizando **Sockets**, **TCP** e **criptografia RSA** para comunicação segura entre clientes e servidor.

## Como correr o projeto
1. Compila e executa `Main.java` para iniciar o servidor.  
2. Compila e executa `ChatClient.java` para iniciar os clientes.  
   - **Nota:** Podes iniciar várias instâncias de `ChatClient.java`, uma para cada utilizador que fará login.  

---

## Comandos
| Comando                          | Descrição                                                                                 | Exemplo                                                                           |
|----------------------------------|------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| **SEND** [email] [mensagem]      | Envia mensagens privadas para outro utilizador.                                          | `SEND coord_local1@protecao.gov.pt Boa Tarde Coordenador Local, como se encontra?` |
| **JOIN** [grupo]                 | Junta (ou cria) um grupo.                                                                | `JOIN Tempestade-Kirk`                                                            |
| **LEAVE** [grupo]                | Sai de um grupo.                                                                         | `LEAVE Tempestade-Kirk`                                                           |
| **GROUPMSG** [grupo] [mensagem]  | Envia mensagens para um grupo.                                                           | `GROUPMSG Tempestade-Kirk Bom dia.`                                               |
| **APPROVE** EVACUATE             | Aprova pedidos de evacuação (*somente* para utilizadores com role `HIGH`).               | `APPROVE EVACUATE`                                                                |
| **APPROVE** ACTIVATE             | Aprova pedidos de ativação (*roles* `HIGH` e `MEDIUM`).                                  | `APPROVE ACTIVATE`                                                                |
| **APPROVE** DISTRIBUTE           | Aprova pedidos de distribuição (*roles* `HIGH`, `MEDIUM` e `LOW`).                       | `APPROVE DISTRIBUTE`                                                              |

---

## Nota Importante  
Para que a funcionalidade de **guardar mensagens em grupo** funcione corretamente para utilizadores **offline**:
1. Mantém **`Main.java`** sempre em execução.
2. Inicia várias instâncias de **`ChatClient.java`**, uma para cada utilizador.
3. Se um cliente fizer login e depois encerrar a instância, as mensagens destinadas a ele serão entregues quando ele se reconectar.

---

## Login

| Email                             | Password | Role    |
|-----------------------------------|----------|---------|
| coord_regional@protecao.gov.pt    | senha123 | HIGH    |
| coord_local1@protecao.gov.pt      | senha456 | MEDIUM  |
| operador1@protecao.gov.pt         | senha321 | LOW     |
| agente_emergencia2@protecao.gov.pt| senha222 | REGULAR |

---

## Grupos Default por Role
| Role    | Grupos                                                           |
|---------|------------------------------------------------------------------|
| **HIGH**    | GRUPO-HIGH, GRUPO-MEDIUM, GRUPO-LOW, GRUPO-REGULAR, GRUPO-GERAL |
| **MEDIUM**  | GRUPO-MEDIUM, GRUPO-LOW, GRUPO-REGULAR, GRUPO-GERAL             |
| **LOW**     | GRUPO-LOW, GRUPO-REGULAR, GRUPO-GERAL                           |
| **REGULAR** | GRUPO-REGULAR, GRUPO-GERAL                                      |

---

## Tecnologias Utilizadas
- **Java** (JDK 17+)
- **Sockets TCP/UDP**
- **Criptografia RSA**
- **Multithreading**

---

## Estrutura do Projeto
- **`Main.java`** → Servidor principal que gerencia as conexões.
- **`ChatClient.java`** → Cliente que permite login e envio de comandos.
- **`GroupService.java`** → Gerenciamento de grupos.
- **`MessageService.java`** → Envio e armazenamento de mensagens.
- **`EncryptionUtil.java` / `DecryptionUtil.java`** → Criptografia RSA.
- **Ficheiros de dados:**
  - `user_groups.txt` → Grupos associados a utilizadores.
  - `system.log` → Logs de sistema.
  - `system_reports.log` → Relatórios periódicos.
  - `individual_messages.txt` → Mensagens privadas offline.
  - `group_messages.txt` → Mensagens de grupo offline.
  - `public.key` e `private.key` → Chaves RSA.
