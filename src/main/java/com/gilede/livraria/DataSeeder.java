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
        if (bookRepository.count() == 0) {
            List<Book> books = List.of(
                    Book.builder()
                            .title("Dom Casmurro")
                            .author("Machado de Assis")
                            .description("Clássico da literatura brasileira que narra a história de Bentinho e Capitu.")
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
                            .title("O Cortiço")
                            .author("Aluísio Azevedo")
                            .description(
                                    "Romance naturalista que retrata a vida em uma habitação coletiva no Rio de Janeiro.")
                            .category("Literatura Brasileira")
                            .price(new BigDecimal("29.90"))
                            .stock(30)
                            .images(List.of("https://m.media-amazon.com/images/I/71yB3cBDLUL.jpg"))
                            .active(true)
                            .year(1890)
                            .isbn("978-8535906783")
                            .rating(4.5)
                            .build(),
                    Book.builder()
                            .title("1984")
                            .author("George Orwell")
                            .description(
                                    "Distopia clássica sobre totalitarismo e vigilância em uma sociedade futurista.")
                            .category("Ficção Científica")
                            .price(new BigDecimal("44.90"))
                            .stock(4) // estoque baixo para testar alerta
                            .images(List.of("https://m.media-amazon.com/images/I/71kxa1-0mfL.jpg"))
                            .active(true)
                            .year(1949)
                            .isbn("978-8535914849")
                            .rating(4.9)
                            .build(),
                    Book.builder()
                            .title("Livro Desativado")
                            .author("Autor Teste")
                            .description("Este livro não deve aparecer no catálogo público.")
                            .category("Teste")
                            .price(new BigDecimal("9.90"))
                            .stock(100)
                            .images(List.of())
                            .active(false)
                            .build());
            bookRepository.saveAll(books);
            log.info("Livros de exemplo criados: {} livros", books.size());
        }
    }
}
