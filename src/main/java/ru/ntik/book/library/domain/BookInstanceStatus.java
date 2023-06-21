package ru.ntik.book.library.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import ru.ntik.book.library.domain.enums.BookInstanceState;

import java.time.LocalDate;

import static ru.ntik.book.library.util.Constants.BOOK_INSTANCE_STATUS_REGION_NAME;

@Entity

@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE,
        region = BOOK_INSTANCE_STATUS_REGION_NAME)

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookInstanceStatus extends StoredObject{
    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 20)
    private BookInstanceState state;

    private LocalDate movedToUserDate;

    private Long toUser;

    private LocalDate movedToOwnerDate;

    BookInstanceStatus(Long creator) {
        super(creator);
        this.state = BookInstanceState.JUST_ADDED;
        this.movedToUserDate = null;
        this.toUser = null;
        this.movedToOwnerDate = null;
    }

    public BookInstanceStatus moveToUser(Long user) {
        state = BookInstanceState.ON_USER;
        movedToUserDate = LocalDate.now();
        toUser = user;
        movedToOwnerDate = null;
        return this;
    }

    public BookInstanceStatus moveToOwner() {
        state = BookInstanceState.ON_OWNER;
        movedToUserDate = null;
        toUser = null;
        movedToOwnerDate = LocalDate.now();
        return this;
    }
}
