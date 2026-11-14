package example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FastApiClient {

    private static final Logger log = LoggerFactory.getLogger(FastApiClient.class);

    private final RestClient client;

    public FastApiClient(
            @Value("${fastapi.url}") String baseUrl,
            @Value("${fastapi.secret-path}") String secretPath,
            RestClient.Builder builder) {

        String internalKey = readSecret(secretPath);

        // TEMPLATE_AND_VALUES strictly encodes each path variable,
        // like encodeURIComponent in the Node version.
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(baseUrl);
        uriFactory.setEncodingMode(EncodingMode.TEMPLATE_AND_VALUES);

        this.client = builder
                .uriBuilderFactory(uriFactory)
                .defaultHeader("X-Internal-Key", internalKey)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json, text/html")
                .build();
    }

    public UpstreamResponse get(String template, Object... vars) {
        return exchange(HttpMethod.GET, template, null, vars);
    }

    public UpstreamResponse post(String template, Object body, Object... vars) {
        return exchange(HttpMethod.POST, template, body, vars);
    }

    private UpstreamResponse exchange(HttpMethod method, String template, Object body, Object[] vars) {
        RestClient.RequestBodySpec spec = client.method(method).uri(template, vars);
        if (body != null) {
            spec.contentType(MediaType.APPLICATION_JSON).body(body);
        }
        // exchange() doesn't throw on 4xx/5xx, so statuses pass straight through.
        // The body is kept as a String, so JSON and HTML are both returned untouched.
        return spec.exchange((request, response) -> new UpstreamResponse(
                response.getStatusCode().value(),
                response.getHeaders().getContentType(),
                new String(response.getBody().readAllBytes(),
                        charsetOf(response.getHeaders().getContentType()))
        ));
    }

    private static Charset charsetOf(MediaType type) {
        return type != null && type.getCharset() != null
                ? type.getCharset()
                : StandardCharsets.UTF_8;
    }

    private static String readSecret(String path) {
        try {
            return Files.readString(Path.of(path)).trim();
        } catch (Exception e) {
            log.error("Could not read secret: {}", path);
            System.exit(1);
            return null; // unreachable
        }
    }
}
