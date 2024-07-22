package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.persistence.*;
@Getter
@Setter
@Entity
@Table(name = "indexes")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigserial")
    Long id;

    @JoinColumn(name = "page_id", columnDefinition = "bigint")
    @ManyToOne
    Page page;

    @JoinColumn(name = "lemma_id", columnDefinition = "bigint")
    @ManyToOne
    Lemma lemma;

    @Column(name = "rank", columnDefinition = "double precision")
    Float rank;
}
