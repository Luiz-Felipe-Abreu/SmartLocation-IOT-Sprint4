package br.com.fiap.smartlocation.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller para servir vídeos gerados pela análise Python
 */
@Controller
@RequestMapping("/__videos_disabled__")
@Slf4j
public class VideoController {

    @Value("${python.notebook.path:../visao_computacional/SmartLocation.ipynb}")
    private String notebookPath;

    /**
     * Desabilitado: o serviço de vídeos agora é feito via ResourceHandler em WebConfig.
     * Este endpoint permanece apenas para referência e não é utilizado.
     */
    @GetMapping("/**")
    public ResponseEntity<Resource> servirVideo() throws IOException {
        // Obter o caminho completo a partir da requisição
        String requestPath = org.springframework.web.context.request.RequestContextHolder
            .currentRequestAttributes()
            .getAttribute("org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping", 0)
            .toString();
        
        // Remover /videos/ do início
        String relativePath = requestPath.substring("/videos/".length());
        
        Path notebookDir = Paths.get(notebookPath).getParent().toAbsolutePath();
        Path videoPath = notebookDir.resolve(relativePath).normalize();
        
        log.info("Tentando servir vídeo: {}", videoPath);
        
        // Validar que o arquivo existe e está dentro do diretório permitido
        if (!Files.exists(videoPath) || !videoPath.startsWith(notebookDir)) {
            log.warn("Vídeo não encontrado ou fora do diretório permitido: {}", videoPath);
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = new FileSystemResource(videoPath);
        
        // Determinar tipo de mídia baseado na extensão
        String contentType = Files.probeContentType(videoPath);
        if (contentType == null) {
            contentType = "video/mp4"; // Fallback
        }
        
        log.info("Servindo vídeo: {} ({})", videoPath.getFileName(), contentType);
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + videoPath.getFileName() + "\"")
            .body(resource);
    }
}
