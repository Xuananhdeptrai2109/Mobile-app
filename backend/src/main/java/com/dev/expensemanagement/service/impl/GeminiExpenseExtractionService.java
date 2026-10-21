package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.response.ExpenseExtractionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

@Service
public class GeminiExpenseExtractionService {

    private static final String GEMINI_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    private static final long MAX_UPLOAD_SIZE_BYTES = 10 * 1024 * 1024;
    private static final Set<String> IMAGE_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/heic", "image/heif"
    );
    private static final Set<String> AUDIO_MIME_TYPES = Set.of(
            "audio/mpeg", "audio/mp4", "audio/wav", "audio/x-wav",
            "audio/ogg", "application/ogg", "audio/webm", "audio/aac"
    );

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    @Value("${GEMINI_MODEL:gemini-2.0-flash}")
    private String model;

    public ExpenseExtractionResponse extractFromReceipt(MultipartFile image) {
        return extract(
                image,
                "Analyze this receipt image and return ONLY one valid JSON object with fields: "
                        + "name, amount, note, expenseDate, merchantName, rawText. "
                        + "Use the merchant or bill title as a concise Vietnamese expense name. "
                        + "amount is the final total payable, as an unformatted JSON number in VND (for example 1000000, never 1.000.000). "
                        + "expenseDate must be ISO yyyy-MM-dd or null. Set an unknown field to null.",
                IMAGE_MIME_TYPES,
                "Ảnh hóa đơn"
        );
    }

    public ExpenseExtractionResponse extractFromAudio(MultipartFile audio) {
        return extract(
                audio,
                "Transcribe this Vietnamese audio and return ONLY one valid JSON object with fields: "
                        + "name, amount, note, expenseDate, merchantName, rawText. "
                        + "Infer a concise expense name from the activity, removing conversational words. "
                        + "For example, 'toi nay an lau nuong voi moi nguoi het 1 trieu' becomes name 'An lau nuong' and amount 1000000. "
                        + "amount must be an unformatted JSON number in VND (for example 1000000, never 1.000.000). "
                        + "expenseDate must be ISO yyyy-MM-dd or null. Set an unknown field to null.",
                AUDIO_MIME_TYPES,
                "File ghi âm"
        );
    }

    private ExpenseExtractionResponse extract(
            MultipartFile file,
            String prompt,
            Set<String> allowedMimeTypes,
            String label
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Chua cau hinh GEMINI_API_KEY trong .env");
        }

        try {
            ValidatedUpload upload = validateAndReadFile(file, allowedMimeTypes, label);
            String responseText = callGemini(upload, prompt);
            return parseExtraction(responseText);
        } catch (IOException e) {
            throw new IllegalStateException("Khong doc duoc file upload", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Qua trinh goi Gemini bi gian doan", e);
        }
    }

    private String callGemini(ValidatedUpload upload, String prompt) throws IOException, InterruptedException {
        String encodedData = Base64.getEncoder().encodeToString(upload.bytes());
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of("text", prompt),
                                        Map.of(
                                                "inlineData", Map.of(
                                                        "mimeType", upload.mimeType(),
                                                        "data", encodedData
                                                )
                                        )
                                }
                        )
                }
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL_TEMPLATE.formatted(model)))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Dịch vụ AI tạm thời không khả dụng");
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new IllegalStateException("Gemini khong tra ve noi dung hop le");
        }

        return textNode.asText();
    }

    private ExpenseExtractionResponse parseExtraction(String responseText) throws IOException {
        String json = cleanupJson(responseText);
        JsonNode node = objectMapper.readTree(json);

        return ExpenseExtractionResponse.builder()
                .name(textOrNull(node, "name"))
                .amount(positiveDecimalOrNull(node, "amount"))
                .note(textOrNull(node, "note"))
                .expenseDate(dateOrNull(node, "expenseDate"))
                .merchantName(textOrNull(node, "merchantName"))
                .rawText(textOrNull(node, "rawText"))
                .message("Trich xuat thong tin chi tieu thanh cong")
                .success(true)
                .build();
    }

    private String cleanupJson(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private String textOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }

    private BigDecimal decimalOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull() || value.asText().isBlank()) {
            return null;
        }
        if (value.isNumber()) {
            return value.decimalValue();
        }

        String raw = value.asText().replaceAll("[^0-9,.-]", "");
        if (raw.isBlank()) {
            return null;
        }

        int commaCount = raw.length() - raw.replace(",", "").length();
        int dotCount = raw.length() - raw.replace(".", "").length();
        if (commaCount + dotCount > 1) {
            // VND normally has no fractional part: 1.000.000 or 1,000,000.
            raw = raw.replace(",", "").replace(".", "");
        } else if (commaCount == 1) {
            raw = raw.replace(',', '.');
        }

        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("AI tra ve so tien khong hop le");
        }
    }

    private BigDecimal positiveDecimalOrNull(JsonNode node, String fieldName) {
        BigDecimal value = decimalOrNull(node, fieldName);
        return value != null && value.signum() > 0
                ? value.setScale(0, RoundingMode.HALF_UP)
                : null;
    }

    private LocalDate dateOrNull(JsonNode node, String fieldName) {
        String value = textOrNull(node, fieldName);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private ValidatedUpload validateAndReadFile(
            MultipartFile file,
            Set<String> allowedMimeTypes,
            String label
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(label + " khong duoc trong");
        }
        if (file.getSize() > MAX_UPLOAD_SIZE_BYTES) {
            throw new IllegalArgumentException(label + " toi da 10 MB");
        }
        String mimeType = normalizeMimeType(file.getContentType());
        if (mimeType == null || !allowedMimeTypes.contains(mimeType)) {
            throw new IllegalArgumentException(label + " co dinh dang khong ho tro");
        }

        byte[] bytes = file.getBytes();
        String detectedMimeType = detectMimeType(bytes, allowedMimeTypes);
        if (detectedMimeType == null || !mimeTypesMatch(mimeType, detectedMimeType)) {
            throw new IllegalArgumentException(label + " không khớp định dạng khai báo");
        }
        return new ValidatedUpload(bytes, detectedMimeType);
    }

    private String normalizeMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        return switch (mimeType.toLowerCase()) {
            case "image/heif" -> "image/heic";
            case "audio/x-wav" -> "audio/wav";
            case "application/ogg" -> "audio/ogg";
            case "video/mp4" -> "audio/mp4";
            default -> mimeType.toLowerCase();
        };
    }

    private boolean mimeTypesMatch(String declared, String detected) {
        return normalizeMimeType(declared).equals(normalizeMimeType(detected));
    }

    private String detectMimeType(byte[] bytes, Set<String> allowedMimeTypes) {
        if (startsWith(bytes, 0xFF, 0xD8, 0xFF)) return "image/jpeg";
        if (startsWith(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) return "image/png";
        if (asciiAt(bytes, 0, "RIFF") && asciiAt(bytes, 8, "WEBP")) return "image/webp";
        if (asciiAt(bytes, 4, "ftyp") && isHeicBrand(bytes)) return "image/heic";

        if (asciiAt(bytes, 0, "RIFF") && asciiAt(bytes, 8, "WAVE")) return "audio/wav";
        if (asciiAt(bytes, 0, "OggS")) return "audio/ogg";
        if (startsWith(bytes, 0x1A, 0x45, 0xDF, 0xA3)) return "audio/webm";
        if (isAac(bytes)) return "audio/aac";
        if (asciiAt(bytes, 0, "ID3") || isMp3Frame(bytes)) return "audio/mpeg";
        if (asciiAt(bytes, 4, "ftyp") && allowedMimeTypes.contains("audio/mp4")) return "audio/mp4";
        return null;
    }

    private boolean isHeicBrand(byte[] bytes) {
        return asciiAt(bytes, 8, "heic")
                || asciiAt(bytes, 8, "heix")
                || asciiAt(bytes, 8, "hevc")
                || asciiAt(bytes, 8, "mif1");
    }

    private boolean isAac(byte[] bytes) {
        return bytes.length >= 2
                && unsigned(bytes[0]) == 0xFF
                && (unsigned(bytes[1]) & 0xF6) == 0xF0;
    }

    private boolean isMp3Frame(byte[] bytes) {
        return bytes.length >= 2
                && unsigned(bytes[0]) == 0xFF
                && (unsigned(bytes[1]) & 0xE0) == 0xE0;
    }

    private boolean startsWith(byte[] bytes, int... expected) {
        if (bytes.length < expected.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if (unsigned(bytes[i]) != expected[i]) return false;
        }
        return true;
    }

    private boolean asciiAt(byte[] bytes, int offset, String expected) {
        if (bytes.length < offset + expected.length()) return false;
        for (int i = 0; i < expected.length(); i++) {
            if (unsigned(bytes[offset + i]) != expected.charAt(i)) return false;
        }
        return true;
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }

    private record ValidatedUpload(byte[] bytes, String mimeType) {}
}
