package com.gilede.livraria.mapper;

import com.gilede.livraria.dto.BookDTOs;
import com.gilede.livraria.model.Book;
import com.gilede.livraria.model.Review;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookMapper {

    public BookDTOs.BookResponse toResponse(Book book) {
        List<BookDTOs.ReviewResponse> reviews = book.getReviews() == null
                ? List.of()
                : book.getReviews().stream().map(this::toReviewResponse).toList();

        return new BookDTOs.BookResponse(
                book.getId().toString(),
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getCategory(),
                book.getPrice(),
                book.getStock(),
                book.getImages(),
                book.getActive(),
                book.getYear(),
                book.getMlId(),
                book.getMlSynced(),
                book.getSalesCount(),
                book.getRating(),
                book.getIsbn(),
                reviews);
    }

    public BookDTOs.ReviewResponse toReviewResponse(Review review) {
        return new BookDTOs.ReviewResponse(
                review.getId().toString(),
                review.getUserId().toString(),
                review.getUserName(),
                review.getRating(),
                review.getComment(),
                review.getDate().toString());
    }

    public Book toEntity(BookDTOs.BookRequest request) {
        return Book.builder()
                .title(request.title())
                .author(request.author())
                .description(request.description())
                .category(request.category())
                .price(request.price())
                .stock(request.stock())
                .images(request.images() != null ? request.images() : List.of())
                .active(request.active() != null ? request.active() : true)
                .year(request.year())
                .mlId(request.mlId())
                .mlSynced(request.mlSynced() != null ? request.mlSynced() : false)
                .rating(request.rating())
                .isbn(request.isbn())
                .build();
    }

    public void updateEntity(Book book, BookDTOs.BookRequest request) {
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setDescription(request.description());
        book.setCategory(request.category());
        book.setPrice(request.price());
        book.setStock(request.stock());
        if (request.images() != null)
            book.setImages(request.images());
        if (request.active() != null)
            book.setActive(request.active());
        book.setYear(request.year());
        book.setMlId(request.mlId());
        if (request.mlSynced() != null)
            book.setMlSynced(request.mlSynced());
        book.setRating(request.rating());
        book.setIsbn(request.isbn());
    }
}
