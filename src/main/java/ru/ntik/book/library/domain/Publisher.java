package ru.ntik.book.library.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static ru.ntik.book.library.domain.Publisher.GRAPH_FETCH_ALL;
import static ru.ntik.book.library.util.Constants.PUBLISHER_REGION_NAME;

@Entity

@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE,
        region = PUBLISHER_REGION_NAME)
@OptimisticLocking(type = OptimisticLockType.DIRTY)
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@NamedEntityGraph(name = GRAPH_FETCH_ALL, includeAllAttributes = true)

public class Publisher extends PersistentObject {

    @OneToMany(mappedBy = "publisher", fetch = FetchType.LAZY)
    private final Set<BookDefinition> bookDefinitions = new HashSet<>();

    public Publisher(String name, String description, Long creator) {
        super(name, description, creator);
    }

    public Set<BookDefinition> getBookDefinitions() {
        return Collections.unmodifiableSet(bookDefinitions);
    }

    public static final String GRAPH_FETCH_ALL = "Publisher.FETCH_ALL";
}