# LHM Agent — Monitor de Hardware com RabbitMQ

Agente Java que coleta dados de hardware (CPU, GPU e memória) usando o **LibreHardwareMonitor** e os publica em uma fila
**RabbitMQ** para consumo remoto. Roda em segundo plano com ícone na *system tray* do Windows utilizando um pico de 70mb
de ram e 1% maximo de CPU.

---

## Visão geral

O agente é responsável por:

1. **Iniciar o LibreHardwareMonitor.exe** automaticamente como processo filho.
2. **Consumir o endpoint HTTP local** (`http://localhost:8085/data.json`) exposto pelo LHM via Feign + Jackson.
3. **Mapear dinamicamente** os JsonPointers dos sensores configurados (temperatura de CPU/GPU, carga de CPU e uso de
   memória) percorrendo a árvore JSON do LHM.
4. **Publicar leituras periódicas** (a cada 1 segundo) na fila `MinhaFila` do RabbitMQ (CloudAMQP).
5. **Exibir status** através de um ícone na *system tray* e logs coloridos no console.

A arquitetura é orientada a eventos, usando `SubmissionPublisher<AppEvent<?>>` do Java Flow API para desacoplar
produção, consumo, logging e visualização.

---

## Estrutura do projeto

```
PROJETINHO/
├── LibreHardwareMonitor/        # Binário do LibreHardwareMonitor + DLLs
│   └── LibreHardwareMonitor.exe
└── SISTEMA/                      # Aplicação Java (Maven)
    ├── pom.xml
    └── src/main/java/com/fernandoprado/lhmagent/
        ├── Main.java
        ├── Controller/
        │   ├── client/LhmClient.java                 # Cliente Feign do LHM
        │   ├── core/LhmProcessManager.java           # Inicia/encerra o processo do LHM
        │   ├── enviroment/EnvConfiguration.java      # Carrega .env (dotenv)
        │   ├── logger/                               # Logging com cores ANSI
        │   ├── messaging/                            # Conexão e envio ao RabbitMQ
        │   ├── model/                                # AppEvent, Sensor
        │   ├── service/                              # MainService, HardwareBusca, HardwareFinder
        │   └── threads/                              # MainThread + GetThread
        └── view/
            ├── TrayView.java                         # Ícone na system tray
            └── ViewPrint.java                        # Saída no console
```

---

## Pilha tecnológica

| Camada             | Tecnologia                                     |
|--------------------|------------------------------------------------|
| Linguagem          | Java 23                                        |
| Build              | Maven (`maven-assembly-plugin` para *fat-jar*) |
| HTTP Client        | OpenFeign 13.2.1 + feign-jackson               |
| JSON               | Jackson Databind 2.17.0                        |
| Mensageria         | RabbitMQ (`amqp-client` 5.20.0) — CloudAMQP    |
| Configuração       | dotenv-java 3.0.0                              |
| Logging            | Logback 1.5.3 + SLF4J                          |
| Acesso nativo      | JNA / JNA-Platform 5.14.0                      |
| TUI (lib.)         | Lanterna 3.1.5                                 |
| Coleta de hardware | LibreHardwareMonitor (binário externo)         |

---

## Requisitos

- **Java 23** (JDK)
- **Maven 3.6+**
- **Windows** (a *system tray* e o LibreHardwareMonitor.exe são dependências de plataforma)
- Acesso à internet para o broker CloudAMQP (ou um RabbitMQ local)

---

## Configuração (.env)

Crie um arquivo `.env` ao lado do `.jar` (ou na raiz, se executado pela IDE). Todas as chaves possuem valor padrão,
então o arquivo é **opcional**.

```env
# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_URI=amqps://usuario:senha@host.cloudamqp.com/vhost

# Identificação do agente
AGENT_ID=default-agent

# URL do LibreHardwareMonitor (HTTP server interno)
LHM_CLIENT_URL=http://localhost:8085

# Palavras-chave dos sensores no LHM (devem bater com o "Text" do JSON)
CPU_TEMP_KEYWORD=Core (Tctl/Tdie)
GPU_TEMP_KEYWORD=GPU Hot Spot
CPU_LOAD_KEYWORD=CPU Total
MEMORY_USAGE=Memory
```

> Os *keywords* mudam conforme o fabricante (Intel/AMD, NVIDIA/AMD GPU). Abra `http://localhost:8085/data.json` com o
> LHM rodando para descobrir o `Text` exato de cada sensor.

