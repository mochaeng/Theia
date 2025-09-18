package com.mochaeng.theia_api.query.application.web.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record QueryRequest(
    @NotBlank(message = "Query cannot be blank")
    @Size(
        min = 1,
        max = 1000,
        message = "Query must be between 1 and 1000 characters"
    )
    String query,

    @NotEmpty
    List<
        @Pattern(
            regexp = "^(title|abstract|fulltext)$",
            message = "Field must be one of: title, abstract, fulltext"
        ) String
    > fields,

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 10, message = "Limit cannot exceed 10")
    Integer limit,

    @DecimalMin(
        value = "0.0",
        message = "Threshold must be between 0.0 and 1.0"
    )
    @DecimalMax(
        value = "1.0",
        message = "Threshold must be between 0.0 and 1.0"
    )
    Float threshold
) {}
