package ru.ntik.book.library.repository;

import io.github.yashchenkon.assertsqlcount.test.AssertSqlQueriesCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.ntik.book.library.domain.BookDefinition;
import ru.ntik.book.library.domain.Publisher;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class PublisherRepositoryTest {

    @Autowired
    PublisherRepository publisherRepository;

    @BeforeEach
    void resetSqlCount() {
        AssertSqlQueriesCount.reset();
    }

    @DisplayName("Тест lazy fetch для Book Definition")
    @Test
    void lazyFetchForBookDefinition() {
        Publisher publisher = publisherRepository.findById(1L).orElse(null);
        assertThat(publisher).isNotNull();
        AssertSqlQueriesCount.assertSelectCount(1);

        final Set<BookDefinition> bookDefinitions = publisher.getBookDefinitions();
        AssertSqlQueriesCount.assertSelectCount(1);

        for (BookDefinition bd : bookDefinitions) {
            assertThat(bd.getName()).isNotNull();
        }

        AssertSqlQueriesCount.assertSelectCount(2);
    }

    @DisplayName("Не модифицируемая коллекция")
    @Test
    void unmodifiedCollection() {
        Publisher publisher = publisherRepository.findById(1L).orElse(null);
        assertThat(publisher).isNotNull();
        AssertSqlQueriesCount.assertSelectCount(1);

        final Set<BookDefinition> bookDefinitions = publisher.getBookDefinitions();
        assertThrows(UnsupportedOperationException.class, () -> bookDefinitions.add(null));

        assertThat(bookDefinitions).isNotEmpty();
        AssertSqlQueriesCount.assertSelectCount(2);
    }

    @DisplayName("Забираем книги в eager c помощью графа")
    @Test
    void eagerFetchBooks() {
        Publisher publisher = publisherRepository.fetchById(1L).orElse(null);
        assertThat(publisher).isNotNull();
        AssertSqlQueriesCount.assertSelectCount(1);

        final Set<BookDefinition> bookDefinitions = publisher.getBookDefinitions();
        AssertSqlQueriesCount.assertSelectCount(1);

        for (BookDefinition bd : bookDefinitions) {
            assertThat(bd.getName()).isNotNull();
        }
        AssertSqlQueriesCount.assertSelectCount(1);
    }
}