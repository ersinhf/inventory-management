package com.smartinventory.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kategori oluşturma/güncelleme isteği")
public class CategoryRequest {

    @Schema(description = "Kategori adı", example = "Ofis Malzemeleri")
    @NotBlank(message = "Kategori adı boş olamaz")
    @Size(max = 100)
    private String name;

    @Schema(description = "Açıklama", example = "Kalem, defter, ataç vb.")
    @Size(max = 300)
    private String description;
}
