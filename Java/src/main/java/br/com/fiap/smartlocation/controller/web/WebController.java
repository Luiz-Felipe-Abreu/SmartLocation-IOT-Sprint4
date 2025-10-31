package br.com.fiap.smartlocation.controller.web;

import br.com.fiap.smartlocation.dto.DeteccaoDTO;
import br.com.fiap.smartlocation.model.DeteccaoMoto;
import br.com.fiap.smartlocation.model.Moto;
import br.com.fiap.smartlocation.service.DeteccaoMotoService;
import br.com.fiap.smartlocation.service.MotoService;
import br.com.fiap.smartlocation.service.PythonIntegrationService;
import br.com.fiap.smartlocation.model.Patio;
import br.com.fiap.smartlocation.repository.PatioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller para páginas web do sistema
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final MotoService motoService;
    private final DeteccaoMotoService deteccaoService;
    private final PythonIntegrationService pythonService;
    private final PatioRepository patioRepository;

    /**
     * Página inicial - Dashboard
     */
    @GetMapping("/")
    public String index(Model model) {
        log.info("Acessando página inicial");
        List<DeteccaoMoto> deteccoes;
        List<Moto> motos;
        try {
            deteccoes = deteccaoService.listarTodas();
            motos = motoService.listarTodas();
            model.addAttribute("totalDeteccoes", deteccoes.size());
            model.addAttribute("totalMotos", motos.size());
        } catch (Exception e) {
            log.error("Falha ao carregar dados do banco na página inicial", e);
            model.addAttribute("erroDb", "Banco de dados indisponível no momento (ORA-02391). Tente novamente em alguns minutos.");
            deteccoes = List.of();
            motos = List.of();
            model.addAttribute("totalDeteccoes", 0);
            model.addAttribute("totalMotos", 0);
        }
        
        model.addAttribute("deteccoes", deteccoes);
        model.addAttribute("motos", motos);
        model.addAttribute("analiseEmExecucao", pythonService.isAnaliseEmExecucao());
        
        return "index";
    }

    /**
     * Página de análise
     */
    @GetMapping("/analise")
    public String analise(Model model) {
        log.info("Acessando página de análise");
        
        model.addAttribute("analiseEmExecucao", pythonService.isAnaliseEmExecucao());
        
        try {
            List<DeteccaoDTO> deteccoesPendentes = pythonService.carregarDeteccoesPendentes();
            model.addAttribute("deteccoesPendentes", deteccoesPendentes);
            model.addAttribute("temResultados", !deteccoesPendentes.isEmpty());
            
            // Buscar caminho do vídeo gerado
            String caminhoVideo = pythonService.obterCaminhoVideoGerado();
            if (caminhoVideo != null) {
                model.addAttribute("caminhoVideo", caminhoVideo);
                model.addAttribute("temVideo", true);
            } else {
                model.addAttribute("temVideo", false);
            }

            // Buscar caminho do gráfico/imagen gerado
            String caminhoGrafico = pythonService.obterCaminhoGraficoGerado();
            if (caminhoGrafico != null) {
                model.addAttribute("caminhoGrafico", caminhoGrafico);
                model.addAttribute("temGrafico", true);
            } else {
                model.addAttribute("temGrafico", false);
            }
        } catch (Exception e) {
            log.error("Erro ao carregar detecções pendentes", e);
            model.addAttribute("temResultados", false);
            model.addAttribute("temVideo", false);
            model.addAttribute("temGrafico", false);
        }
        
        return "analise";
    }

    /**
     * Iniciar análise Python
     */
    @PostMapping("/analise/iniciar")
    public String iniciarAnalise(RedirectAttributes redirectAttributes) {
        log.info("Iniciando análise via interface web");
        
        if (pythonService.isAnaliseEmExecucao()) {
            redirectAttributes.addFlashAttribute("erro", "Já existe uma análise em execução");
            return "redirect:/analise";
        }

        try {
            pythonService.executarAnalise();
            redirectAttributes.addFlashAttribute("sucesso", "Análise iniciada com sucesso! Aguarde...");
        } catch (Exception e) {
            log.error("Erro ao iniciar análise", e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao iniciar análise: " + e.getMessage());
        }
        
        return "redirect:/analise";
    }

    /**
     * Salvar detecções no banco
     */
    @PostMapping("/analise/salvar")
    public String salvarDeteccoes(RedirectAttributes redirectAttributes) {
        log.info("Salvando detecções no banco de dados");
        
        try {
            List<DeteccaoDTO> deteccoesPendentes = pythonService.carregarDeteccoesPendentes();
            
            if (deteccoesPendentes.isEmpty()) {
                redirectAttributes.addFlashAttribute("aviso", "Nenhuma detecção pendente para salvar");
                return "redirect:/analise";
            }

            // Converter DTOs para entidades e salvar
            List<DeteccaoMoto> deteccoes = deteccoesPendentes.stream()
                    .map(dto -> {
                        DeteccaoMoto d = new DeteccaoMoto();
                        d.setIdMoto(dto.getIdMoto());
                        d.setPosicaoX(dto.getPosicaoX());
                        d.setPosicaoY(dto.getPosicaoY());
                        d.setConfianca(dto.getConfianca());
                        d.setHorarioRegistro(LocalDateTime.now());
                        return d;
                    })
                    .toList();

            deteccaoService.salvarVarias(deteccoes);
            
            redirectAttributes.addFlashAttribute("sucesso", 
                deteccoes.size() + " detecções salvas com sucesso!");
            
        } catch (Exception e) {
            log.error("Erro ao salvar detecções", e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar detecções: " + e.getMessage());
        }
        
        return "redirect:/";
    }

    /**
     * Descartar detecções pendentes
     */
    @PostMapping("/analise/descartar")
    public String descartarDeteccoes(RedirectAttributes redirectAttributes) {
        log.info("Descartando detecções pendentes");
        
        redirectAttributes.addFlashAttribute("sucesso", "Detecções descartadas");
        
        return "redirect:/analise";
    }

    /**
     * Página de motos
     */
    @GetMapping("/motos")
    public String listarMotos(Model model) {
        log.info("Acessando página de motos");
        try {
            List<Moto> motos = motoService.listarTodas();
            model.addAttribute("motos", motos);
        } catch (Exception e) {
            log.error("Falha ao listar motos (BD indisponível)", e);
            model.addAttribute("motos", List.of());
            model.addAttribute("erroDb", "Banco de dados indisponível no momento. Tente novamente em alguns minutos.");
        }
        
        return "motos";
    }

    /**
     * Formulário - Nova Moto
     */
    @GetMapping("/motos/novo")
    public String novaMoto(Model model) {
        model.addAttribute("titulo", "Nova Moto");
        model.addAttribute("moto", new Moto());
        return "moto_form";
    }

    /**
     * Criar Moto
     */
    @PostMapping("/motos")
    public String criarMoto(@ModelAttribute Moto moto,
                            @RequestParam(value = "patioId", required = false) Long patioId,
                            RedirectAttributes redirectAttributes) {
        try {
            if (patioId != null) {
                patioRepository.findById(patioId).ifPresent(moto::setPatio);
            }
            motoService.salvar(moto);
            redirectAttributes.addFlashAttribute("sucesso", "Moto criada com sucesso!");
            return "redirect:/motos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao criar moto: " + e.getMessage());
            return "redirect:/motos/novo";
        }
    }

    /**
     * Formulário - Editar Moto
     */
    @GetMapping("/motos/editar/{id}")
    public String editarMotoForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return motoService.buscarPorId(id)
                .map(moto -> {
                    model.addAttribute("titulo", "Editar Moto");
                    model.addAttribute("moto", moto);
                    model.addAttribute("patioId", moto.getPatio() != null ? moto.getPatio().getId() : null);
                    return "moto_form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("erro", "Moto não encontrada");
                    return "redirect:/motos";
                });
    }

    /**
     * Atualizar Moto
     */
    @PostMapping("/motos/editar/{id}")
    public String atualizarMoto(@PathVariable Long id,
                                @ModelAttribute Moto motoForm,
                                @RequestParam(value = "patioId", required = false) Long patioId,
                                RedirectAttributes redirectAttributes) {
        try {
            if (patioId != null) {
                Patio patio = patioRepository.findById(patioId).orElse(null);
                motoForm.setPatio(patio);
            } else {
                motoForm.setPatio(null);
            }
            motoService.atualizar(id, motoForm);
            redirectAttributes.addFlashAttribute("sucesso", "Moto atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar moto: " + e.getMessage());
        }
        return "redirect:/motos";
    }

    /**
     * Página de detecções
     */
    @GetMapping("/deteccoes")
    public String listarDeteccoes(Model model) {
        log.info("Acessando página de detecções");
        try {
            List<DeteccaoMoto> deteccoes = deteccaoService.listarTodas();
            model.addAttribute("deteccoes", deteccoes);
        } catch (Exception e) {
            log.error("Falha ao listar detecções (BD indisponível)", e);
            model.addAttribute("deteccoes", List.of());
            model.addAttribute("erroDb", "Banco de dados indisponível no momento. Tente novamente em alguns minutos.");
        }
        
        return "deteccoes";
    }

    /**
     * Formulário - Nova Detecção
     */
    @GetMapping("/deteccoes/nova")
    public String novaDeteccao(Model model) {
        model.addAttribute("titulo", "Nova Detecção");
        model.addAttribute("deteccao", new DeteccaoMoto());
        return "deteccao_form";
    }

    /**
     * Criar Detecção
     */
    @PostMapping("/deteccoes")
    public String criarDeteccao(@ModelAttribute DeteccaoMoto deteccao,
                                RedirectAttributes redirectAttributes) {
        try {
            deteccaoService.salvar(deteccao);
            redirectAttributes.addFlashAttribute("sucesso", "Detecção criada com sucesso!");
            return "redirect:/deteccoes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao criar detecção: " + e.getMessage());
            return "redirect:/deteccoes/nova";
        }
    }

    /**
     * Formulário - Editar Detecção
     */
    @GetMapping("/deteccoes/editar/{id}")
    public String editarDeteccaoForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return deteccaoService.buscarPorId(id)
                .map(det -> {
                    model.addAttribute("titulo", "Editar Detecção");
                    model.addAttribute("deteccao", det);
                    return "deteccao_form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("erro", "Detecção não encontrada");
                    return "redirect:/deteccoes";
                });
    }

    /**
     * Atualizar Detecção
     */
    @PostMapping("/deteccoes/editar/{id}")
    public String atualizarDeteccao(@PathVariable Long id,
                                    @ModelAttribute DeteccaoMoto form,
                                    RedirectAttributes redirectAttributes) {
        try {
            DeteccaoMoto existente = deteccaoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Detecção não encontrada"));
            existente.setIdMoto(form.getIdMoto());
            existente.setPosicaoX(form.getPosicaoX());
            existente.setPosicaoY(form.getPosicaoY());
            existente.setConfianca(form.getConfianca());
            existente.setHorarioRegistro(form.getHorarioRegistro());
            deteccaoService.salvar(existente);
            redirectAttributes.addFlashAttribute("sucesso", "Detecção atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar detecção: " + e.getMessage());
        }
        return "redirect:/deteccoes";
    }

    /**
     * Deletar moto
     */
    @PostMapping("/motos/deletar/{id}")
    public String deletarMoto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Deletando moto ID: {}", id);
        
        try {
            motoService.deletar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Moto deletada com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao deletar moto", e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao deletar moto: " + e.getMessage());
        }
        
        return "redirect:/motos";
    }

    /**
     * Deletar detecção
     */
    @PostMapping("/deteccoes/deletar/{id}")
    public String deletarDeteccao(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Deletando detecção ID: {}", id);
        
        try {
            deteccaoService.deletar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Detecção deletada com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao deletar detecção", e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao deletar detecção: " + e.getMessage());
        }
        
        return "redirect:/deteccoes";
    }
}
