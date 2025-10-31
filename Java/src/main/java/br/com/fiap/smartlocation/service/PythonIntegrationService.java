package br.com.fiap.smartlocation.service;

import br.com.fiap.smartlocation.dto.AnaliseResultadoDTO;
import br.com.fiap.smartlocation.dto.DeteccaoDTO;
import br.com.fiap.smartlocation.model.DeteccaoMoto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Serviço responsável por executar o notebook Python e processar os resultados
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PythonIntegrationService {

    @Value("${python.executable:python}")
    private String pythonExecutable;

    @Value("${python.notebook.path:../visao_computacional/SmartLocation.ipynb}")
    private String notebookPath;

    @Value("${python.output.path:../visao_computacional/output}")
    private String outputPath;

    private final DeteccaoMotoService deteccaoService;
    private final ObjectMapper objectMapper;

    private Process processoAtual;
    private boolean analiseEmExecucao = false;

    /**
     * Executa a análise do notebook Python de forma assíncrona
     */
    public CompletableFuture<AnaliseResultadoDTO> executarAnalise() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (analiseEmExecucao) {
                    throw new RuntimeException("Já existe uma análise em execução");
                }

                analiseEmExecucao = true;
                log.info("Iniciando análise Python...");

                // Limpar arquivos temporários antes de iniciar
                limparArquivosTemporarios();

                // Executar o notebook diretamente
                AnaliseResultadoDTO resultado = executarNotebook();

                log.info("Análise concluída com sucesso");
                analiseEmExecucao = false;

                return resultado;

            } catch (Exception e) {
                log.error("Erro ao executar análise Python", e);
                analiseEmExecucao = false;
                
                AnaliseResultadoDTO erro = new AnaliseResultadoDTO();
                erro.setStatus("ERRO");
                erro.setMensagem("Erro ao executar análise: " + e.getMessage());
                erro.setDataAnalise(LocalDateTime.now());
                return erro;
            }
        });
    }

    /**
     * Executa o notebook Jupyter diretamente usando jupyter nbconvert --execute
     */
    private AnaliseResultadoDTO executarNotebook() throws IOException, InterruptedException {
        log.info("Executando notebook: {}", notebookPath);
        
        Path notebookFile = Paths.get(notebookPath).toAbsolutePath();
        Path outputDir = notebookFile.getParent();

        if (!Files.exists(notebookFile)) {
            throw new FileNotFoundException("Notebook não encontrado: " + notebookFile);
        }

        // Comando: jupyter nbconvert --execute --to notebook --inplace SmartLocation.ipynb
        // Isso executa o notebook e salva os resultados no próprio arquivo
        ProcessBuilder pb = new ProcessBuilder(
            "jupyter", "nbconvert",
            "--execute",
            "--to", "notebook",
            "--inplace",
            "--ExecutePreprocessor.timeout=600",  // Timeout de 10 minutos
            notebookFile.getFileName().toString()
        );
        
        pb.directory(outputDir.toFile());
        pb.redirectErrorStream(true);

        processoAtual = pb.start();

        // Capturar saída em tempo real
        BufferedReader reader = new BufferedReader(new InputStreamReader(processoAtual.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        
        while ((line = reader.readLine()) != null) {
            log.info("Jupyter: {}", line);
            output.append(line).append("\n");
        }

        // Aguardar conclusão (timeout de 15 minutos)
        boolean finished = processoAtual.waitFor(15, TimeUnit.MINUTES);

        if (!finished) {
            processoAtual.destroy();
            throw new RuntimeException("Execução do notebook excedeu o tempo limite de 15 minutos");
        }

        int exitCode = processoAtual.exitValue();
        
        if (exitCode != 0) {
            log.error("Saída do processo: {}", output.toString());
            throw new RuntimeException("Notebook finalizou com erro. Exit code: " + exitCode);
        }

        log.info("Notebook executado com sucesso!");

        // Processar resultados
        return processarResultados();
    }

    /**
     * Processa os resultados da análise Python
     */
    private AnaliseResultadoDTO processarResultados() throws IOException {
        log.info("Processando resultados da análise...");

        AnaliseResultadoDTO resultado = new AnaliseResultadoDTO();
        resultado.setStatus("SUCESSO");
        resultado.setDataAnalise(LocalDateTime.now());

        // O notebook salva os resultados em runs/track dentro do diretório do notebook
        Path notebookDir = Paths.get(notebookPath).getParent().toAbsolutePath();
        Path outputDir = notebookDir.resolve("runs").resolve("track");
        
        if (!Files.exists(outputDir)) {
            log.warn("Diretório de saída não encontrado: {}, tentando diretório alternativo", outputDir);
            outputDir = Paths.get(outputPath).toAbsolutePath();
            
            if (!Files.exists(outputDir)) {
                log.warn("Diretório alternativo também não existe, usando diretório do notebook");
                outputDir = notebookDir;
            }
        }
        
        log.info("Procurando resultados em: {}", outputDir);

        // Buscar vídeo de saída
        Path videoPath = buscarArquivo(outputDir, "*.mp4", "*.avi", "*.mov");
        if (videoPath != null) {
            resultado.setCaminhoVideo(videoPath.toString());
            log.info("Vídeo encontrado: {}", videoPath);
        }

        // Buscar gráfico primeiro em runs/analise_detalhada, depois em outputDir
        Path analiseDir = notebookDir.resolve("runs").resolve("analise_detalhada");
        Path graficoPath = null;
        if (Files.exists(analiseDir)) {
            graficoPath = buscarArquivo(analiseDir, "*.png", "*.jpg", "grafico*.png");
        }
        if (graficoPath == null) {
            graficoPath = buscarArquivo(outputDir, "*.png", "*.jpg", "grafico*.png");
        }
        if (graficoPath != null) {
            resultado.setCaminhoGrafico(graficoPath.toString());
            log.info("Gráfico encontrado: {}", graficoPath);
        }

        // Buscar arquivo JSON com detecções
        Path jsonPath = buscarArquivo(outputDir, "deteccoes*.json", "resultado*.json");
        if (jsonPath != null) {
            resultado.setCaminhoLog(jsonPath.toString());
            
            // Ler e contar detecções
            List<DeteccaoDTO> deteccoes = lerDeteccoesDoJSON(jsonPath);
            resultado.setTotalDeteccoes(deteccoes.size());
            
            log.info("Total de detecções encontradas: {}", deteccoes.size());
        } else {
            resultado.setTotalDeteccoes(0);
        }

        resultado.setMensagem("Análise concluída com sucesso. " + resultado.getTotalDeteccoes() + " detecções encontradas.");

        return resultado;
    }

    /**
     * Busca um arquivo no diretório que corresponda aos padrões fornecidos
     */
    private Path buscarArquivo(Path diretorio, String... padroes) throws IOException {
        if (!Files.exists(diretorio)) {
            return null;
        }

        for (String padrao : padroes) {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + padrao);
            
            List<Path> arquivos = Files.walk(diretorio, 2)
                .filter(Files::isRegularFile)
                .filter(p -> matcher.matches(p.getFileName()))
                .sorted((p1, p2) -> {
                    try {
                        return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .collect(Collectors.toList());

            if (!arquivos.isEmpty()) {
                return arquivos.get(0);
            }
        }

        return null;
    }

    /**
     * Lê as detecções do arquivo JSON gerado pelo Python
     */
    private List<DeteccaoDTO> lerDeteccoesDoJSON(Path jsonPath) {
        try {
            String conteudo = Files.readString(jsonPath);
            
            // Tentar parsear como array de DeteccaoDTO
            DeteccaoDTO[] array = objectMapper.readValue(conteudo, DeteccaoDTO[].class);
            return List.of(array);
            
        } catch (Exception e) {
            log.error("Erro ao ler detecções do JSON", e);
            return new ArrayList<>();
        }
    }

    /**
     * Carrega as detecções do último resultado para exibição
     */
    public List<DeteccaoDTO> carregarDeteccoesPendentes() throws IOException {
        // Procurar primeiro em runs/track
        Path notebookDir = Paths.get(notebookPath).getParent().toAbsolutePath();
        Path outputDir = notebookDir.resolve("runs").resolve("track");
        
        if (!Files.exists(outputDir)) {
            log.warn("Diretório runs/track não encontrado, tentando output");
            outputDir = Paths.get(outputPath).toAbsolutePath();
            
            if (!Files.exists(outputDir)) {
                log.warn("Diretório output não encontrado, usando diretório do notebook");
                outputDir = notebookDir;
            }
        }

        Path jsonPath = buscarArquivo(outputDir, "deteccoes*.json", "resultado*.json");
        
        if (jsonPath != null) {
            return lerDeteccoesDoJSON(jsonPath);
        }

        return new ArrayList<>();
    }

    /**
     * Obtém o caminho relativo do vídeo gerado pela análise
     */
    public String obterCaminhoVideoGerado() {
        try {
            Path notebookDir = Paths.get(notebookPath).getParent().toAbsolutePath();
            Path outputDir = notebookDir.resolve("runs").resolve("track");

            // Tentar também diretório alternativo configurado (output)
            if (!Files.exists(outputDir)) {
                log.warn("Diretório runs/track não encontrado, tentando diretório alternativo de saída");
                outputDir = Paths.get(outputPath).toAbsolutePath();
            }
            
            Path videoPath = buscarArquivo(outputDir, "*.mp4", "*.avi", "*.mov");
            
            if (videoPath != null) {
                // Retornar caminho relativo a partir de visao_computacional
                Path relative = notebookDir.relativize(videoPath);
                return relative.toString().replace("\\", "/");
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Erro ao buscar vídeo gerado", e);
            return null;
        }
    }

    /**
     * Obtém o caminho relativo do gráfico (imagem) gerado pela análise
     */
    public String obterCaminhoGraficoGerado() {
        try {
            Path notebookDir = Paths.get(notebookPath).getParent().toAbsolutePath();
            // Priorizar runs/analise_detalhada
            Path analiseDir = notebookDir.resolve("runs").resolve("analise_detalhada");
            Path outputDir = notebookDir.resolve("runs").resolve("track");

            Path graficoPath = null;
            if (Files.exists(analiseDir)) {
                graficoPath = buscarArquivo(analiseDir, "*.png", "*.jpg", "grafico*.png");
            }
            if (graficoPath == null) {
                if (!Files.exists(outputDir)) {
                    log.warn("Diretório runs/track não encontrado, tentando diretório alternativo de saída");
                    outputDir = Paths.get(outputPath).toAbsolutePath();
                    if (!Files.exists(outputDir)) {
                        outputDir = notebookDir;
                    }
                }
                graficoPath = buscarArquivo(outputDir, "*.png", "*.jpg", "grafico*.png");
            }

            if (graficoPath != null) {
                Path relative = notebookDir.relativize(graficoPath);
                return relative.toString().replace("\\", "/");
            }

            return null;
        } catch (Exception e) {
            log.error("Erro ao buscar gráfico gerado", e);
            return null;
        }
    }

    /**
     * Limpa arquivos temporários antes de uma nova análise
     */
    private void limparArquivosTemporarios() throws IOException {
        log.info("Limpando arquivos temporários da execução anterior...");

        Path notebookDir = Paths.get(notebookPath).getParent().toAbsolutePath();

        // 1. Limpar pasta runs inteira (inclui runs/track e subpastas)
        Path runsDir = notebookDir.resolve("runs");
        if (Files.exists(runsDir)) {
            log.info("Removendo pasta runs: {}", runsDir);
            deletarDiretorioRecursivo(runsDir);
        }
        // Recriar estrutura esperada para saídas
        try {
            Files.createDirectories(runsDir.resolve("track"));
            Files.createDirectories(runsDir.resolve("analise_detalhada"));
            log.info("Recriada estrutura de saídas: {}/track e {}/analise_detalhada", runsDir, runsDir);
        } catch (IOException e) {
            log.warn("Não foi possível recriar diretórios de saída em {}: {}", runsDir, e.getMessage());
        }
        
        // 2. Remover arquivo yolov8m.pt (modelo baixado)
        Path modelPath = notebookDir.resolve("yolov8m.pt");
        if (Files.exists(modelPath)) {
            try {
                Files.delete(modelPath);
                log.info("Modelo YOLOv8 removido: {}", modelPath);
            } catch (IOException e) {
                log.warn("Não foi possível remover modelo: {}", modelPath, e);
            }
        }
        
        // 3. Remover CSV de detecções
        Path csvPath = notebookDir.resolve("deteccoes_motos_completo.csv");
        if (Files.exists(csvPath)) {
            try {
                Files.delete(csvPath);
                log.info("CSV de detecções removido: {}", csvPath);
            } catch (IOException e) {
                log.warn("Não foi possível remover CSV: {}", csvPath, e);
            }
        }
        
        // 4. Limpar saídas do notebook (executar comando jupyter nbconvert --clear-output)
        try {
            log.info("Limpando saídas do notebook...");
            ProcessBuilder pb = new ProcessBuilder(
                "jupyter", "nbconvert",
                "--clear-output",
                "--inplace",
                Paths.get(notebookPath).getFileName().toString()
            );
            pb.directory(notebookDir.toFile());
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            
            if (finished && process.exitValue() == 0) {
                log.info("Saídas do notebook limpas com sucesso");
            } else {
                log.warn("Não foi possível limpar saídas do notebook automaticamente");
            }
        } catch (Exception e) {
            log.warn("Erro ao limpar saídas do notebook: {}", e.getMessage());
        }

        log.info("Limpeza de arquivos temporários concluída!");
    }
    
    /**
     * Deleta um diretório e todo seu conteúdo recursivamente
     */
    private void deletarDiretorioRecursivo(Path diretorio) throws IOException {
        if (!Files.exists(diretorio)) {
            return;
        }
        
        Files.walk(diretorio)
                .sorted((p1, p2) -> -p1.compareTo(p2)) // Ordem reversa para deletar arquivos antes das pastas
            .forEach(path -> {
                try {
                    Files.delete(path);
                    log.debug("Removido: {}", path);
                } catch (IOException e) {
                    log.warn("Não foi possível remover: {}", path, e);
                }
            });
    }

    /**
     * Verifica se há uma análise em execução
     */
    public boolean isAnaliseEmExecucao() {
        return analiseEmExecucao;
    }

    /**
     * Cancela a análise em execução
     */
    public void cancelarAnalise() {
        if (processoAtual != null && processoAtual.isAlive()) {
            log.warn("Cancelando análise em execução...");
            processoAtual.destroy();
            analiseEmExecucao = false;
        }
    }
}
