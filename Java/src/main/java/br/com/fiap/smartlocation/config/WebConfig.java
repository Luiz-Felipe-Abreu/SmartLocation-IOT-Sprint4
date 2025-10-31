package br.com.fiap.smartlocation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${python.notebook.path:../visao_computacional/SmartLocation.ipynb}")
    private String notebookPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Base física: diretório da pasta visao_computacional, resolvido de forma absoluta
        Path notebookFile = Paths.get(notebookPath).toAbsolutePath();
        Path visaoComputacionalDir = notebookFile.getParent();

        // Em Windows precisamos usar barra / no prefixo file:
        String baseLocation = "file:" + visaoComputacionalDir.toString().replace('\\', '/') + "/";

        registry.addResourceHandler("/videos/**")
                .addResourceLocations(baseLocation)
                .setCachePeriod(0); // sem cache para mostrar novos vídeos imediatamente
    }
}
