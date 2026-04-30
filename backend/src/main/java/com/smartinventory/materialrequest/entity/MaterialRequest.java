package com.smartinventory.materialrequest.entity;

import com.smartinventory.common.entity.BaseEntity;
import com.smartinventory.materialrequest.enums.MaterialRequestStatus;
import com.smartinventory.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "material_requests", indexes = {
    @Index(name = "idx_mr_status", columnList = "status"),
    @Index(name = "idx_mr_requested_by", columnList = "requested_by_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialRequestStatus status;

    /**
     * Talebin gerekçesi (departman, ne için kullanılacak vb.).
     */
    @Column(length = 500)
    private String reason;

    /**
     * Onaylayan veya reddeden kişi.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by_id")
    private User decidedBy;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    /**
     * Red gerekçesi veya onay notu.
     */
    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    @Builder.Default
    @OneToMany(mappedBy = "materialRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MaterialRequestItem> items = new ArrayList<>();

    public void addItem(MaterialRequestItem item) {
        items.add(item);
        item.setMaterialRequest(this);
    }
}
