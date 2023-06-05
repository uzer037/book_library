package ru.ntik.book.library.testutils;

import ru.ntik.book.library.domain.BookDefinition;
import ru.ntik.book.library.domain.enums.BookLanguage;

public class TestUtils {
    public static final String BOOK_NAME = "BOOK_NAME";
    public static final String BOOK_DESC = "BOOK_DESCRIPTION";
    public static final Long CREATOR = 10L;
    public static final int RELEASE_YEAR = 2020;
    public static final String ISBN = "978-5-4461-0512-0";
    public static final String COVER_TYPE = "paperback";
    public static final int PAGE_COUNT = 1000;
    public static final BookLanguage BOOK_LANGUAGE = BookLanguage.RUSSIAN;

    public static BookDefinition createBookDefinition() {
        return new BookDefinition(BOOK_NAME, BOOK_DESC, CREATOR, RELEASE_YEAR, COVER_TYPE, ISBN, PAGE_COUNT, BOOK_LANGUAGE);
    }
}