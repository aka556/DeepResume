package interview.guide.common.ai;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.regex.Pattern;

public final class ApiPathResolver {

  private static final int DEFAULT_CONNECT_TIMEOUT = 10000;
  private static final int DEFAULT_READ_TIMEOUT = 300000;

  private static final Pattern TRAILING_VERSION = Pattern.compile("/v\\d+[a-zA-Z0-9]*$");

  private ApiPathResolver() {}

  public static OpenAiApi buildOpenAiApi(String baseUrl, String apiKey) {
    return buildOpenAiApi(baseUrl, apiKey, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
  }

  public static OpenAiApi buildOpenAiApi(String baseUrl, String apiKey,
      int connectTimeout, int readTimeout) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(connectTimeout);
    requestFactory.setReadTimeout(readTimeout);

    RestClient.Builder restClientBuilder = RestClient.builder()
        .requestFactory(requestFactory);

    OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
        .baseUrl(baseUrl)
        .apiKey(apiKey)
        .restClientBuilder(restClientBuilder);
    if (baseUrlContainsVersion(baseUrl)) {
      apiBuilder.completionsPath("/chat/completions").embeddingsPath("/embeddings");
    }
    return apiBuilder.build();
  }

  public static boolean baseUrlContainsVersion(String baseUrl) {
    if (baseUrl == null || baseUrl.isBlank()) {
      return false;
    }
    String stripped = stripTrailingSlashes(baseUrl.trim());
    return TRAILING_VERSION.matcher(stripped).find();
  }

  public static String stripTrailingSlashes(String value) {
    if (value == null) {
      return "";
    }
    String result = value.trim();
    while (result.endsWith("/")) {
      result = result.substring(0, result.length() - 1);
    }
    return result;
  }
}
