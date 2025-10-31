# SmartLocation – Sprint 4 Mottu

## Visão geral do problema e solução
A Mottu enfrenta um desafio logístico recorrente: a baixa visibilidade sobre onde as motos estão estacionadas dentro dos pátios, o que provoca gargalos na operação diária (perda de tempo na localização, filas para retirada/entrega, baixa rotatividade dos boxes e inconsistências no inventário de ativos). Como solução, desenvolvemos o SmartLocation, um sistema integrado que usa visão computacional para detectar e localizar motos automaticamente nas áreas de pátio e integra esses dados com uma aplicação web (Java/Spring) e um banco de dados relacional (Oracle).

Nesta entrega final (Sprint 4), consolidamos:
- A detecção por visão computacional (YOLO) executada a partir do back-end Java, com limpeza automática de artefatos e publicação dos resultados.
- Uma aplicação web para gestão e análise (motos, detecções e indicadores) com rotas claras e UI simples.
- Integração com banco Oracle para persistência de motos e eventos (detecções, movimentações, posições), com atenção aos limites de sessão e à integridade referencial.

O resultado é uma visão unificada do pátio: o vídeo processado fica disponível para download e os gráficos/imagens de análise são exibidos diretamente na interface, apoiando a tomada de decisão e a melhoria do fluxo logístico.

## Tecnologias utilizadas
- Back-end
  - Java 17
  - Spring Boot 3.x (Web, Data JPA, DevTools)
  - Hibernate ORM 6.x
  - HikariCP (pool de conexões; tuning para respeitar limites de sessão do Oracle)
  - Maven Wrapper (mvnw) para build e execução
- Banco de dados
  - Oracle Database (schema acadêmico da disciplina)
  - Scripts SQL em `database/` para tabelas e relacionamentos
- Front-end
  - Thymeleaf (templates em `src/main/resources/templates`)
  - CSS simples em `static/css/`
  - JavaScript leve para atualização de indicadores em tempo real
- Visão computacional (pasta `visao_computacional/`)
  - Notebook Jupyter (`SmartLocation.ipynb`) e script Python
  - Detecção com modelo YOLO (Ultralytics), com apoio de bibliotecas comuns de visão computacional (ex.: OpenCV)
  - Organização de saídas em `visao_computacional/runs/track` e `visao_computacional/runs/analise_detalhada`

Observação: o notebook é disparado pelo serviço Java e executado sem depender de scripts temporários no disco; antes de cada execução, os diretórios de saída são limpos e recriados para manter o ambiente consistente.

## Como a aplicação funciona
### Fluxo de alto nível
1. O usuário acessa a aplicação web e inicia/visualiza uma análise.
2. O back-end Java aciona a execução do notebook Jupyter (via nbconvert) que realiza a detecção YOLO e produz:
   - Um vídeo com anotações (motos detectadas) disponível para download.
   - Imagens/gráficos de análise (por exemplo, mapa/heatmap/contagens) exibidos diretamente no front-end.
3. Os dados transacionais (motos, detecções, movimentações, posições) são persistidos no Oracle, permitindo CRUD completo e indicadores operacionais.
4. Indicadores são atualizados periodicamente no front-end por uma pequena chamada a API, sem manter conexões longas com o banco.

### Rotas principais (Web)
- `/` (Dashboard)
  - Página inicial com um painel de indicadores (totais por status de moto, atualização recente etc.).
  - Badge de notificação na navegação (Motos) quando há mudanças relevantes.
- `/analise`
  - Página de análise: exibe a imagem/gráfico gerado e disponibiliza o vídeo processado para download.
  - A execução do notebook realiza limpeza de diretórios e salva as saídas em `visao_computacional/runs/...`.
- `/motos`
  - CRUD completo (criar, editar, atualizar status e deletar) diretamente na UI.
  - A exclusão trata corretamente dependências (detecções, movimentações, posições e outras tabelas relacionadas) para evitar erros de integridade.
- `/deteccoes`
  - Lista e consulta das detecções registradas pelo sistema.

### Rotas (API)
- `/api/indicadores` (GET)
  - Retorna contagens agregadas de motos por status e timestamp da última atualização.
  - Usada pelo front-end para atualizar o painel e o badge periodicamente (sem WebSocket, reduzindo consumo de sessões no Oracle).

### Detecção YOLO e publicação de resultados
- A detecção (YOLO) roda dentro do pipeline do notebook. O Java dispara a execução e, ao finalizar, varre as pastas de saídas para identificar:
  - O caminho do vídeo anotado (disponível apenas para download na UI);
  - O caminho da imagem/gráfico de análise (exibida inline na página de análise).
- Um mapeamento de recursos estáticos expõe as pastas de `visao_computacional/` sob a rota `/videos/**`, permitindo baixar o vídeo diretamente pelo navegador.
- Antes de cada execução, diretórios como `runs/track` e `runs/analise_detalhada` são limpos e recriados para garantir resultados consistentes e evitar acúmulo de artefatos.

### Banco de dados e integridade
- O acesso ao Oracle utiliza um pool de conexões com tamanho controlado (HikariCP) para respeitar limites de sessão.
- Operações de exclusão em motos realizam, programaticamente, a remoção ordenada de dependentes (incluindo tabelas externas ao JPA quando necessário), evitando erros como ORA-02292.
- As entidades e repositórios JPA oferecem consultas derivadas e contagens por status para compor indicadores e telas.

