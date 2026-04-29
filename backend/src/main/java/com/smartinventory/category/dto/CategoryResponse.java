package com.smartinventory.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kategori bilgi yanıtı")
public class CategoryResponse {

    @Schema(description = "Kategori ID", example = "1")
    private Long id;

    @Schema(description = "Kategori adı", example = "Ofis Malzemeleri")
    private String name;

    @Schema(description = "Açıklama", example = "Kalem, defter, ataç vb.")
    private String description;
}
