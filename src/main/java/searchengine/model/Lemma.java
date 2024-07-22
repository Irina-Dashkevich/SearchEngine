package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.persistence.*;
@Getter
@Setter
@Entity
@Table(name="lemmas")

public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigserial")
    Long id;

    @ManyToOne
    @JoinColumn(name = "site_id", columnDefinition = "bigint")
    Site site;

    @Column(name = "lemma", columnDefinition = "varchar(255)")
    String lemma;

    @Column(name = "frequency", columnDefinition = "integer")
    Integer frequency;
}
