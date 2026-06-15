package com.gilede.livraria.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    @Builder.Default
    private Integer salesCount = 0;

    /**
     * Lista de URLs de imagens armazenadas como elementos separados em tabela
     * própria.
     * O frontend espera: images: string[]
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_images", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    private Integer year;

    /** ID do produto no Mercado Livre */
    private String mlId;

    @Builder.Default
    private Boolean mlSynced = false;

    private Double rating;

    private String isbn;

    private String publisher;

    private Integer pages;

    /**
     * Reviews são entidades separadas mapeadas aqui para facilitar o carregamento
     * no endpoint GET /books/:id (o frontend espera reviews dentro do book).
     */
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Favorite> favorites = new ArrayList<>();
}
