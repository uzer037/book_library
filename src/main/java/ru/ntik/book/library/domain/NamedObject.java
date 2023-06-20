package ru.ntik.book.library.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.ntik.book.library.util.Checker;

import java.util.Objects;

import static ru.ntik.book.library.util.Constants.*;

@MappedSuperclass
@SequenceGenerator(name = ID_GENERATOR, sequenceName = "po_seq", initialValue = 50)

@Getter
@NoArgsConstructor

public abstract class NamedObject extends StoredObject {

    @Column(name = COLUMN_NAME, columnDefinition = COLUMN_NAME_DEFINITION)
    private String name;

    @Column(name = COLUMN_DESCRIPTION_NAME, columnDefinition = COLUMN_DESCRIPTION_DEFINITION)
    private String description;


    protected NamedObject(String name, String description, Long creator) {
        super(creator);
        setName(name);
        setDescription(description);
    }

    public void setName(String name) {
        Objects.requireNonNull(name);
        this.name = Checker.checkStringLength(name, MIN_STRING_LENGTH, PO_MAX_NAME_LENGTH);
    }

    public void setDescription(String description) {
        this.description = description == null ? null : Checker.checkStringLength(description, PO_MIN_DESC_LENGTH, LONG_STRING_LENGTH);
    }
    @Override
    public String toString() {
        return String.format("%s { id=%d, name='%s'}", getClass().getSimpleName(), getId(), getName());
    }

    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DESCRIPTION_NAME = "description";
    private static final String COLUMN_NAME_DEFINITION =
            "VARCHAR(" + PO_MAX_NAME_LENGTH + ") CHECK (length(" + COLUMN_NAME + ") >= " + MIN_STRING_LENGTH + ") NOT NULL";
    private static final String COLUMN_DESCRIPTION_DEFINITION =
            "VARCHAR(" + LONG_STRING_LENGTH + ") CHECK (length(" + COLUMN_DESCRIPTION_NAME + ") >= " + PO_MIN_DESC_LENGTH + ")";
}