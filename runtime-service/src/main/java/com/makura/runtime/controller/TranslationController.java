package com.makura.runtime.controller;

import com.makura.runtime.auth.ApiKeyValidator;
import com.makura.runtime.model.Route;
import com.makura.runtime.repository.RouteRepository;
import com.makura.runtime.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST API endpoints for translation requests
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/translate")
@Tag(name = "Translation", description = "ISO 20022 message translation endpoints")
public class TranslationController {

    private final TranslationService translationService;
    private final ApiKeyValidator apiKeyValidator;
    private final RouteRepository routeRepository;

    public TranslationController(TranslationService translationService, ApiKeyValidator apiKeyValidator, RouteRepository routeRepository) {
        this.translationService = translationService;
        this.apiKeyValidator = apiKeyValidator;
        this.routeRepository = routeRepository;
    }

    /**
     * Translate request endpoint
     */
    @Operation(
        summary = "Translate message to/from ISO 20022",
        description = "Translates messages between source format and ISO 20022 format based on the route configuration. " +
            "Use direction=request (default) to transform source to ISO. Use direction=response to transform ISO back to source format. " +
            "For ACTIVE routes, the ISO message is forwarded to the configured endpoint. " +
            "For PASSIVE routes, the ISO message is returned directly.",
        security = @SecurityRequirement(name = "ApiKeyAuth")
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Source message in JSON, SOAP, or XML format. Format must match the route's inboundFormat configuration.",
        required = true,
        content = {
            @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "JSON Payment Request",
                        summary = "JSON Example",
                        description = "Example JSON payment request for routes with inboundFormat: JSON",
                        value = "{\n" +
                                "  \"source\": {\n" +
                                "    \"customer\": {\n" +
                                "      \"account\": \"1234567890\",\n" +
                                "      \"name\": \"John Doe\"\n" +
                                "    },\n" +
                                "    \"amount\": \"1000.50\",\n" +
                                "    \"currency\": \"USD\",\n" +
                                "    \"reference\": \"TXN-2024-001\",\n" +
                                "    \"creditor\": {\n" +
                                "      \"account\": \"9876543210\",\n" +
                                "      \"name\": \"Jane Smith\"\n" +
                                "    }\n" +
                                "  }\n" +
                                "}"
                    )
                }
            ),
            @Content(
                mediaType = "application/xml",
                examples = {
                    @ExampleObject(
                        name = "XML Payment Request",
                        summary = "XML Example",
                        description = "Example XML payment request for routes with inboundFormat: XML",
                        value = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<PaymentRequest>\n" +
                                "    <Customer>\n" +
                                "        <AccountNumber>1234567890</AccountNumber>\n" +
                                "        <CustomerName>John Doe</CustomerName>\n" +
                                "    </Customer>\n" +
                                "    <Amount>\n" +
                                "        <Value>1000.50</Value>\n" +
                                "        <CurrencyCode>USD</CurrencyCode>\n" +
                                "    </Amount>\n" +
                                "    <TransactionReference>TXN-2024-001</TransactionReference>\n" +
                                "</PaymentRequest>"
                    )
                }
            ),
            @Content(
                mediaType = "text/xml",
                examples = {
                    @ExampleObject(
                        name = "SOAP Request",
                        summary = "SOAP Example",
                        description = "Example SOAP request for routes with inboundFormat: SOAP",
                        value = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                                "    <soap:Body>\n" +
                                "        <PaymentRequest>\n" +
                                "            <Customer>\n" +
                                "                <AccountNumber>1234567890</AccountNumber>\n" +
                                "                <CustomerName>John Doe</CustomerName>\n" +
                                "            </Customer>\n" +
                                "            <Amount>1000.50</Amount>\n" +
                                "            <Currency>USD</Currency>\n" +
                                "        </PaymentRequest>\n" +
                                "    </soap:Body>\n" +
                                "</soap:Envelope>"
                    )
                }
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Translation successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TranslationResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Success Response",
                        summary = "Successful Translation",
                        value = "{\n" +
                                "  \"response\": \"{\\\"status\\\": \\\"ACSP\\\", \\\"reference\\\": \\\"CLR-REF-2024-001\\\"}\",\n" +
                                "  \"correlationId\": \"550e8400-e29b-41d4-a716-446655440000\"\n" +
                                "}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or expired API key",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Unauthorized",
                    value = "{\n" +
                            "  \"error\": \"Invalid or expired API key\",\n" +
                            "  \"correlationId\": \"550e8400-e29b-41d4-a716-446655440000\"\n" +
                            "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Translation failed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Error Response",
                    value = "{\n" +
                            "  \"error\": \"Route not found: SYSTEM_TO_NIP\",\n" +
                            "  \"correlationId\": \"550e8400-e29b-41d4-a716-446655440000\"\n" +
                            "}"
                )
            )
        )
    })
    @PostMapping(
        value = "/{routeId}",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, "text/xml"},
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> translate(
            @Parameter(description = "Route identifier (e.g., SYSTEM_TO_HYDROGEN)", required = true, example = "SYSTEM_TO_HYDROGEN")
            @PathVariable String routeId,
            @Parameter(description = "API key for authentication", required = true, example = "mak_test1234567890abcdef")
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Parameter(description = "Optional correlation ID for request tracking", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Parameter(description = "Translation direction: 'request' (default) transforms source to ISO, 'response' transforms ISO to source", example = "response")
            @RequestParam(value = "direction", required = false, defaultValue = "request") String direction,
            @RequestBody String requestBody) {

        // Validate API key
        if (!apiKeyValidator.validateApiKey(routeId, apiKey)) {
            log.warn("Invalid API key for routeId: {}", routeId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid or expired API key", correlationId));
        }

        // Generate correlation ID if not provided
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        // Process translation based on direction
        TranslationService.TranslationResult result;
        if ("response".equalsIgnoreCase(direction)) {
            // Response transformation: ISO -> Source format
            result = translationService.translateResponse(routeId, requestBody, correlationId);
        } else {
            // Request transformation: Source -> ISO format (default)
            result = translationService.translateRequest(routeId, requestBody, correlationId);
        }

        if (result.isSuccess()) {
            // Determine response format based on route's inbound format
            Route route = routeRepository.findByRouteId(routeId).orElse(null);
            String responseFormat = route != null ? route.getInboundFormat().name() : "JSON";
            
            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                .header("X-Correlation-Id", result.getCorrelationId());
            
            // Return response in the same format as input
            if ("SOAP".equalsIgnoreCase(responseFormat)) {
                // SOAP: Return SOAP envelope directly
                return responseBuilder
                    .contentType(MediaType.TEXT_XML)
                    .body(result.getMessage());
            } else if ("XML".equalsIgnoreCase(responseFormat) || "PROPRIETARY_XML".equalsIgnoreCase(responseFormat)) {
                // XML: Return XML directly
                return responseBuilder
                    .contentType(MediaType.APPLICATION_XML)
                    .body(result.getMessage());
            } else {
                // JSON: Wrap in response object with correlationId
                return responseBuilder
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new TranslationResponse(result.getMessage(), result.getCorrelationId()));
            }
        } else {
            // Error responses: Always JSON for consistency
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Correlation-Id", result.getCorrelationId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(result.getMessage(), result.getCorrelationId()));
        }
    }

    @Schema(description = "Successful translation response")
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class TranslationResponse {
        @Schema(description = "Translated response message", example = "{\"status\": \"ACSP\", \"reference\": \"CLR-REF-2024-001\"}")
        private String response;
        
        @Schema(description = "Correlation ID for request tracking", example = "550e8400-e29b-41d4-a716-446655440000")
        private String correlationId;
    }

    @Schema(description = "Error response")
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        @Schema(description = "Error message", example = "Route not found: SYSTEM_TO_NIP")
        private String error;
        
        @Schema(description = "Correlation ID for request tracking", example = "550e8400-e29b-41d4-a716-446655440000")
        private String correlationId;
    }
}

