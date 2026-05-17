package com.gilede.livraria;

import com.gilede.livraria.model.Book;
import com.gilede.livraria.model.Role;
import com.gilede.livraria.model.User;
import com.gilede.livraria.repository.BookRepository;
import com.gilede.livraria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedSampleBooks();
    }

    private void seedUsers() {
        // Admin padrão exigido pelo frontend
        if (!userRepository.existsByEmail("livrariagiledevieira@gmail.com")) {
            User admin = User.builder()
                    .name("Administrador Gilede Vieira")
                    .email("livrariagiledevieira@gmail.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Usuário admin criado: {}", admin.getEmail());
        }

        // Cliente padrão exigido pelo frontend
        if (!userRepository.existsByEmail("giovani.vieira@email.com")) {
            User customer = User.builder()
                    .name("Giovani Vieira")
                    .email("giovani.vieira@email.com")
                    .password(passwordEncoder.encode("cliente123"))
                    .role(Role.CUSTOMER)
                    .build();
            userRepository.save(customer);
            log.info("Usuário cliente criado: {}", customer.getEmail());
        }
    }

    private void seedSampleBooks() {
        if (bookRepository.count() > 0) {
            return;
        }

        List<Book> books = List.of(
                Book.builder()
                        .title("Quem Pensa Enriquece")
                        .author("Napoleon Hill")
                        .description("Clássico de autoajuda sobre mentalidade, propósito e disciplina financeira.")
                        .category("Autoajuda")
                        .price(new BigDecimal("49.90"))
                        .stock(25)
                        .images(List.of("https://m.media-amazon.com/images/I/81m1gRZ9YWL.jpg"))
                        .active(true)
                        .year(1937)
                        .isbn("978-8576842217")
                        .rating(4.8)
                        .build(),
                Book.builder()
                        .title("A Cabana")
                        .author("William P. Young")
                        .description("Romance com forte apelo espiritual e reflexão sobre fé, perdão e reconstrução.")
                        .category("Evangélico")
                        .price(new BigDecimal("44.90"))
                        .stock(20)
                        .images(List.of("https://m.media-amazon.com/images/I/81lQ0QZ7wJL.jpg"))
                        .active(true)
                        .year(2007)
                        .isbn("978-8532521194")
                        .rating(4.7)
                        .build(),
                Book.builder()
                        .title("Verity")
                        .author("Colleen Hoover")
                        .description("Suspense psicológico com segredos, tensão crescente e reviravoltas.")
                        .category("Suspense")
                        .price(new BigDecimal("59.90"))
                        .stock(18)
                        .images(List.of("https://m.media-amazon.com/images/I/81W5nQKZ8rL.jpg"))
                        .active(true)
                        .year(2018)
                        .isbn("978-6555652070")
                        .rating(4.6)
                        .build(),
                Book.builder()
                        .title("O Morro dos Ventos Uivantes")
                        .author("Emily Brontë")
                        .description("Romance clássico marcado por paixão, tragédia e obsessão.")
                        .category("Romance")
                        .price(new BigDecimal("39.90"))
                        .stock(22)
                        .images(List.of("https://m.media-amazon.com/images/I/81qvQq5mVYL.jpg"))
                        .active(true)
                        .year(1847)
                        .isbn("978-8532529961")
                        .rating(4.5)
                        .build(),
                Book.builder()
                        .title("Matemática para Vencer")
                        .author("Equipe Educacional")
                        .description("Material didático com foco em exercícios e prática escolar.")
                        .category("Didático")
                        .price(new BigDecimal("69.90"))
                        .stock(35)
                        .images(List.of("https://m.media-amazon.com/images/I/71Uq4F8w9pL.jpg"))
                        .active(true)
                        .year(2024)
                        .isbn("978-0000000001")
                        .rating(4.2)
                        .build(),
                Book.builder()
                        .title("Dom Casmurro")
                        .author("Machado de Assis")
                        .description("Clássico da literatura brasileira sobre memória, ciúme e ambiguidade narrativa.")
                        .category("Literatura Brasileira")
                        .price(new BigDecimal("39.90"))
                        .stock(50)
                        .images(List.of("https://m.media-amazon.com/images/I/81BRGNWmg+L.jpg"))
                        .active(true)
                        .year(1899)
                        .isbn("978-8535929010")
                        .rating(4.8)
                        .build(),
                Book.builder()
                        .title("1984")
                        .author("George Orwell")
                        .description("Distopia clássica sobre totalitarismo e vigilância em uma sociedade futurista.")
                        .category("Ficção Científica")
                        .price(new BigDecimal("44.90"))
                        .stock(4)
                        .images(List.of("https://m.media-amazon.com/images/I/71kxa1-0mfL.jpg"))
                        .active(true)
                        .year(1949)
                        .isbn("978-8535914849")
                        .rating(4.9)
                        .build(),
                Book.builder()
                        .title("O Pequeno Príncipe")
                        .author("Antoine de Saint-Exupéry")
                        .description("Obra infantil clássica sobre amizade, imaginação e valores humanos.")
                        .category("Infantil")
                        .price(new BigDecimal("29.90"))
                        .stock(40)
                        .images(List.of("https://m.media-amazon.com/images/I/81eB+7+CkUL.jpg"))
                        .active(true)
                        .year(1943)
                        .isbn("978-8574063609")
                        .rating(4.9)
                        .build()
        );

        bookRepository.saveAll(books);
        log.info("Livros de exemplo criados: {} livros", books.size());
    }
}
