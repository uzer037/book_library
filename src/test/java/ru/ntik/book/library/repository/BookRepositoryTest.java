package ru.ntik.book.library.repository;

import io.github.yashchenkon.assertsqlcount.test.AssertSqlQueriesCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import ru.ntik.book.library.domain.BookDefinition;
import ru.ntik.book.library.testutils.TestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("h2")
@Transactional

class BookRepositoryTest {

    @Autowired
    BookRepository bookRepository;

    @BeforeEach
    void resetSqlCount() {
        AssertSqlQueriesCount.reset();
    }

    @DisplayName("Поищем по id")
    @Test
    void findByIdTest() {
        BookDefinition bd = bookRepository.findById(1L).orElse(null);
        assertThat(bd).isNotNull();

        AssertSqlQueriesCount.assertSelectCount(1);

        bd = bookRepository.findById(2L).orElse(null);
        assertThat(bd).isNotNull();

        bd = bookRepository.findById(-2L).orElse(null);
        assertThat(bd).isNull();

        AssertSqlQueriesCount.assertSelectCount(3);
    }

    @DisplayName("Проверим L1 cache")
    @Test
    void checkL1cache() {
        BookDefinition bd = bookRepository.findById(1L).orElse(null);
        assertThat(bd).isNotNull();
        bd.setName(bd.getName() + bd.getName());

        BookDefinition bd2 = bookRepository.findById(1L).orElse(null);
        assertThat(bd2).isNotNull();
        bd2.setName(bd2.getName() + bd2.getName());

        //Те же самые объекты
        assertThat(bd).isEqualTo(bd2);
        assertThat(bd.getName()).isEqualTo(bd.getName());
        assertThat(bd).isSameAs(bd2);

        BookDefinition bd3 = bookRepository.findById(1L).orElse(null);
        assertThat(bd3).isNotNull();
        assertThat(bd).isSameAs(bd3);

        AssertSqlQueriesCount.assertSelectCount(1);
    }

    @DisplayName("Поищем все")
    @Test
    void findAllTest() {
        List<BookDefinition> bds = bookRepository.findAll();
        assertThat(bds).isNotEmpty();

        AssertSqlQueriesCount.assertSelectCount(1);
    }


    @DisplayName("Должен сохранить объект")
    @Test
    void saveEntityTest() {
        BookDefinition bd = TestUtils.createBookDefinition();
        bd = bookRepository.save(bd);

        assertThat(bd.getId()).isNotNull();
        assertThat(bd.getCreator()).isNotNull().isPositive();
        assertThat(bd.getVersion()).isZero();

        //Нет lazy полей. Потом проверить c creator
        AssertSqlQueriesCount.assertInsertCount(1);
        AssertSqlQueriesCount.assertSelectCount(0);
    }

    @DisplayName("Вносим изменения")
    @Test
    void mergeTest() {
        BookDefinition bd = bookRepository.findById(1L).orElse(null);
        assertThat(bd).isNotNull();
        AssertSqlQueriesCount.assertSelectCount(1);

        bd.setName("New name");
        bd.setPageCount(100);

//      no flush here, no update
        bd = bookRepository.save(bd);
        AssertSqlQueriesCount.assertInsertCount(0);
        assertThat(bd.getVersion()).isEqualTo((short)0);

        //here 1 update. save dirty and flush
        bd = bookRepository.saveAndFlush(bd);
        AssertSqlQueriesCount.assertUpdateCount(1);
        assertThat(bd.getVersion()).isEqualTo((short)1);
    }

    @DisplayName("Удаляем объект")
    @Test
    void removeTest() {
        BookDefinition bd = bookRepository.findById(1L).orElse(null);
        assertThat(bd).isNotNull();

        bookRepository.delete(bd);
        AssertSqlQueriesCount.assertDeleteCount(0);
        bookRepository.flush();
        AssertSqlQueriesCount.assertDeleteCount(1);

        assertThatCode(() -> bookRepository.deleteById(-20L)).doesNotThrowAnyException();
    }

    @DisplayName("Проверяем Optimisic lock на версии")
    @Test
    void checkOptimisticLock() throws InterruptedException {

        BookDefinition bd = bookRepository.findById(3L).orElse(null);
        assertThat(bd).isNotNull();

        short oldVersion = bd.getVersion();

        //Start in new tread fon new persistence context
        Thread one = new Thread(this::updateInNewTransaction);
        one.start();
        one.join();

        //Not dirty, no change version
        assertThat(bd.getVersion()).isEqualTo(oldVersion);
        bd.setName("Cool name");
        bd.setPageCount(1000);
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> bookRepository.saveAndFlush(bd));
    }

    @Commit
    //АККУРАТНО, объект с id = 3 сохранится в ходе тестов и поднимется версия, происходит КОММИТ, не роллбек
    void updateInNewTransaction() {
        //Select here
        BookDefinition bd = bookRepository.findById(3L).orElse(null);
        assertThat(bd).isNotNull();
        bd.setName("new version");
        short currentVersion = bd.getVersion();

        //Select here again
        bd = bookRepository.saveAndFlush(bd);
        //check update version
        assertThat(bd.getVersion()).isEqualTo((short)(currentVersion + 1));
    }
}