> **Importante:** o LHM precisa estar configurado para expor o servidor web na porta 8085. Em
`LibreHardwareMonitor.exe` → *Options* → *Remote Web Server* → *Run* (porta 8085).

---

## Como compilar e executar

### 1. Compilar o *fat-jar*

A partir da pasta `SISTEMA/`:

```bash
mvn clean package
```

Isso gera, em `SISTEMA/target/`:

- `SISTEMA-1.0-SNAPSHOT.jar` — sem dependências
- `SISTEMA-1.0-SNAPSHOT-jar-with-dependencies.jar` — *uber-jar* executável

### 2. Executar

O agente espera encontrar a pasta `LibreHardwareMonitor/` no diretório atual de execução, pois o `LhmProcessManager`
invoca:

```java
new ProcessBuilder("LibreHardwareMonitor/LibreHardwareMonitor.exe");
```

Layout recomendado de execução:

```
<pasta-de-execucao>/
├── LibreHardwareMonitor/
│   └── LibreHardwareMonitor.exe
├── SISTEMA-1.0-SNAPSHOT-jar-with-dependencies.jar
└── .env                  (opcional)
```

Rodar:

```bash
java -jar SISTEMA-1.0-SNAPSHOT-jar-with-dependencies.jar
```

---

## Fluxo interno

1. **`Main`** instancia `MainService` e chama `initProgram()`.
2. **`MainService`**:
    - Cria um `SubmissionPublisher<AppEvent<?>>` central.
    - Inicializa `ViewPrint`, `TrayView`, `MessagingService` e `LogService` como *consumers* do publisher.
    - Inicia o processo do LibreHardwareMonitor via `LhmProcessManager.startLhm()`.
    - Faz *polling* até o endpoint `/data.json` responder (com até 10 tentativas).
    - Repassa o JSON inicial para `MainThread.start(node)`.
3. **`HardwareBusca`** percorre a árvore JSON e gera um `Map<String, JsonPointer>` com o caminho para cada sensor
   configurado.
4. **`MainThread`** agenda execução a cada 1 s; cada tick dispara o `GetThread`, que:
    - Chama o LHM via Feign.
    - Lê valores atuais com `HardwareFinder.lerValoresAtuais(...)`.
    - Publica um `AppEvent` do tipo `UPDATE` com o `Map<String, String>` no publisher.
5. **`MessagingService`** consome eventos `UPDATE`, serializa em JSON com Jackson e publica em `MinhaFila` via
   `channel.basicPublish`.
6. **`TrayView`** atualiza o *tooltip* do ícone conforme eventos `OK`/`ERROR`.
7. **`LogService`** imprime logs coloridos no console (`ERROR`, `WARN`, `OK`) com timestamp e nome da classe.

---

## Tipos de evento (`AppEvent.EventType`)

| Tipo                                | Uso                                                  |
|-------------------------------------|------------------------------------------------------|
| `INIT`                              | Estado inicial do `GetThread`                        |
| `UPDATE`                            | Carrega payload `Map<String,String>` para o RabbitMQ |
| `OK` / `ERROR`                      | Estado de saúde para a `TrayView`                    |
| `LOG_OK` / `LOG_WARN` / `LOG_ERROR` | Eventos consumidos pelo `LogService`                 |
| `WARN`                              | Reservado                                            |

---

## Encerramento

O encerramento limpo é feito pelo menu *CLOSE* do ícone na *system tray*:

```java
RabbitMQProvider.closeConnection();
LhmProcessManager.

closeLhm();
System.

exit(0);
```

Também é acionado automaticamente caso o agente não consiga conectar ao LHM em até 10 tentativas.

---

## Troubleshooting

- **`ERRO AO CONECTAR AO LHM`**: verifique se o servidor web do LibreHardwareMonitor está habilitado na porta `8085`.
- **`FALHA AO DECLARAR CONEXAO COM RABBIT`**: valide a `RABBITMQ_URI` e a conectividade com o CloudAMQP.
- **Sensores não aparecem**: ajuste as `*_KEYWORD` no `.env` para casar exatamente com o campo `Text` do `data.json` do
  LHM.
- **`TRAY VIEW NAO SUPORTADO`**: ambiente sem suporte a *system tray* (servidor headless, alguns Linux sem libs). O
  agente continua funcionando, apenas sem o ícone.
- **Build falhando com versão de Java**: o `pom.xml` está fixado em `maven.compiler.source/target = 23`. Ajuste para a
  sua JDK ou instale a 23.

---

## Autor

**Fernando Prado** — `com.fernandoprado.lhmagent`