## Conclusão
O SmartLocation unifica visão computacional, back-end web e persistência de dados para oferecer visibilidade operacional nos pátios da Mottu. Com a detecção automática de motos, a limpeza e publicação de resultados, e uma UI focada em indicadores e CRUDs essenciais, o sistema endereça os principais gargalos de localização e gestão de ativos, trazendo agilidade e confiabilidade à operação.


## Estrutura do projeto
Principais pastas e arquivos para se orientar:

- `database/`
  - `2TDSA_2025_CodigoSql_Integrates.sql`: script SQL com DDL/DML utilizados no Oracle (tabelas, relacionamentos e dados base para os testes).

- `Java/` (aplicação web – Spring Boot)
  - `pom.xml`: dependências, plugins e configuração do Maven.
  - `mvnw.cmd` / `mvnw`: wrappers do Maven para rodar sem instalar globalmente.
  - `src/main/java/br/com/fiap/smartlocation/`
    - `SmartLocationApplication.java`: classe principal (boot da aplicação).
    - `controller/`
      - `api/`: controladores REST (ex.: `IndicadoresRestController`).
      - `web/`: controladores MVC (rotas de páginas – dashboard, análise, motos, detecções).
    - `dto/`: objetos de transferência de dados (ex.: `AnaliseResultadoDTO`, `DeteccaoDTO`, `IndicadoresDTO`).
    - `model/`: entidades JPA (ex.: `Moto`, `DeteccaoMoto`, `Movimentacao`, `Posicao`, `Patio`).
    - `repository/`: repositórios Spring Data JPA (consultas e operações em banco).
    - `service/`: regras de negócio e integrações (ex.: `PythonIntegrationService`, `MotoService`).
  - `src/main/resources/`
    - `application.properties` e `application-dev.properties`: configurações (Oracle, pool de conexões, caminho do notebook etc.).
    - `static/`: arquivos estáticos (CSS/JS). O mapeamento também expõe outputs da visão computacional sob `/videos/**`.
    - `templates/`: páginas Thymeleaf (`index.html`, `analise.html`, `motos.html`, `deteccoes.html`).
  - `test-setup.bat` / `test-setup.sh`: utilitários de ambiente usados durante o desenvolvimento.

- `visao_computacional/` (notebook e artefatos da visão computacional)
  - `SmartLocation.ipynb`: notebook que executa a detecção (YOLO) e gera os resultados.
  - `smartlocation_script.py`: script auxiliar de visão computacional (quando necessário).
  - `runs/track/` e `runs/analise_detalhada/`: saídas geradas a cada execução (vídeo anotado, imagens/gráficos).
  - `output/`, `video/`: outras saídas/insumos usados em testes e demonstrações.

- `target/` (gerado pelo build do Maven)
  - Artefatos compilados e empacotados (classes, recursos e JARs de saída).

## Como executar
### Pré-requisitos
- Windows com PowerShell.
- JDK 17 instalado (ou use o Maven Wrapper que baixa dependências automaticamente).
- Python 3.10+ com pacotes: Jupyter, nbconvert, Ultralytics (YOLO), OpenCV.
  - Exemplo de instalação (ambiente local Python):

```powershell
pip install jupyter nbconvert ultralytics opencv-python
```

### Configuração
1. Verifique as credenciais do Oracle em `Java/src/main/resources/application.properties`:
   - `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`.
   - O pool (HikariCP) está ajustado para respeitar limites de sessão do Oracle.
2. Aponte o caminho do notebook (se configurável) via propriedade como `python.notebook.path`, por exemplo:
   - `python.notebook.path=../visao_computacional/SmartLocation.ipynb`
3. Certifique-se de que o Python e os pacotes necessários estão acessíveis no PATH do sistema onde a aplicação Java será executada.

### Rodando a aplicação (Windows – PowerShell)
Na raiz do projeto, entre na pasta `Java` e use o Maven Wrapper:

```powershell
cd .\Java
.\mvnw.cmd -DskipTests spring-boot:run
```

Por padrão, a aplicação sobe na porta 8080. Se a porta estiver ocupada, rode em outra (ex.: 8081):

```powershell
.\mvnw.cmd -DskipTests spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

Depois de iniciar, acesse no navegador:
- Dashboard: http://localhost:8080/
- Análise: http://localhost:8080/analise
- Motos (CRUD): http://localhost:8080/motos
- Detecções: http://localhost:8080/deteccoes
- API (indicadores): http://localhost:8080/api/indicadores

Observações importantes:
- A página `/analise` exibe a imagem/gráfico gerado e disponibiliza o vídeo processado apenas para download.
- Os resultados ficam publicados sob `/videos/**`, mapeando os arquivos gerados em `visao_computacional/runs/...`.
- Antes de cada execução do notebook, os diretórios de saída são limpos e recriados automaticamente.
- Em ambientes com limite de sessões Oracle, o pool de conexões é mantido pequeno (fixo) para evitar estouro de sessões.

### Participantes
- Pedro Gomes – RM 553907
- Luiz Felipe Abreu – RM 555197
- Matheus Munuera – RM 557812

FIAP – Centro Universitário FIAP  
Curso – Análise e Desenvolvimento de Sistemas  
Semestre – 4/4