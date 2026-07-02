package com.example.charbonecolo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_financier")
public class JournalFinancierModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_operation", nullable = false)
    private LocalDateTime dateOperation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_type_journal", nullable = false)
    private TypeJournalModel typeJournal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_origine")
    private OrigineModel origine;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal debit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal credit = BigDecimal.ZERO;

    @Column(length = 50)
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String description;

    public JournalFinancierModel() {}

    public JournalFinancierModel(LocalDateTime dateOperation, TypeJournalModel typeJournal,
                                  OrigineModel origine, BigDecimal debit,
                                  BigDecimal credit, String reference, String description) {
        this.dateOperation = dateOperation;
        this.typeJournal = typeJournal;
        this.origine = origine;
        this.debit = debit;
        this.credit = credit;
        this.reference = reference;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateOperation() { return dateOperation; }
    public void setDateOperation(LocalDateTime dateOperation) { this.dateOperation = dateOperation; }

    public TypeJournalModel getTypeJournal() { return typeJournal; }
    public void setTypeJournal(TypeJournalModel typeJournal) { this.typeJournal = typeJournal; }

    public OrigineModel getOrigine() { return origine; }
    public void setOrigine(OrigineModel origine) { this.origine = origine; }

    public BigDecimal getDebit() { return debit; }
    public void setDebit(BigDecimal debit) { this.debit = debit; }

    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
